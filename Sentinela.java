/** Inimigo que permanece parado e vigia a sua área. */
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
