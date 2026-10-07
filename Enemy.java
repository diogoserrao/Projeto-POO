import greenfoot.*;
import java.awt.Rectangle;
import java.util.List;

/** Um inimigo pode patrulhar uma zona ou ficar parado. */
public class Enemy extends Personagem {
    private static final int RAIO_VISAO = 100;
    private static final double ANGULO_VISAO = 60.0;
    private static final int COOLDOWN_ATAQUE = 60;
    private static final boolean MOSTRAR_VISAO = true;
    private static final int TEMPO_PAUSA_NA_EXTREMIDADE = 8;
    private static final int DISTANCIA_DESACELERACAO = 4;

    private enum EstadoPatrulha {
        MOVENDO,
        PAUSANDO
    }

    private final boolean mexe;
    private final boolean horizontal;
    private final int limiteInicial;
    private final int limiteFinal;
    private final int velocidade;
    private final Rectangle patrulha;
    private int sentido = 1;
    private int contadorAtaque;
    private EstadoPatrulha estadoPatrulha = EstadoPatrulha.MOVENDO;
    private int contadorPausa;

    public Enemy(
            int personagem,
            int velocidade,
            Rectangle patrulha) {

        super(personagem);

        this.mexe = true;
        this.horizontal = patrulha.width >= patrulha.height;
        this.limiteInicial = 0;
        this.limiteFinal = 0;
        this.velocidade = Math.max(1, velocidade);
        this.patrulha = patrulha;
    }

    public Enemy(int personagem, boolean mexe, boolean horizontal,
            int limiteInicial, int limiteFinal, int velocidade) {
        this(personagem, mexe, horizontal, limiteInicial, limiteFinal,
                velocidade, 0);
    }

    public Enemy(int personagem, boolean mexe, boolean horizontal,
            int limiteInicial, int limiteFinal, int velocidade,
            int nivel) {
        super(personagem, nivel);
        this.patrulha = null;
        this.mexe = mexe;
        this.horizontal = horizontal;
        this.limiteInicial = Math.min(limiteInicial, limiteFinal);
        this.limiteFinal = Math.max(limiteInicial, limiteFinal);
        this.velocidade = Math.max(1, velocidade);
    }

    public Enemy(int personagem) {
        this(personagem, false, true, 0, 0, 1, 0);
    }

    @Override
    public void act() {
        boolean estaAMover = mexe && patrulhar();
        atualizarAnimacao(estaAMover);
        atualizarAtaque();
    }

    private void atualizarAtaque() {
        if (contadorAtaque > 0) {
            contadorAtaque--;
        }

        Player alvo = encontrarAlvo();
        if (alvo != null && contadorAtaque == 0) {
            alvo.perderVida();
            contadorAtaque = COOLDOWN_ATAQUE;
        }
    }

    /** Escolhe o jogador vivo, visivel e mais proximo. */
    private Player encontrarAlvo() {
        if (!(getWorld() instanceof MyWorld)) {
            return null;
        }

        List<Player> jogadores = ((MyWorld) getWorld()).getJogadores();
        Player maisProximo = null;
        double menorDistancia = Double.MAX_VALUE;

        for (Player jogador : jogadores) {
            if (jogador == null || !jogador.estaVivo()
                    || jogador.getNivel() != getNivel()) {
                continue;
            }

            double distancia = distanciaA(jogador);
            if (distancia <= RAIO_VISAO
                    && distancia < menorDistancia
                    && estaNoConeDeVisao(jogador)
                    && temLinhaDeVisao(jogador)) {
                maisProximo = jogador;
                menorDistancia = distancia;
            }
        }

        return maisProximo;
    }

    private double distanciaA(Player jogador) {
        double dx = jogador.getX() - getX();
        double dy = jogador.getY() - getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private boolean estaNoConeDeVisao(Player jogador) {
        double dx = jogador.getX() - getX();
        double dy = jogador.getY() - getY();
        double comprimento = Math.sqrt(dx * dx + dy * dy);

        if (comprimento == 0) {
            return true;
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

        double produto = (dx * direcaoX + dy * direcaoY) / comprimento;
        double angulo = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, produto))));
        return angulo <= ANGULO_VISAO / 2.0;
    }

    /** Percorre a linha entre os atores e consulta a colisao do nivel atual. */
    private boolean temLinhaDeVisao(Player jogador) {
        if (!(getWorld() instanceof MyWorld)) {
            return false;
        }

        DungeonMap mapa = ((MyWorld) getWorld()).getMapa();
        if (mapa == null) {
            return false;
        }

        double distancia = distanciaA(jogador);
        int passos = Math.max(1, (int) Math.ceil(distancia / 4.0));
        for (int passo = 1; passo < passos; passo++) {
            double fracao = (double) passo / passos;
            int x = (int) Math.round(getX() + (jogador.getX() - getX()) * fracao);
            int y = (int) Math.round(getY() + (jogador.getY() - getY()) * fracao);
            if (mapa.estaBloqueado(new Rectangle(x, y, 1, 1), getNivel())) {
                return false;
            }
        }
        return true;
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

    private boolean patrulhar() {
        if (patrulha != null) {
            return patrulharZona();
        }

        return patrulharEntreLimites(limiteInicial, limiteFinal);
    }

    private boolean podeMover(int x, int y) {
        if (!(getWorld() instanceof MyWorld)) {
            return false;
        }
        DungeonMap mapa = ((MyWorld) getWorld()).getMapa();
        return mapa != null && !mapa.estaBloqueado(getPes(x, y), getNivel());
    }

    private boolean patrulharZona() {
        int limite = horizontal
                ? (sentido > 0 ? patrulha.x + patrulha.width : patrulha.x)
                : (sentido > 0 ? patrulha.y + patrulha.height : patrulha.y);
        return patrulharAte(limite);
    }

    private boolean patrulharEntreLimites(int inicio, int fim) {
        int limite = horizontal
                ? (sentido > 0 ? fim : inicio)
                : (sentido > 0 ? fim : inicio);
        return patrulharAte(limite);
    }

    /** Move ate ao extremo sem o ultrapassar e gere a pausa da viragem. */
    private boolean patrulharAte(int limite) {
        if (estadoPatrulha == EstadoPatrulha.PAUSANDO) {
            contadorPausa--;
            if (contadorPausa <= 0) {
                sentido *= -1;
                estadoPatrulha = EstadoPatrulha.MOVENDO;
                definirDirecao(direcaoDoSentido());
            }
            return false;
        }

        int novoX = getX();
        int novoY = getY();
        int atual = horizontal ? novoX : novoY;
        int distancia = Math.abs(limite - atual);

        definirDirecao(direcaoDoSentido());

        if (distancia == 0) {
            iniciarPausa();
            return false;
        }

        int passo = velocidade;
        if (distancia <= DISTANCIA_DESACELERACAO) {
            passo = Math.max(1, velocidade / 2);
        }
        passo = Math.min(passo, distancia);

        if (horizontal) {
            novoX += sentido * passo;
        } else {
            novoY += sentido * passo;
        }

        if (!podeMover(novoX, novoY)) {
            iniciarPausa();
            return false;
        }

        setLocation(novoX, novoY);

        if (passo == distancia) {
            iniciarPausa();
            return false;
        }
        return true;
    }

    private void iniciarPausa() {
        estadoPatrulha = EstadoPatrulha.PAUSANDO;
        contadorPausa = TEMPO_PAUSA_NA_EXTREMIDADE;
    }

    private int direcaoDoSentido() {
        if (horizontal) {
            return sentido > 0 ? DIREITA : ESQUERDA;
        }
        return sentido > 0 ? BAIXO : CIMA;
    }
}
