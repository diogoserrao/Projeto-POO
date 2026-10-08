import greenfoot.*;
import java.awt.Rectangle;

public class Player extends Personagem {
    private final Controlos controlos;
    private final int velocidade = 4;
    private int ultimoY;
    private boolean emTransicaoDeEscada;
    private boolean teclaInteracaoPressionada;

    public Player(int skin, Controlos controlos) {
        this(skin, controlos, 0);
    }

    public Player(int skin, Controlos controlos, int nivel) {
        super(skin, nivel);
        this.controlos = controlos;
    }

    @Override
    public void act() {
        int dx = 0;
        int dy = 0;

        if (Greenfoot.isKeyDown(controlos.getEsquerda()))
            dx -= velocidade;
        if (Greenfoot.isKeyDown(controlos.getDireita()))
            dx += velocidade;
        if (Greenfoot.isKeyDown(controlos.getCima()))
            dy -= velocidade;
        if (Greenfoot.isKeyDown(controlos.getBaixo()))
            dy += velocidade;

        if (dx != 0 || dy != 0) {
            if (dy < 0)
                definirDirecao(CIMA);
            else if (dy > 0)
                definirDirecao(BAIXO);
            else if (dx < 0)
                definirDirecao(ESQUERDA);
            else
                definirDirecao(DIREITA);
        }

        boolean movimento = false;
        if (dx != 0)
            movimento |= tentarMover(getX() + dx, getY());
        if (dy != 0)
            movimento |= tentarMover(getX(), getY() + dy);
        atualizarAnimacao(movimento);
        processarInteracao();

    }

    private void processarInteracao() {
        boolean teclaPressionada = Greenfoot.isKeyDown(controlos.getInteragir());

        /* Algumas versões/teclados do Greenfoot identificam ENTER como RETURN. */
        if ("enter".equals(controlos.getInteragir())) {
            teclaPressionada |= Greenfoot.isKeyDown("return");
        }

        if (teclaPressionada && !teclaInteracaoPressionada) {
            Interagivel interagivel = procurarInteragivelPerto();
            if (interagivel != null) {
                interagivel.interagir(this);
            }
        }

        teclaInteracaoPressionada = teclaPressionada;
    }

    private Interagivel procurarInteragivelPerto() {
        DungeonMap mapa = getMapa();
        if (mapa == null) {
            return null;
        }

        Rectangle areaDosPes = new Rectangle(getX() - 12, getY() + 12, 24, 24);

        for (Interagivel interagivel : mapa.getInteragiveis()) {
            if (interagivel.getArea().intersects(areaDosPes)) {
                return interagivel;
            }
        }

        return null;
    }

    public Interagivel getInteragivelPerto() {
        return procurarInteragivelPerto();
    }

    public String getTeclaInteracao() {
        return controlos.getInteragir();
    }

    private boolean tentarMover(int x, int y) {
        DungeonMap mapa = getMapa();
        if (mapa != null && !mapa.estaBloqueado(getPes(x, y), getNivel())) {
            setLocation(x, y);
            return true;
        }
        return false;
    }

    /** Atualiza a transicao de escada deste jogador. */
    public void atualizarEscada(DungeonMap mapa) {
        if (mapa == null) {
            return;
        }

        int yAtual = getY();
        boolean estaNaEscada = mapa.estaNaEscada(getX(), yAtual);

        /*
         * So muda de andar quando existe movimento vertical dentro da escada.
         * Entrar ou permanecer parado na escada nao muda o nivel.
         */
        if (estaNaEscada && yAtual != ultimoY && !emTransicaoDeEscada) {
            if (yAtual > ultimoY) {
                descerNivel();
            } else {
                subirNivel();
            }

            emTransicaoDeEscada = true;
        }

        /* Quando sai da escada, permite uma nova transicao. */
        if (!estaNaEscada) {
            emTransicaoDeEscada = false;
        }

        ultimoY = yAtual;
    }
}
