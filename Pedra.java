import greenfoot.Actor;
import greenfoot.GreenfootImage;
import greenfoot.World;

/** Um objeto não letal que pode ser lançado para distrair inimigos. */
public class Pedra extends Actor {
    private static final int TAMANHO = 16;
    private static final int VELOCIDADE = 8;
    private static final int ALCANCE_MAXIMO = 240;
    private static final int TEMPO_NO_CHAO = 150;

    private enum Estado {
        POR_RECOLHER,
        A_VOAR,
        ATERRADA
    }

    private Estado estado;
    private Direcao direcao;
    private int distanciaPercorrida;
    private int contadorNoChao;

    /** Cria uma pedra disponível para ser recolhida. */
    public Pedra() {
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
        this.converterDirecao(direcao);
        this.estado = Estado.A_VOAR;
        this.distanciaPercorrida = 0;
        this.contadorNoChao = 0;
        carregarImagem();
    }

    /** Sobrecarga conveniente para código que já trabalha com Direcao. */
    public Pedra(Direcao direcao) {
        this.direcao = direcao == null ? Direcao.BAIXO : direcao;
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
        GreenfootImage imagem = new GreenfootImage("pedra.png");
        // A imagem existente já tem aproximadamente TAMANHO x TAMANHO.
        if (imagem.getWidth() != TAMANHO || imagem.getHeight() != TAMANHO) {
            int maiorDimensao = Math.max(imagem.getWidth(), imagem.getHeight());
            int largura = Math.max(1, imagem.getWidth() * TAMANHO / maiorDimensao);
            int altura = Math.max(1, imagem.getHeight() * TAMANHO / maiorDimensao);
            imagem.scale(largura, altura);
        }
        setImage(imagem);
    }

    @Override
    public void act() {
        if (estado == Estado.A_VOAR) {
            voar();
        } else if (estado == Estado.ATERRADA) {
            if (contadorNoChao > 0) {
                contadorNoChao--;
            }
            if (contadorNoChao == 0) {
                World mundo = getWorld();
                if (mundo != null) {
                    mundo.removeObject(this);
                }
            }
        }
    }

    private void voar() {
        World mundo = getWorld();
        if (!(mundo instanceof MyWorld)) {
            return;
        }

        int deslocamentoX = direcao.getDx() * VELOCIDADE;
        int deslocamentoY = direcao.getDy() * VELOCIDADE;
        int novoX = getX() + deslocamentoX;
        int novoY = getY() + deslocamentoY;
        DungeonMap mapa = ((MyWorld) mundo).getMapa();

        if (mapa == null
                || distanciaPercorrida + VELOCIDADE >= ALCANCE_MAXIMO
                || mapa.temColisaoNaZona(novoX - 4, novoY - 4,
                        novoX + 4, novoY + 4)) {
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

    public boolean fazDistracao() {
        return estado != Estado.POR_RECOLHER;
    }
}
