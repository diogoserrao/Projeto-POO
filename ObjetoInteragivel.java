import java.awt.Rectangle;

/** Base comum dos objetos do mapa com os quais o jogador pode interagir. */
public abstract class ObjetoInteragivel implements Interagivel {
    public static final int FRAME_FINAL = 4;

    private final Rectangle area;

    protected ObjetoInteragivel(Rectangle area) {
        this.area = area;
    }

    /** Extrai o identificador usado pelos nomes dos objetos no mapa Tiled. */
    protected String obterIdentificador(String nome) {
        String normalizado = nome == null ? "" : nome.trim().toLowerCase();
        int separador = normalizado.indexOf('_');
        return separador >= 0 && separador + 1 < normalizado.length()
                ? normalizado.substring(separador + 1) : normalizado;
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
