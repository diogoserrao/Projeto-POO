import java.awt.Rectangle;

/** Representa uma chave interagível do mapa. */
public class Chave implements Interagivel {
    private final String tipo;
    private final Rectangle area;
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

    public boolean estaRecolhida() {
        return recolhida;
    }

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

        jogador.adicionarChave(tipo);
        recolhida = true;

        DungeonMap mapa = jogador.getMapa();
        if (mapa != null) {
            mapa.removerInteragivel(this);
        }
    }
}
