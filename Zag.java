import greenfoot.*;

/** Jogador 1: Swordsman nível 2, controlado por WASD. */
public class Zag extends Actor {
    private static final int FRAME_SIZE = 64;
    private static final int DISPLAY_SIZE = 96;
    private static final int WALK_FRAMES = 6;
    private static final int IDLE_FRAMES = 12;
    private static final int BAIXO = 0;
    private static final int ESQUERDA = 1;
    private static final int DIREITA = 2;
    private static final int CIMA = 3;

    private final GreenfootImage walkSpritesheet;
    private final GreenfootImage idleSpritesheet;
    private int frame;
    private int linhaDirecao = BAIXO;
    private int contadorAnimacao;
    private int velocidade = 4;
    private boolean estavaEmMovimento;

    public Zag() {
        walkSpritesheet = new GreenfootImage("jogador/PNG/Swordsman_lvl2/With_shadow/Swordsman_lvl2_Walk_with_shadow.png");
        idleSpritesheet = new GreenfootImage("jogador/PNG/Swordsman_lvl2/With_shadow/Swordsman_lvl2_Idle_with_shadow.png");
        mostrarFrame(0);
    }

    public void act() {
        boolean movimento = false;
        if (Greenfoot.isKeyDown("w")) { tentarMover(getX(), getY() - velocidade); linhaDirecao = CIMA; movimento = true; }
        if (Greenfoot.isKeyDown("s")) { tentarMover(getX(), getY() + velocidade); linhaDirecao = BAIXO; movimento = true; }
        if (Greenfoot.isKeyDown("a")) { tentarMover(getX() - velocidade, getY()); linhaDirecao = ESQUERDA; movimento = true; }
        if (Greenfoot.isKeyDown("d")) { tentarMover(getX() + velocidade, getY()); linhaDirecao = DIREITA; movimento = true; }
        atualizarAnimacao(movimento);
    }

    private void atualizarAnimacao(boolean movimento) {
        if (movimento != estavaEmMovimento) {
            frame = 0;
            contadorAnimacao = 0;
            estavaEmMovimento = movimento;
            mostrarFrame(0);
        }
        contadorAnimacao++;
        if (contadorAnimacao >= (movimento ? 5 : 12)) {
            contadorAnimacao = 0;
            frame = (frame + 1) % (movimento ? WALK_FRAMES : IDLE_FRAMES);
            mostrarFrame(frame);
        }
    }

    private void tentarMover(int x, int y) {
        MyWorld mundo = (MyWorld)getWorld();
        if (mundo != null && mundo.podeMover(this, x, y)) setLocation(x, y);
    }

    private void mostrarFrame(int numero) {
        GreenfootImage spritesheet = estavaEmMovimento ? walkSpritesheet : idleSpritesheet;
        GreenfootImage imagem = new GreenfootImage(FRAME_SIZE, FRAME_SIZE);
        imagem.drawImage(spritesheet, -numero * FRAME_SIZE, -linhaDirecao * FRAME_SIZE);
        imagem.scale(DISPLAY_SIZE, DISPLAY_SIZE);
        setImage(imagem);
    }
}
