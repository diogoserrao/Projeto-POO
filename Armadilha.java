import java.awt.Rectangle;
import java.util.IdentityHashMap;
import java.util.Map;

/** Uma zona de perigo animada, opcionalmente controlada por uma alavanca. */
public class Armadilha implements Interagivel {
    private static final int DURACAO_DANO = 30;

    private final String identificador;
    private final Rectangle area;
    private final boolean podeSerDesativada;
    private final Map<Player, Integer> cooldowns = new IdentityHashMap<Player, Integer>();
    private int[] duracoes = new int[] { 28, 10, 10, 10, 28, 10, 10, 10 };
    private int frame;
    private int tempoRestante;
    private boolean desativadaPermanentemente;

    public Armadilha(String identificador, Rectangle area) {
        this(identificador, area, identificador != null && identificador.matches("armadilha_\\d+"));
    }

    public Armadilha(String identificador, Rectangle area, boolean podeSerDesativada) {
        if (area == null) throw new IllegalArgumentException("A area da armadilha nao pode ser null");
        this.identificador = identificador == null ? "armadilha" : identificador.trim();
        this.area = new Rectangle(area);
        this.podeSerDesativada = podeSerDesativada;
        this.tempoRestante = this.duracoes[0];
    }

    public String getIdentificador() { return identificador; }
    public boolean podeSerDesativada() { return podeSerDesativada; }
    public boolean estaDesativadaPermanentemente() { return desativadaPermanentemente; }
    public Rectangle getArea() { return new Rectangle(area); }

    /** Recebe as duracoes reais lidas da animacao do tileset, em frames do Greenfoot. */
    public void configurarAnimacao(int[] duracoesFrames) {
        if (duracoesFrames == null || duracoesFrames.length == 0) return;
        duracoes = duracoesFrames.clone();
        frame = Math.min(frame, duracoes.length - 1);
        tempoRestante = Math.max(1, duracoes[frame]);
    }

    /** Avanca a animacao sem alterar uma armadilha permanentemente desativada. */
    public void atualizar() {
        for (Map.Entry<Player, Integer> entrada : cooldowns.entrySet()) {
            if (entrada.getValue() > 0) entrada.setValue(entrada.getValue() - 1);
        }
        if (desativadaPermanentemente) return;
        tempoRestante--;
        if (tempoRestante <= 0) {
            frame = (frame + 1) % duracoes.length;
            tempoRestante = Math.max(1, duracoes[frame]);
        }
    }

    /** O perigo acompanha a parte ascendente/descendente da animacao; os extremos sao seguros. */
    public boolean podeCausarDano() {
        return !desativadaPermanentemente && frame > 0 && frame < duracoes.length - 1;
    }

    public void desativarPermanentemente() {
        if (!podeSerDesativada) return;
        desativadaPermanentemente = true;
        frame = 0;
        tempoRestante = Integer.MAX_VALUE;
        cooldowns.clear();
    }

    /** Aplica no maximo um impacto por jogador durante o intervalo de invulnerabilidade da armadilha. */
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
    @Override public void interagir(Player jogador) { /* Armadilhas nao sao acionadas pelo jogador. */ }
}
