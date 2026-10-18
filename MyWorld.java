import greenfoot.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.awt.Rectangle;
import java.awt.Point;

public class MyWorld extends World {

    private DungeonMap mapa;

    /* "Fase" representa os mapas Dungeon1, Dungeon2 e Dungeon3. */
    private int faseAtual = 1;
    private static final int MAX_FASES = 3;

    private final List<Player> jogadores = new ArrayList<Player>();
    private final List<Player> jogadoresVisiveis = Collections.unmodifiableList(jogadores);
    private Hud hud;
    private boolean teclaProximaFasePressionada = false;
    private boolean jogoTerminou = false;

    public MyWorld() {
        super(1280, 960, 1, false);
        carregarFase(1);
    }

    public void carregarFase(int fase) {

        this.faseAtual = fase;

        CarregadorTiled carregador = new CarregadorTiled(faseAtual);
        mapa = new DungeonMap(carregador.getDados());

        /*
         * IMPORTANTE:
         * Mantemos exatamente a forma original de carregar
         * a imagem do mapa.
         */
        setBackground(mapa.getImagem());

        removeObjects(getObjects(null));

        configurarSpawnAtor();
        hud = new Hud(this, jogadores, new java.util.function.IntSupplier() {
            @Override
            public int getAsInt() {
                return faseAtual;
            }
        }, MAX_FASES);
        configurarInimigos();
        configurarPedras();

        hud.atualizar();
    }

    private void configurarSpawnAtor() {

        List<Point> spawns = mapa.getSpawnsJogadores();
        Point p1 = spawns.size() > 0 ? spawns.get(0) : new Point(220, 280);
        Point p2 = spawns.size() > 1 ? spawns.get(1) : new Point(p1.x + 32, p1.y);

        jogadores.clear();

        jogadores.add(new Player(
                2,
                new Controlos("a", "d", "w", "s", "e", "q")));
        jogadores.add(new Player(
                3,
                new Controlos("left", "right", "up", "down", "enter", "shift")));

        addObject(jogadores.get(0), p1.x, p1.y);
        addObject(jogadores.get(1), p2.x, p2.y);

    }

    /** Cria os inimigos da fase. */
    private void configurarInimigos() {

        ArrayList<ConfiguracaoInimigo> inimigos = mapa.getConfiguracoesInimigos();

        for (int i = 0; i < inimigos.size(); i++) {
            ConfiguracaoInimigo configuracao = inimigos.get(i);
            Rectangle spawn = configuracao.getSpawn();
            int spawnX = spawn.x + spawn.width / 2;
            int spawnY = spawn.y + spawn.height / 2;
            int personagem = (i % 2) + 1;

            Movimento movimento = configuracao.temPatrulha()
                    ? new PatrulhaZona(configuracao.getPatrulha(), 2)
                    : new Parado();
            Enemy inimigo = new Enemy(personagem, movimento);

            addObject(inimigo, spawnX, spawnY);
            inimigo.orientarPara(jogadorMaisProximo(inimigo));
        }

    }

    private void configurarPedras() {
        for (Point posicao : mapa.getPosicoesPedras()) {
            Pedra pedra = new Pedra();
            addObject(pedra, posicao.x, posicao.y);
            mapa.adicionarInteragivel(pedra);
        }
    }

    private Player jogadorMaisProximo(Enemy inimigo) {
        Player maisProximo = null;
        double menorDistancia = Double.MAX_VALUE;

        for (Player jogador : jogadores) {
            double dx = jogador.getX() - inimigo.getX();
            double dy = jogador.getY() - inimigo.getY();
            double distancia = Math.sqrt(dx * dx + dy * dy);

            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                maisProximo = jogador;
            }
        }

        return maisProximo;
    }

    public void proximaFase() {

        if (faseAtual < MAX_FASES) {

            carregarFase(faseAtual + 1);

        } else {

            showText(
                    "PARABÉNS! VOCÊ VENCEU O JOGO!",
                    getWidth() / 2,
                    getHeight() / 2);
        }
    }

    @Override
    public void act() {

        if (jogoTerminou) {
            return;
        }

        /*
         * A tecla N fica disponível como teste para mudar de
         * Dungeon1 -> Dungeon2 -> Dungeon3.
         */
        boolean teclaN = Greenfoot.isKeyDown("n");

        if (teclaN &&
                !teclaProximaFasePressionada) {

            proximaFase();
        }

        teclaProximaFasePressionada = teclaN;

        hud.atualizar();
        verificarDerrota();
    }

    /** Verifica a condição cooperativa de derrota apenas uma vez. */
    private void verificarDerrota() {
        for (Player jogador : jogadores) {
            if (!jogador.estaVivo()) {
                jogador.iniciarMorte();
                return;
            }
        }
    }

    /** Muda para o ecrã de derrota e bloqueia a lógica desta partida. */
    public void terminarJogo() {
        if (jogoTerminou) {
            return;
        }
        jogoTerminou = true;
        Greenfoot.setWorld(new GameOverWorld());
    }

    /** Permite aos atores parar imediatamente quando a partida terminou. */
    public boolean estaEmGameOver() {
        return jogoTerminou;
    }

    public DungeonMap getMapa() {
        return mapa;
    }

    /** Lista de jogadores disponivel para sistemas do mundo, como a IA. */
    public List<Player> getJogadores() {
        return jogadoresVisiveis;
    }
}
