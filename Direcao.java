/**
 * Enumeração das quatro direções ortogonais usadas pelo jogo.
 *
 * <p>Cada valor associa a direção visual ao deslocamento cartesiano que é
 * utilizado pelo movimento, pelas colisões e pelo cone de visão.</p>
 */
public enum Direcao {
    BAIXO(0, 1),
    ESQUERDA(-1, 0),
    DIREITA(1, 0),
    CIMA(0, -1);

    private final int dx;
    private final int dy;

    Direcao(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /** Devolve o deslocamento horizontal associado à direção. */
    public int getDx() {
        return dx;
    }

    /** Devolve o deslocamento vertical associado à direção. */
    public int getDy() {
        return dy;
    }
}
