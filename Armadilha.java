import greenfoot.Actor;
import greenfoot.GreenfootImage;
import java.awt.Rectangle;
import java.util.IdentityHashMap;
import java.util.Map;

/** Zona de perigo autónoma: estado lógico, animação e dano pertencem ao ator. */
public class Armadilha extends Actor implements Interagivel {
    private static final int DURACAO_DANO = 30;
    private final String identificador;
    private final Rectangle area;
    private final boolean podeSerDesativada;
    private final Map<Player, Integer> cooldowns = new IdentityHashMap<Player, Integer>();
    private int[] duracoes = new int[] { 28, 10, 10, 10, 28, 10, 10, 10 };
    private GreenfootImage[] imagens = new GreenfootImage[0];
    private int frame;
    private int tempoRestante;
    private boolean desativadaPermanentemente;
    private int centroVisualX;
    private int centroVisualY;

    public Armadilha(String identificador, Rectangle area) {
        this(identificador, area, identificador != null && identificador.matches("armadilha_\\d+"));
    }

    public Armadilha(String identificador, Rectangle area, boolean podeSerDesativada) {
        if (area == null) throw new IllegalArgumentException("A area da armadilha nao pode ser null");
        this.identificador = identificador == null ? "armadilha" : identificador.trim();
        this.area = new Rectangle(area);
        this.podeSerDesativada = podeSerDesativada;
        this.centroVisualX = (int) area.getCenterX();
        this.centroVisualY = (int) area.getCenterY();
        this.tempoRestante = duracoes[0];
    }

    /** Recursos preparados pelo carregador, antes do início do jogo. */
    public void configurarRecursosVisuais(GreenfootImage[] imagens, int centroX, int centroY) {
        if (imagens == null || imagens.length == 0) return;
        this.imagens = imagens.clone();
        this.centroVisualX = centroX;
        this.centroVisualY = centroY;
        setImage(this.imagens[0]);
    }

    public int getCentroVisualX() { return centroVisualX; }
    public int getCentroVisualY() { return centroVisualY; }
    public String getIdentificador() { return identificador; }
    public boolean podeSerDesativada() { return podeSerDesativada; }
    public boolean estaDesativadaPermanentemente() { return desativadaPermanentemente; }
    public Rectangle getArea() { return new Rectangle(area); }

    public void configurarAnimacao(int[] duracoesFrames) {
        if (duracoesFrames == null || duracoesFrames.length == 0) return;
        duracoes = duracoesFrames.clone();
        frame = Math.min(frame, duracoes.length - 1);
        tempoRestante = Math.max(1, duracoes[frame]);
    }

    @Override
    public void act() {
        atualizarCooldowns();
        if (!desativadaPermanentemente) {
            tempoRestante--;
            if (tempoRestante <= 0) avancarQuadro();
        }
        aplicarDanoAosJogadores();
    }

    private void atualizarCooldowns() {
        for (Map.Entry<Player, Integer> entrada : cooldowns.entrySet()) {
            if (entrada.getValue() > 0) entrada.setValue(entrada.getValue() - 1);
        }
    }

    private void avancarQuadro() {
        frame = (frame + 1) % duracoes.length;
        tempoRestante = Math.max(1, duracoes[frame]);
        if (imagens.length > 0 && frame < imagens.length) setImage(imagens[frame]);
    }

    private void aplicarDanoAosJogadores() {
        if (!podeCausarDano() || !(getWorld() instanceof MyWorld)) return;
        for (Player jogador : ((MyWorld) getWorld()).getJogadores()) aplicarDano(jogador);
    }

    public boolean podeCausarDano() {
        return !desativadaPermanentemente && frame > 0 && frame < duracoes.length - 1;
    }

    public void desativarPermanentemente() {
        if (!podeSerDesativada) return;
        desativadaPermanentemente = true;
        frame = 0;
        tempoRestante = Integer.MAX_VALUE;
        cooldowns.clear();
        if (imagens.length > 0) setImage(imagens[0]);
    }

    public void aplicarDano(Player jogador) {
        if (jogador == null || !podeCausarDano() || !jogador.estaVivo()
                || !area.intersects(jogador.getPes())) return;
        Integer cooldown = cooldowns.get(jogador);
        if (cooldown == null || cooldown <= 0) {
            jogador.receberDano();
            cooldowns.put(jogador, DURACAO_DANO);
        }
    }

    @Override public boolean temAnimacao() { return true; }
    @Override public int getFrameVisual() { return desativadaPermanentemente ? 0 : frame; }
    @Override public int getRaioSprite() { return 0; }
    @Override public String getDescricao() { return "Armadilha"; }
    @Override public void interagir(Player jogador) { /* Não é acionada pelo jogador. */ }
}
