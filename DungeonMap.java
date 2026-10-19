import greenfoot.GreenfootImage;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Consultas de runtime sobre o mapa de uma única camada de colisão. */
public class DungeonMap {
    private static final int TILE = 32;
    private static final int MAP_WIDTH = 40;
    private static final int MAP_HEIGHT = 30;
    private static final int WORLD_WIDTH = MAP_WIDTH * TILE;
    private static final int WORLD_HEIGHT = MAP_HEIGHT * TILE;

    private final GreenfootImage imagem;
    private final GreenfootImage imagemBase;
    private final ArrayList<Interagivel> interagiveis;
    private final ArrayList<Armadilha> armadilhas;
    private final ArrayList<Porta> portas;
    private final Map<Interagivel, ArrayList<int[]>> tilesDosObjetos;
    private final ArrayList<ConfiguracaoInimigo> configuracoesInimigos;
    private final ArrayList<Point> posicoesPedras;
    private final ArrayList<Point> spawnsJogadores;
    private final boolean[][] colisaoComum;
    private final ArrayList<CarregadorTiled.Tileset> tilesets;
    private final java.util.Map<Integer, CarregadorTiled.TileAnimation> animacoesTiles;
    private final int fase;

    public DungeonMap() { this(1); }

    public DungeonMap(int fase) { this(new CarregadorTiled(fase).getDados()); }

    public DungeonMap(CarregadorTiled.DadosMapa dados) {
        imagem = dados.imagem;
        imagemBase = dados.imagemBase;
        interagiveis = dados.interagiveis;
        armadilhas = dados.armadilhas;
        portas = dados.portas;
        tilesDosObjetos = dados.tilesDosObjetos;
        configuracoesInimigos = dados.configuracoesInimigos;
        posicoesPedras = dados.posicoesPedras;
        spawnsJogadores = dados.spawnsJogadores;
        colisaoComum = dados.colisaoComum;
        tilesets = dados.tilesets;
        animacoesTiles = dados.animacoesTiles;
        fase = dados.fase;
    }

    public GreenfootImage getImagem() { return imagem; }

    /** Devolve uma vista não modificável dos objetos do mapa. */
    public List<Interagivel> getInteragiveis() {
        return Collections.unmodifiableList(interagiveis);
    }

    /** Regista um objeto interagível criado durante a execução do mundo. */
    public void adicionarInteragivel(Interagivel interagivel) {
        if (interagivel != null && !interagiveis.contains(interagivel)) {
            interagiveis.add(interagivel);
            registarInteragivel(interagivel);
        }
    }

    /** Regista um objeto e prepara os tiles que lhe pertencem. */
    public void registarInteragivel(Interagivel objeto) {
        if (objeto != null && !tilesDosObjetos.containsKey(objeto)) {
            tilesDosObjetos.put(objeto, new ArrayList<int[]>());
        }
    }

    /** Remove um objeto interagível sem expor a coleção interna. */
    public void removerInteragivel(Interagivel interagivel) {
        interagiveis.remove(interagivel);
        tilesDosObjetos.remove(interagivel);
        redesenharObjetos();
    }

    /** Reconstrói o mapa base e reaplica o estado visual de todos os objetos. */
    public void redesenharObjetos() {
        CarregadorTiled.redesenharObjetos(new CarregadorTiled.DadosMapa(
                imagem, imagemBase, interagiveis, armadilhas, portas, tilesDosObjetos,
                configuracoesInimigos, posicoesPedras, spawnsJogadores, colisaoComum,
                tilesets, animacoesTiles, fase));
    }

    public List<Armadilha> getArmadilhas() {
        return Collections.unmodifiableList(armadilhas);
    }

    public ArrayList<ConfiguracaoInimigo> getConfiguracoesInimigos() { return configuracoesInimigos; }
    public ArrayList<Point> getPosicoesPedras() { return posicoesPedras; }
    public ArrayList<Point> getSpawnsJogadores() { return spawnsJogadores; }

    public boolean temColisaoNaZona(int x1, int y1, int x2, int y2) {
        if (x1 < 0 || y1 < 0 || x2 >= WORLD_WIDTH || y2 >= WORLD_HEIGHT) return true;
        for (int cx = x1 / TILE; cx <= x2 / TILE; cx++) {
            for (int cy = y1 / TILE; cy <= y2 / TILE; cy++) {
                if (colisaoComum[cx][cy]) return true;
            }
        }
        return false;
    }

    public boolean estaBloqueado(Rectangle pes) {
        if (pes == null) return true;
        if (temColisaoNaZona(pes.x, pes.y, pes.x + pes.width - 1,
                pes.y + pes.height - 1)) return true;
        return encontrarPortaFechada(pes) != null;
    }

    public Porta encontrarPortaFechada(Rectangle area) {
        if (area == null) return null;
        for (Porta porta : portas)
            if (porta.estaFechada() && porta.getArea().intersects(area)) return porta;
        return null;
    }
}
