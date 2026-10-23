import greenfoot.*;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Classe abstrata que concentra o estado e o comportamento visual comum a
 * jogadores e inimigos.
 *
 * <p>Encapsula vidas, dano, morte, direção, sprites e animações. As subclasses
 * implementam apenas {@link #agir()}, ficando o ciclo Greenfoot, a gestão dos
 * frames e as transições de morte centralizados nesta classe. Assim, a mesma
 * referência de tipo base pode representar personagens concretos diferentes.</p>
 */
public abstract class Personagem extends Actor {
    protected static final int FRAME_SIZE = 64;
    protected static final int DISPLAY_SIZE = EscalaJogo.px(96);
    private static final int IDLE_FRAMES_CIMA = 4;

    private static final int PES_OFFSET_X = EscalaJogo.px(7);
    private static final int PES_OFFSET_Y = EscalaJogo.px(18);
    private static final int PES_LARGURA = EscalaJogo.px(15);
    private static final int PES_ALTURA = EscalaJogo.px(11);
    private static final int DURACAO_FLASH_DANO = 8;
    private static final Map<String, GreenfootImage> SPRITESHEETS =
            new HashMap<String, GreenfootImage>();
    /** Frames prontos partilhados por personagens que usam o mesmo spritesheet. */
    private static final Map<GreenfootImage, GreenfootImage[][]> FRAMES_CACHE =
            new IdentityHashMap<GreenfootImage, GreenfootImage[][]>();
    private static final Map<GreenfootImage, GreenfootImage> FLASH_CACHE =
            new IdentityHashMap<GreenfootImage, GreenfootImage>();

    private final int vidasMaximas;
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
    private Direcao linhaDirecao = Direcao.BAIXO;
    private int contadorAnimacao;
    private boolean estavaEmMovimento;
    private boolean emAtaque;
    private boolean emMorte;
    private boolean terminouMorte;
    private int contadorFlashDano;

    /**
     * Carrega as animações padrão da personagem identificada pela skin.
     *
     * @param personagem número usado no nome dos spritesheets
     * @param vidasMaximas número inicial e máximo de vidas
     */
    protected Personagem(int personagem, int vidasMaximas) {
        this(carregarSpritesheet(personagem, "Walk"),
             carregarSpritesheet(personagem, "Idle"),
             carregarSpritesheet(personagem, "attack"),
             carregarSpritesheet(personagem, "Death"), vidasMaximas);
    }

    /** Cria uma personagem usando sprites fornecidos pelo chamador. */
    protected Personagem(GreenfootImage walkSpritesheet,
                         GreenfootImage idleSpritesheet, int vidasMaximas) {
        this(walkSpritesheet, idleSpritesheet, idleSpritesheet,
             idleSpritesheet, vidasMaximas);
    }

    private Personagem(GreenfootImage walkSpritesheet,
                       GreenfootImage idleSpritesheet,
                       GreenfootImage attackSpritesheet,
                       GreenfootImage deathSpritesheet, int vidasMaximas) {
        if (vidasMaximas <= 0) {
            throw new IllegalArgumentException("O numero maximo de vidas deve ser positivo");
        }
        this.vidasMaximas = vidasMaximas;
        this.walkSpritesheet = walkSpritesheet;
        this.idleSpritesheet = idleSpritesheet;
        this.attackSpritesheet = attackSpritesheet;
        this.deathSpritesheet = deathSpritesheet;
        this.vidas = vidasMaximas;
        this.walkFrames = walkSpritesheet.getWidth() / FRAME_SIZE;
        this.idleFrames = idleSpritesheet.getWidth() / FRAME_SIZE;
        this.attackFrames = attackSpritesheet.getWidth() / FRAME_SIZE;
        this.deathFrames = deathSpritesheet.getWidth() / FRAME_SIZE;
        int maiorNumeroFrames = Math.max(walkFrames, Math.max(idleFrames,
                Math.max(attackFrames, Math.max(deathFrames, IDLE_FRAMES_CIMA))));
        this.imagensAnimacao = new GreenfootImage[4][Direcao.values().length]
                [maiorNumeroFrames];
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

    /** Atualiza a linha direcional da animação sem interromper uma morte. */
    protected void definirDirecao(Direcao direcao) {
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
    public Direcao getDirecao() {
        return linhaDirecao;
    }

    /** Avança a animação normal ou delega a uma animação especial em curso. */
    protected void atualizarAnimacao(boolean movimento) {
        if (contadorFlashDano > 0) {
            contadorFlashDano--;
            if (contadorFlashDano == 0) {
                mostrarFrame();
            }
        }

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
        return linhaDirecao == Direcao.CIMA ? IDLE_FRAMES_CIMA : idleFrames;
    }

    protected void mostrarFrame() {
        // Os frames são extraídos e guardados em cache por spritesheet para
        // evitar repetir o recorte das imagens em todos os ciclos do jogo.
        int tipoAnimacao = emMorte ? 3 : emAtaque ? 2
                : estavaEmMovimento ? 1 : 0;
        int linhaSpritesheet = linhaDirecao.ordinal();
        GreenfootImage imagem = imagensAnimacao[tipoAnimacao][linhaSpritesheet]
                [frame];

        if (imagem == null) {
            GreenfootImage spritesheet;
            if (emMorte) {
                spritesheet = deathSpritesheet;
            } else if (emAtaque) {
                spritesheet = attackSpritesheet;
            } else {
                spritesheet = estavaEmMovimento ? walkSpritesheet : idleSpritesheet;
            }
            GreenfootImage[][] framesDoSpritesheet = FRAMES_CACHE.get(spritesheet);
            if (framesDoSpritesheet == null) {
                framesDoSpritesheet = new GreenfootImage[Direcao.values().length]
                        [Math.max(IDLE_FRAMES_CIMA,
                                spritesheet.getWidth() / FRAME_SIZE)];
                FRAMES_CACHE.put(spritesheet, framesDoSpritesheet);
            }
            imagem = framesDoSpritesheet[linhaSpritesheet][frame];
            if (imagem == null) {
                imagem = new GreenfootImage(FRAME_SIZE, FRAME_SIZE);
                imagem.drawImage(spritesheet, -frame * FRAME_SIZE,
                        -linhaSpritesheet * FRAME_SIZE);
                imagem.scale(DISPLAY_SIZE, DISPLAY_SIZE);
                framesDoSpritesheet[linhaSpritesheet][frame] = imagem;
            }
            imagensAnimacao[tipoAnimacao][linhaSpritesheet][frame] = imagem;
        }

        if (contadorFlashDano > 0) {
            setImage(tingirDeVermelho(imagem));
        } else {
            setImage(imagem);
        }
    }

    /** Inicia o efeito visual de dano e atualiza imediatamente a imagem. */
    protected void iniciarFlashDano() {
        contadorFlashDano = DURACAO_FLASH_DANO;
        mostrarFrame();
    }

    /** Cria uma cópia avermelhada sem alterar a imagem original da animação. */
    private GreenfootImage tingirDeVermelho(GreenfootImage original) {
        GreenfootImage existente = FLASH_CACHE.get(original);
        if (existente != null) return existente;
        GreenfootImage tingida = new GreenfootImage(original);

        for (int x = 0; x < tingida.getWidth(); x++) {
            for (int y = 0; y < tingida.getHeight(); y++) {
                Color originalPixel = tingida.getColorAt(x, y);
                if (originalPixel.getAlpha() > 0) {
                    int vermelho = Math.min(255,
                            originalPixel.getRed() + 120);
                    int verde = originalPixel.getGreen() / 2;
                    int azul = originalPixel.getBlue() / 2;
                    Color cor = new Color(vermelho, verde, azul,
                            originalPixel.getAlpha());
                    tingida.setColorAt(x, y, cor);
                }
            }
        }

        FLASH_CACHE.put(original, tingida);
        return tingida;
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

    /** Indica se a personagem está a reproduzir a animação de morte. */
    public boolean estaEmMorte() {
        return emMorte;
    }

    /** Indica se a animação de ataque ainda está a decorrer. */
    protected boolean estaEmAtaque() {
        return emAtaque;
    }

    /** Indica se o último frame da morte já foi mostrado. */
    public boolean terminouMorte() {
        return terminouMorte;
    }

    private void atualizarAnimacaoEspecial() {
        // Ataque e morte avançam até ao fim; ao contrário da caminhada, não
        // reiniciam continuamente a animação especial.
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

    /** Devolve o mundo do jogo, quando este personagem já lhe pertence. */
    protected MyWorld getMundo() {
        World mundo = getWorld();

        if (mundo instanceof MyWorld) {
            return (MyWorld) mundo;
        }

        return null;
    }

    /** Fluxo comum a todos os personagens (Template Method). */
    @Override
    public final void act() {
        // Este método fixa o ciclo comum: um personagem morto já não executa
        // a lógica da subclasse e só termina a animação antes do hook final.
        if (mundoEstaEmGameOver()) {
            return;
        }

        if (!estaVivo()) {
            if (!estaEmMorte()) {
                iniciarMorte();
            }
            atualizarAnimacao(false);
            if (terminouMorte()) {
                aoTerminarMorte();
            }
            return;
        }

        agir();
    }

    /** Comportamento específico implementado por cada personagem. */
    protected abstract void agir();

    /** Hook para as regras específicas após a animação de morte. */
    protected void aoTerminarMorte() {
        // Por defeito, não há nenhuma ação adicional.
    }

    /** Devolve o mapa do mundo atual, quando este disponibiliza um mapa. */
    protected DungeonMap getMapa() {
        MyWorld mundo = getMundo();
        return mundo == null ? null : mundo.getMapa();
    }

    /** Consulta o estado de fim de jogo sem duplicar dependências nas subclasses. */
    protected boolean mundoEstaEmGameOver() {
        MyWorld mundo = getMundo();
        return mundo != null && mundo.estaEmGameOver();
    }

    /** Termina a partida quando este personagem pertence ao mundo do jogo. */
    protected void terminarJogoNoMundo() {
        MyWorld mundo = getMundo();
        if (mundo != null) {
            mundo.terminarJogo();
        }
    }

    /** Devolve o número de vidas/corações atuais deste personagem. */
    public int getVidas() {
        return vidas;
    }

    /** Devolve o número máximo de vidas/corações deste personagem. */
    public int getMaxVidas() {
        return vidasMaximas;
    }

    /** Indica se este personagem ainda tem pelo menos uma vida. */
    public boolean estaVivo() {
        return vidas > 0;
    }

    /** Retira exatamente uma vida, sem permitir valores negativos. */
    private void perderVida() {
        if (vidas > 0) {
            vidas--;
            iniciarFlashDano();
        }
    }

    /** Aplica um dano e inicia a morte quando a última vida é perdida. */
    public void receberDano() {
        receberDano(1);
    }

    /** Variante protegida para efeitos que causam mais do que uma vida de dano. */
    protected void receberDano(int quantidade) {
        if (quantidade <= 0 || !estaVivo() || estaEmMorte()) {
            return;
        }
        // O teste estaVivo no ciclo impede que um dano múltiplo reduza vidas
        // abaixo de zero e mantém a transição para morte numa única vez.
        for (int i = 0; i < quantidade && estaVivo(); i++) {
            perderVida();
        }
        if (!estaVivo()) {
            iniciarMorte();
        }
    }

    /** Recupera exatamente uma vida, sem ultrapassar o máximo. */
    public void ganharVida() {
        if (vidas < vidasMaximas) {
            vidas++;
        }
    }

    /** Devolve a área de colisão correspondente aos pés atuais. */
    public Rectangle getPes() {
        return getPes(getX(), getY());
    }

    /** Calcula a área dos pés para uma posição futura sem mover o ator. */
    public Rectangle getPes(int x, int y) {
        return new Rectangle(x - PES_OFFSET_X, y + PES_OFFSET_Y,
                             PES_LARGURA, PES_ALTURA);
    }
}
