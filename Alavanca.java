import java.util.ArrayList;
import java.util.List;

/** Representa uma alavanca interagível do mapa. */
public class Alavanca extends ObjetoInteragivel {
    private final String nome;
    private boolean ativada;
    private final List<Armadilha> armadilhas = new ArrayList<Armadilha>();

    public Alavanca(String nome, java.awt.Rectangle area) {
        super(area);
        this.nome = nome;
    }

    public boolean estaAtivada() {
        return ativada;
    }

    public String getIdentificador() { return nome.trim().toLowerCase(); }

    public void associarArmadilha(Armadilha armadilha) {
        if (armadilha != null && !armadilhas.contains(armadilha)) armadilhas.add(armadilha);
        if (ativada && armadilha != null) armadilha.desativarPermanentemente();
    }

    @Override
    public boolean temAnimacao() { return true; }

    @Override
    public int getFrameVisual() { return ativada ? FRAME_FINAL : 0; }

    @Override
    protected String getAcao() { return "ativar a alavanca"; }

    @Override
    protected String getTextoConcluido() { return "Alavanca ativada"; }

    @Override
    protected boolean estaUtilizado() { return ativada; }

    @Override
    public void interagir(Player jogador, DungeonMap mapa, MyWorld mundo) {
        if (jogador == null || ativada) return;
        ativada = true;
        for (Armadilha armadilha : armadilhas) armadilha.desativarPermanentemente();
        if (mapa != null) mapa.redesenharObjetos();
    }
}
