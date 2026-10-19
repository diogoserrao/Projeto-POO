import greenfoot.Greenfoot;
import java.awt.Rectangle;

/** Representa uma chave interagível do mapa. */
public class Chave implements Interagivel {
    private final String tipo;
    private final Rectangle area;
    private Porta porta;
    private boolean recolhida;

    public Chave(String nome, Rectangle area) {
        this.tipo = obterTipo(nome);
        this.area = area;
    }

    private String obterTipo(String nome) {
        String nomeNormalizado = nome == null ? "" : nome.trim().toLowerCase();
        int separador = nomeNormalizado.indexOf('_');
        return separador >= 0 && separador + 1 < nomeNormalizado.length()
                ? nomeNormalizado.substring(separador + 1)
                : nomeNormalizado;
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
    public Rectangle getArea() {
        return area;
    }

    @Override
    public String getDescricao() {
        return recolhida ? "Chave já recolhida" : "Apanhar chave";
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
