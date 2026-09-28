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

    /*
     * Evita que o mesmo jogador suba/desça várias vezes
     * enquanto permanece na escada.
     */
    private boolean transicaoEscadaJogador1 = false;
    private boolean transicaoEscadaJogador2 = false;

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
        transicaoEscadaJogador1 = false;
        transicaoEscadaJogador2 = false;

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

    private void atualizarInterface() {

        showText(
                "FASE " + faseAtual + " / " + MAX_FASES,
                80,
                25);

        showText(
                "Jogador 1: andar " +
                        jogador1.getNivel() +
                        " | Jogador 2: andar " +
                        jogador2.getNivel(),
                330,
                25);

        showText(
                "WASD: Jogador 1 | Setas: Jogador 2 | N: Próxima Fase",
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
     * Verifica se algum jogador chegou ao topo ou à base
     * de uma escada e atualiza o seu nível.
     *
     * Descer:
     * movimento para baixo -> nível - 1
     *
     * Subir:
     * movimento para cima -> nível + 1
     */
    private void verificarEscadas() {

        verificarEscadaJogador(
                jogador1,
                true);

        verificarEscadaJogador(
                jogador2,
                false);
    }

    private void verificarEscadaJogador(
            Player jogador,
            boolean primeiroJogador) {

        if (jogador == null) {
            return;
        }

        int yAtual = jogador.getY();

        int ultimoY;

        if (primeiroJogador) {
            ultimoY = ultimoYJogador1;
        } else {
            ultimoY = ultimoYJogador2;
        }

        /*
         * O jogador mudou de posição e está numa escada.
         */
        if (yAtual != ultimoY &&
                mapa.estaNaPassagem(
                        jogador.getX(),
                        yAtual)) {

            boolean transicaoAtiva;

            if (primeiroJogador) {
                transicaoAtiva = transicaoEscadaJogador1;
            } else {
                transicaoAtiva = transicaoEscadaJogador2;
            }

            if (!transicaoAtiva) {

                /*
                 * Y aumenta -> desce no ecrã -> nível -1
                 *
                 * Y diminui -> sobe no ecrã -> nível +1
                 */
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
        }

        /*
         * Só permite uma nova mudança de nível depois
         * de o jogador sair da passagem.
         */
        if (!mapa.estaNaPassagem(
                jogador.getX(),
                jogador.getY())) {

            if (primeiroJogador) {
                transicaoEscadaJogador1 = false;
            } else {
                transicaoEscadaJogador2 = false;
            }
        }

        /*
         * Guarda a posição anterior.
         */
        if (primeiroJogador) {
            ultimoYJogador1 = yAtual;
        } else {
            ultimoYJogador2 = yAtual;
        }
    }

    /**
     * A colisão usa apenas a zona inferior do personagem.
     *
     * A máscara do DungeonMap é pixel a pixel, por isso não
     * precisamos considerar um quadrado inteiro de 32x32.
     */
    public boolean podeMover(
            Actor jogador,
            int novoX,
            int novoY) {

        /*
         * A sala do jogador 2 só tem uma entrada válida:
         * a porta inferior.
         */
        if (movimentoBloqueadoNaSalaDoJogador2(
                jogador,
                novoX,
                novoY)) {

            return false;
        }

        int raio = 8;

        int[] offsetsX = {
                -raio,
                -4,
                0,
                4,
                raio
        };

        int[] offsetsY = {
                16,
                20,
                24,
                28
        };

        for (int dx : offsetsX) {

            for (int dy : offsetsY) {

                int x = novoX + dx;
                int y = novoY + dy;

                /*
                 * Água não é uma superfície caminhável.
                 */
                if (mapa.estaNaAgua(x, y)) {
                    return false;
                }

                if (mapa.estaBloqueado(x, y)) {

                    if (!mapa.estaNaEscada(x, y) &&
                            !mapa.estaNaPassagem(x, y)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean movimentoBloqueadoNaSalaDoJogador2(
            Actor jogador,
            int novoX,
            int novoY) {

        /*
         * Coordenadas em pixels do mundo.
         */
        int salaEsquerda = 32;
        int salaDireita = 128;
        int salaTopo = 288;
        int salaBase = 384;

        /*
         * Vão inferior alinhado com a porta.
         */
        int portaEsquerda = 96;
        int portaDireita = 128;

        int xAtual = jogador.getX();
        int yAtual = jogador.getY();

        boolean dentroAgora = xAtual >= salaEsquerda &&
                xAtual < salaDireita &&
                yAtual >= salaTopo &&
                yAtual < salaBase;

        boolean dentroDepois = novoX >= salaEsquerda &&
                novoX < salaDireita &&
                novoY >= salaTopo &&
                novoY < salaBase;

        /*
         * Se não entrou nem saiu da sala,
         * não há bloqueio especial.
         */
        if (dentroAgora == dentroDepois) {
            return false;
        }

        boolean passaPelaPorta = novoX >= portaEsquerda &&
                novoX < portaDireita &&
                ((yAtual >= salaBase && novoY < salaBase) ||
                        (yAtual < salaBase && novoY >= salaBase));

        return !passaPelaPorta;
    }

    public int getFaseAtual() {
        return faseAtual;
    }

    public int getNivelJogador1() {

        if (jogador1 == null) {
            return 0;
        }

        return jogador1.getNivel();
    }

    public int getNivelJogador2() {

        if (jogador2 == null) {
            return 0;
        }

        return jogador2.getNivel();
    }

    /**
     * Permite consultar o nível de qualquer Player.
     */
    public int getNivelDoJogador(Actor jogador) {

        if (jogador instanceof Player) {

            Player player = (Player) jogador;

            return player.getNivel();
        }

        return 0;
    }
}
