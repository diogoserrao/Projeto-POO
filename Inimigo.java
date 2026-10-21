import greenfoot.*;
import java.util.List;

/** Um inimigo delega o movimento para a implementacao recebida. */
public abstract class Inimigo extends Personagem {
    /* O alcance do golpe e a visão são regras independentes. */
    private static final int ALCANCE_GOLPE = 100;
    private static final int ALCANCE_GOLPE_QUADRADO =
            ALCANCE_GOLPE * ALCANCE_GOLPE;
    private static final int COOLDOWN_ATAQUE = 60;
    private static final boolean ATAQUES_ATIVOS = false;
    /** A visão permanece sempre ativa; a otimização está na camada em cache. */
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
    private ConeVisao coneVisao;
    private Direcao direcaoCone;
    private int raioCone = -1;
    private double anguloCone = Double.NaN;
    private int raioDetecaoCache = -1;
    private double raioDetecaoQuadradoCache;
    private double anguloDetecaoCache = Double.NaN;
    private double cosenoMeioConeCache;

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

        if (!estaEmAtaque()
                && (alvo == null || !ATAQUES_ATIVOS)
                && contadorDistracao == 0) {
            boolean estaAMover = movimento.mover(this);
            atualizarAnimacao(estaAMover);
        } else {
            atualizarAnimacao(false);
        }
        atualizarImagemCone();
        detetarEAtacarJogador(alvo);
    }

    /** Atualiza a distração provocada pela primeira pedra nova no cone de visão. */
    private void verificarPedras() {
        if (contadorDistracao > 0) {
            contadorDistracao--;
        }

        MyWorld mundo = getMundo();
        if (mundo == null) {
            return;
        }

        List<Pedra> pedras = mundo.getPedras();
        for (Pedra pedra : pedras) {
            if (pedra == null || pedra == ultimaPedraVista
                    || !pedra.fazDistracao()) {
                continue;
            }

            int dx = pedra.getX() - getX();
            int dy = pedra.getY() - getY();
            double distanciaQuadrado = dx * dx + dy * dy;
            double raioDetecaoPedrasQuadrado = getRaioDetecaoQuadrado();
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
     * Deteta o jogador válido mais próximo e aplica-lhe dano quando os ataques
     * dos inimigos estão ativos.
     *
     * Este método pertence à classe Inimigo, por isso é executado por todos os
     * inimigos, independentemente do movimento que cada um utiliza.
     */
    public void detetarEAtacarJogador(Player alvo) {
        if (!ATAQUES_ATIVOS) {
            return;
        }

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
        atualizarImagemCone();
    }

    /** Cria a camada visual depois de o ator estar no mundo. */
    public void inicializarConeVisao() {
        MyWorld mundo = getMundo();
        if (!MOSTRAR_VISAO || coneVisao != null || mundo == null) return;
        coneVisao = new ConeVisao(getRaioVisao(), getAnguloVisao());
        mundo.addObject(coneVisao, getX(), getY());
        atualizarImagemCone();
    }

    private void atualizarImagemCone() {
        if (coneVisao == null) return;
        int raio = getRaioVisao();
        double angulo = getAnguloVisao();
        Direcao direcao = getDirecao();
        if (raio != raioCone || Double.compare(angulo, anguloCone) != 0
                || direcao != direcaoCone) {
            coneVisao.atualizar(raio, angulo, direcao);
            raioCone = raio;
            anguloCone = angulo;
            direcaoCone = direcao;
        }
        if (coneVisao.getX() != getX() || coneVisao.getY() != getY()) {
            coneVisao.setLocation(getX(), getY());
        }
    }

    @Override
    protected void aoTerminarMorte() {
        MyWorld mundo = getMundo();
        if (mundo != null) {
            if (coneVisao != null && coneVisao.getWorld() == mundo) {
                mundo.removeObject(coneVisao);
            }
            if (!pontuacaoAtribuida) {
                mundo.adicionarPontos(morteFurtiva ? 200 : 100);
                pontuacaoAtribuida = true;
            }
            mundo.removerInimigo(this);
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
        MyWorld mundo = getMundo();
        if (mundo == null) {
            return null;
        }

        /* Consulta os atores que estão realmente no mundo. */
        List<Player> jogadores = mundo.getJogadores();
        Player maisProximo = null;
        double menorDistanciaQuadrado = Double.MAX_VALUE;
        int raioVisao = getRaioVisao();
        double raioVisaoQuadrado = getRaioDetecaoQuadrado();
        double cosenoMeioCone = getCosenoMeioConeVisao();

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

    private double getRaioDetecaoQuadrado() {
        int raio = getRaioVisao();
        if (raio != raioDetecaoCache) {
            raioDetecaoCache = raio;
            raioDetecaoQuadradoCache = (double) raio * raio;
        }
        return raioDetecaoQuadradoCache;
    }

    private double getCosenoMeioConeVisao() {
        double angulo = getAnguloVisao();
        if (Double.compare(angulo, anguloDetecaoCache) != 0) {
            anguloDetecaoCache = angulo;
            cosenoMeioConeCache = Math.cos(Math.toRadians(angulo / 2.0));
        }
        return cosenoMeioConeCache;
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

    /** Orienta o cone inicial para um jogador sem alterar o movimento. */
    public void orientarPara(Player jogador) {
        if (jogador == null) {
            return;
        }

        int diferencaX = jogador.getX() - getX();
        int diferencaY = jogador.getY() - getY();

        if (Math.abs(diferencaX) >= Math.abs(diferencaY)) {
            definirDirecaoMovimento(diferencaX >= 0
                    ? Direcao.DIREITA : Direcao.ESQUERDA);
        } else {
            definirDirecaoMovimento(diferencaY >= 0
                    ? Direcao.BAIXO : Direcao.CIMA);
        }
    }
}
