import java.awt.Rectangle;

/** Representa uma alavanca interagível do mapa. */
public class Alavanca implements Interagivel {
    private final String nome;
    private final Rectangle area;
    private boolean ativada;

    public Alavanca(String nome, Rectangle area) {
        this.nome = nome;
        this.area = area;
    }

    public boolean estaAtivada() {
        return ativada;
    }

    @Override
    public Rectangle getArea() {
        return area;
    }

    @Override
    public String getDescricao() {
        return ativada ? "Alavanca ativada" : "Ativar alavanca";
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
        if (jogador != null) {
            ativada = true;
        }
    }
}
