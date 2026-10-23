import greenfoot.Greenfoot;
import java.awt.Rectangle;

/**
 * Porta trancada que só pode ser aberta com a chave que lhe está associada.
 *
 * <p>A porta encapsula o consumo da chave e a atualização visual. A classe
 * {@link Saida} herda este mecanismo e acrescenta as condições de conclusão da
 * fase, demonstrando especialização por herança e sobrescrita.</p>
 */
public class Porta extends ObjetoInteragivel {
    private final String identificador;
    private boolean aberta;

    public Porta(String nome, Rectangle area) {
        super(area);
        identificador = obterIdentificador(nome);
    }

    /** Devolve o identificador normalizado desta porta. */
    public String getIdentificador() {
        return identificador;
    }

    /** Indica se a porta já foi aberta. */
    public boolean estaAberta() {
        return aberta;
    }

    /** Indica se a porta ainda bloqueia a passagem. */
    public boolean estaFechada() {
        return !aberta;
    }

    /** Abre apenas com a chave desta porta e consome-a do jogador. */
    public boolean abrir(Player jogador, DungeonMap mapa, MyWorld mundo) {
        // A porta valida a chave antes de alterar o próprio estado; por isso
        // uma tentativa sem chave não produz som nem redesenho.
        if (aberta)
            return true;
        if (jogador == null)
            return false;
        if (!consumirChaveCorreta(jogador))
            return false;
        concluirAbertura(mapa);
        return true;
    }

    /**
     * Consome exclusivamente uma chave que corresponda a esta porta.
     * Subclasses especiais reutilizam esta operação depois de validarem as
     * suas próprias condições de abertura.
     */
    protected final boolean consumirChaveCorreta(Player jogador) {
        if (jogador == null)
            return false;
        Chave chave = jogador.encontrarChave(this);
        return chave != null && jogador.consumirChave(chave);
    }

    /** Atualiza o estado visual depois de uma abertura validada. */
    protected final void concluirAbertura(DungeonMap mapa) {
        aberta = true;
        Greenfoot.playSound("win.wav");
        if (mapa != null)
            mapa.redesenharObjetos();
    }

    @Override
    public boolean temAnimacao() {
        return true;
    }

    @Override
    public int getFrameVisual() {
        return aberta ? FRAME_FINAL : 0;
    }

    @Override
    protected String getAcao() {
        return "abrir (chave " + identificador + ")";
    }

    @Override
    protected String getTextoConcluido() {
        return "Porta aberta";
    }

    @Override
    protected boolean estaUtilizado() {
        return aberta;
    }

    @Override
    public void interagir(Player jogador, DungeonMap mapa, MyWorld mundo) {
        abrir(jogador, mapa, mundo);
    }
}

