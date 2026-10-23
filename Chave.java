import greenfoot.Greenfoot;

/**
 * Chave que o jogador pode recolher e que fica associada a uma porta concreta.
 *
 * <p>A associação por referência, em vez de apenas pelo texto do nome, garante
 * que uma chave abre exatamente a porta definida pelo mapa. Depois de recolhida,
 * deixa de ser desenhada e passa para o inventário do jogador.</p>
 */
public class Chave extends ObjetoInteragivel {
    private final String tipo;
    private Porta porta;
    private boolean recolhida;

    public Chave(String nome, java.awt.Rectangle area) {
        super(area);
        this.tipo = obterIdentificador(nome);
    }

    /** Devolve o identificador da porta que esta chave pode abrir. */
    public String getTipo() {
        return tipo;
    }

    /** Liga esta chave à instância concreta da porta correspondente. */
    public void associarPorta(Porta porta) { this.porta = porta; }
    /** Devolve a porta associada à chave. */
    public Porta getPorta() { return porta; }
    /** Indica se esta chave abre exatamente a porta recebida. */
    public boolean abre(Porta porta) { return porta != null && this.porta == porta; }

    /** Indica se a chave já foi transferida para o inventário de um jogador. */
    public boolean estaRecolhida() {
        return recolhida;
    }

    @Override
    public boolean estaVisivel() { return !recolhida; }

    /** A chave ocupa apenas o tile onde está desenhada. */
    @Override
    public int getRaioSprite() { return 0; }

    @Override
    protected String getAcao() { return "apanhar a chave"; }

    @Override
    protected String getTextoConcluido() { return "Chave já recolhida"; }

    @Override
    protected boolean estaUtilizado() { return recolhida; }

    @Override
    public void interagir(Player jogador, DungeonMap mapa, MyWorld mundo) {
        // A ordem garante que o som e a remoção visual só ocorrem depois de a
        // chave ter sido efetivamente adicionada ao inventário.
        if (recolhida || jogador == null) {
            return;
        }

        jogador.adicionarChave(this);
        recolhida = true;
        // A chave só produz som depois de entrar realmente no inventário.
        Greenfoot.playSound("chave.wav");

        if (mapa != null) {
            mapa.removerInteragivel(this);
        }
    }
}
