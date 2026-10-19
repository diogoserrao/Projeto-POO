import greenfoot.Greenfoot;

/** Representa um baú interagível do mapa. */
public class Bau extends ObjetoInteragivel {
    private boolean aberto;

    public Bau(String nome, java.awt.Rectangle area) {
        super(area);
        this.aberto = false;
    }

    public void abrir() {
        if (!aberto) {
            aberto = true;
            // O som acompanha a transição fechada -> aberto, uma única vez.
            Greenfoot.playSound("bau.mp3");
        }
    }

    public boolean estaAberto() {
        return aberto;
    }

    @Override
    public boolean temAnimacao() { return true; }

    @Override
    public int getFrameVisual() { return aberto ? FRAME_FINAL : 0; }

    @Override
    public String getDescricao() {
        return aberto ? "Baú vazio" : textoTecla(null, "abrir");
    }

    @Override
    public String getDescricao(Player jogador) {
        return aberto
                ? "Baú vazio"
                : "Premir " + jogador.getTeclaInteracao().toUpperCase() + " para abrir";
    }

    @Override
    public void interagir(Player jogador) {
        if (jogador == null || aberto) return;
        abrir();
        DungeonMap mapa = jogador.getMapa();
        if (mapa != null) mapa.redesenharObjetos();
    }
}
