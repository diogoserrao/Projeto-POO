import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Plataforma que percorre horizontalmente o percurso definido no mapa.
 *
 * <p>Antes de se mover, regista os jogadores apoiados nela e aplica-lhes o
 * mesmo deslocamento horizontal, preservando a posição relativa à plataforma.</p>
 */
public class PlataformaMovel extends Plataforma {
    private final Rectangle percurso;
    private final int velocidade;
    private int sentido = 1;

    public PlataformaMovel(Rectangle area, Rectangle percurso) {
        this(area, percurso, 1);
    }

    public PlataformaMovel(Rectangle area, Rectangle percurso, int velocidade) {
        super(area);
        if (percurso == null || percurso.width <= 0) {
            throw new IllegalArgumentException("O percurso da plataforma tem de ser válido");
        }
        if (velocidade <= 0) throw new IllegalArgumentException("A velocidade deve ser positiva");
        this.percurso = new Rectangle(percurso);
        this.velocidade = velocidade;
        setLocation(area.x + area.width / 2, area.y + area.height / 2);
    }

    @Override
    public void act() {
        if (getWorld() == null) return;
        // Guarda os jogadores antes do deslocamento para transportar apenas
        // quem estava efetivamente apoiado nesta plataforma neste frame.
        List<Player> suportados = new ArrayList<Player>();
        for (Player jogador : getWorld().getObjects(Player.class)) {
            if (suporta(jogador.getPes())) suportados.add(jogador);
        }

        Rectangle antiga = getArea();
        int minimo = percurso.x;
        int maximo = Math.max(minimo, percurso.x + percurso.width - antiga.width);
        int novoX = antiga.x + sentido * velocidade;
        if (novoX <= minimo) { novoX = minimo; sentido = 1; }
        if (novoX >= maximo) { novoX = maximo; sentido = -1; }
        // O percurso é limitado para que a área completa da plataforma nunca
        // ultrapasse os extremos configurados no mapa.
        int dx = novoX - antiga.x;
        Rectangle nova = new Rectangle(novoX, antiga.y, antiga.width, antiga.height);
        setLocation(nova.x + nova.width / 2, nova.y + nova.height / 2);
        atualizarArea(nova);
        for (Player jogador : suportados) jogador.serTransportado(dx, 0);
    }
}
