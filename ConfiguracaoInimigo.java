import java.awt.Rectangle;

/**
 * Guarda a posição inicial e a zona de patrulha de um inimigo.
 */
public class ConfiguracaoInimigo {

    private final Rectangle spawn;
    private final Rectangle patrulha;

    public ConfiguracaoInimigo(Rectangle spawn, Rectangle patrulha) {
        this.spawn = spawn == null ? null : new Rectangle(spawn);
        this.patrulha = patrulha == null ? null : new Rectangle(patrulha);
    }

    public Rectangle getSpawn() {
        return spawn == null ? null : new Rectangle(spawn);
    }

    public Rectangle getPatrulha() {
        return patrulha == null ? null : new Rectangle(patrulha);
    }

    public boolean temPatrulha() {
        return patrulha != null;
    }
}