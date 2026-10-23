import java.awt.Rectangle;

/**
 * Dados imutáveis de colocação de um inimigo lidos a partir do mapa.
 *
 * <p>A configuração mantém o ponto de nascimento e, quando existe, o
 * retângulo de patrulha. As cópias devolvidas evitam que o carregador ou o
 * mundo alterem acidentalmente a geometria original.</p>
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
