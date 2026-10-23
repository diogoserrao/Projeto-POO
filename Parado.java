/** Estratégia de movimento que mantém o inimigo no ponto onde foi colocado. */
public class Parado implements Movimento {
    @Override
    public boolean mover(Inimigo inimigo) {
        return false;
    }
}
