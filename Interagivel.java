import java.awt.Rectangle;

/** Contrato dos objetos com os quais um jogador pode interagir. */
public interface Interagivel {
    Rectangle getArea();

    String getDescricao();

    default String getDescricao(Player jogador) {
        return getDescricao();
    }

    void interagir(Player jogador);
}
