import greenfoot.*;

public class Player extends Personagem {
    private final Controlos controlos;
    private final int velocidade = 4;
    private int nivel;
    private int ultimoY;
    private boolean emTransicaoDeEscada;

    public Player(int skin, Controlos controlos) {
        super(skin);
        this.controlos = controlos;
    }

    @Override
    public void act() {
        int dx = 0;
        int dy = 0;

        if (Greenfoot.isKeyDown(controlos.getEsquerda()))
            dx -= velocidade;
        if (Greenfoot.isKeyDown(controlos.getDireita()))
            dx += velocidade;
        if (Greenfoot.isKeyDown(controlos.getCima()))
            dy -= velocidade;
        if (Greenfoot.isKeyDown(controlos.getBaixo()))
            dy += velocidade;

        if (dx != 0 || dy != 0) {
            if (dy < 0)
                definirDirecao(CIMA);
            else if (dy > 0)
                definirDirecao(BAIXO);
            else if (dx < 0)
                definirDirecao(ESQUERDA);
            else
                definirDirecao(DIREITA);
        }

        boolean movimento = false;
        if (dx != 0)
            movimento |= tentarMover(getX() + dx, getY());
        if (dy != 0)
            movimento |= tentarMover(getX(), getY() + dy);
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

    public int getUltimoY() {
        return ultimoY;
    }

    public void setUltimoY(int ultimoY) {
        this.ultimoY = ultimoY;
    }

    public boolean isEmTransicaoDeEscada() {
        return emTransicaoDeEscada;
    }

    public void setEmTransicaoDeEscada(boolean emTransicaoDeEscada) {
        this.emTransicaoDeEscada = emTransicaoDeEscada;
    }
}
