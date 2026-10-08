import greenfoot.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.awt.Rectangle;
import java.awt.Point;

public class MyWorld extends World {

    private DungeonMap mapa;

    /*
     * "Fase" representa os mapas Dungeon1, Dungeon2 e Dungeon3.
     *
     * "Nivel" representa o andar dentro da própria masmorra.
     * Cada jogador tem o seu próprio nível.
     */
    private int faseAtual = 1;
    private static final int MAX_FASES = 3;

    private final List<Player> jogadores = new ArrayList<Player>();
    private Hud hud;
    /*
     * Impede que o mesmo jogador mude de andar várias vezes
     * enquanto permanece dentro da mesma escada.
     */
    private boolean teclaProximaFasePressionada = false;

    public MyWorld() {
        super(1280, 960, 1, false);
        carregarFase(1);
    }

    public void carregarFase(int fase) {

        this.faseAtual = fase;

        /*
         * Quando começamos uma nova fase/mapa,
         * os jogadores começam novamente no nível 0.
         */
        mapa = new DungeonMap(faseAtual);

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

        hud.atualizar();
    }

    private void configurarSpawnAtor() {

        List<Point> spawns = mapa.getSpawnsJogadores();
        Point p1 = spawns.size() > 0 ? spawns.get(0) : new Point(220, 280);
        Point p2 = spawns.size() > 1 ? spawns.get(1) : new Point(p1.x + 32, p1.y);

        jogadores.clear();

        jogadores.add(new Player(
                2,
                new Controlos("a", "d", "w", "s", "e")));
        jogadores.add(new Player(
                3,
                new Controlos("left", "right", "up", "down", "enter")));

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
        }

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

        verificarEscadas();
        hud.atualizar();
    }

    /**
     * Verifica se um jogador está a atravessar uma escada.
     *
     * A posição da escada vem diretamente do Tiled. A alteração
     * de nível acontece apenas uma vez por passagem pela escada.
     */
    private void verificarEscadas() {

        for (Player jogador : jogadores) {
            jogador.atualizarEscada(mapa);
        }
    }

    public DungeonMap getMapa() {
        return mapa;
    }

    /** Lista de jogadores disponivel para sistemas do mundo, como a IA. */
    public List<Player> getJogadores() {
        return Collections.unmodifiableList(jogadores);
    }
}
