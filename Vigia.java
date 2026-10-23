/**
 * Inimigo rápido que percorre o mapa com movimento aleatório persistente.
 *
 * <p>Usa uma velocidade superior e um cone de visão mais curto, criando um
 * perfil diferente sem alterar o comportamento comum de {@link Inimigo}.</p>
 */
public class Vigia extends Inimigo {
    private static final int VELOCIDADE = 3;
    private static final int DURACAO_DIRECAO = 24;

    public Vigia(int personagem) {
        super(personagem);
        definirMovimento(new Aleatorio(VELOCIDADE, DURACAO_DIRECAO));
    }

    /** O Vigia desloca-se mais depressa e deteta numa area mais curta. */
    @Override
    protected int getRaioVisao() {
        return 60;
    }

    @Override
    protected double getAnguloVisao() {
        return 75.0;
    }
}
