import greenfoot.*;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

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
    private static final Map<String, GreenfootImage> SPRITESHEETS =
            new HashMap<String, GreenfootImage>();

    private int nivel;
    private int vidas;

    private final GreenfootImage walkSpritesheet;
    private final GreenfootImage idleSpritesheet;
    private final GreenfootImage attackSpritesheet;
    private final GreenfootImage deathSpritesheet;
    private final int walkFrames;
    private final int idleFrames;
    private final int attackFrames;
    private final int deathFrames;
    private final GreenfootImage[][][] imagensAnimacao;
    private int frame;
    private int linhaDirecao = BAIXO;
    private int contadorAnimacao;
    private boolean estavaEmMovimento;
    private boolean emAtaque;
    private boolean emMorte;
    private boolean terminouMorte;

    protected Personagem(int personagem) {
        this(personagem, 0);
    }

    protected Personagem(int personagem, int nivel) {
        this(carregarSpritesheet(personagem, "Walk"),
             carregarSpritesheet(personagem, "Idle"),
             carregarSpritesheet(personagem, "attack"),
             carregarSpritesheet(personagem, "Death"), nivel);
    }

    protected Personagem(GreenfootImage walkSpritesheet,
                         GreenfootImage idleSpritesheet) {
        this(walkSpritesheet, idleSpritesheet, idleSpritesheet,
             idleSpritesheet, 0);
    }

    protected Personagem(GreenfootImage walkSpritesheet,
                         GreenfootImage idleSpritesheet, int nivel) {
        this(walkSpritesheet, idleSpritesheet, idleSpritesheet,
             idleSpritesheet, nivel);
    }

    private Personagem(GreenfootImage walkSpritesheet,
                       GreenfootImage idleSpritesheet,
                       GreenfootImage attackSpritesheet,
                       GreenfootImage deathSpritesheet, int nivel) {
        this.walkSpritesheet = walkSpritesheet;
        this.idleSpritesheet = idleSpritesheet;
        this.attackSpritesheet = attackSpritesheet;
        this.deathSpritesheet = deathSpritesheet;
        this.nivel = nivel;
        this.vidas = MAX_VIDAS;
        this.walkFrames = walkSpritesheet.getWidth() / FRAME_SIZE;
        this.idleFrames = idleSpritesheet.getWidth() / FRAME_SIZE;
        this.attackFrames = attackSpritesheet.getWidth() / FRAME_SIZE;
        this.deathFrames = deathSpritesheet.getWidth() / FRAME_SIZE;
        int maiorNumeroFrames = Math.max(walkFrames, Math.max(idleFrames,
                Math.max(attackFrames, Math.max(deathFrames, IDLE_FRAMES_CIMA))));
        this.imagensAnimacao = new GreenfootImage[4][4][maiorNumeroFrames];
        mostrarFrame();
    }

    private static GreenfootImage carregarSpritesheet(int personagem,
                                                       String animacao) {
        String pasta = "jogador/PNG/Swordsman_lvl" + personagem
            + "/With_shadow/";
        String caminho = pasta + "Swordsman_lvl" + personagem
            + "_" + animacao + "_with_shadow.png";
        GreenfootImage spritesheet = SPRITESHEETS.get(caminho);
        if (spritesheet == null) {
            spritesheet = new GreenfootImage(caminho);
            SPRITESHEETS.put(caminho, spritesheet);
        }
        return spritesheet;
    }

    protected void definirDirecao(int direcao) {
        if (emMorte) {
            return;
        }
        if (linhaDirecao == direcao) {
            return;
        }

        linhaDirecao = direcao;
        frame %= numeroFramesAnimacao(estavaEmMovimento);
        mostrarFrame();
    }

    /** Devolve a direcao para sistemas que precisam de consultar o olhar. */
    public int getDirecao() {
        return linhaDirecao;
    }

    protected void atualizarAnimacao(boolean movimento) {
        if (emAtaque || emMorte) {
            atualizarAnimacaoEspecial();
            return;
        }
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
        int tipoAnimacao = emMorte ? 3 : emAtaque ? 2
                : estavaEmMovimento ? 1 : 0;
        GreenfootImage imagem = imagensAnimacao[tipoAnimacao][linhaDirecao][frame];

        if (imagem == null) {
            GreenfootImage spritesheet;
            if (emMorte) {
                spritesheet = deathSpritesheet;
            } else if (emAtaque) {
                spritesheet = attackSpritesheet;
            } else {
                spritesheet = estavaEmMovimento ? walkSpritesheet : idleSpritesheet;
            }
            imagem = new GreenfootImage(FRAME_SIZE, FRAME_SIZE);
            imagem.drawImage(spritesheet, -frame * FRAME_SIZE,
                    -linhaDirecao * FRAME_SIZE);
            imagem.scale(DISPLAY_SIZE, DISPLAY_SIZE);
            imagensAnimacao[tipoAnimacao][linhaDirecao][frame] = imagem;
        }

        setImage(imagem);
    }

    /** Começa uma animação de ataque que é reproduzida uma única vez. */
    protected void iniciarAtaque() {
        if (emMorte || emAtaque) {
            return;
        }
        emAtaque = true;
        frame = 0;
        contadorAnimacao = 0;
        mostrarFrame();
    }

    /** Começa a animação de morte e mantém o último frame visível no fim. */
    public void iniciarMorte() {
        if (emMorte) {
            return;
        }
        emMorte = true;
        emAtaque = false;
        estavaEmMovimento = false;
        frame = 0;
        contadorAnimacao = 0;
        terminouMorte = false;
        mostrarFrame();
    }

    public boolean estaEmMorte() {
        return emMorte;
    }

    protected boolean estaEmAtaque() {
        return emAtaque;
    }

    public boolean terminouMorte() {
        return terminouMorte;
    }

    private void atualizarAnimacaoEspecial() {
        contadorAnimacao++;
        if (contadorAnimacao < 5) {
            return;
        }
        contadorAnimacao = 0;
        int total = emMorte ? deathFrames : attackFrames;
        if (frame < total - 1) {
            frame++;
            mostrarFrame();
        } else if (emAtaque) {
            emAtaque = false;
            frame = 0;
            mostrarFrame();
        } else {
            terminouMorte = true;
        }
    }

    /** Devolve o mapa do mundo atual, quando este disponibiliza um mapa. */
    protected DungeonMap getMapa() {
        World mundo = getWorld();
        if (!(mundo instanceof MyWorld)) {
            return null;
        }

        return ((MyWorld) mundo).getMapa();
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
