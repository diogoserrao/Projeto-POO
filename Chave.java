import greenfoot.Greenfoot;

/** Representa uma chave interagível do mapa. */
public class Chave extends ObjetoInteragivel {
    private final String tipo;
    private Porta porta;
    private boolean recolhida;

    public Chave(String nome, java.awt.Rectangle area) {
        super(area);
        this.tipo = obterIdentificador(nome);
    }

    public String getTipo() {
        return tipo;
    }

    public void associarPorta(Porta porta) { this.porta = porta; }
    public Porta getPorta() { return porta; }
    public boolean abre(Porta porta) { return porta != null && this.porta == porta; }

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
    public void interagir(Player jogador) {
        if (recolhida || jogador == null) {
            return;
        }

        jogador.adicionarChave(this);
        recolhida = true;
        // A chave só produz som depois de entrar realmente no inventário.
        Greenfoot.playSound("chave.wav");

        DungeonMap mapa = jogador.getMapa();
        if (mapa != null) {
            mapa.removerInteragivel(this);
        }
    }
}
