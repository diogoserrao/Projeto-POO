import greenfoot.*;

public class MyWorld extends World {
    private DungeonMap mapa;
    private int faseAtual = 1;
    private static final int MAX_FASES = 3;

    private boolean teclaProximaFasePressionada = false;

    public MyWorld() {
        super(1280, 960, 1, false);
        carregarFase(1);
    }

    public void carregarFase(int fase) {
        this.faseAtual = fase;

        mapa = new DungeonMap(faseAtual);
        setBackground(mapa.getImagem());

        removeObjects(getObjects(null));

        configurarSpawnAtor();
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

    private void atualizarInterface() {
        showText(
            "FASE " + faseAtual + " / " + MAX_FASES,
            80,
            25
        );

        showText(
            "WASD: Zag | Setas: Zig | N: Próxima Fase",
            450,
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
        boolean teclaN =
            Greenfoot.isKeyDown("n");

        if (teclaN &&
            !teclaProximaFasePressionada) {

            proximaFase();
        }

        teclaProximaFasePressionada = teclaN;
    }

    /**
     * A colisão agora usa apenas a zona inferior do personagem.
     *
     * A máscara do DungeonMap é pixel a pixel, por isso não
     * precisamos considerar um quadrado inteiro de 32x32.
     */
    public boolean podeMover(
            Actor jogador,
            int novoX,
            int novoY) {

        int raio = 5;

        /*
         * Dois níveis na zona dos pés.
         * Isto evita que a cabeça/corpo do sprite
         * impeça a passagem.
         */
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

                if (mapa.estaBloqueado(
                        novoX + dx,
                        novoY + dy)) {

                    return false;
                }
            }
        }

        return true;
    }

    public int getFaseAtual() {
        return faseAtual;
    }
}
