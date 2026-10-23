import greenfoot.*;

/**
 * Menu inicial que apresenta os controlos e inicia uma nova partida ao premir
 * Enter.
 */
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
        prepararFundo(imagem, DESTAQUE);

        desenharTexto(imagem, "AGENTES ZIG & ZAG", LARGURA / 2, 260,
                new Font("SansSerif", true, false, 58), DESTAQUE);
        desenharTexto(imagem, "OPERAÇÃO: SOMBRAS", LARGURA / 2, 315,
                new Font("SansSerif", true, false, 22), new Color(155, 165, 180));
        desenharTexto(imagem, "Explore a masmorra, encontre as chaves e escape.",
                LARGURA / 2, 400, new Font("SansSerif", false, false, 26), TEXTO);
        desenharTexto(imagem, "Os dois jogadores têm de cooperar e chegar juntos à saída final.",
                LARGURA / 2, 440, new Font("SansSerif", false, false, 22), TEXTO);
        desenharTexto(imagem, "P1  |  WASD para mover  |  E para interagir",
                LARGURA / 2, 500, new Font("SansSerif", false, false, 24), TEXTO);
        desenharTexto(imagem, "P2  |  SETAS para mover  |  ENTER para interagir",
                LARGURA / 2, 545, new Font("SansSerif", false, false, 24), TEXTO);
        desenharTexto(imagem, "Encontra as chaves, desbloqueia as portas e evita os inimigos.",
                LARGURA / 2, 625, new Font("SansSerif", false, false, 22),
                SECUNDARIO);
        desenharTexto(imagem, "PRESSIONE ENTER PARA COMEÇAR",
                LARGURA / 2, 790, new Font("SansSerif", true, false, 28), DESTAQUE);
        setBackground(imagem);
    }
}

