import greenfoot.Actor;
import java.awt.Rectangle;

/** Base comum às plataformas que podem suportar jogadores. */
public abstract class Plataforma extends Actor {
    private Rectangle area;

    protected Plataforma(Rectangle area) {
        if (area == null || area.width <= 0 || area.height <= 0) {
            throw new IllegalArgumentException("A área da plataforma tem de ser válida");
        }
        this.area = new Rectangle(area);
        setImage(CarregadorTiled.criarImagemPlataforma(this.area.width, this.area.height));
    }

    public Rectangle getArea() { return new Rectangle(area); }

    protected void atualizarArea(Rectangle novaArea) {
        if (novaArea == null) throw new IllegalArgumentException("A área não pode ser nula");
        area = new Rectangle(novaArea);
    }

    /** Considera suportado um jogador cujo retângulo dos pés toca o topo. */
    public boolean suporta(Rectangle pes) {
        if (pes == null) return false;
        int basePes = pes.y + pes.height;
        return pes.x < area.x + area.width && pes.x + pes.width > area.x
                && basePes >= area.y && basePes <= area.y + 8;
    }
}
