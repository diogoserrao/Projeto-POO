import java.awt.Rectangle;

/**
 * Contrato comum dos elementos do mapa que podem colaborar com um jogador.
 *
 * <p>O mapa trata chaves, portas, alavancas, pedras e outros objetos através
 * desta abstração, aplicando polimorfismo sem conhecer a classe concreta. Os
 * métodos predefinidos fornecem comportamentos visuais e de interface opcionais
 * para objetos que não precisam de os substituir.</p>
 */
public interface Interagivel {
    Rectangle getArea();

    String getDescricao();

    /** Devolve a indicação apresentada ao jogador que está perto do objeto. */
    default String getDescricao(Player jogador) {
        return getDescricao();
    }

    /** Indica se o objeto deve aparecer na camada visual do mapa. */
    default boolean estaVisivel() { return true; }

    /** Indica se os tiles deste objeto devem ser desenhados pelo mapa. */
    default boolean deveDesenharTiles() { return true; }

    /** Indica se o estado visual do objeto usa vários frames. */
    default boolean temAnimacao() { return false; }

    /** Indica se a proximidade seleciona o objeto com a tecla de interação. */
    default boolean usaTeclaInteracao() { return true; }

    /** Devolve o frame lógico a desenhar, quando o objeto é animado. */
    default int getFrameVisual() { return 0; }

    /** Devolve a margem de tiles usada ao desenhar o sprite do objeto. */
    default int getRaioSprite() { return 1; }

    /** Executa a interação com o contexto explícito do mundo atual. */
    void interagir(Player jogador, DungeonMap mapa, MyWorld mundo);
}
