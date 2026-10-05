import java.awt.Rectangle;

/** Representa um baú interagível do mapa. */
public class Bau implements Interagivel {
    private String nome;
    private Rectangle area;
    private boolean aberto;

    public Bau(String nome, Rectangle area) {
        this.nome = nome;
        this.area = area;
        this.aberto = false;
    }

    public void abrir() {
        if (!aberto) {
            aberto = true;
        }
    }

    public boolean estaAberto() {
        return aberto;
    }

    @Override
    public Rectangle getArea() {
        return area;
    }

    @Override
    public String getDescricao() {
        return aberto ? "Baú vazio" : "Premir E para abrir";
    }

    @Override
    public String getDescricao(Player jogador) {
        return aberto
                ? "Baú vazio"
                : "Premir " + jogador.getTeclaInteracao().toUpperCase() + " para abrir";
    }

    @Override
    public void interagir(Player jogador) {
        abrir();
    }
}
