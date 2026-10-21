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
    public String getDescricao() {
        return recolhida ? "Chave já recolhida" : textoTecla(null, "apanhar a chave");
    }

    @Override
    public String getDescricao(Player jogador) {
        return recolhida
                ? "Chave já recolhida"
                : "Premir " + jogador.getTeclaInteracao().toUpperCase()
                        + " para apanhar a chave";
    }

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
