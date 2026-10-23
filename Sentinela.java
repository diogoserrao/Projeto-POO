/**
 * Inimigo imóvel que vigia uma área ampla.
 *
 * <p>Especializa {@link Inimigo} através da estratégia {@link Parado}; só a
 * deteção e o ataque continuam ativos.</p>
 */
public class Sentinela extends Inimigo {
    public Sentinela(int personagem) {
        super(personagem);
        definirMovimento(new Parado());
    }

    @Override
    protected int getRaioVisao() {
        return 160;
    }

    @Override
    protected double getAnguloVisao() {
        return 40.0;
    }
}
