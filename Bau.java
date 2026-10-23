import greenfoot.Greenfoot;

/**
 * Baú interagível que passa uma única vez do estado fechado para aberto.
 *
 * <p>O estado é mantido no objeto para que a imagem, a mensagem do HUD e o
 * som acompanhem a mesma transição, sem repetir o efeito em interações futuras.</p>
 */
public class Bau extends ObjetoInteragivel {
    private boolean aberto;

    public Bau(String nome, java.awt.Rectangle area) {
        super(area);
        this.aberto = false;
    }

    /** Abre o baú uma única vez e reproduz o som correspondente. */
    public void abrir() {
        if (!aberto) {
            aberto = true;
            // O som acompanha a transição fechada -> aberto, uma única vez.
            Greenfoot.playSound("bau.mp3");
        }
    }

    /** Indica se o baú já foi aberto. */
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
    public void interagir(Player jogador, DungeonMap mapa, MyWorld mundo) {
        if (jogador == null || aberto) return;
        abrir();
        if (mapa != null) mapa.redesenharObjetos();
    }
}
