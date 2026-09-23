import greenfoot.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/** Carrega o mapa exportado pelo Tiled e fornece colisões ao mundo. */
public class DungeonMap {
    private static final int SOURCE_TILE = 16;
    private static final int TILE = 32;
    
    private static final int MIN_X = -20;
    private static final int MIN_Y = -6;
    private static final int MAP_WIDTH = 40;  // 1280 px
    private static final int MAP_HEIGHT = 30; // 960 px

    private final GreenfootImage imagem;
    private final Set<String> bloqueados = new HashSet<String>();

    private static class Tileset {
        int firstGid;
        int columns;
        String imagePath;
    }

    private final int fase;

    public DungeonMap() {
        this(1);
    }

    public DungeonMap(int fase) {
        this.fase = Math.max(1, Math.min(3, fase));
        imagem = new GreenfootImage(MAP_WIDTH * TILE, MAP_HEIGHT * TILE);
        imagem.setColor(new greenfoot.Color(13, 17, 24));
        imagem.fill();
        carregar();
        aplicarAmbiente();
    }

    public GreenfootImage getImagem() {
        return imagem;
    }

    public int getFase() {
        return fase;
    }

    public boolean estaBloqueado(int x, int y) {
    if (x < 0 || x >= MAP_WIDTH * TILE || y < 0 || y >= MAP_HEIGHT * TILE) {
        return true;
    }

    // Posição direta do pixel do boneco na grelha do ecrã
    int screenTileX = x / TILE;
    int screenTileY = y / TILE;

    return bloqueados.contains(chave(screenTileX, screenTileY));
    }

    private void carregar() {
        try {
            File ficheiro = new File("images/mundo/Tiled_files/Dungeon" + fase + ".tmx");
            if (!ficheiro.exists()) {
                ficheiro = new File("images/mundo/Tiled_files/Dungeon1.tmx");
            }
            
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(ficheiro);
            Element mapa = doc.getDocumentElement();
            ArrayList<Tileset> tilesets = lerTilesets(mapa);

            NodeList layers = mapa.getChildNodes();
            for (int i = 0; i < layers.getLength(); i++) {
                Node node = layers.item(i);
                if (!(node instanceof Element) || !"layer".equals(node.getNodeName())) {
                    continue;
                }
                Element layer = (Element)node;
                String nome = layer.getAttribute("name").toLowerCase();
                
                // IMPORTANTE: Bloqueia apenas camadas que explicitamente tenham "wall" ou "parede"
                // Desconsidera termos genéricos que possam colidir com camadas de chão
                boolean colide = nome.contains("wall") || nome.contains("parede");
                desenharCamadas(layer, tilesets, colide);
            }
        } catch (Exception erro) {
            System.out.println("Erro ao carregar o ficheiro .tmx: " + erro);
        }
    }

    private void aplicarAmbiente() {
        greenfoot.Color cor;
        if (fase == 2) cor = new greenfoot.Color(22, 42, 58, 55);
        else if (fase == 3) cor = new greenfoot.Color(62, 30, 45, 65);
        else cor = new greenfoot.Color(8, 8, 18, 25);
        imagem.setColor(cor);
        imagem.fillRect(0, 0, imagem.getWidth(), imagem.getHeight());
    }

    private ArrayList<Tileset> lerTilesets(Element mapa) {
        ArrayList<Tileset> resultado = new ArrayList<Tileset>();
        NodeList nodes = mapa.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (!(node instanceof Element) || !"tileset".equals(node.getNodeName())) continue;
            Element ts = (Element)node;
            Element img = (Element)ts.getElementsByTagName("image").item(0);
            if (img == null) continue;
            Tileset t = new Tileset();
            t.firstGid = Integer.parseInt(ts.getAttribute("firstgid"));
            t.columns = Integer.parseInt(ts.getAttribute("columns"));
            
            String src = img.getAttribute("source");
            t.imagePath = "images/mundo/Tiled_files/" + new File(src).getName();
            resultado.add(t);
        }
        return resultado;
    }

    private void desenharCamadas(Element layer, ArrayList<Tileset> tilesets, boolean colide) {
        NodeList chunks = layer.getElementsByTagName("chunk");
        
        if (chunks.getLength() > 0) {
            for (int c = 0; c < chunks.getLength(); c++) {
                Element chunk = (Element)chunks.item(c);
                int origemX = Integer.parseInt(chunk.getAttribute("x"));
                int origemY = Integer.parseInt(chunk.getAttribute("y"));
                int largura = Integer.parseInt(chunk.getAttribute("width"));
                processarCSV(chunk.getTextContent(), origemX, origemY, largura, tilesets, colide);
            }
        } else {
            Element data = (Element) layer.getElementsByTagName("data").item(0);
            if (data != null) {
                processarCSV(data.getTextContent(), 0, 0, MAP_WIDTH, tilesets, colide);
            }
        }
    }

    private void processarCSV(String texto, int origemX, int origemY, int largura, ArrayList<Tileset> tilesets, boolean colide) {
    String[] valores = texto.replace('\n', ' ').replace('\r', ' ').split(",");
    for (int i = 0; i < valores.length; i++) {
        String valor = valores[i].trim();
        if (valor.length() == 0) continue;
        long gid = Long.parseLong(valor) & 0x1fffffffL;
        if (gid == 0) continue;
        
        int tx = origemX + (i % largura);
        int ty = origemY + (i / largura);
        
        // Passa o parâmetro 'colide' para o desenharTile
        desenharTile((int)gid, tx, ty, tilesets, colide);
    }
}

    private void desenharTile(int gid, int tx, int ty, ArrayList<Tileset> tilesets, boolean colide) {
    Tileset escolhido = null;
    for (Tileset t : tilesets) {
        if (t.firstGid <= gid && (escolhido == null || t.firstGid > escolhido.firstGid)) {
            escolhido = t;
        }
    }
    if (escolhido == null) return;

    try {
        GreenfootImage folha = new GreenfootImage(escolhido.imagePath);
        int local = gid - escolhido.firstGid;
        int sx = (local % escolhido.columns) * SOURCE_TILE;
        int sy = (local / escolhido.columns) * SOURCE_TILE;
        
        GreenfootImage tile = new GreenfootImage(SOURCE_TILE, SOURCE_TILE);
        tile.drawImage(folha, -sx, -sy);
        tile.scale(TILE, TILE);

        // Posição real em pixels no ecrã do Greenfoot
        int px = (tx - MIN_X) * TILE;
        int py = (ty - MIN_Y) * TILE;
        
        if (px >= 0 && py >= 0 && px < imagem.getWidth() && py < imagem.getHeight()) {
            imagem.drawImage(tile, px, py);
            
            // SE for parede E estiver dentro do ecrã, regista a colisão na grelha do ecrã (0 a 39, 0 a 29)
            if (colide) {
                int screenTileX = px / TILE;
                int screenTileY = py / TILE;
                bloqueados.add(chave(screenTileX, screenTileY));
            }
        }
    } catch (Exception erro) {
        // Ignora imagens não encontradas
    }
}

    private String chave(int x, int y) {
        return x + ":" + y;
    }
}