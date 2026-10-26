import greenfoot.*;

/**
 * Classe abstrata com o aspeto e utilitários comuns aos ecrãs fora da partida.
 *
 * <p>O menu inicial, o ecrã de derrota e o ecrã de vitória herdam as dimensões,
 * cores e operações de desenho, mas mantêm a sua própria mensagem e transição
 * de estado.</p>
 */
public abstract class Ecra extends World {
    /** Todos os mundos usam exatamente a mesma área de apresentação. */
    protected static final int LARGURA = EscalaJogo.WORLD_WIDTH;
    protected static final int ALTURA = EscalaJogo.WORLD_HEIGHT;
    protected static final Color FUNDO = new Color(8, 10, 18);
    protected static final Color TEXTO = new Color(230, 235, 242);
    protected static final Color SECUNDARIO = new Color(165, 175, 192);
    protected static final Color DESTAQUE = new Color(203, 157, 70);

    /** Cria um mundo de ecrã com as dimensões comuns à interface. */
    protected Ecra() {
        super(LARGURA, ALTURA, 1, false);
    }

    /** Desenha o fundo escuro, barras decorativas e cor de destaque. */
    protected void prepararFundo(GreenfootImage imagem, Color corBarra) {
        imagem.setColor(FUNDO);
        imagem.fill();
        imagem.setColor(new Color(21, 26, 39));
        int espessuraBarra = EscalaJogo.px(14);
        imagem.fillRect(0, 0, getWidth(), espessuraBarra);
        imagem.fillRect(0, getHeight() - espessuraBarra,
                getWidth(), espessuraBarra);
        imagem.setColor(corBarra);
        int margem = EscalaJogo.px(150);
        int linha = EscalaJogo.px(190);
        imagem.fillRect(margem, EscalaJogo.px(190),
                getWidth() - 2 * margem, EscalaJogo.px(2));
        imagem.fillRect(margem, getHeight() - linha,
                getWidth() - 2 * margem, EscalaJogo.px(2));
    }

    /** Desenha texto centrado aproximadamente na coordenada horizontal pedida. */
    protected void desenharTexto(GreenfootImage imagem, String texto, int centroX,
            int linhaBase, Font fonte, Color cor) {
        Font fonteEscalada = new Font(fonte.getName(), fonte.isBold(),
                fonte.isItalic(), EscalaJogo.px(fonte.getSize()));
        imagem.setFont(fonteEscalada);
        imagem.setColor(cor);
        int largura = fonteEscalada.getSize() * texto.length() / 2;
        imagem.drawString(texto, EscalaJogo.px(centroX) - largura / 2,
                EscalaJogo.px(linhaBase));
    }

    /** Deteta apenas o instante inicial de pressão de uma tecla. */
    protected boolean teclaNova(String tecla, boolean estavaPressionada) {
        return Greenfoot.isKeyDown(tecla) && !estavaPressionada;
    }
}
