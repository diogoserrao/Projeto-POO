import java.awt.Rectangle;

/** Plataforma parada, com ciclos de visibilidade e piscar antes de desaparecer. */
public class PlataformaTemporaria extends Plataforma {
    private boolean visivel = true;
    private int contador;
    private final int tempoVisivel;
    private final int tempoEscondida;
    private final greenfoot.GreenfootImage imagem;

    public PlataformaTemporaria(Rectangle area) { this(area, 120, 90); }

    public PlataformaTemporaria(Rectangle area, int tempoVisivel, int tempoEscondida) {
        super(area);
        if (tempoVisivel <= 0 || tempoEscondida <= 0) {
            throw new IllegalArgumentException("Os tempos da plataforma devem ser positivos");
        }
        this.tempoVisivel = tempoVisivel;
        this.tempoEscondida = tempoEscondida;
        imagem = new greenfoot.GreenfootImage(getImage());
        setLocation(area.x + area.width / 2, area.y + area.height / 2);
    }

    @Override
    public void act() {
        contador++;
        if (visivel) {
            if (contador > tempoVisivel) {
                visivel = false;
                contador = 0;
                setImage(new greenfoot.GreenfootImage(imagem));
                getImage().setTransparency(0);
            } else if (contador > tempoVisivel - 30) {
                greenfoot.GreenfootImage imagemAtual = new greenfoot.GreenfootImage(imagem);
                imagemAtual.setTransparency((contador / 4) % 2 == 0 ? 255 : 70);
                setImage(imagemAtual);
            }
        } else if (contador >= tempoEscondida) {
            visivel = true;
            contador = 0;
            setImage(new greenfoot.GreenfootImage(imagem));
        }
    }

    @Override
    public boolean suporta(Rectangle pes) {
        return visivel && super.suporta(pes);
    }
}
