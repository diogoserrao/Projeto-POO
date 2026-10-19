/** Inimigo que patrulha uma zona configurada no mapa. */
public class Guarda extends Inimigo {
    public Guarda(int personagem, java.awt.Rectangle zonaPatrulha) {
        super(personagem);
        definirMovimento(new PatrulhaZona(zonaPatrulha, 2));
    }
}
