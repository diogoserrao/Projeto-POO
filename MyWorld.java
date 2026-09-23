import greenfoot.*;

public class MyWorld extends World {
    private DungeonMap mapa;

    /*
     * "Fase" continua a representar os mapas Dungeon1, Dungeon2
     * e Dungeon3.
     *
     * "Nivel" representa o andar dentro da própria masmorra.
     * Cada jogador tem o seu próprio nível.
     */
    private int faseAtual = 1;
    private static final int MAX_FASES = 3;

    private int nivelZag = 0;
    private int nivelZig = 0;

    private int ultimoYZag;
    private int ultimoYZig;

    /*
     * Evita que o mesmo jogador desça/suba várias vezes enquanto
     * permanece parado na extremidade da escada.
     */
    private boolean transicaoEscadaZag = false;
    private boolean transicaoEscadaZig = false;

    private boolean teclaProximaFasePressionada = false;

    public MyWorld() {
        super(1280, 960, 1, false);
        carregarFase(1);
    }

    public void carregarFase(int fase) {
        this.faseAtual = fase;

        /*
         * Quando começamos uma nova fase/mapa, os jogadores
         * começam novamente no nível 0.
         */
        nivelZag = 0;
        nivelZig = 0;

        transicaoEscadaZag = false;
        transicaoEscadaZig = false;

        mapa = new DungeonMap(faseAtual);
        setBackground(mapa.getImagem());

        removeObjects(getObjects(null));

        configurarSpawnAtor();
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

        addObject(
            new Zag(),
            spawnX,
            spawnY
        );

        addObject(
            new Zig(),
            spawnX + 32,
            spawnY
        );
    }

    private void atualizarPosicoesAnteriores() {
        Zag zag = null;

        java.util.List<Zag> zags =
            getObjects(Zag.class);

        if (!zags.isEmpty()) {
            zag = zags.get(0);
        }

        Zig zig = null;

        java.util.List<Zig> zigs =
            getObjects(Zig.class);

        if (!zigs.isEmpty()) {
            zig = zigs.get(0);
        }

        if (zag != null) {
            ultimoYZag = zag.getY();
        }

        if (zig != null) {
            ultimoYZig = zig.getY();
        }
    }

    private void atualizarInterface() {
        showText(
            "FASE " + faseAtual + " / " + MAX_FASES,
            80,
            25
        );

        showText(
            "Zag: andar " + nivelZag +
            " | Zig: andar " + nivelZig,
            330,
            25
        );

        showText(
            "WASD: Zag | Setas: Zig | N: Próxima Fase",
            850,
            25
        );
    }

    public void proximaFase() {
        if (faseAtual < MAX_FASES) {
            carregarFase(faseAtual + 1);
        } else {
            showText(
                "PARABÉNS! VOCÊ VENCEU O JOGO!",
                getWidth() / 2,
                getHeight() / 2
            );
        }
    }

    @Override
    public void act() {

        /*
         * A tecla N fica disponível como teste para mudar de
         * Dungeon1 -> Dungeon2 -> Dungeon3.
         */
        boolean teclaN =
            Greenfoot.isKeyDown("n");

        if (teclaN &&
            !teclaProximaFasePressionada) {

            proximaFase();
        }

        teclaProximaFasePressionada = teclaN;

        verificarEscadas();
        atualizarInterface();
    }

    /**
     * Verifica se algum jogador chegou ao topo ou à base de uma
     * escada e atualiza o andar correspondente.
     *
     * Descer pela escada:
     *      movimento para baixo + base da escada -> nível - 1
     *
     * Subir pela escada:
     *      movimento para cima + topo da escada -> nível + 1
     */
    private void verificarEscadas() {

        Zag zag = null;
        java.util.List<Zag> zags = getObjects(Zag.class);
        if (!zags.isEmpty()) {
            zag = zags.get(0);
        }

        if (zag != null) {
            int yAtual = zag.getY();

            /*
             * A mudança de andar acontece quando o jogador entra
             * numa escada e começa a percorrê-la.
             *
             * Y aumenta -> está a descer no ecrã -> andar -1
             * Y diminui -> está a subir no ecrã -> andar +1
             *
             * Não esperamos pela última tile da escada. Isto evita
             * falhas quando o sprite não consegue ficar exatamente
             * no centro da tile de topo/base.
             */
            if (yAtual != ultimoYZag &&
                mapa.estaNaEscada(zag.getX(), yAtual)) {

                if (!transicaoEscadaZag) {
                    if (yAtual > ultimoYZag) {
                        nivelZag--;
                    } else {
                        nivelZag++;
                    }

                    transicaoEscadaZag = true;
                }
            }

            /*
             * Só pode iniciar outra mudança depois de sair da escada.
             */
            if (!mapa.estaNaEscada(
                    zag.getX(),
                    zag.getY())) {
                transicaoEscadaZag = false;
            }

            ultimoYZag = yAtual;
        }

        Zig zig = null;
        java.util.List<Zig> zigs = getObjects(Zig.class);
        if (!zigs.isEmpty()) {
            zig = zigs.get(0);
        }

        if (zig != null) {
            int yAtual = zig.getY();

            if (yAtual != ultimoYZig &&
                mapa.estaNaEscada(zig.getX(), yAtual)) {

                if (!transicaoEscadaZig) {
                    if (yAtual > ultimoYZig) {
                        nivelZig--;
                    } else {
                        nivelZig++;
                    }

                    transicaoEscadaZig = true;
                }
            }

            if (!mapa.estaNaEscada(
                    zig.getX(),
                    zig.getY())) {
                transicaoEscadaZig = false;
            }

            ultimoYZig = yAtual;
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
         * A sala do Zig (boneco azul) só tem uma entrada válida:
         * a porta inferior. Isto também bloqueia a entrada pelas
         * escadas de pedra no topo da sala.
         */
        if (movimentoBloqueadoNaSalaDoZig(jogador, novoX, novoY)) {
            return false;
        }

        int raio = 5;

        int[] offsetsX = {
            -raio,
            0,
            raio
        };

        int[] offsetsY = {
            20,
            26
        };

        for (int dx : offsetsX) {
            for (int dy : offsetsY) {

                int x = novoX + dx;
                int y = novoY + dy;

                /* Água não é uma superfície caminhável. */
                if (mapa.estaNaAgua(x, y)) {
                    return false;
                }

                if (mapa.estaBloqueado(x, y)) {
                    // Uma escada pode atravessar a máscara de uma parede.
                    // Fora da escada, a colisão normal mantém-se.
                    if (!mapa.estaNaEscada(x, y)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean movimentoBloqueadoNaSalaDoZig(
            Actor jogador,
            int novoX,
            int novoY) {

        /* Coordenadas em pixels do mundo (tx=-19..-17, ty=3..5). */
        int salaEsquerda = 32;
        int salaDireita = 128;
        int salaTopo = 288;
        int salaBase = 384;

        /* Vão inferior alinhado com a porta (tx=-17). */
        int portaEsquerda = 96;
        int portaDireita = 128;

        int xAtual = jogador.getX();
        int yAtual = jogador.getY();

        boolean dentroAgora =
            xAtual >= salaEsquerda &&
            xAtual < salaDireita &&
            yAtual >= salaTopo &&
            yAtual < salaBase;

        boolean dentroDepois =
            novoX >= salaEsquerda &&
            novoX < salaDireita &&
            novoY >= salaTopo &&
            novoY < salaBase;

        if (dentroAgora == dentroDepois) {
            return false;
        }

        boolean passaPelaPorta =
            novoX >= portaEsquerda &&
            novoX < portaDireita &&
            ((yAtual >= salaBase && novoY < salaBase) ||
             (yAtual < salaBase && novoY >= salaBase));

        return !passaPelaPorta;
    }

    public int getFaseAtual() {
        return faseAtual;
    }

    public int getNivelZag() {
        return nivelZag;
    }

    public int getNivelZig() {
        return nivelZig;
    }

    /**
     * Permite que Zag/Zig consultem o seu nível se for necessário
     * noutra parte do jogo.
     */
    public int getNivelDoJogador(Actor jogador) {

        if (jogador instanceof Zag) {
            return nivelZag;
        }

        if (jogador instanceof Zig) {
            return nivelZig;
        }

        return 0;
    }
}
