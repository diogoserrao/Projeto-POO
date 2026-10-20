import greenfoot.*;

/** Base visual comum aos ecrãs fora da partida. */
public abstract class Ecra extends World {
    protected static final int LARGURA = 1280;
    protected static final int ALTURA = 960;
    protected static final Color FUNDO = new Color(8, 10, 18);
    protected static final Color TEXTO = new Color(230, 235, 242);
    protected static final Color SECUNDARIO = new Color(165, 175, 192);
    protected static final Color DESTAQUE = new Color(203, 157, 70);

    protected Ecra() {
        super(LARGURA, ALTURA, 1, false);
    }

    protected void prepararFundo(GreenfootImage imagem, Color corBarra) {
        imagem.setColor(FUNDO);
        imagem.fill();
        imagem.setColor(new Color(21, 26, 39));
        imagem.fillRect(0, 0, getWidth(), 14);
        imagem.fillRect(0, getHeight() - 14, getWidth(), 14);
        imagem.setColor(corBarra);
        imagem.fillRect(150, 190, getWidth() - 300, 2);
        imagem.fillRect(150, getHeight() - 190, getWidth() - 300, 2);
    }

    protected void desenharTexto(GreenfootImage imagem, String texto, int centroX,
            int linhaBase, Font fonte, Color cor) {
        imagem.setFont(fonte);
        imagem.setColor(cor);
        int largura = fonte.getSize() * texto.length() / 2;
        imagem.drawString(texto, centroX - largura / 2, linhaBase);
    }

    protected boolean teclaNova(String tecla, boolean estavaPressionada) {
        return Greenfoot.isKeyDown(tecla) && !estavaPressionada;
    }
}
