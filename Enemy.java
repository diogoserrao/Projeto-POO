import greenfoot.*;
import java.util.List;

/** Um inimigo delega o movimento para a implementacao recebida. */
public class Enemy extends Personagem {
    private static final int RAIO_VISAO = 100;
    private static final int ALCANCE_GOLPE = RAIO_VISAO;
    private static final int ALCANCE_GOLPE_QUADRADO =
            ALCANCE_GOLPE * ALCANCE_GOLPE;
    private static final double ANGULO_VISAO = 60.0;
    private static final int COOLDOWN_ATAQUE = 60;
    private static final boolean MOSTRAR_VISAO = true;
    private static final int VIDAS_INIMIGO = 3;
    private static final int INVULNERABILIDADE_APOS_GOLPE = 20;
    private static final double COSENO_MEIO_CONE = Math.cos(
            Math.toRadians(ANGULO_VISAO / 2.0));
    private static final double COSENO_MEIO_CONE_GOLPE = Math.cos(
            Math.toRadians(45.0));
    private static final double RAIO_VISAO_QUADRADO = RAIO_VISAO * RAIO_VISAO;
    private final Movimento movimento;
    private int contadorAtaque;
    private int contadorInvulnerabilidade;
    private int vidasInimigo = VIDAS_INIMIGO;

    public Enemy(int personagem, Movimento movimento) {
        super(personagem);
        if (movimento == null) {
            throw new IllegalArgumentException("O movimento nao pode ser null");
        }
        this.movimento = movimento;
    }

    @Override
    public void act() {
        World mundo = getWorld();
        if (mundoEstaEmGameOver()) {
            return;
        }

        if (estaEmMorte()) {
            atualizarAnimacao(false);
            if (terminouMorte() && mundo != null) {
                mundo.removeObject(this);
            }
            return;
        }

        if (contadorInvulnerabilidade > 0) {
            contadorInvulnerabilidade--;
        }

        Player alvo = encontrarAlvo();

        if (!estaEmAtaque() && alvo == null) {
            boolean estaAMover = movimento.mover(this);
            atualizarAnimacao(estaAMover);
        } else {
            atualizarAnimacao(false);
        }
        detetarEAtacarJogador(alvo);
    }

    /**
     * Deteta o jogador válido mais próximo e aplica-lhe dano.
     *
     * Este método pertence à classe Enemy, por isso é executado por todos os
     * inimigos, independentemente do movimento que cada um utiliza.
     */
    public void detetarEAtacarJogador(Player alvo) {
        if (contadorAtaque > 0) {
            contadorAtaque--;
        }

        if (alvo != null && contadorAtaque == 0) {
            iniciarAtaque();
            alvo.perderVida();
            contadorAtaque = COOLDOWN_ATAQUE;

            if (!alvo.estaVivo()) {
                alvo.iniciarMorte();
            }
        }
    }

    /** Recebe um golpe do jogador e inicia a morte quando perde a última vida. */
    public void receberAtaque() {
        if (!estaVivo() || estaEmMorte() || contadorInvulnerabilidade > 0) {
            return;
        }

        vidasInimigo--;
        iniciarFlashDano();
        contadorInvulnerabilidade = INVULNERABILIDADE_APOS_GOLPE;
        if (vidasInimigo <= 0) {
            iniciarMorte();
        }
    }

    /** Recebe um golpe do jogador, eliminando imediatamente um golpe pelas costas. */
    public void receberAtaque(Player atacante) {
        if (!estaVivo() || estaEmMorte()) {
            return;
        }

        if (estaDeCostasPara(atacante)) {
            vidasInimigo = 0;
            iniciarMorte();
        } else {
            receberAtaque();
        }
    }

    private boolean estaDeCostasPara(Player jogador) {
        // As direcoes opostas formam os pares 0<->3 e 1<->2, por isso calcula-se 3 - direcao.
        int direcaoOposta = 3 - getDirecao().ordinal();
        return estaNoCone(jogador.getX() - getX(), jogador.getY() - getY(),
                direcaoOposta, ALCANCE_GOLPE_QUADRADO,
                COSENO_MEIO_CONE_GOLPE);
    }

    /** Acrescenta o cone ao frame atual sem alterar a imagem da animacao. */
    @Override
    protected void mostrarFrame() {
        super.mostrarFrame();
        if (!MOSTRAR_VISAO) {
            return;
        }

        GreenfootImage personagem = getImage();
        int tamanho = RAIO_VISAO * 2 + 4;
        GreenfootImage imagem = new GreenfootImage(tamanho, tamanho);
        int centro = tamanho / 2;
        int[] xs = new int[23];
        int[] ys = new int[23];
        xs[0] = centro;
        ys[0] = centro;
        double direcao = direcaoEmGraus();

        for (int i = 0; i <= 21; i++) {
            double graus = Math.toRadians(direcao - ANGULO_VISAO / 2.0
                    + ANGULO_VISAO * i / 21.0);
            xs[i + 1] = centro + (int) Math.round(
                    Math.cos(graus) * RAIO_VISAO);
            ys[i + 1] = centro + (int) Math.round(
                    Math.sin(graus) * RAIO_VISAO);
        }

        imagem.setColor(new Color(255, 220, 70, 45));
        imagem.fillPolygon(xs, ys, xs.length);
        imagem.setColor(new Color(255, 220, 70, 150));
        imagem.drawPolygon(xs, ys, xs.length);
        imagem.drawImage(personagem, centro - personagem.getWidth() / 2,
                centro - personagem.getHeight() / 2);
        setImage(imagem);
    }

    private double direcaoEmGraus() {
        return Math.toDegrees(Math.atan2(getDirecao().getDy(),
                getDirecao().getDx()));
    }

    /** Devolve as vidas próprias do inimigo, usadas pelos ataques do jogador. */
    @Override
    public boolean estaVivo() {
        return vidasInimigo > 0;
    }

    @Override
    public int getVidas() {
        return vidasInimigo;
    }

    /**
     * Verifica se este inimigo pode receber um golpe deste jogador.
     * O ataque depende da proximidade e da direção do jogador, não do lado
     * para o qual o inimigo está virado.
     */
    public boolean podeSerAtacadoPor(Player jogador) {
        if (jogador == null || !estaVivo() || estaEmMorte()) {
            return false;
        }

        return estaNoCone(getX() - jogador.getX(), getY() - jogador.getY(),
                jogador.getDirecao().ordinal(), ALCANCE_GOLPE_QUADRADO,
                COSENO_MEIO_CONE_GOLPE);
    }

    /** Escolhe o jogador vivo, visivel e mais proximo. */
    private Player encontrarAlvo() {
        World mundo = getWorld();
        if (mundo == null) {
            return null;
        }

        /* Consulta os atores que estão realmente no mundo. */
        List<Player> jogadores = mundo.getObjects(Player.class);
        Player maisProximo = null;
        double menorDistanciaQuadrado = Double.MAX_VALUE;

        for (Player jogador : jogadores) {
            if (jogador == null || !jogador.estaVivo()) {
                continue;
            }

            double dx = jogador.getX() - getX();
            double dy = jogador.getY() - getY();
            double distanciaQuadrado = dx * dx + dy * dy;
            if (distanciaQuadrado <= RAIO_VISAO_QUADRADO
                    && distanciaQuadrado < menorDistanciaQuadrado
                    && estaNoCone(dx, dy, getDirecao().ordinal(),
                            RAIO_VISAO_QUADRADO, COSENO_MEIO_CONE)) {
                maisProximo = jogador;
                menorDistanciaQuadrado = distanciaQuadrado;
            }
        }

        return maisProximo;
    }

    private static boolean estaNoCone(double dx, double dy, int direcao,
            double raioQuadrado, double cosenoMeioCone) {
        double distanciaQuadrado = dx * dx + dy * dy;
        if (distanciaQuadrado == 0 || distanciaQuadrado > raioQuadrado) {
            return false;
        }

        int frenteX;
        int frenteY;
        switch (direcao) {
        case 2: // Direcao.DIREITA.ordinal()
            frenteX = 1;
            frenteY = 0;
            break;
        case 1: // Direcao.ESQUERDA.ordinal()
            frenteX = -1;
            frenteY = 0;
            break;
        case 3: // Direcao.CIMA.ordinal()
            frenteX = 0;
            frenteY = -1;
            break;
        default:
            frenteX = 0;
            frenteY = 1;
            break;
        }

        double produto = dx * frenteX + dy * frenteY;
        return produto > 0
                && produto * produto >= distanciaQuadrado
                        * cosenoMeioCone * cosenoMeioCone;
    }

    public boolean podeMoverPara(int x, int y) {
        DungeonMap mapa = getMapa();
        return mapa != null && !mapa.estaBloqueado(getPes(x, y));
    }

    public void moverPara(int x, int y) {
        setLocation(x, y);
    }

    public void definirDirecaoMovimento(Direcao direcao) {
        definirDirecao(direcao);
    }

    public void definirDirecaoInicial(Direcao direcao) {
        definirDirecao(direcao);
    }

    /** Orienta o cone inicial para um jogador sem alterar o movimento. */
    public void orientarPara(Player jogador) {
        if (jogador == null) {
            return;
        }

        int diferencaX = jogador.getX() - getX();
        int diferencaY = jogador.getY() - getY();

        if (Math.abs(diferencaX) >= Math.abs(diferencaY)) {
            definirDirecaoInicial(diferencaX >= 0
                    ? Direcao.DIREITA : Direcao.ESQUERDA);
        } else {
            definirDirecaoInicial(diferencaY >= 0
                    ? Direcao.BAIXO : Direcao.CIMA);
        }
    }
}
