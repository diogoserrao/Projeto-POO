import greenfoot.Actor;
import java.awt.Rectangle;

/**
 * Abstração das plataformas que podem suportar jogadores.
 *
 * <p>Define a geometria e o teste de suporte partilhados por plataformas
 * móveis e temporárias. As subclasses especializam apenas a forma como essa
 * geometria e a disponibilidade mudam ao longo dos frames.</p>
 */
public abstract class Plataforma extends Actor {
    private Rectangle area;

    protected Plataforma(Rectangle area) {
        if (area == null || area.width <= 0 || area.height <= 0) {
            throw new IllegalArgumentException("A área da plataforma tem de ser válida");
        }
        this.area = new Rectangle(area);
        setImage(CarregadorTiled.criarImagemPlataforma(this.area.width, this.area.height));
    }

    /** Devolve uma cópia da área atual da plataforma. */
    public Rectangle getArea() { return new Rectangle(area); }

    /** Atualiza a geometria lógica quando uma plataforma se desloca. */
    protected void atualizarArea(Rectangle novaArea) {
        if (novaArea == null) throw new IllegalArgumentException("A área não pode ser nula");
        area = new Rectangle(novaArea);
    }

    /** Considera suportado um jogador cujo retângulo dos pés toca o topo. */
    /**
     * Verifica se o centro da área dos pés está dentro da plataforma.
     *
     * @param pes retângulo lógico dos pés do jogador
     * @return true quando a plataforma pode servir de apoio
     */
    public boolean suporta(Rectangle pes) {
    if (pes == null) {
        return false;
    }

    int centroPesX = pes.x + pes.width / 2;
    int centroPesY = pes.y + pes.height / 2;

    return area.contains(centroPesX, centroPesY);
}
}
