import greenfoot.GreenfootImage;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Consultas de runtime sobre os dados já preparados pelo CarregadorTiled. */
public class DungeonMap {
    private static final int TILE = 32;
    private static final int MAP_WIDTH = 40;
    private static final int MAP_HEIGHT = 30;
    private static final int WORLD_WIDTH = MAP_WIDTH * TILE;
    private static final int WORLD_HEIGHT = MAP_HEIGHT * TILE;

    private final GreenfootImage imagem;
    private final GreenfootImage imagemBase;
    private final ArrayList<Rectangle> escadas;
    private final ArrayList<Interagivel> interagiveis;
    private final Map<Chave, ArrayList<int[]>> tilesDasChaves;
    private final ArrayList<ConfiguracaoInimigo> configuracoesInimigos;
    private final ArrayList<Point> spawnsJogadores;
    private final boolean[][] colisaoComum;
    private final Map<Integer, boolean[][]> colisaoPorNivel;
    private final ArrayList<CarregadorTiled.Tileset> tilesets;
    private final int fase;

    public DungeonMap() { this(1); }

    public DungeonMap(int fase) { this(new CarregadorTiled(fase).getDados()); }

    public DungeonMap(CarregadorTiled.DadosMapa dados) {
        imagem = dados.imagem;
        imagemBase = dados.imagemBase;
        escadas = dados.escadas;
        interagiveis = dados.interagiveis;
        tilesDasChaves = dados.tilesDasChaves;
        configuracoesInimigos = dados.configuracoesInimigos;
        spawnsJogadores = dados.spawnsJogadores;
        colisaoComum = dados.colisaoComum;
        colisaoPorNivel = dados.colisaoPorNivel;
        tilesets = dados.tilesets;
        fase = dados.fase;
    }

    public GreenfootImage getImagem() { return imagem; }

    /** Devolve uma vista não modificável dos objetos do mapa. */
    public List<Interagivel> getInteragiveis() {
        return Collections.unmodifiableList(interagiveis);
    }

    /** Remove um objeto interagível sem expor a coleção interna. */
    public void removerInteragivel(Interagivel interagivel) {
        interagiveis.remove(interagivel);
        if (interagivel instanceof Chave) {
            CarregadorTiled.redesenharChaves(new CarregadorTiled.DadosMapa(
                    imagem, imagemBase, escadas, interagiveis, tilesDasChaves,
                    configuracoesInimigos, spawnsJogadores, colisaoComum,
                    colisaoPorNivel, tilesets, fase));
        }
    }

    public ArrayList<ConfiguracaoInimigo> getConfiguracoesInimigos() { return configuracoesInimigos; }
    public ArrayList<Point> getSpawnsJogadores() { return spawnsJogadores; }

    public boolean temColisaoNaZona(int x1, int y1, int x2, int y2, int nivel) {
        if (x1 < 0 || y1 < 0 || x2 >= WORLD_WIDTH || y2 >= WORLD_HEIGHT) return true;
        boolean[][] doNivel = colisaoPorNivel.get(nivel);
        for (int cx = x1 / TILE; cx <= x2 / TILE; cx++) {
            for (int cy = y1 / TILE; cy <= y2 / TILE; cy++) {
                if (colisaoComum[cx][cy] || (doNivel != null && doNivel[cx][cy])) return true;
            }
        }
        return false;
    }

    public boolean estaBloqueado(Rectangle pes, int nivel) {
        if (pes == null) return true;
        return temColisaoNaZona(pes.x, pes.y, pes.x + pes.width - 1,
                pes.y + pes.height - 1, nivel);
    }

    public boolean estaNaEscada(int x, int y) {
        Rectangle zonaPes = new Rectangle(x - 6, y + 18, 12, 12);
        for (Rectangle escada : escadas) if (escada.intersects(zonaPes)) return true;
        return false;
    }
}
