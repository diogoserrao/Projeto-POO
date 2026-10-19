import greenfoot.*;
import java.util.List;

/** Um inimigo delega o movimento para a implementacao recebida. */
public abstract class Inimigo extends Personagem {
    /* O alcance do golpe e a visão são regras independentes. */
    private static final int ALCANCE_GOLPE = 100;
    private static final int ALCANCE_GOLPE_QUADRADO =
            ALCANCE_GOLPE * ALCANCE_GOLPE;
    private static final int COOLDOWN_ATAQUE = 60;
    private static final boolean MOSTRAR_VISAO = true;
    private static final int INVULNERABILIDADE_APOS_GOLPE = 20;
    private static final int DURACAO_DISTRACAO = 180;
    private static final double COSENO_MEIO_CONE_GOLPE = Math.cos(
            Math.toRadians(45.0));
    private Movimento movimento;
    private int contadorAtaque;
    private int contadorInvulnerabilidade;
    private int contadorDistracao;
    private Pedra ultimaPedraVista;
    private boolean morteFurtiva;
    private boolean pontuacaoAtribuida;

    protected Inimigo(int personagem) {
        super(personagem, 3);
    }

    /** Raio, em píxeis, do cone de visão por defeito. */
    protected int getRaioVisao() {
        return 100;
    }

    /** Ângulo, em graus, do cone de visão por defeito. */
    protected double getAnguloVisao() {
        return 60.0;
    }

    /** Configura o movimento específico da subclasse concreta. */
    protected final void definirMovimento(Movimento movimento) {
        if (movimento == null) {
            throw new IllegalArgumentException("O movimento nao pode ser null");
        }
        this.movimento = movimento;
    }

    @Override
    protected void agir() {
        if (movimento == null) {
            return;
        }
        if (contadorInvulnerabilidade > 0) {
            contadorInvulnerabilidade--;
        }

        verificarPedras();
        Player alvo = encontrarAlvo();

        if (!estaEmAtaque() && alvo == null && contadorDistracao == 0) {
            boolean estaAMover = movimento.mover(this);
            atualizarAnimacao(estaAMover);
        } else {
            atualizarAnimacao(false);
        }
        detetarEAtacarJogador(alvo);
    }

    /** Atualiza a distração provocada pela primeira pedra nova no cone de visão. */
    private void verificarPedras() {
        if (contadorDistracao > 0) {
            contadorDistracao--;
        }

        World mundo = getMundo();
        if (mundo == null) {
            return;
        }

        List<Pedra> pedras = mundo.getObjects(Pedra.class);
        for (Pedra pedra : pedras) {
            if (pedra == null || pedra == ultimaPedraVista
                    || !pedra.fazDistracao()) {
                continue;
            }

            int dx = pedra.getX() - getX();
            int dy = pedra.getY() - getY();
            double distanciaQuadrado = dx * dx + dy * dy;
            int raioVisao = getRaioVisao();
            double raioDetecaoPedrasQuadrado = (double) raioVisao * raioVisao;
            if (distanciaQuadrado <= raioDetecaoPedrasQuadrado
                    && distanciaQuadrado > 0) {
                ultimaPedraVista = pedra;
                contadorDistracao = DURACAO_DISTRACAO;
                orientarPara(dx, dy);
                break;
            }
        }
    }

    /** Orienta o inimigo para uma posição relativa, sem iniciar movimento. */
    private void orientarPara(int dx, int dy) {
        if (Math.abs(dx) >= Math.abs(dy)) {
            definirDirecaoMovimento(dx > 0
                    ? Direcao.DIREITA : Direcao.ESQUERDA);
        } else {
            definirDirecaoMovimento(dy > 0
                    ? Direcao.BAIXO : Direcao.CIMA);
        }
    }

    /**
     * Deteta o jogador válido mais próximo e aplica-lhe dano.
     *
     * Este método pertence à classe Inimigo, por isso é executado por todos os
     * inimigos, independentemente do movimento que cada um utiliza.
     */
    public void detetarEAtacarJogador(Player alvo) {
        if (contadorAtaque > 0) {
            contadorAtaque--;
        }

        if (alvo != null && contadorAtaque == 0) {
            iniciarAtaque();
            alvo.receberDano();
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

        int vidasAntes = getVidas();
        receberDano();
        contadorInvulnerabilidade = INVULNERABILIDADE_APOS_GOLPE;
        if (getVidas() < vidasAntes) {
            reproduzirSomDano();
        }
    }

    /** Recebe um golpe do jogador, eliminando imediatamente um golpe pelas costas. */
    public void receberAtaque(Player atacante) {
        if (!estaVivo() || estaEmMorte()) {
            return;
        }

        if (estaDeCostasPara(atacante)) {
            int vidasAntes = getVidas();
            receberDano(getMaxVidas());
            if (getVidas() < vidasAntes) {
                reproduzirSomDano();
                if (!estaVivo()) {
                    morteFurtiva = true;
                }
            }
        } else {
            receberAtaque();
        }
    }

    /** Som de dano apenas para golpes que retiraram pelo menos uma vida. */
    private void reproduzirSomDano() {
        Greenfoot.playSound("dano.wav");
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
        int raioVisao = getRaioVisao();
        double anguloVisao = getAnguloVisao();
        int tamanho = raioVisao * 2 + 4;
        GreenfootImage imagem = new GreenfootImage(tamanho, tamanho);
        int centro = tamanho / 2;
        int[] xs = new int[23];
        int[] ys = new int[23];
        xs[0] = centro;
        ys[0] = centro;
        double direcao = direcaoEmGraus();

        for (int i = 0; i <= 21; i++) {
            double graus = Math.toRadians(direcao - anguloVisao / 2.0
                    + anguloVisao * i / 21.0);
            xs[i + 1] = centro + (int) Math.round(
                    Math.cos(graus) * raioVisao);
            ys[i + 1] = centro + (int) Math.round(
                    Math.sin(graus) * raioVisao);
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

    @Override
    protected void aoTerminarMorte() {
        MyWorld mundo = getMundo();
        if (mundo != null) {
            if (!pontuacaoAtribuida) {
                mundo.adicionarPontos(morteFurtiva ? 200 : 100);
                pontuacaoAtribuida = true;
            }
            mundo.removeObject(this);
        }
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
        World mundo = getMundo();
        if (mundo == null) {
            return null;
        }

        /* Consulta os atores que estão realmente no mundo. */
        List<Player> jogadores = mundo.getObjects(Player.class);
        Player maisProximo = null;
        double menorDistanciaQuadrado = Double.MAX_VALUE;
        int raioVisao = getRaioVisao();
        double raioVisaoQuadrado = (double) raioVisao * raioVisao;
        double cosenoMeioCone = Math.cos(
                Math.toRadians(getAnguloVisao() / 2.0));

        for (Player jogador : jogadores) {
            if (jogador == null || !jogador.estaVivo()) {
                continue;
            }

            double dx = jogador.getX() - getX();
            double dy = jogador.getY() - getY();
            double distanciaQuadrado = dx * dx + dy * dy;
            if (distanciaQuadrado <= raioVisaoQuadrado
                    && distanciaQuadrado < menorDistanciaQuadrado
                    && estaNoCone(dx, dy, getDirecao().ordinal(),
                            raioVisaoQuadrado, cosenoMeioCone)) {
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
