import greenfoot.Actor;
import greenfoot.GreenfootImage;
import greenfoot.World;
import java.awt.Rectangle;

/**
 * Pedra que pode ser recolhida, lançada para criar uma distração e depois
 * desaparecer quando permanece demasiado tempo no chão.
 *
 * <p>O estado interno distingue a pedra disponível, em voo, aterrada e
 * recolhida. Os inimigos observam apenas as pedras aterradas, mantendo a
 * responsabilidade da distração separada do movimento do jogador.</p>
 */
public class Pedra extends Actor implements Interagivel {
    private static final int TAMANHO = 16;
    private static final int VELOCIDADE = 8;
    private static final int ALCANCE_MAXIMO = 240;
    private static final int TEMPO_NO_CHAO = 150;
    private static final GreenfootImage IMAGEM = carregarImagemPartilhada();

    /** Estados do ciclo de vida de uma pedra no mundo. */
    private enum Estado {
        POR_RECOLHER,
        A_VOAR,
        ATERRADA,
        RECOLHIDA
    }

    private Estado estado;
    private Direcao direcao;
    private final DungeonMap mapa;
    private int distanciaPercorrida;
    private int contadorNoChao;

    /** Cria uma pedra disponível para ser recolhida. */
    public Pedra() {
        this.mapa = null;
        this.estado = Estado.POR_RECOLHER;
        this.direcao = Direcao.BAIXO;
        this.distanciaPercorrida = 0;
        this.contadorNoChao = 0;
        carregarImagem();
    }

    /**
     * Cria uma pedra em voo. O inteiro usa os ordinais da enumeração
     * Direcao, mantendo a assinatura prevista para o lançamento.
     */
    public Pedra(int direcao) {
        this(direcao, null);
    }

    /** Cria uma pedra em voo associada ao mapa que controla as colisões. */
    public Pedra(int direcao, DungeonMap mapa) {
        this.converterDirecao(direcao);
        this.mapa = mapa;
        this.estado = Estado.A_VOAR;
        this.distanciaPercorrida = 0;
        this.contadorNoChao = 0;
        carregarImagem();
    }

    /** Sobrecarga conveniente para código que já trabalha com Direcao. */
    public Pedra(Direcao direcao) {
        this(direcao, null);
    }

    /** Cria uma pedra em voo associada ao mapa que controla as colisões. */
    public Pedra(Direcao direcao, DungeonMap mapa) {
        this.direcao = direcao == null ? Direcao.BAIXO : direcao;
        this.mapa = mapa;
        this.estado = Estado.A_VOAR;
        this.distanciaPercorrida = 0;
        this.contadorNoChao = 0;
        carregarImagem();
    }

    private void converterDirecao(int direcaoRecebida) {
        Direcao[] direcoes = Direcao.values();
        if (direcaoRecebida < 0 || direcaoRecebida >= direcoes.length) {
            throw new IllegalArgumentException("Direcao invalida: "
                    + direcaoRecebida);
        }
        this.direcao = direcoes[direcaoRecebida];
    }

    private void carregarImagem() {
        setImage(IMAGEM);
    }

    private static GreenfootImage carregarImagemPartilhada() {
        GreenfootImage imagem = new GreenfootImage("pedra.png");
        // A imagem existente já tem aproximadamente TAMANHO x TAMANHO.
        if (imagem.getWidth() != TAMANHO || imagem.getHeight() != TAMANHO) {
            int maiorDimensao = Math.max(imagem.getWidth(), imagem.getHeight());
            int largura = Math.max(1, imagem.getWidth() * TAMANHO / maiorDimensao);
            int altura = Math.max(1, imagem.getHeight() * TAMANHO / maiorDimensao);
            imagem.scale(largura, altura);
        }
        return imagem;
    }

    @Override
    public void act() {
        // Uma pedra só é removida depois de ficar aterrada durante o intervalo
        // definido; pedras em voo e pedras iniciais seguem fluxos distintos.
        if (estado == Estado.A_VOAR) {
            voar();
        } else if (estado == Estado.ATERRADA) {
            if (contadorNoChao > 0) {
                contadorNoChao--;
            }
            if (contadorNoChao == 0) {
                MyWorld mundo = getMundo();
                if (mundo != null) {
                    mundo.removerPedra(this);
                    mundo.removeObject(this);
                }
            }
        }
    }

    private void voar() {
        World mundo = getWorld();
        if (mundo == null) {
            return;
        }

        int deslocamentoX = direcao.getDx() * VELOCIDADE;
        int deslocamentoY = direcao.getDy() * VELOCIDADE;
        int novoX = getX() + deslocamentoX;
        int novoY = getY() + deslocamentoY;
        // A pedra termina o voo ao atingir o alcance máximo ou uma zona
        // bloqueada, ficando disponível para a distração dos inimigos.
        if (mapa == null
                || distanciaPercorrida + VELOCIDADE >= ALCANCE_MAXIMO
                || mapa.estaBloqueado(new Rectangle(novoX - 4, novoY - 4, 9, 9))) {
            aterrar();
            return;
        }

        setLocation(novoX, novoY);
        distanciaPercorrida += VELOCIDADE;
    }

    private void aterrar() {
        estado = Estado.ATERRADA;
        contadorNoChao = TEMPO_NO_CHAO;
    }

    public boolean estaPorRecolher() {
        return estado == Estado.POR_RECOLHER;
    }

    /** Área de interação da pedra disponível no chão. */
    @Override
    public Rectangle getArea() {
        return new Rectangle(getX() - 24, getY() - 24, 48, 48);
    }

    @Override
    public String getDescricao() {
        return estaPorRecolher() ? "Apanhar pedra" : "Pedra indisponível";
    }

    @Override
    public String getDescricao(Player jogador) {
        if (!estaPorRecolher()) {
            return "Pedra indisponível";
        }
        return jogador == null
                ? getDescricao()
                : "Premir " + jogador.getTeclaInteracao().toUpperCase()
                        + " para apanhar a pedra";
    }

    /** Devolve o mundo do jogo quando a pedra já foi adicionada a ele. */
    public MyWorld getMundo() {
        World mundo = getWorld();
        return mundo instanceof MyWorld ? (MyWorld) mundo : null;
    }

    /** Pedras são recolhidas automaticamente e não usam a tecla de interação. */
    @Override
    public boolean usaTeclaInteracao() {
        return false;
    }

    /** Recolhe a pedra apenas enquanto ela é um ponto inicial disponível. */
    @Override
    public void interagir(Player jogador, DungeonMap mapa, MyWorld mundo) {
        if (!estaPorRecolher() || jogador == null || getWorld() == null) {
            return;
        }

        estado = Estado.RECOLHIDA;
        jogador.adicionarPedra();

        if (mapa != null) {
            mapa.removerInteragivel(this);
        }
        if (mundo != null) {
            mundo.removerPedra(this);
            mundo.removeObject(this);
        }
    }

    public boolean fazDistracao() {
        return estado == Estado.ATERRADA;
    }
}
