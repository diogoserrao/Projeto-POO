import greenfoot.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.Rectangle;

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
        configurarInimigos();

        atualizarInterface();
    }

    private void configurarSpawnAtor() {

        int spawnX = 220;
        int spawnY = 280;

        switch (faseAtual) {

            case 1:
                spawnX = 220;
                spawnY = 280;
                break;

            case 2:
                spawnX = 200;
                spawnY = 320;
                break;

            case 3:
                spawnX = 200;
                spawnY = 200;
                break;
        }

        /*
         * Um único tipo de classe para os dois jogadores.
         */
        jogadores.clear();

        jogadores.add(new Player(
                2,
                new Controlos("a", "d", "w", "s", "e")));
        jogadores.add(new Player(
                3,
                new Controlos("left", "right", "up", "down", "enter")));

        addObject(
                jogadores.get(0),
                spawnX,
                spawnY);

        addObject(
                jogadores.get(1),
                spawnX + 32,
                spawnY);
        for (Player jogador : jogadores) {
            jogador.setUltimoY(jogador.getY());
        }
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

            Enemy inimigo = configuracao.temPatrulha()
                    ? new Enemy(personagem, 2, configuracao.getPatrulha())
                    : new Enemy(personagem);

            addObject(inimigo, spawnX, spawnY);
        }

    }

    private void atualizarInterface() {

        showText(
                "FASE " + faseAtual + " / " + MAX_FASES,
                80,
                25);

        String controlos = "WASD: Jogador 1 | Setas: Jogador 2 | N: Próxima Fase";
        for (int i = 0; i < jogadores.size(); i++) {
            Interagivel interagivel = jogadores.get(i).getInteragivelPerto();
            if (interagivel != null) {
                controlos += " | P" + (i + 1) + ": "
                        + interagivel.getDescricao(jogadores.get(i));
            }
        }

        showText(
                controlos,
                850,
                25);
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
        atualizarInterface();
    }

    /**
     * Verifica se um jogador está a atravessar uma escada.
     *
     * A posição da escada vem diretamente do Tiled. A alteração
     * de nível acontece apenas uma vez por passagem pela escada.
     */
    private void verificarEscadas() {

        for (Player jogador : jogadores) {
            processarEscada(jogador);
        }
    }

    private void processarEscada(Player jogador) {

        if (jogador == null) {
            return;
        }

        int yAtual = jogador.getY();

        int ultimoY = jogador.getUltimoY();

        boolean estaNaEscada = mapa.estaNaEscada(
                jogador.getX(),
                yAtual);

        boolean transicaoAtiva = jogador.isEmTransicaoDeEscada();

        /*
         * Só muda de andar quando existe movimento vertical dentro
         * da escada. Entrar ou permanecer parado na escada não muda
         * o nível.
         */
        if (estaNaEscada &&
                yAtual != ultimoY &&
                !transicaoAtiva) {

            if (yAtual > ultimoY) {
                jogador.descerNivel();
            } else {
                jogador.subirNivel();
            }

            jogador.setEmTransicaoDeEscada(true);
        }

        /*
         * Quando o jogador sai da escada, permite uma nova transição.
         */
        if (!estaNaEscada) {

            jogador.setEmTransicaoDeEscada(false);
        }

        jogador.setUltimoY(yAtual);
    }

    public DungeonMap getMapa() {
        return mapa;
    }
}
