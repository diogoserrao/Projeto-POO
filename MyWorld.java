import greenfoot.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.awt.Rectangle;
import java.awt.Point;

public class MyWorld extends World {

    private DungeonMap mapa;

    /* As fases disponíveis são os mapas Dungeon1 e Dungeon2. */
    private int faseAtual = 1;
    private static final int MAX_FASES = 2;
    private int pontuacao = 0;

    private final List<Player> jogadores = new ArrayList<Player>();
    private final List<Player> jogadoresVisiveis = Collections.unmodifiableList(jogadores);
    private final List<Pedra> pedras = new ArrayList<Pedra>();
    private final List<Pedra> pedrasVisiveis = Collections.unmodifiableList(pedras);
    private Hud hud;
    private boolean transicaoEmCurso = false;
    private boolean jogoTerminou = false;
    private boolean jogoVenceu = false;

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
        setBackground(mapa.getImagemParaRenderizacao());

        removeObjects(getObjects(null));

        configurarArmadilhas();
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

    /** Adiciona cada armadilha uma única vez; a animação corre no próprio ator. */
    private void configurarArmadilhas() {
        for (Armadilha armadilha : mapa.getArmadilhas()) {
            if (armadilha.getWorld() == null) {
                addObject(armadilha, armadilha.getCentroVisualX(), armadilha.getCentroVisualY());
            }
        }
    }

    private void configurarSpawnAtor() {

        List<Point> spawns = mapa.getSpawnsJogadores();
        Point p1 = spawns.size() > 0 ? spawns.get(0) : new Point(220, 280);
        Point p2 = spawns.size() > 1 ? spawns.get(1) : new Point(p1.x + 32, p1.y);

        jogadores.clear();
        pedras.clear();

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

        List<ConfiguracaoInimigo> inimigos = mapa.getConfiguracoesInimigos();

        for (int i = 0; i < inimigos.size(); i++) {
            ConfiguracaoInimigo configuracao = inimigos.get(i);
            Rectangle spawn = configuracao.getSpawn();
            int spawnX = spawn.x + spawn.width / 2;
            int spawnY = spawn.y + spawn.height / 2;
            int personagem = (i % 2) + 1;

            // Cada terceiro spawn usa o Vigia; os restantes conservam a regra
            // original baseada na existência de uma zona de patrulha.
            Inimigo inimigo = i % 3 == 2
                    ? new Vigia(personagem)
                    : configuracao.temPatrulha()
                        ? new Guarda(personagem, configuracao.getPatrulha())
                        : new Sentinela(personagem);

            addObject(inimigo, spawnX, spawnY);
            inimigo.inicializarConeVisao();
            inimigo.orientarPara(jogadorMaisProximo(inimigo));
        }

    }

    private void configurarPedras() {
        for (Point posicao : mapa.getPosicoesPedras()) {
            Pedra pedra = new Pedra();
            addObject(pedra, posicao.x, posicao.y);
            adicionarPedra(pedra);
            mapa.adicionarInteragivel(pedra);
        }
    }

    private Player jogadorMaisProximo(Inimigo inimigo) {
        Player maisProximo = null;
        double menorDistanciaQuadrado = Double.MAX_VALUE;

        for (Player jogador : jogadores) {
            double dx = jogador.getX() - inimigo.getX();
            double dy = jogador.getY() - inimigo.getY();
            double distanciaQuadrado = dx * dx + dy * dy;

            if (distanciaQuadrado < menorDistanciaQuadrado) {
                menorDistanciaQuadrado = distanciaQuadrado;
                maisProximo = jogador;
            }
        }

        return maisProximo;
    }

    public void proximaFase() {

        if (jogoVenceu || transicaoEmCurso) {
            return;
        }

        transicaoEmCurso = true;
        if (faseAtual < MAX_FASES) {
            carregarFase(faseAtual + 1);
        } else {
            jogoVenceu = true;
            Greenfoot.playSound("win.wav");
            Greenfoot.setWorld(new VitoriaWorld(pontuacao));
        }
        transicaoEmCurso = false;
    }

    @Override
    public void act() {

        if (jogoTerminou || jogoVenceu) {
            return;
        }

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

    /** Devolve a pontuacao acumulada na partida atual. */
    public int getPontuacao() {
        return pontuacao;
    }

    /** Adiciona pontos validos sem expor o estado interno da pontuacao. */
    public void adicionarPontos(int pontos) {
        if (pontos > 0) {
            pontuacao += pontos;
        }
    }

    /** Lista de jogadores disponivel para sistemas do mundo, como a IA. */
    public List<Player> getJogadores() {
        return jogadoresVisiveis;
    }

    public List<Pedra> getPedras() {
        return pedrasVisiveis;
    }

    public void adicionarPedra(Pedra pedra) {
        if (pedra != null && !pedras.contains(pedra)) pedras.add(pedra);
    }

    public void removerPedra(Pedra pedra) {
        pedras.remove(pedra);
    }
}
