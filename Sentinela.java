/** Inimigo que permanece parado e vigia a sua área. */
public class Sentinela extends Inimigo {
    public Sentinela(int personagem) {
        super(personagem);
        definirMovimento(new Parado());
    }
}
