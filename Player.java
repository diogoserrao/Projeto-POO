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

        boolean movimento = false;

        if (numeroJogador == 1) {

            // Jogador 1 - WASD

            if (Greenfoot.isKeyDown("w")) {
                tentarMover(getX(), getY() - velocidade);
                linhaDirecao = CIMA;
                movimento = true;
            }

            if (Greenfoot.isKeyDown("s")) {
                tentarMover(getX(), getY() + velocidade);
                linhaDirecao = BAIXO;
                movimento = true;
            }

            if (Greenfoot.isKeyDown("a")) {
                tentarMover(getX() - velocidade, getY());
                linhaDirecao = ESQUERDA;
                movimento = true;
            }

            if (Greenfoot.isKeyDown("d")) {
                tentarMover(getX() + velocidade, getY());
                linhaDirecao = DIREITA;
                movimento = true;
            }

        } else {

            // Jogador 2 - Setas

            if (Greenfoot.isKeyDown("up")) {
                tentarMover(getX(), getY() - velocidade);
                linhaDirecao = CIMA;
                movimento = true;
            }

            if (Greenfoot.isKeyDown("down")) {
                tentarMover(getX(), getY() + velocidade);
                linhaDirecao = BAIXO;
                movimento = true;
            }

            if (Greenfoot.isKeyDown("left")) {
                tentarMover(getX() - velocidade, getY());
                linhaDirecao = ESQUERDA;
                movimento = true;
            }

            if (Greenfoot.isKeyDown("right")) {
                tentarMover(getX() + velocidade, getY());
                linhaDirecao = DIREITA;
                movimento = true;
            }
        }

        atualizarAnimacao(movimento);
    }

    private void tentarMover(int x, int y) {

        MyWorld mundo = (MyWorld) getWorld();

        if (mundo != null &&
            mundo.podeMover(this, x, y)) {

            setLocation(x, y);
        }
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

    public void resetarNivel() {
        nivel = 0;
    }

    public int getNumeroJogador() {
        return numeroJogador;
    }
}