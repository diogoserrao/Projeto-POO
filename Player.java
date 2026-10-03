import greenfoot.*;

public class Player extends Personagem {
    private final int numeroJogador;
    private final int velocidade = 4;
    private int nivel;

    public Player(int numeroJogador) {
        super(numeroJogador == 1 ? 2 : 3);
        this.numeroJogador = numeroJogador;
    }

    @Override
    public void act() {
        int dx = 0;
        int dy = 0;

        if (numeroJogador == 1) {
            if (Greenfoot.isKeyDown("a")) dx -= velocidade;
            if (Greenfoot.isKeyDown("d")) dx += velocidade;
            if (Greenfoot.isKeyDown("w")) dy -= velocidade;
            if (Greenfoot.isKeyDown("s")) dy += velocidade;
        } else {
            if (Greenfoot.isKeyDown("left")) dx -= velocidade;
            if (Greenfoot.isKeyDown("right")) dx += velocidade;
            if (Greenfoot.isKeyDown("up")) dy -= velocidade;
            if (Greenfoot.isKeyDown("down")) dy += velocidade;
        }

        if (dx != 0 || dy != 0) {
            if (dy < 0) definirDirecao(CIMA);
            else if (dy > 0) definirDirecao(BAIXO);
            else if (dx < 0) definirDirecao(ESQUERDA);
            else definirDirecao(DIREITA);
        }

        boolean movimento = false;
        if (dx != 0) movimento |= tentarMover(getX() + dx, getY());
        if (dy != 0) movimento |= tentarMover(getX(), getY() + dy);
        atualizarAnimacao(movimento);
    }

    private boolean tentarMover(int x, int y) {
        MyWorld mundo = (MyWorld) getWorld();
        if (mundo != null && mundo.podeMover(this, x, y)) {
            setLocation(x, y);
            return true;
        }
        return false;
    }

    public int getNivel() {
        return nivel;
    }

    public void subirNivel() {
        nivel++;
    }

    public void descerNivel() {
        nivel--;
    }
}
