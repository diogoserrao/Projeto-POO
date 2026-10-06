import java.awt.Rectangle;

/**
 * Guarda a posição inicial e a zona de patrulha de um inimigo.
 */
public class ConfiguracaoInimigo {

    private final Rectangle spawn;
    private final Rectangle patrulha;

    public ConfiguracaoInimigo(Rectangle spawn, Rectangle patrulha) {
        this.spawn = spawn;
        this.patrulha = patrulha;
    }

    public Rectangle getSpawn() {
        return spawn;
    }

    public Rectangle getPatrulha() {
        return patrulha;
    }

    public boolean temPatrulha() {
        return patrulha != null;
    }
}