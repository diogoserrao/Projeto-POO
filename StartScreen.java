import greenfoot.*;
/** Menu inicial do jogo. */
public class StartScreen extends World {
    private static final int LARGURA = 1280;
    private static final int ALTURA = 960;
    private static final Color FUNDO = new Color(8, 10, 18);
    private static final Color VERMELHO = new Color(190, 45, 55);
    private static final Color TEXTO = new Color(230, 235, 242);
    private boolean teclaEnterPressionada;

    public StartScreen() {
        super(LARGURA, ALTURA, 1, false);
        desenharMenu();
    }

    @Override
    public void act() {
        boolean teclaEnter = Greenfoot.isKeyDown("enter")
                || Greenfoot.isKeyDown("return");
        if (teclaEnter && !teclaEnterPressionada) {
            Greenfoot.setWorld(new MyWorld());
        }
        teclaEnterPressionada = teclaEnter;
    }

    private void desenharMenu() {
        GreenfootImage imagem = new GreenfootImage(LARGURA, ALTURA);
        imagem.setColor(FUNDO);
        imagem.fill();
        imagem.setColor(new Color(22, 27, 40));
        imagem.fillRect(0, 0, LARGURA, 12);
        imagem.fillRect(0, ALTURA - 12, LARGURA, 12);
        imagem.setColor(VERMELHO);
        imagem.fillRect(120, 190, LARGURA - 240, 3);
        imagem.fillRect(120, 720, LARGURA - 240, 3);

        desenharTexto(imagem, "AGENTES ZIG & ZAG", LARGURA / 2, 260,
                new Font("SansSerif", true, false, 58), VERMELHO);
        desenharTexto(imagem, "OPERAÇÃO: SOMBRAS", LARGURA / 2, 315,
                new Font("SansSerif", true, false, 22), new Color(155, 165, 180));
        desenharTexto(imagem, "Dois agentes. Uma missão. Nenhum alarme.",
                LARGURA / 2, 400, new Font("SansSerif", false, false, 26), TEXTO);
        desenharTexto(imagem, "P1  |  WASD para mover  |  E para interagir",
                LARGURA / 2, 500, new Font("SansSerif", false, false, 24), TEXTO);
        desenharTexto(imagem, "P2  |  SETAS para mover  |  ENTER para interagir",
                LARGURA / 2, 545, new Font("SansSerif", false, false, 24), TEXTO);
        desenharTexto(imagem, "Explore as fases, evite os inimigos e abra os baús.",
                LARGURA / 2, 625, new Font("SansSerif", false, false, 22),
                new Color(175, 185, 200));
        desenharTexto(imagem, "PRESSIONE ENTER PARA COMEÇAR",
                LARGURA / 2, 790, new Font("SansSerif", true, false, 28), VERMELHO);
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

