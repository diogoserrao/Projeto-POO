import greenfoot.*;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class Player extends Personagem {
    private static final int MAX_PEDRAS = 3;
    /* O som só é preparado quando ocorre dano, não durante a criação do mundo. */
    private static GreenfootSound somDano;
    private final Controlos controlos;
    private final int velocidade = 4;
    private boolean teclaInteracaoPressionada;
    private boolean teclaLancarPressionada;
    private int pedras = 0;
    private final List<Chave> chaves = new ArrayList<Chave>();

    public Player(int skin, Controlos controlos) {
        super(skin, 3);
        this.controlos = controlos;
    }

    @Override
    protected void agir() {
        if (estaEmAtaque()) {
            atualizarAnimacao(false);
            return;
        }

        int dx = 0;
        int dy = 0;

        if (Greenfoot.isKeyDown(controlos.getEsquerda()))
            dx -= velocidade;
        if (Greenfoot.isKeyDown(controlos.getDireita()))
            dx += velocidade;
        if (Greenfoot.isKeyDown(controlos.getCima()))
            dy -= velocidade;
        if (Greenfoot.isKeyDown(controlos.getBaixo()))
            dy += velocidade;

        if (dx != 0 || dy != 0) {
            if (dy < 0)
                definirDirecao(Direcao.CIMA);
            else if (dy > 0)
                definirDirecao(Direcao.BAIXO);
            else if (dx < 0)
                definirDirecao(Direcao.ESQUERDA);
            else
                definirDirecao(Direcao.DIREITA);
        }

        boolean movimento = false;
        if (dx != 0)
            movimento |= tentarMover(getX() + dx, getY());
        if (dy != 0)
            movimento |= tentarMover(getX(), getY() + dy);
        atualizarAnimacao(movimento);
        processarInteracao();
        recolherPedras();
        processarLancamento();

    }

    @Override
    protected void aoTerminarMorte() {
        terminarJogoNoMundo();
    }

    private void processarInteracao() {
        boolean teclaPressionada = Greenfoot.isKeyDown(controlos.getInteragir());

        /* Algumas versões/teclados do Greenfoot identificam ENTER como RETURN. */
        if ("enter".equals(controlos.getInteragir())) {
            teclaPressionada |= Greenfoot.isKeyDown("return");
        }

        if (teclaPressionada && !teclaInteracaoPressionada) {
            if (!atacarInimigoPelasCostas()) {
                Interagivel interagivel = procurarInteragivelPerto();
                if (interagivel != null) {
                    interagivel.interagir(this);
                } else {
                    iniciarAtaque();
                }
            }
        }

        teclaInteracaoPressionada = teclaPressionada;
    }

    /** Lança uma pedra apenas no início da pressão da tecla configurada. */
    private void processarLancamento() {
        boolean teclaPressionada = Greenfoot.isKeyDown(controlos.getLancar());

        if (teclaPressionada && !teclaLancarPressionada) {
            lancarPedra();
        }

        teclaLancarPressionada = teclaPressionada;
    }

    /**
     * Cria uma pedra em frente ao jogador e consome uma unidade do inventário.
     * A posição inicial deixa espaço suficiente para a pedra não ficar sobre
     * a imagem da personagem.
     */
    private void lancarPedra() {
        World mundo = getMundo();
        if (mundo == null || pedras <= 0) {
            return;
        }

        Direcao direcao = getDirecao();
        int distanciaInicial = 20;
        int x = getX() + direcao.getDx() * distanciaInicial;
        int y = getY() + direcao.getDy() * distanciaInicial;

        DungeonMap mapa = getMapa();
        if (mapa != null && mapa.estaBloqueado(new Rectangle(x - 4, y - 4, 9, 9))) {
            return;
        }

        pedras--;
        mundo.addObject(new Pedra(direcao, mapa), x, y);
        // O som só é emitido depois de a pedra ser efetivamente criada.
        Greenfoot.playSound("pedra.mp3");
    }

    private static GreenfootSound criarSomDano() {
        GreenfootSound som = new GreenfootSound("dano.wav");
        som.setVolume(35);
        return som;
    }

    private static GreenfootSound obterSomDano() {
        if (somDano == null) {
            somDano = criarSomDano();
        }
        return somDano;
    }

    /** Reproduz dano apenas quando o jogador continua vivo após o golpe. */
    @Override
    public void receberDano() {
        if (!estaVivo() || estaEmMorte()) {
            return;
        }
        super.receberDano();
        if (estaVivo()) {
            obterSomDano().play();
        }
    }

    /** O som de morte é emitido uma única vez, quando a morte começa. */
    @Override
    public void iniciarMorte() {
        if (estaEmMorte()) {
            return;
        }
        Greenfoot.playSound("die.mp3");
        super.iniciarMorte();
    }

    /** Recolhe automaticamente as pedras disponíveis que estão próximas. */
    private void recolherPedras() {
        if (pedras >= MAX_PEDRAS) {
            return;
        }

        List<Pedra> pedrasPerto = getObjectsInRange(24, Pedra.class);
        for (Pedra pedra : pedrasPerto) {
            if (pedras >= MAX_PEDRAS) {
                break;
            }
            if (pedra.estaPorRecolher()) {
                pedra.interagir(this);
            }
        }
    }

    /** Ataca o inimigo mais próximo, mas apenas se estiver atrás dele. */
    private boolean atacarInimigoPelasCostas() {
        World mundo = getMundo();
        if (mundo == null) {
            return false;
        }

        List<Inimigo> inimigos = mundo.getObjects(Inimigo.class);
        Inimigo alvo = null;
        double menorDistanciaQuadrado = Double.MAX_VALUE;

        for (Inimigo inimigo : inimigos) {
            if (inimigo.podeSerAtacadoPor(this)) {
                double dx = inimigo.getX() - getX();
                double dy = inimigo.getY() - getY();
                double distanciaQuadrado = dx * dx + dy * dy;
                if (distanciaQuadrado < menorDistanciaQuadrado) {
                    menorDistanciaQuadrado = distanciaQuadrado;
                    alvo = inimigo;
                }
            }
        }

        if (alvo == null) {
            return false;
        }

        iniciarAtaque();
        alvo.receberAtaque(this);
        return true;
    }

    private Interagivel procurarInteragivelPerto() {
        DungeonMap mapa = getMapa();
        if (mapa == null) {
            return null;
        }

        Rectangle areaDosPes = new Rectangle(getX() - 12, getY() + 12, 24, 24);

        for (Interagivel interagivel : mapa.getInteragiveis()) {
            if (!(interagivel instanceof Pedra)
                    && interagivel.getArea().intersects(areaDosPes)) {
                return interagivel;
            }
        }

        return null;
    }

    public Interagivel getInteragivelPerto() {
        return procurarInteragivelPerto();
    }

    public String getTeclaInteracao() {
        return controlos.getInteragir();
    }

    public Controlos getControlos() {
        return controlos;
    }

    /** Adiciona uma chave apenas ao inventário deste jogador. */
    public void adicionarChave(Chave chave) {
        if (chave != null && !chaves.contains(chave)) chaves.add(chave);
    }

    /** Indica se este jogador possui uma chave do tipo indicado. */
    public boolean temChave(String tipo) {
        if (tipo == null) return false;
        for (Chave chave : chaves)
            if (tipo.trim().equalsIgnoreCase(chave.getTipo())) return true;
        return false;
    }

    /** Devolve uma visão apenas de leitura das chaves deste jogador. */
    public List<Chave> getChaves() {
        return Collections.unmodifiableList(chaves);
    }

    public int getNumeroChaves() { return chaves.size(); }

    public Chave encontrarChave(Porta porta) {
        for (Chave chave : chaves) if (chave.abre(porta)) return chave;
        return null;
    }

    boolean consumirChave(Chave chave) { return chaves.remove(chave); }

    /** Devolve o número de pedras ainda disponíveis neste jogador. */
    public int getPedras() {
        return pedras;
    }

    /** Adiciona uma pedra ao inventário deste jogador. */
    public void adicionarPedra() {
        if (pedras < MAX_PEDRAS) {
            pedras++;
        }
    }

    /** Indica se este jogador ainda pode lançar uma pedra. */
    public boolean temPedras() {
        return pedras > 0;
    }

    private boolean tentarMover(int x, int y) {
        DungeonMap mapa = getMapa();
        if (mapa != null) {
            Rectangle pes = getPes(x, y);
            Porta porta = mapa.encontrarPortaFechada(pes);
            if (porta != null && !porta.abrir(this)) return false;
        }
        if (mapa != null && !mapa.estaBloqueado(getPes(x, y))) {
            setLocation(x, y);
            return true;
        }
        return false;
    }

}
