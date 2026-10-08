/** Define um comportamento de movimento para um inimigo. */
public interface Movimento {
    /**
     * Tenta mover o inimigo.
     *
     * @return true quando o inimigo se moveu; false caso contrario
     */
    boolean mover(Enemy inimigo);
}
