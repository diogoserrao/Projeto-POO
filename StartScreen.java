import greenfoot.*;

/** Menu inicial que apresenta a missão e os controlos dos dois jogadores. */
public class StartScreen extends Ecra {
    private boolean teclaEnterPressionada;

    /** Cria o menu e desenha a informação inicial da missão. */
    public StartScreen() {
        desenharMenu();
    }

    @Override
    /** Inicia uma partida quando Enter passa de não pressionada a pressionada. */
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
        prepararFundoInicio(imagem);

        desenharTexto(imagem, "AGENTES ZIG & ZAG", LARGURA / 2, 205,
                new Font("SansSerif", true, false, 58), DESTAQUE);
        desenharTexto(imagem, "OPERAÇÃO: SOMBRAS", LARGURA / 2, 260,
                new Font("SansSerif", true, false, 22), new Color(155, 165, 180));
        desenharTexto(imagem, "Infiltrem-se na masmorra, encontrem as chaves e escapem sem serem vistos.",
                LARGURA / 2, 330, new Font("SansSerif", false, false, 22), TEXTO);
        desenharTexto(imagem, "Os dois agentes têm de cooperar e chegar juntos à saída final.",
                LARGURA / 2, 370, new Font("SansSerif", false, false, 22), TEXTO);

        int larguraCartao = 450;
        int espacoEntreCartoes = 34;
        int inicioCartoes = (LARGURA - 2 * larguraCartao - espacoEntreCartoes) / 2;
        desenharCartaoJogador(imagem, inicioCartoes, 425, larguraCartao, 340, "ZIG", "Agente 1",
                new Color(94, 164, 244), "W", "A", "S", "D", "E", "Q", "CTRL");
        desenharCartaoJogador(imagem, inicioCartoes + larguraCartao + espacoEntreCartoes,
                425, larguraCartao, 340, "ZAG", "Agente 2",
                new Color(105, 202, 139), "▲", "◀", "▼", "▶", "ENTER", "SHIFT", "<");

        desenharTexto(imagem, "Ataca pelas costas para eliminar em silêncio. Lança pedras para distrair os guardas.",
                LARGURA / 2, 820, new Font("SansSerif", false, false, 18), SECUNDARIO);
        desenharTexto(imagem, "PRESSIONE ENTER PARA COMEÇAR",
                LARGURA / 2, 865, new Font("SansSerif", true, false, 28), DESTAQUE);
        setBackground(imagem);
    }

    private void prepararFundoInicio(GreenfootImage imagem) {
        imagem.setColor(FUNDO);
        imagem.fill();
        imagem.setColor(new Color(21, 26, 39));
        int espessuraBarra = EscalaJogo.px(14);
        imagem.fillRect(0, 0, getWidth(), espessuraBarra);
        imagem.fillRect(0, getHeight() - espessuraBarra,
                getWidth(), espessuraBarra);
        imagem.setColor(DESTAQUE);
        int margem = EscalaJogo.px(130);
        imagem.fillRect(margem, EscalaJogo.px(125),
                getWidth() - 2 * margem, EscalaJogo.px(2));
        imagem.fillRect(margem, EscalaJogo.px(795),
                getWidth() - 2 * margem, EscalaJogo.px(2));
    }

    private void desenharCartaoJogador(GreenfootImage imagem, int x, int y,
            int largura, int altura, String nome, String subtitulo, Color cor,
            String cima, String esquerda, String baixo, String direita,
            String interagir, String lancar, String saltar) {
        imagem.setColor(new Color(26, 32, 48));
        imagem.fillRect(EscalaJogo.px(x), EscalaJogo.px(y),
                EscalaJogo.px(largura), EscalaJogo.px(altura));
        imagem.setColor(new Color(61, 70, 91));
        imagem.drawRect(EscalaJogo.px(x), EscalaJogo.px(y),
                EscalaJogo.px(largura), EscalaJogo.px(altura));
        imagem.setColor(cor);
        imagem.fillRect(EscalaJogo.px(x), EscalaJogo.px(y),
                EscalaJogo.px(largura), EscalaJogo.px(8));

        desenharTextoEsquerda(imagem, nome, x + 25, y + 50,
                new Font("SansSerif", true, false, 28), cor);
        desenharTextoEsquerda(imagem, subtitulo, x + 25, y + 78,
                new Font("SansSerif", false, false, 17), SECUNDARIO);
        desenharTextoEsquerda(imagem, "Mover", x + 25, y + 135,
                new Font("SansSerif", true, false, 18), TEXTO);
        desenharTextoEsquerda(imagem, "Interagir / atacar", x + 25, y + 225,
                new Font("SansSerif", false, false, 17), TEXTO);
        desenharTextoEsquerda(imagem, "Lançar pedra", x + 25, y + 275,
                new Font("SansSerif", false, false, 17), TEXTO);
        desenharTextoEsquerda(imagem, "Saltar", x + 25, y + 320,
                new Font("SansSerif", false, false, 17), TEXTO);

        int teclasX = x + largura - 125;
        desenharTecla(imagem, cima, teclasX, y + 100);
        desenharTecla(imagem, esquerda, teclasX - 65, y + 158);
        desenharTecla(imagem, baixo, teclasX, y + 158);
        desenharTecla(imagem, direita, teclasX + 65, y + 158);
        desenharTecla(imagem, interagir, teclasX + 5, y + 212);
        desenharTecla(imagem, lancar, teclasX + 5, y + 262);
        desenharTecla(imagem, saltar, teclasX + 5, y + 312);
    }

    private void desenharTecla(GreenfootImage imagem, String texto, int centroX, int centroY) {
        int largura = Math.max(42, texto.length() * 14 + 20);
        int altura = 42;
        imagem.setColor(new Color(38, 47, 68));
        imagem.fillRect(EscalaJogo.px(centroX - largura / 2),
                EscalaJogo.px(centroY - altura / 2),
                EscalaJogo.px(largura), EscalaJogo.px(altura));
        imagem.setColor(new Color(105, 119, 148));
        imagem.drawRect(EscalaJogo.px(centroX - largura / 2),
                EscalaJogo.px(centroY - altura / 2),
                EscalaJogo.px(largura), EscalaJogo.px(altura));
        desenharTexto(imagem, texto, centroX, centroY + 8,
                new Font("SansSerif", true, false, texto.length() > 4 ? 16 : 21), TEXTO);
    }

    private void desenharTextoEsquerda(GreenfootImage imagem, String texto, int x,
            int linhaBase, Font fonte, Color cor) {
        Font fonteEscalada = new Font(fonte.getName(), fonte.isBold(),
                fonte.isItalic(), EscalaJogo.px(fonte.getSize()));
        imagem.setFont(fonteEscalada);
        imagem.setColor(cor);
        imagem.drawString(texto, EscalaJogo.px(x), EscalaJogo.px(linhaBase));
    }
}

