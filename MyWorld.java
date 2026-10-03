import greenfoot.*;

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

    private Player jogador1;
    private Player jogador2;

    private int ultimoYJogador1;
    private int ultimoYJogador2;

    private boolean transicaoEscadaJogador1 = false;
    private boolean transicaoEscadaJogador2 = false;
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

        atualizarPosicoesAnteriores();
        atualizarInterface(null, null);
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
        jogador1 = new Player(1);
        jogador2 = new Player(2);

        addObject(
                jogador1,
                spawnX,
                spawnY);

        addObject(
                jogador2,
                spawnX + 32,
                spawnY);
    }

    /**
     * Cria os inimigos da fase. O parâmetro patrulha define se cada inimigo
     * anda dentro de uma zona ou fica parado.
     */
    private void configurarInimigos() {

        switch (faseAtual) {

            case 1:
                adicionarInimigo(520, 280, 1, true, true, 460, 640);
                adicionarInimigo(745, 520, 2, true, false, 450, 590);
                adicionarInimigoParado(600, 180, 1);
                adicionarInimigoParado(930, 365, 2);
                break;

            case 2:
                adicionarInimigo(420, 250, 1, true, true, 360, 560);
                adicionarInimigo(820, 690, 2, true, false, 610, 790);
                adicionarInimigoParado(650, 215, 2);
                adicionarInimigoParado(1060, 500, 1);
                break;

            case 3:
                adicionarInimigo(340, 180, 1, true, true, 260, 470);
                adicionarInimigo(920, 330, 2, true, false, 250, 470);
                adicionarInimigoParado(560, 560, 1);
                adicionarInimigoParado(1040, 720, 2);
                break;
        }
    }

    private void adicionarInimigo(int x, int y, int personagem,
            boolean patrulha, boolean horizontal,
            int limiteInicial, int limiteFinal) {
        addObject(new Enemy(personagem, patrulha, horizontal,
                limiteInicial, limiteFinal, 2), x, y);
    }

    private void adicionarInimigoParado(int x, int y, int personagem) {
        addObject(new Enemy(personagem), x, y);
    }

    private void atualizarPosicoesAnteriores() {

        if (jogador1 != null) {
            ultimoYJogador1 = jogador1.getY();
        }

        if (jogador2 != null) {
            ultimoYJogador2 = jogador2.getY();
        }
    }

    private void atualizarInterface(String interacao1, String interacao2) {

        showText(
                "FASE " + faseAtual + " / " + MAX_FASES,
                80,
                25);

        String controlos = "WASD: Jogador 1 | Setas: Jogador 2 | N: Próxima Fase";
        if (interacao1 != null) {
            controlos += " | BAÚ P1: " + interacao1;
        }
        if (interacao2 != null) {
            controlos += " | BAÚ P2: " + interacao2;
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
        atualizarPosicoesAnteriores();

        String interacao1 = getInteracaoEm(jogador1);
        String interacao2 = getInteracaoEm(jogador2);
        atualizarInterface(interacao1, interacao2);
    }

    /**
     * Verifica se um jogador está a atravessar uma escada.
     *
     * A posição da escada vem diretamente do Tiled. A alteração
     * de nível acontece apenas uma vez por passagem pela escada.
     */
    private void verificarEscadas() {

        processarEscadaJogador(
                jogador1,
                true);

        processarEscadaJogador(
                jogador2,
                false);
    }

    private void processarEscadaJogador(
            Player jogador,
            boolean primeiroJogador) {

        if (jogador == null) {
            return;
        }

        int yAtual = jogador.getY();

        int ultimoY = primeiroJogador
                ? ultimoYJogador1
                : ultimoYJogador2;

        boolean estaNaEscada = mapa.estaNaEscada(
                jogador.getX(),
                yAtual);

        boolean transicaoAtiva = primeiroJogador
                ? transicaoEscadaJogador1
                : transicaoEscadaJogador2;

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

            if (primeiroJogador) {
                transicaoEscadaJogador1 = true;
            } else {
                transicaoEscadaJogador2 = true;
            }
        }

        /*
         * Quando o jogador sai da escada, permite uma nova transição.
         */
        if (!estaNaEscada) {

            if (primeiroJogador) {
                transicaoEscadaJogador1 = false;
            } else {
                transicaoEscadaJogador2 = false;
            }
        }
    }

    /**
     * Testa apenas a caixa dos pés do personagem.
     *
     * Mantemos a colisão no DungeonMap, que é a única classe
     * responsável por saber onde o mapa é sólido.
     */
    public boolean podeMover(
            Actor jogador,
            int novoX,
            int novoY) {

        if (mapa == null) {
            return false;
        }

        /*
         * Caixa pequena na zona dos pés.
         * Evita que o corpo 96x96 do sprite colida com paredes.
         */
        final int metadeLargura = 7;
        final int topoPés = 18;
        final int fundoPés = 28;

        // A colisão depende do andar em que o jogador está.
        return !mapa.temColisaoNaZona(
                novoX - metadeLargura,
                novoY + topoPés,
                novoX + metadeLargura,
                novoY + fundoPés,
                getNivelDoJogador(jogador));
    }

    /**
     * Permite consultar o nível de qualquer Player.
     */
    public int getNivelDoJogador(Actor jogador) {

        if (jogador instanceof Player) {
            return ((Player) jogador).getNivel();
        }

        return 0;
    }

    public String getInteracaoEm(Player jogador) {

        if (mapa == null || jogador == null) {
            return null;
        }

        return mapa.getInteracaoEm(
                jogador.getX(),
                jogador.getY());
    }
}
