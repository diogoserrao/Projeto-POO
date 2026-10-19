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
    public String getDescricao() {
        return ativada ? "Alavanca ativada" : textoTecla(null, "ativar a alavanca");
    }

    @Override
    public String getDescricao(Player jogador) {
        return ativada
                ? "Alavanca ativada"
                : "Premir " + jogador.getTeclaInteracao().toUpperCase()
                        + " para ativar a alavanca";
    }

    @Override
    public void interagir(Player jogador) {
        if (jogador == null || ativada) return;
        ativada = true;
        for (Armadilha armadilha : armadilhas) armadilha.desativarPermanentemente();
        DungeonMap mapa = jogador.getMapa();
        if (mapa != null) mapa.redesenharObjetos();
    }
}
