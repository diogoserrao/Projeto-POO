import greenfoot.*;

/** Ecrã apresentado quando um dos agentes fica sem vidas. */
public class GameOverWorld extends Ecra {
    private static final Color FUNDO = new Color(12, 8, 14);
    private static final Color VERMELHO = new Color(205, 48, 58);
    private boolean teclaReiniciarPressionada;
    private boolean teclaEscapePressionada;

    public GameOverWorld() {
        desenharEcrã();
    }

    @Override
    public void act() {
        boolean teclaR = Greenfoot.isKeyDown("r");
        boolean teclaEscape = Greenfoot.isKeyDown("escape")
                || Greenfoot.isKeyDown("esc");
        if (teclaR && !teclaReiniciarPressionada) {
            Greenfoot.setWorld(new MyWorld());
        } else if (teclaEscape && !teclaEscapePressionada) {
            Greenfoot.setWorld(new StartScreen());
        }
        teclaReiniciarPressionada = teclaR;
        teclaEscapePressionada = teclaEscape;
    }

    private void desenharEcrã() {
        GreenfootImage imagem = new GreenfootImage(getWidth(), getHeight());
        prepararFundo(imagem, VERMELHO);
        desenharTexto(imagem, "MISSÃO FALHADA", LARGURA / 2, 370,
                new Font("SansSerif", true, false, 60), VERMELHO);
        desenharTexto(imagem, "Um dos agentes ficou sem vidas.", LARGURA / 2, 455,
                new Font("SansSerif", false, false, 28), TEXTO);
        desenharTexto(imagem, "R  |  RECOMEÇAR MISSÃO", LARGURA / 2, 545,
                new Font("SansSerif", true, false, 26), TEXTO);
        desenharTexto(imagem, "ESC  |  REGRESSAR AO MENU", LARGURA / 2, 600,
                new Font("SansSerif", true, false, 26), SECUNDARIO);
        setBackground(imagem);
    }
}

