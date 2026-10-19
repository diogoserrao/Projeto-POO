import java.awt.Rectangle;

/** Representa uma porta trancada definida no mapa Tiled. */
public class Porta implements Interagivel {
    private final String identificador;
    private final Rectangle area;
    private boolean aberta;

    public Porta(String nome, Rectangle area) {
        String normalizado = nome == null ? "" : nome.trim().toLowerCase();
        int separador = normalizado.indexOf('_');
        identificador = separador >= 0 && separador + 1 < normalizado.length()
                ? normalizado.substring(separador + 1) : normalizado;
        this.area = area;
    }

    public String getIdentificador() { return identificador; }
    public boolean estaAberta() { return aberta; }
    public boolean estaFechada() { return !aberta; }

    /** Abre apenas com a chave desta porta e consome-a do jogador. */
    public boolean abrir(Player jogador) {
        if (aberta) return true;
        if (jogador == null) return false;
        Chave chave = jogador.encontrarChave(this);
        if (chave == null || !chave.abre(this) || !jogador.consumirChave(chave)) return false;
        aberta = true;
        DungeonMap mapa = jogador.getMapa();
        if (mapa != null) mapa.redesenharObjetos();
        return true;
    }

    @Override public Rectangle getArea() { return area; }
    @Override public boolean temAnimacao() { return true; }
    @Override public int getFrameVisual() { return aberta ? FRAME_FINAL : 0; }
    @Override public String getDescricao() {
        return aberta ? "Porta aberta" : "Porta trancada (chave " + identificador + ")";
    }
    @Override public String getDescricao(Player jogador) {
        return aberta ? "Porta aberta" : "Porta trancada: precisa da chave " + identificador;
    }
    @Override public void interagir(Player jogador) { abrir(jogador); }
}
