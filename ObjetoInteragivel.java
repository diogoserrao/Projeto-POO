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
        if (jogador == null) {
            return acao;
        }
        return "Premir " + jogador.getTeclaInteracao().toUpperCase()
                + " para " + acao;
    }

    /**
     * Template Method para construir a descrição de qualquer objeto
     * interagível.
     */
    @Override
    public String getDescricao(Player jogador) {
        return estaUtilizado() ? getTextoConcluido()
                : textoTecla(jogador, getAcao());
    }

    /** Mantém a sobrecarga prevista pela interface. */
    @Override
    public String getDescricao() {
        return getDescricao(null);
    }

    /** Indica a ação apresentada enquanto o objeto ainda está disponível. */
    protected abstract String getAcao();

    /** Indica o texto apresentado depois de o objeto ser utilizado. */
    protected abstract String getTextoConcluido();

    /** Devolve se o objeto já foi utilizado. */
    protected abstract boolean estaUtilizado();
}
