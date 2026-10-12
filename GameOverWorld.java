import greenfoot.*;
/** Ecrã apresentado quando um dos agentes fica sem vidas. */
public class GameOverWorld extends World {
    private static final int LARGURA = 1280;
    private static final int ALTURA = 960;
    private static final Color FUNDO = new Color(12, 8, 14);
    private static final Color VERMELHO = new Color(205, 48, 58);
    private static final Color TEXTO = new Color(235, 235, 240);
    private boolean teclaReiniciarPressionada;
    private boolean teclaEscapePressionada;

    public GameOverWorld() {
        super(LARGURA, ALTURA, 1, false);
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
        GreenfootImage imagem = new GreenfootImage(LARGURA, ALTURA);
        imagem.setColor(FUNDO);
        imagem.fill();
        imagem.setColor(new Color(35, 19, 28));
        imagem.fillRect(0, 0, LARGURA, 12);
        imagem.fillRect(0, ALTURA - 12, LARGURA, 12);
        imagem.setColor(VERMELHO);
        imagem.fillRect(180, 250, LARGURA - 360, 3);
        imagem.fillRect(180, 650, LARGURA - 360, 3);
        desenharTexto(imagem, "MISSÃO FALHADA", LARGURA / 2, 370,
                new Font("SansSerif", true, false, 60), VERMELHO);
        desenharTexto(imagem, "Um dos agentes ficou sem vidas.", LARGURA / 2, 455,
                new Font("SansSerif", false, false, 28), TEXTO);
        desenharTexto(imagem, "R  |  RECOMEÇAR MISSÃO", LARGURA / 2, 545,
                new Font("SansSerif", true, false, 26), TEXTO);
        desenharTexto(imagem, "ESC  |  REGRESSAR AO MENU", LARGURA / 2, 600,
                new Font("SansSerif", true, false, 26), new Color(180, 185, 200));
        setBackground(imagem);
    }

    private void desenharTexto(GreenfootImage imagem, String texto, int centroX,
            int centroY, Font fonte, Color cor) {
        imagem.setFont(fonte);
        imagem.setColor(cor);
        int larguraAproximada = texto.length() * fonte.getSize() / 2;
        imagem.drawString(texto, centroX - larguraAproximada / 2, centroY);
    }
}

