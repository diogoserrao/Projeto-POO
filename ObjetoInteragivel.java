import java.awt.Rectangle;

/** Base comum dos objetos do mapa com os quais o jogador pode interagir. */
public abstract class ObjetoInteragivel implements Interagivel {
    public static final int FRAME_FINAL = 4;

    private final Rectangle area;

    protected ObjetoInteragivel(Rectangle area) {
        this.area = area;
    }

    @Override
    public Rectangle getArea() {
        return area;
    }

    /** Constrói a indicação de interação usada pelos objetos do mapa. */
    protected String textoTecla(Player jogador, String acao) {
        return "Premir E para " + acao;
    }
}
