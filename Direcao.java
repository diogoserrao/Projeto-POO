/** Direções lógicas usadas pelo movimento e pela animação dos personagens. */
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

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
}
