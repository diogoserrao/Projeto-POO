import greenfoot.*;
import java.util.List;

/** Um inimigo delega o movimento para a implementacao recebida. */
public class Enemy extends Personagem {
    private static final int RAIO_VISAO = 100;
    private static final double ANGULO_VISAO = 60.0;
    private static final int COOLDOWN_ATAQUE = 60;
    private static final int VIDAS_INIMIGO = 3;
    private static final int INVULNERABILIDADE_APOS_GOLPE = 20;
    /*
     * O cone de visão era redesenhado numa imagem 204x204 em cada frame de
     * animação de todos os inimigos. Mantemos a lógica de visão, mas não
     * renderizamos esta indicação durante o jogo.
     */
    private static final boolean MOSTRAR_VISAO = false;
    private static final double COSENO_MEIO_CONE = Math.cos(
            Math.toRadians(ANGULO_VISAO / 2.0));
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
        if (mundo instanceof MyWorld && ((MyWorld) mundo).estaEmGameOver()) {
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

        if (!estaEmAtaque()) {
            boolean estaAMover = movimento.mover(this);
            atualizarAnimacao(estaAMover);
        } else {
            atualizarAnimacao(false);
        }
        detetarEAtacarJogador();
    }

    /**
     * Deteta o jogador válido mais próximo e aplica-lhe dano.
     *
     * Este método pertence à classe Enemy, por isso é executado por todos os
     * inimigos, independentemente do movimento que cada um utiliza.
     */
    public void detetarEAtacarJogador() {
        World mundo = getWorld();
        if (contadorAtaque > 0) {
            contadorAtaque--;
        }

        Player alvo = encontrarAlvo();
        if (alvo != null && contadorAtaque == 0) {
            iniciarAtaque();
            alvo.perderVida();
            contadorAtaque = COOLDOWN_ATAQUE;

            if (!alvo.estaVivo() && mundo instanceof MyWorld) {
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
        contadorInvulnerabilidade = INVULNERABILIDADE_APOS_GOLPE;
        if (vidasInimigo <= 0) {
            iniciarMorte();
        }
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

    /** Indica se o jogador está atrás deste inimigo e a uma distância válida. */
    public boolean estaDeCostasPara(Player jogador) {
        if (jogador == null || !estaVivo() || estaEmMorte()
                || jogador.getNivel() != getNivel()) {
            return false;
        }

        double dx = jogador.getX() - getX();
        double dy = jogador.getY() - getY();
        double distanciaQuadrado = dx * dx + dy * dy;
        if (distanciaQuadrado == 0 || distanciaQuadrado > 90 * 90) {
            return false;
        }

        double frenteX = 0;
        double frenteY = 0;
        switch (getDirecao()) {
        case DIREITA:
            frenteX = 1;
            break;
        case ESQUERDA:
            frenteX = -1;
            break;
        case CIMA:
            frenteY = -1;
            break;
        default:
            frenteY = 1;
            break;
        }

        // O jogador tem de estar no cone traseiro do inimigo.
        double produtoTraseiro = dx * frenteX + dy * frenteY;
        if (produtoTraseiro >= -Math.sqrt(distanciaQuadrado)
                * COSENO_MEIO_CONE) {
            return false;
        }

        // E tem de estar virado para o inimigo para poder desferir o golpe.
        double paraInimigoX = -dx;
        double paraInimigoY = -dy;
        double direcaoJogadorX = 0;
        double direcaoJogadorY = 0;
        switch (jogador.getDirecao()) {
        case DIREITA:
            direcaoJogadorX = 1;
            break;
        case ESQUERDA:
            direcaoJogadorX = -1;
            break;
        case CIMA:
            direcaoJogadorY = -1;
            break;
        default:
            direcaoJogadorY = 1;
            break;
        }

        double produtoFrontal = paraInimigoX * direcaoJogadorX
                + paraInimigoY * direcaoJogadorY;
        return produtoFrontal >= Math.sqrt(distanciaQuadrado)
                * COSENO_MEIO_CONE;
    }

    /**
     * Verifica se este inimigo pode receber um golpe deste jogador.
     * O ataque depende da proximidade e da direção do jogador, não do lado
     * para o qual o inimigo está virado.
     */
    public boolean podeSerAtacadoPor(Player jogador) {
        if (jogador == null || !estaVivo() || estaEmMorte()
                || jogador.getNivel() != getNivel()) {
            return false;
        }

        int dx = getX() - jogador.getX();
        int dy = getY() - jogador.getY();
        int distanciaQuadrada = dx * dx + dy * dy;
        if (distanciaQuadrada == 0 || distanciaQuadrada > 90 * 90) {
            return false;
        }

        double direcaoX = 0;
        double direcaoY = 0;
        switch (jogador.getDirecao()) {
        case DIREITA:
            direcaoX = 1;
            break;
        case ESQUERDA:
            direcaoX = -1;
            break;
        case CIMA:
            direcaoY = -1;
            break;
        default:
            direcaoY = 1;
            break;
        }

        double produto = dx * direcaoX + dy * direcaoY;
        return produto > 0 && produto * produto >= distanciaQuadrada
                * COSENO_MEIO_CONE * COSENO_MEIO_CONE;
    }

    /** Escolhe o jogador vivo, visivel e mais proximo. */
    private Player encontrarAlvo() {
        World mundo = getWorld();
        if (mundo == null) {
            return null;
        }

        if (!(mundo instanceof MyWorld)) {
            return null;
        }

        /* Consulta os atores que estão realmente no mundo. */
        List<Player> jogadores = mundo.getObjects(Player.class);
        Player maisProximo = null;
        double menorDistanciaQuadrado = Double.MAX_VALUE;

        for (Player jogador : jogadores) {
            if (jogador == null || !jogador.estaVivo()
                    || jogador.getNivel() != getNivel()) {
                continue;
            }

            double dx = jogador.getX() - getX();
            double dy = jogador.getY() - getY();
            double distanciaQuadrado = dx * dx + dy * dy;
            if (distanciaQuadrado <= RAIO_VISAO_QUADRADO
                    && distanciaQuadrado < menorDistanciaQuadrado
                    && estaNoConeDeVisao(dx, dy, distanciaQuadrado)) {
                maisProximo = jogador;
                menorDistanciaQuadrado = distanciaQuadrado;
            }
        }

        return maisProximo;
    }

    private boolean estaNoConeDeVisao(double dx, double dy,
            double distanciaQuadrado) {
        // Um alvo no mesmo ponto nao tem uma direcao relativa bem definida.
        if (distanciaQuadrado == 0) {
            return false;
        }

        double direcaoX = 0;
        double direcaoY = 0;
        switch (getDirecao()) {
        case DIREITA:
            direcaoX = 1;
            break;
        case ESQUERDA:
            direcaoX = -1;
            break;
        case CIMA:
            direcaoY = -1;
            break;
        default:
            direcaoY = 1;
            break;
        }

        double produto = dx * direcaoX + dy * direcaoY;
        return produto > 0
                && produto * produto >= distanciaQuadrado
                        * COSENO_MEIO_CONE * COSENO_MEIO_CONE;
    }

    /** Acrescenta o cone a seguir ao frame normal, sem afetar colisoes. */
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
            xs[i + 1] = centro + (int) Math.round(Math.cos(graus) * RAIO_VISAO);
            ys[i + 1] = centro + (int) Math.round(Math.sin(graus) * RAIO_VISAO);
        }
        imagem.setColor(new greenfoot.Color(255, 220, 70, 45));
        imagem.fillPolygon(xs, ys, xs.length);
        imagem.setColor(new greenfoot.Color(255, 220, 70, 150));
        imagem.drawPolygon(xs, ys, xs.length);
        imagem.drawImage(personagem, centro - personagem.getWidth() / 2,
                centro - personagem.getHeight() / 2);
        setImage(imagem);
    }

    private double direcaoEmGraus() {
        switch (getDirecao()) {
        case DIREITA:
            return 0;
        case CIMA:
            return -90;
        case ESQUERDA:
            return 180;
        default:
            return 90;
        }
    }

    public boolean podeMoverPara(int x, int y) {
        DungeonMap mapa = getMapa();
        return mapa != null && !mapa.estaBloqueado(getPes(x, y), getNivel());
    }

    public void moverPara(int x, int y) {
        setLocation(x, y);
    }

    public void definirDirecaoMovimento(int direcao) {
        definirDirecao(direcao);
    }

    public void definirDirecaoInicial(int direcao) {
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
            definirDirecaoInicial(diferencaX >= 0 ? DIREITA : ESQUERDA);
        } else {
            definirDirecaoInicial(diferencaY >= 0 ? BAIXO : CIMA);
        }
    }
}
