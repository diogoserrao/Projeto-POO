/**
 * Inimigo que percorre uma zona retangular configurada no mapa.
 *
 * <p>A classe especializa {@link Inimigo} escolhendo a estratégia
 * {@link PatrulhaZona} e ajustando o alcance e o ângulo da deteção.</p>
 */
public class Guarda extends Inimigo {
    public Guarda(int personagem, java.awt.Rectangle zonaPatrulha) {
        super(personagem);
        definirMovimento(new PatrulhaZona(zonaPatrulha, EscalaJogo.px(2)));
    }

    @Override
    protected int getRaioVisao() {
        return EscalaJogo.px(75);
    }

    @Override
    protected double getAnguloVisao() {
        return 100.0;
    }
}
