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
        // Posiciona os atores exatamente no centro da sala inicial visível
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

        addObject(new Zag(), spawnX, spawnY);
        addObject(new Zig(), spawnX + 32, spawnY);
    }

    private void atualizarInterface() {
        showText("FASE " + faseAtual + " / " + MAX_FASES, 80, 25);
        showText("WASD: Zag | Setas: Zig | N: Próxima Fase", 450, 25);
    }

    public void proximaFase() {
        if (faseAtual < MAX_FASES) {
            carregarFase(faseAtual + 1);
        } else {
            showText("PARABÉNS! VOCÊ VENCEU O JOGO!", getWidth() / 2, getHeight() / 2);
        }
    }

    @Override
    public void act() {
        boolean teclaN = Greenfoot.isKeyDown("n");
        if (teclaN && !teclaProximaFasePressionada) {
            proximaFase();
        }
        teclaProximaFasePressionada = teclaN;
    }

    // Testa um único ponto central nos pés do personagem para não prender em portas
    public boolean podeMover(Actor jogador, int novoX, int novoY) {
    int raioPes = 6;
    // Testa os pés em X e Y
    return !mapa.estaBloqueado(novoX - raioPes, novoY + 10) &&
           !mapa.estaBloqueado(novoX + raioPes, novoY + 10);
}

    public int getFaseAtual() {
        return faseAtual;
    }
}