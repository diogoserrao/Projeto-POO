/** Um inimigo pode patrulhar uma zona ou ficar parado. */
public class Enemy extends Personagem {
    private final boolean mexe;
    private final boolean horizontal;
    private final int limiteInicial;
    private final int limiteFinal;
    private final int velocidade;
    private int sentido = 1;

    public Enemy(int personagem, boolean mexe, boolean horizontal,
                 int limiteInicial, int limiteFinal, int velocidade) {
        super(personagem);
        this.mexe = mexe;
        this.horizontal = horizontal;
        this.limiteInicial = Math.min(limiteInicial, limiteFinal);
        this.limiteFinal = Math.max(limiteInicial, limiteFinal);
        this.velocidade = Math.max(1, velocidade);
    }

    public Enemy(int personagem) {
        this(personagem, false, true, 0, 0, 1);
    }

    @Override
    public void act() {
        boolean estaAMover = mexe && patrulhar();
        atualizarAnimacao(estaAMover);
    }

    private boolean patrulhar() {
        int novoX = getX();
        int novoY = getY();

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
        return ((MyWorld) getWorld()).podeMover(this, x, y);
    }
}
