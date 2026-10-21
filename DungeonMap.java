import greenfoot.GreenfootImage;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    private final ArrayList<Rectangle> zonasVazio;
    private final ArrayList<Plataforma> plataformas;
    private final boolean[][] colisaoComum;
    private final ArrayList<CarregadorTiled.Tileset> tilesets;
    private final java.util.Map<Integer, CarregadorTiled.TileAnimation> animacoesTiles;
    private final int fase;
    private long revisaoInteracoes;

    public DungeonMap() { this(1); }

    public DungeonMap(int fase) { this(new CarregadorTiled(fase).getDados()); }

    public DungeonMap(CarregadorTiled.DadosMapa dados) {
        imagem = dados.getImagem();
        imagemBase = dados.getImagemBase();
        interagiveis = new ArrayList<Interagivel>(dados.getInteragiveis());
        armadilhas = new ArrayList<Armadilha>(dados.getArmadilhas());
        portas = new ArrayList<Porta>(dados.getPortas());
        tilesDosObjetos = copiarTilesDosObjetos(dados.getTilesDosObjetos());
        configuracoesInimigos = new ArrayList<ConfiguracaoInimigo>(
                dados.getConfiguracoesInimigos());
        posicoesPedras = new ArrayList<Point>(dados.getPosicoesPedras());
        spawnsJogadores = new ArrayList<Point>(dados.getSpawnsJogadores());
        zonasVazio = copiarRetangulos(dados.getZonasVazio());
        plataformas = new ArrayList<Plataforma>(dados.getPlataformas());
        colisaoComum = dados.getColisaoComum();
        tilesets = new ArrayList<CarregadorTiled.Tileset>(dados.getTilesets());
        animacoesTiles = new HashMap<Integer, CarregadorTiled.TileAnimation>(
                dados.getAnimacoesTiles());
        fase = dados.getFase();
    }

    private static Map<Interagivel, ArrayList<int[]>> copiarTilesDosObjetos(
            Map<Interagivel, List<int[]>> origem) {
        Map<Interagivel, ArrayList<int[]>> copia =
                new HashMap<Interagivel, ArrayList<int[]>>();
        for (Map.Entry<Interagivel, List<int[]>> entrada : origem.entrySet()) {
            ArrayList<int[]> tiles = new ArrayList<int[]>();
            for (int[] tile : entrada.getValue()) {
                tiles.add(tile.clone());
            }
            copia.put(entrada.getKey(), tiles);
        }
        return copia;
    }

    /**
     * Devolve uma cópia da imagem para que um consumidor não possa alterar o
     * fundo interno do mapa através do getter.
     */
    public GreenfootImage getImagem() { return new GreenfootImage(imagem); }

    /**
     * A imagem usada pelo World tem de ser a mesma que é redesenhada quando
     * o estado dos objetos muda. Este acesso é deliberadamente package-private
     * e não faz parte da API de consulta do mapa.
     */
    GreenfootImage getImagemParaRenderizacao() { return imagem; }

    /** Devolve uma vista não modificável dos objetos do mapa. */
    public List<Interagivel> getInteragiveis() {
        return Collections.unmodifiableList(interagiveis);
    }

    /** Regista um objeto interagível criado durante a execução do mundo. */
    public void adicionarInteragivel(Interagivel interagivel) {
        if (interagivel != null && !interagiveis.contains(interagivel)) {
            interagiveis.add(interagivel);
            registarInteragivel(interagivel);
            revisaoInteracoes++;
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
        revisaoInteracoes++;
        redesenharObjetos();
    }

    public long getRevisaoInteracoes() { return revisaoInteracoes; }

    /** Reconstrói o mapa base e reaplica o estado visual de todos os objetos. */
    public void redesenharObjetos() {
        CarregadorTiled.redesenharObjetos(new CarregadorTiled.DadosMapa(
                imagem, imagemBase, interagiveis, armadilhas, portas, tilesDosObjetos,
                configuracoesInimigos, posicoesPedras, spawnsJogadores, zonasVazio, plataformas, colisaoComum,
                tilesets, animacoesTiles, fase));
    }

    public List<Armadilha> getArmadilhas() {
        return Collections.unmodifiableList(armadilhas);
    }

    /** Devolve uma vista não modificável das configurações de inimigos. */
    public List<ConfiguracaoInimigo> getConfiguracoesInimigos() {
        return Collections.unmodifiableList(configuracoesInimigos);
    }

    /** Devolve uma vista não modificável das posições das pedras. */
    public List<Point> getPosicoesPedras() {
        return copiarPontos(posicoesPedras);
    }

    /** Devolve uma vista não modificável dos pontos de spawn dos jogadores. */
    public List<Point> getSpawnsJogadores() {
        return copiarPontos(spawnsJogadores);
    }

    private static List<Point> copiarPontos(List<Point> pontos) {
        ArrayList<Point> copia = new ArrayList<Point>();
        for (Point ponto : pontos) {
            copia.add(new Point(ponto));
        }
        return Collections.unmodifiableList(copia);
    }

    private static ArrayList<Rectangle> copiarRetangulos(List<Rectangle> origem) {
        ArrayList<Rectangle> copia = new ArrayList<Rectangle>();
        for (Rectangle area : origem) copia.add(new Rectangle(area));
        return copia;
    }

    public List<Rectangle> getZonasVazio() {
        return Collections.unmodifiableList(copiarRetangulos(zonasVazio));
    }

    public List<Plataforma> getPlataformas() {
        return Collections.unmodifiableList(plataformas);
    }

    public boolean estaNoVazio(Rectangle pes) {
        if (pes == null) return false;
        for (Rectangle vazio : zonasVazio) if (vazio.intersects(pes)) return true;
        return false;
    }

    public boolean estaSuportado(Rectangle pes) {
        if (pes == null) return false;
        for (Plataforma plataforma : plataformas) if (plataforma.suporta(pes)) return true;
        return false;
    }

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
