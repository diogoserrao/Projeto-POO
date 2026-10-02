import greenfoot.*;

/** Jogador ou inimigo, com animação e movimento comuns. */
public class Personagem extends Actor {
    private static final int FRAME_SIZE = 64;
    private static final int DISPLAY_SIZE = 96;
    private static final int BAIXO = 0, ESQUERDA = 1, DIREITA = 2, CIMA = 3;

    private final GreenfootImage walkSpritesheet;
    private final GreenfootImage idleSpritesheet;
    private final int walkFrames;
    private final int idleFrames;
    private final boolean jogador;
    private final int numeroJogador;
    private final boolean mexe;
    private final boolean horizontal;
    private final int limiteInicial;
    private final int limiteFinal;
    private final int velocidade;
    private int sentido = 1;
    private int frame;
    private int linhaDirecao = BAIXO;
    private int contadorAnimacao;
    private boolean estavaEmMovimento;
    private int nivel;

    /** Cria um jogador. */
    public Personagem(int numeroJogador) {
        this(carregarSpritesheet(numeroJogador, "Walk", true),
             carregarSpritesheet(numeroJogador, "Idle", true),
             true, numeroJogador, false, true, 0, 0, 4);
    }

    /** Cria um inimigo que pode patrulhar ou ficar parado. */
    public Personagem(int personagem, boolean mexe, boolean horizontal,
                      int limiteInicial, int limiteFinal, int velocidade) {
        this(carregarSpritesheet(personagem, "Walk", false),
             carregarSpritesheet(personagem, "Idle", false),
             false, 0, mexe, horizontal,
             Math.min(limiteInicial, limiteFinal),
             Math.max(limiteInicial, limiteFinal), Math.max(1, velocidade));
    }

    private Personagem(GreenfootImage walkSpritesheet,
                       GreenfootImage idleSpritesheet, boolean jogador,
                       int numeroJogador, boolean mexe, boolean horizontal,
                       int limiteInicial, int limiteFinal, int velocidade) {
        this.walkSpritesheet = walkSpritesheet;
        this.idleSpritesheet = idleSpritesheet;
        this.walkFrames = walkSpritesheet.getWidth() / FRAME_SIZE;
        this.idleFrames = idleSpritesheet.getWidth() / FRAME_SIZE;
        this.jogador = jogador;
        this.numeroJogador = numeroJogador;
        this.mexe = mexe;
        this.horizontal = horizontal;
        this.limiteInicial = limiteInicial;
        this.limiteFinal = limiteFinal;
        this.velocidade = velocidade;
        mostrarFrame();
    }

    private static GreenfootImage carregarSpritesheet(int personagem,
                                                       String animacao,
                                                       boolean jogador) {
        int nivelPersonagem = jogador ? (personagem == 1 ? 2 : 3) : personagem;
        String pasta = "jogador/PNG/Swordsman_lvl" + nivelPersonagem
            + "/With_shadow/";
        return new GreenfootImage(pasta + "Swordsman_lvl" + nivelPersonagem
            + "_" + animacao + "_with_shadow.png");
    }

    @Override
    public void act() {
        boolean movimento = jogador ? lerTeclas() : mexe && patrulhar();
        atualizarAnimacao(movimento);
    }

    private boolean lerTeclas() {
        int dx = 0, dy = 0;
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
        boolean movimento = false;
        if (dx != 0) movimento |= tentarMover(getX() + dx, getY());
        if (dy != 0) movimento |= tentarMover(getX(), getY() + dy);
        return movimento;
    }

    private boolean patrulhar() {
        int novoX = getX(), novoY = getY();
        if (horizontal) {
            novoX += sentido * velocidade;
            linhaDirecao = sentido > 0 ? DIREITA : ESQUERDA;
        } else {
            novoY += sentido * velocidade;
            linhaDirecao = sentido > 0 ? BAIXO : CIMA;
        }
        int coordenada = horizontal ? novoX : novoY;
        if (coordenada < limiteInicial || coordenada > limiteFinal
                || !tentarMover(novoX, novoY)) {
            sentido *= -1;
            return false;
        }
        return true;
    }

    private boolean tentarMover(int x, int y) {
        if (getWorld() instanceof MyWorld
                && ((MyWorld) getWorld()).podeMover(this, x, y)) {
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

    private void mostrarFrame() {
        GreenfootImage spritesheet = estavaEmMovimento
            ? walkSpritesheet : idleSpritesheet;
        GreenfootImage imagem = new GreenfootImage(FRAME_SIZE, FRAME_SIZE);
        imagem.drawImage(spritesheet, -frame * FRAME_SIZE,
                         -linhaDirecao * FRAME_SIZE);
        imagem.scale(DISPLAY_SIZE, DISPLAY_SIZE);
        setImage(imagem);
    }

    public int getNivel() { return nivel; }
    public void subirNivel() { nivel++; }
    public void descerNivel() { nivel--; }
}
