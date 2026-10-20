import greenfoot.*;

/** Ecrã final apresentado depois de os jogadores escaparem da masmorra. */
public class VitoriaWorld extends Ecra {
    private final int pontuacao;
    private boolean teclaReiniciarPressionada;
    private boolean teclaEscapePressionada;

    public VitoriaWorld(int pontuacao) {
        this.pontuacao = pontuacao;
        desenharEcrã();
    }

    @Override
    public void act() {
        boolean teclaR = Greenfoot.isKeyDown("r");
        boolean teclaEscape = Greenfoot.isKeyDown("escape")
                || Greenfoot.isKeyDown("esc");
        if (teclaNova("r", teclaReiniciarPressionada)) {
            Greenfoot.setWorld(new MyWorld());
        } else if (teclaEscape && !teclaEscapePressionada) {
            Greenfoot.setWorld(new StartScreen());
        }
        teclaReiniciarPressionada = teclaR;
        teclaEscapePressionada = teclaEscape;
    }

    private void desenharEcrã() {
        GreenfootImage imagem = new GreenfootImage(getWidth(), getHeight());
        prepararFundo(imagem, DESTAQUE);
        imagem.setColor(new Color(25, 30, 44));
        imagem.fillRect(220, 235, getWidth() - 440, 430);
        imagem.setColor(new Color(55, 61, 78));
        imagem.drawRect(220, 235, getWidth() - 441, 429);
        desenharTexto(imagem, "CONSEGUISTE ESCAPAR", getWidth() / 2, 345,
                new Font("SansSerif", true, false, 56), DESTAQUE);
        desenharTexto(imagem, "DA MASMORRA!", getWidth() / 2, 415,
                new Font("SansSerif", true, false, 42), DESTAQUE);
        desenharTexto(imagem, "Os dois jogadores encontraram as chaves,", getWidth() / 2, 490,
                new Font("SansSerif", false, false, 25), TEXTO);
        desenharTexto(imagem, "desbloquearam as portas e concluíram a missão.", getWidth() / 2, 530,
                new Font("SansSerif", false, false, 25), TEXTO);
        desenharTexto(imagem, "PONTUAÇÃO FINAL: " + pontuacao, getWidth() / 2, 600,
                new Font("SansSerif", true, false, 28), TEXTO);
        desenharTexto(imagem, "R  |  NOVA PARTIDA", getWidth() / 2, 755,
                new Font("SansSerif", true, false, 26), TEXTO);
        desenharTexto(imagem, "ESC  |  REGRESSAR AO MENU", getWidth() / 2, 805,
                new Font("SansSerif", true, false, 26), SECUNDARIO);
        setBackground(imagem);
    }
}
