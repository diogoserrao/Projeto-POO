import java.awt.Rectangle;

/** Contrato dos objetos com os quais um jogador pode interagir. */
public interface Interagivel {
    Rectangle getArea();

    String getDescricao();

    default String getDescricao(Player jogador) {
        return getDescricao();
    }

    default boolean estaVisivel() { return true; }

    default boolean temAnimacao() { return false; }

    /** Indica se o objeto deve ser selecionado pela tecla de interação. */
    default boolean usaTeclaInteracao() { return true; }

    default int getFrameVisual() { return 0; }

    default int getRaioSprite() { return 1; }

    void interagir(Player jogador);
}
