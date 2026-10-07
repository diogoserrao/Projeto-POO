import greenfoot.*;
import java.awt.Rectangle;

/** Classe base para jogadores e inimigos. */
public abstract class Personagem extends Actor {
    protected static final int FRAME_SIZE = 64;
    protected static final int DISPLAY_SIZE = 96;
    protected static final int BAIXO = 0;
    protected static final int ESQUERDA = 1;
    protected static final int DIREITA = 2;
    protected static final int CIMA = 3;
    private static final int IDLE_FRAMES_CIMA = 4;

    private static final int PES_OFFSET_X = 7;
    private static final int PES_OFFSET_Y = 18;
    private static final int PES_LARGURA = 15;
    private static final int PES_ALTURA = 11;
    private static final int MAX_VIDAS = 3;

    private int nivel;
    private int vidas;

    private final GreenfootImage walkSpritesheet;
    private final GreenfootImage idleSpritesheet;
    private final int walkFrames;
    private final int idleFrames;
    private int frame;
    private int linhaDirecao = BAIXO;
    private int contadorAnimacao;
    private boolean estavaEmMovimento;

    protected Personagem(int personagem) {
        this(personagem, 0);
    }

    protected Personagem(int personagem, int nivel) {
        this(carregarSpritesheet(personagem, "Walk"),
             carregarSpritesheet(personagem, "Idle"), nivel);
    }

    protected Personagem(GreenfootImage walkSpritesheet,
                         GreenfootImage idleSpritesheet) {
        this(walkSpritesheet, idleSpritesheet, 0);
    }

    protected Personagem(GreenfootImage walkSpritesheet,
                         GreenfootImage idleSpritesheet, int nivel) {
        this.walkSpritesheet = walkSpritesheet;
        this.idleSpritesheet = idleSpritesheet;
        this.nivel = nivel;
        this.vidas = MAX_VIDAS;
        this.walkFrames = walkSpritesheet.getWidth() / FRAME_SIZE;
        this.idleFrames = idleSpritesheet.getWidth() / FRAME_SIZE;
        mostrarFrame();
    }

    private static GreenfootImage carregarSpritesheet(int personagem,
                                                       String animacao) {
        String pasta = "jogador/PNG/Swordsman_lvl" + personagem
            + "/With_shadow/";
        return new GreenfootImage(pasta + "Swordsman_lvl" + personagem
            + "_" + animacao + "_with_shadow.png");
    }

    protected void definirDirecao(int direcao) {
        if (linhaDirecao == direcao) {
            return;
        }

        linhaDirecao = direcao;
        frame %= numeroFramesAnimacao(estavaEmMovimento);
        mostrarFrame();
    }

    protected void atualizarAnimacao(boolean movimento) {
        if (movimento != estavaEmMovimento) {
            frame = 0;
            contadorAnimacao = 0;
            estavaEmMovimento = movimento;
            mostrarFrame();
        }

        contadorAnimacao++;
        if (contadorAnimacao >= (movimento ? 5 : 12)) {
            contadorAnimacao = 0;
            int numeroFrames = numeroFramesAnimacao(movimento);
            frame = (frame + 1) % numeroFrames;
            mostrarFrame();
        }
    }

    private int numeroFramesAnimacao(boolean movimento) {
        if (movimento) {
            return walkFrames;
        }
        return linhaDirecao == CIMA ? IDLE_FRAMES_CIMA : idleFrames;
    }

    protected void mostrarFrame() {
        GreenfootImage spritesheet = estavaEmMovimento
            ? walkSpritesheet : idleSpritesheet;
        GreenfootImage imagem = new GreenfootImage(FRAME_SIZE, FRAME_SIZE);
        imagem.drawImage(spritesheet, -frame * FRAME_SIZE,
                         -linhaDirecao * FRAME_SIZE);
        imagem.scale(DISPLAY_SIZE, DISPLAY_SIZE);
        setImage(imagem);
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public void subirNivel() {
        nivel++;
    }

    public void descerNivel() {
        nivel--;
    }

    /** Devolve o número de vidas/corações atuais deste personagem. */
    public int getVidas() {
        return vidas;
    }

    /** Devolve o número máximo de vidas/corações deste personagem. */
    public int getMaxVidas() {
        return MAX_VIDAS;
    }

    /** Indica se este personagem ainda tem pelo menos uma vida. */
    public boolean estaVivo() {
        return vidas > 0;
    }

    /** Retira exatamente uma vida, sem permitir valores negativos. */
    public void perderVida() {
        if (vidas > 0) {
            vidas--;
        }
    }

    /** Recupera exatamente uma vida, sem ultrapassar o máximo. */
    public void ganharVida() {
        if (vidas < MAX_VIDAS) {
            vidas++;
        }
    }

    public Rectangle getPes() {
        return getPes(getX(), getY());
    }

    public Rectangle getPes(int x, int y) {
        return new Rectangle(x - PES_OFFSET_X, y + PES_OFFSET_Y,
                             PES_LARGURA, PES_ALTURA);
    }
}
