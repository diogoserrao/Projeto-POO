import greenfoot.*;
import java.awt.Rectangle;

/** Um inimigo pode patrulhar uma zona ou ficar parado. */
public class Enemy extends Personagem {
    private final boolean mexe;
    private final boolean horizontal;
    private final int limiteInicial;
    private final int limiteFinal;
    private final int velocidade;
    private final Rectangle patrulha;
    private int sentido = 1;

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
    }

    private boolean patrulhar() {
        int novoX = getX();
        int novoY = getY();

        if (patrulha != null) {
            return patrulharZona();
        }

        if (horizontal) {
            novoX += sentido * velocidade;
            definirDirecao(sentido > 0 ? DIREITA : ESQUERDA);
        } else {
            novoY += sentido * velocidade;
            definirDirecao(sentido > 0 ? BAIXO : CIMA);
        }

        int coordenada = horizontal ? novoX : novoY;
        if (coordenada < limiteInicial || coordenada > limiteFinal
                || !podeMover(novoX, novoY)) {
            sentido *= -1;
            return false;
        }

        setLocation(novoX, novoY);
        return true;
    }

    private boolean podeMover(int x, int y) {
        if (!(getWorld() instanceof MyWorld)) {
            return false;
        }
        DungeonMap mapa = ((MyWorld) getWorld()).getMapa();
        return mapa != null && !mapa.estaBloqueado(getPes(x, y), getNivel());
    }

    private boolean patrulharZona() {

        int novoX = getX();
        int novoY = getY();

        if (patrulha.width >= patrulha.height) {

            novoX += sentido * velocidade;

            definirDirecao(
                    sentido > 0 ? DIREITA : ESQUERDA);

            if (novoX < patrulha.x
                    || novoX > patrulha.x + patrulha.width) {

                sentido *= -1;
                return false;
            }

        } else {

            novoY += sentido * velocidade;

            definirDirecao(
                    sentido > 0 ? BAIXO : CIMA);

            if (novoY < patrulha.y
                    || novoY > patrulha.y + patrulha.height) {

                sentido *= -1;
                return false;
            }
        }

        if (!podeMover(novoX, novoY)) {
            sentido *= -1;
            return false;
        }

        setLocation(novoX, novoY);
        return true;
    }
}
