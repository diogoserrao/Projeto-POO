import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

/**
 * Escala visual comum ao mundo, ao mapa e aos atores.
 *
 * <p>O Greenfoot 3.1.0 não expõe um zoom do canvas. A escala é, por isso,
 * escolhida antes da construção do mundo, com base na área útil do ecrã.
 * Todas as coordenadas continuam a ser coordenadas do jogo; esta classe
 * apenas converte as dimensões para a apresentação.</p>
 */
public final class EscalaJogo {
    public static final int MAP_WIDTH = 40;
    public static final int MAP_HEIGHT = 30;
    public static final int TILE = calcularTile();
    public static final int WORLD_WIDTH = MAP_WIDTH * TILE;
    public static final int WORLD_HEIGHT = MAP_HEIGHT * TILE;

    private EscalaJogo() { }

    private static int calcularTile() {
        double escala = 1.0;
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                Rectangle area = GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getMaximumWindowBounds();
                double largura = Math.max(1, area.width - 32) / 1280.0;
                double altura = Math.max(1, area.height - 90) / 960.0;
                escala = Math.min(1.0, Math.min(largura, altura));
            } else {
                escala = 0.5;
            }
        } catch (Throwable ignorado) {
            // Em compilação/headless, conserva uma apresentação segura.
            escala = 0.5;
        }
        // 16 px/tile é o limite inferior que mantém sprites e HUD legíveis.
        return Math.max(16, Math.min(32, (int) Math.floor(32 * escala)));
    }

    /** Converte uma dimensão lógica de 32 px para a escala atual. */
    public static int px(int valor) {
        return Math.max(1, (int) Math.round(valor * TILE / 32.0));
    }
}
