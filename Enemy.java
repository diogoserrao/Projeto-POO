import greenfoot.*;

/** Um inimigo pode patrulhar uma zona ou ficar parado. */
public class Enemy extends Actor {

    private static final int FRAME_SIZE = 64;
    private static final int DISPLAY_SIZE = 96;
    private static final int FRAMES = 5;

    private static final int BAIXO = 0;
    private static final int ESQUERDA = 1;
    private static final int DIREITA = 2;
    private static final int CIMA = 3;

    private final GreenfootImage walkSpritesheet;
    private final GreenfootImage idleSpritesheet;
    private final boolean mexe;
    private final boolean horizontal;
    private final int limiteInicial;
    private final int limiteFinal;
    private final int velocidade;

    private int sentido = 1;
    private int frame;
    private int linhaDirecao = BAIXO;
    private int contadorAnimacao;

    public Enemy(int personagem, boolean mexe, boolean horizontal,
                 int limiteInicial, int limiteFinal, int velocidade) {
        String pasta = "jogador/PNG/Swordsman_lvl" + personagem
            + "/With_shadow/";
        String prefixo = "Swordsman_lvl" + personagem;
        walkSpritesheet = new GreenfootImage(
            pasta + prefixo + "_Walk_with_shadow.png");
        idleSpritesheet = new GreenfootImage(
            pasta + prefixo + "_Idle_with_shadow.png");

        this.mexe = mexe;
        this.horizontal = horizontal;
        this.limiteInicial = Math.min(limiteInicial, limiteFinal);
        this.limiteFinal = Math.max(limiteInicial, limiteFinal);
        this.velocidade = Math.max(1, velocidade);
        mostrarFrame();
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
            linhaDirecao = sentido > 0 ? DIREITA : ESQUERDA;
        } else {
            novoY += sentido * velocidade;
            linhaDirecao = sentido > 0 ? BAIXO : CIMA;
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

    private void atualizarAnimacao(boolean estaAMover) {
        contadorAnimacao++;
        if (contadorAnimacao >= (estaAMover ? 5 : 12)) {
            contadorAnimacao = 0;
            frame = (frame + 1) % FRAMES;
            mostrarFrame();
        }
    }

    private void mostrarFrame() {
        GreenfootImage imagem = new GreenfootImage(FRAME_SIZE, FRAME_SIZE);
        GreenfootImage spritesheet = mexe
            ? walkSpritesheet
            : idleSpritesheet;
        imagem.drawImage(spritesheet,
            -frame * FRAME_SIZE,
            -linhaDirecao * FRAME_SIZE);
        imagem.scale(DISPLAY_SIZE, DISPLAY_SIZE);
        setImage(imagem);
    }
}
