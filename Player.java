import greenfoot.*;

public class Player extends Actor {

    private static final int FRAME_SIZE = 64;
    private static final int DISPLAY_SIZE = 96;

    private static final int WALK_FRAMES = 6;
    private static final int IDLE_FRAMES = 12;

    private static final int BAIXO = 0;
    private static final int ESQUERDA = 1;
    private static final int DIREITA = 2;
    private static final int CIMA = 3;

    private final int numeroJogador;

    private final GreenfootImage walkSpritesheet;
    private final GreenfootImage idleSpritesheet;

    private int frame;
    private int linhaDirecao = BAIXO;
    private int contadorAnimacao;
    private int velocidade = 4;

    private boolean estavaEmMovimento;

    // Cada jogador guarda o seu próprio nível
    private int nivel = 0;

    public Player(int numeroJogador) {

        this.numeroJogador = numeroJogador;

        String pastaSprites;

        if (numeroJogador == 1) {
            pastaSprites =
                "jogador/PNG/Swordsman_lvl2/With_shadow/";
        } else {
            pastaSprites =
                "jogador/PNG/Swordsman_lvl3/With_shadow/";
        }

        walkSpritesheet = new GreenfootImage(
            pastaSprites + "Swordsman_lvl" +
            (numeroJogador == 1 ? "2" : "3") +
            "_Walk_with_shadow.png"
        );

        idleSpritesheet = new GreenfootImage(
            pastaSprites + "Swordsman_lvl" +
            (numeroJogador == 1 ? "2" : "3") +
            "_Idle_with_shadow.png"
        );

        mostrarFrame(0);
    }

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
            if (dy < 0) linhaDirecao = CIMA;
            else if (dy > 0) linhaDirecao = BAIXO;
            else if (dx < 0) linhaDirecao = ESQUERDA;
            else linhaDirecao = DIREITA;
        }

        // Move cada eixo separadamente para deslizar pelas paredes.
        boolean movimento = false;
        if (dx != 0) movimento |= tentarMover(getX() + dx, getY());
        if (dy != 0) movimento |= tentarMover(getX(), getY() + dy);

        atualizarAnimacao(movimento);
    }

    private boolean tentarMover(int x, int y) {

        MyWorld mundo = (MyWorld) getWorld();

        if (mundo != null &&
            mundo.podeMover(this, x, y)) {

            setLocation(x, y);
            return true;
        }

        return false;
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

            frame = (frame + 1) %
                    (movimento ? WALK_FRAMES : IDLE_FRAMES);

            mostrarFrame(frame);
        }
    }

    private void mostrarFrame(int numero) {

        GreenfootImage spritesheet =
            estavaEmMovimento
            ? walkSpritesheet
            : idleSpritesheet;

        GreenfootImage imagem =
            new GreenfootImage(
                FRAME_SIZE,
                FRAME_SIZE
            );

        imagem.drawImage(
            spritesheet,
            -numero * FRAME_SIZE,
            -linhaDirecao * FRAME_SIZE
        );

        imagem.scale(
            DISPLAY_SIZE,
            DISPLAY_SIZE
        );

        setImage(imagem);
    }

    // -------------------------
    // NÍVEL DO JOGADOR
    // -------------------------

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
