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
    protected String getAcao() { return "abrir"; }

    @Override
    protected String getTextoConcluido() { return "Baú vazio"; }

    @Override
    protected boolean estaUtilizado() { return aberto; }

    @Override
    public void interagir(Player jogador) {
        if (jogador == null || aberto) return;
        abrir();
        DungeonMap mapa = jogador.getMapa();
        if (mapa != null) mapa.redesenharObjetos();
    }
}
