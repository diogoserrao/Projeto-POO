/** Comportamento de um inimigo que permanece parado. */
public class Parado implements Movimento {
    @Override
    public boolean mover(Enemy inimigo) {
        return false;
    }
}
