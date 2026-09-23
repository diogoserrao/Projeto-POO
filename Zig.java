import greenfoot.*;

public class Zig extends Actor {
    private static final int FRAME = 64;
    private static final int FRAMES = 4;
    private GreenfootImage spritesheet;
    private int frame = 0;
    private int contadorAnimacao = 0;
    private int velocidade = 4;

    public Zig() {
        spritesheet = new GreenfootImage("jogador/72 Character Free/Char 5/Character 5.png");
        mostrarFrame(0);
    }

    public void act() {
        boolean movimento = false;
        if (Greenfoot.isKeyDown("up")) { tentarMover(getX(), getY() - velocidade); movimento = true; }
        if (Greenfoot.isKeyDown("down")) { tentarMover(getX(), getY() + velocidade); movimento = true; }
        if (Greenfoot.isKeyDown("left")) { tentarMover(getX() - velocidade, getY()); movimento = true; }
        if (Greenfoot.isKeyDown("right")) { tentarMover(getX() + velocidade, getY()); movimento = true; }
        atualizarAnimacao(movimento);
    }

    private void atualizarAnimacao(boolean movimento) {
        contadorAnimacao++;
        if ((movimento && contadorAnimacao >= 5) || (!movimento && contadorAnimacao >= 12)) {
            contadorAnimacao = 0;
            frame = (frame + 1) % FRAMES;
            mostrarFrame(frame);
        }
    }

    private void tentarMover(int x, int y) {
        MyWorld mundo = (MyWorld)getWorld();
        if (mundo != null && mundo.podeMover(this, x, y)) setLocation(x, y);
    }

    private void mostrarFrame(int numero) {
        GreenfootImage imagem = new GreenfootImage(FRAME, FRAME);
        imagem.drawImage(spritesheet, -numero * FRAME, 0);
        setImage(imagem);
    }
}
