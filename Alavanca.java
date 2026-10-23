import java.util.ArrayList;
import java.util.List;

/**
 * Alavanca que pode desativar permanentemente as armadilhas que lhe foram
 * associadas no mapa.
 *
 * <p>O carregador estabelece as associações e a interação apenas altera o
 * estado da alavanca e das armadilhas; o {@link DungeonMap} fica responsável
 * por redesenhar a camada visual.</p>
 */
public class Alavanca extends ObjetoInteragivel {
    private final String nome;
    private boolean ativada;
    private final List<Armadilha> armadilhas = new ArrayList<Armadilha>();

    public Alavanca(String nome, java.awt.Rectangle area) {
        super(area);
        this.nome = nome;
    }

    /** Indica se a alavanca já foi ativada. */
    public boolean estaAtivada() {
        return ativada;
    }

    /** Devolve o identificador usado pelo carregador para ligar armadilhas. */
    public String getIdentificador() { return nome.trim().toLowerCase(); }

    /** Associa uma armadilha e sincroniza imediatamente o estado se necessário. */
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
        // A alavanca é uma transição irreversível: todas as armadilhas
        // associadas passam ao estado seguro na mesma interação.
        if (jogador == null || ativada) return;
        ativada = true;
        for (Armadilha armadilha : armadilhas) armadilha.desativarPermanentemente();
        if (mapa != null) mapa.redesenharObjetos();
    }
}
