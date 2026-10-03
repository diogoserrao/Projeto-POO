import greenfoot.*;

/** Classe base para jogadores e inimigos. */
public abstract class Personagem extends Actor {
    protected static final int FRAME_SIZE = 64;
    protected static final int DISPLAY_SIZE = 96;
    protected static final int BAIXO = 0;
    protected static final int ESQUERDA = 1;
    protected static final int DIREITA = 2;
    protected static final int CIMA = 3;

    private final GreenfootImage walkSpritesheet;
    private final GreenfootImage idleSpritesheet;
    private final int walkFrames;
    private final int idleFrames;
    private int frame;
    private int linhaDirecao = BAIXO;
    private int contadorAnimacao;
    private boolean estavaEmMovimento;

    protected Personagem(int personagem) {
        this(carregarSpritesheet(personagem, "Walk"),
             carregarSpritesheet(personagem, "Idle"));
    }

    protected Personagem(GreenfootImage walkSpritesheet,
                         GreenfootImage idleSpritesheet) {
        this.walkSpritesheet = walkSpritesheet;
        this.idleSpritesheet = idleSpritesheet;
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
        linhaDirecao = direcao;
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
            int numeroFrames = movimento ? walkFrames : idleFrames;
            frame = (frame + 1) % numeroFrames;
            mostrarFrame();
        }
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
}
