import greenfoot.*;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player extends Personagem {
    private final Controlos controlos;
    private final int velocidade = 4;
    private int ultimoY;
    private boolean emTransicaoDeEscada;
    private boolean teclaInteracaoPressionada;
    private final Set<String> chaves = new HashSet<String>();

    public Player(int skin, Controlos controlos) {
        this(skin, controlos, 0);
    }

    public Player(int skin, Controlos controlos, int nivel) {
        super(skin, nivel);
        this.controlos = controlos;
    }

    @Override
    public void act() {
        World mundo = getWorld();
        if (mundo instanceof MyWorld && ((MyWorld) mundo).estaEmGameOver()) {
            return;
        }

        if (!estaVivo()) {
            if (!estaEmMorte()) {
                iniciarMorte();
            }
            atualizarAnimacao(false);
            if (terminouMorte() && mundo instanceof MyWorld) {
                ((MyWorld) mundo).terminarJogo();
            }
            return;
        }

        if (estaEmAtaque()) {
            atualizarAnimacao(false);
            return;
        }

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
                definirDirecao(Direcao.CIMA);
            else if (dy > 0)
                definirDirecao(Direcao.BAIXO);
            else if (dx < 0)
                definirDirecao(Direcao.ESQUERDA);
            else
                definirDirecao(Direcao.DIREITA);
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
            if (!atacarInimigoPelasCostas()) {
                Interagivel interagivel = procurarInteragivelPerto();
                if (interagivel != null) {
                    interagivel.interagir(this);
                }
            }
        }

        teclaInteracaoPressionada = teclaPressionada;
    }

    /** Ataca o inimigo mais próximo, mas apenas se estiver atrás dele. */
    private boolean atacarInimigoPelasCostas() {
        World mundo = getWorld();
        if (mundo == null) {
            return false;
        }

        List<Enemy> inimigos = mundo.getObjects(Enemy.class);
        Enemy alvo = null;
        double menorDistanciaQuadrado = Double.MAX_VALUE;

        for (Enemy inimigo : inimigos) {
            if (inimigo.podeSerAtacadoPor(this)) {
                double dx = inimigo.getX() - getX();
                double dy = inimigo.getY() - getY();
                double distanciaQuadrado = dx * dx + dy * dy;
                if (distanciaQuadrado < menorDistanciaQuadrado) {
                    menorDistanciaQuadrado = distanciaQuadrado;
                    alvo = inimigo;
                }
            }
        }

        if (alvo == null) {
            return false;
        }

        iniciarAtaque();
        alvo.receberAtaque();
        return true;
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

    public Controlos getControlos() {
        return controlos;
    }

    /** Adiciona uma chave apenas ao inventário deste jogador. */
    public void adicionarChave(String tipo) {
        if (tipo != null && !tipo.trim().isEmpty()) {
            chaves.add(tipo.trim().toLowerCase());
        }
    }

    /** Indica se este jogador possui uma chave do tipo indicado. */
    public boolean temChave(String tipo) {
        return tipo != null && chaves.contains(tipo.trim().toLowerCase());
    }

    /** Devolve uma visão apenas de leitura das chaves deste jogador. */
    public Set<String> getChaves() {
        return Collections.unmodifiableSet(chaves);
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
