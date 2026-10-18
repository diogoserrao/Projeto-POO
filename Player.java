import greenfoot.*;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player extends Personagem {
    private final Controlos controlos;
    private final int velocidade = 4;
    private boolean teclaInteracaoPressionada;
    private boolean teclaLancarPressionada;
    private int pedras = 1;
    private final Set<String> chaves = new HashSet<String>();

    public Player(int skin, Controlos controlos) {
        super(skin);
        this.controlos = controlos;
    }

    @Override
    public void act() {
        if (mundoEstaEmGameOver()) {
            return;
        }

        if (!estaVivo()) {
            if (!estaEmMorte()) {
                iniciarMorte();
            }
            atualizarAnimacao(false);
            if (terminouMorte()) {
                terminarJogoNoMundo();
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
        processarLancamento();

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
                } else {
                    iniciarAtaque();
                }
            }
        }

        teclaInteracaoPressionada = teclaPressionada;
    }

    /** Lança uma pedra apenas no início da pressão da tecla configurada. */
    private void processarLancamento() {
        boolean teclaPressionada = Greenfoot.isKeyDown(controlos.getLancar());

        if (teclaPressionada && !teclaLancarPressionada) {
            lancarPedra();
        }

        teclaLancarPressionada = teclaPressionada;
    }

    /**
     * Cria uma pedra em frente ao jogador e consome uma unidade do inventário.
     * A posição inicial deixa espaço suficiente para a pedra não ficar sobre
     * a imagem da personagem.
     */
    private void lancarPedra() {
        World mundo = getWorld();
        if (mundo == null || pedras <= 0) {
            return;
        }

        Direcao direcao = getDirecao();
        int distanciaInicial = 40;
        int x = getX() + direcao.getDx() * distanciaInicial;
        int y = getY() + direcao.getDy() * distanciaInicial;

        pedras--;
        mundo.addObject(new Pedra(direcao), x, y);
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

    /** Devolve o número de pedras ainda disponíveis neste jogador. */
    public int getPedras() {
        return pedras;
    }

    /** Adiciona uma pedra ao inventário deste jogador. */
    public void adicionarPedra() {
        pedras++;
    }

    /** Indica se este jogador ainda pode lançar uma pedra. */
    public boolean temPedras() {
        return pedras > 0;
    }

    private boolean tentarMover(int x, int y) {
        DungeonMap mapa = getMapa();
        if (mapa != null && !mapa.estaBloqueado(getPes(x, y))) {
            setLocation(x, y);
            return true;
        }
        return false;
    }

}
