import greenfoot.Greenfoot;
import java.awt.Rectangle;

/** Representa uma porta trancada definida no mapa Tiled. */
public class Porta extends ObjetoInteragivel {
    private final String identificador;
    private boolean aberta;

    public Porta(String nome, Rectangle area) {
        super(area);
        identificador = obterIdentificador(nome);
    }

    public String getIdentificador() { return identificador; }
    public boolean estaAberta() { return aberta; }
    public boolean estaFechada() { return !aberta; }

    /** Abre apenas com a chave desta porta e consome-a do jogador. */
    public boolean abrir(Player jogador) {
        if (aberta) return true;
        if (jogador == null) return false;
        if (!consumirChaveCorreta(jogador)) return false;
        concluirAbertura(jogador);
        return true;
    }

    /**
     * Consome exclusivamente uma chave que corresponda a esta porta.
     * Subclasses especiais reutilizam esta operação depois de validarem as
     * suas próprias condições de abertura.
     */
    protected final boolean consumirChaveCorreta(Player jogador) {
        if (jogador == null) return false;
        Chave chave = jogador.encontrarChave(this);
        return chave != null && jogador.consumirChave(chave);
    }

    /** Atualiza o estado visual depois de uma abertura validada. */
    protected final void concluirAbertura(Player jogador) {
        aberta = true;
        Greenfoot.playSound("win.wav");
        DungeonMap mapa = jogador == null ? null : jogador.getMapa();
        if (mapa != null) mapa.redesenharObjetos();
    }

    @Override public boolean temAnimacao() { return true; }
    @Override public int getFrameVisual() { return aberta ? FRAME_FINAL : 0; }
    @Override public String getDescricao() {
        return aberta ? "Porta aberta" : "Porta trancada (chave " + identificador + ")";
    }
    @Override public String getDescricao(Player jogador) { return getDescricao(); }
    @Override public void interagir(Player jogador) { abrir(jogador); }
}
