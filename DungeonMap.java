import greenfoot.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/** Carrega o mapa exportado pelo Tiled e fornece colisões ao mundo. */
public class DungeonMap {
    private static final int TILE = 16;
    private static final int MIN_X = -20;
    private static final int MIN_Y = -6;
    private static final int MAP_WIDTH = 40;
    private static final int MAP_HEIGHT = 30;

    private final GreenfootImage imagem;
    private final Set<String> bloqueados = new HashSet<String>();

    private static class Tileset {
        int firstGid;
        int columns;
        String imagePath;
    }

    public DungeonMap() {
        imagem = new GreenfootImage(MAP_WIDTH * TILE, MAP_HEIGHT * TILE);
        imagem.setColor(new greenfoot.Color(13, 17, 24));
        imagem.fill();
        carregar();
    }

    public GreenfootImage getImagem() {
        return imagem;
    }

    public boolean estaBloqueado(int x, int y) {
        int tx = (int)Math.floor((double)x / TILE) + MIN_X;
        int ty = (int)Math.floor((double)y / TILE) + MIN_Y;
        return tx < MIN_X || tx >= MIN_X + MAP_WIDTH ||
               ty < MIN_Y || ty >= MIN_Y + MAP_HEIGHT ||
               bloqueados.contains(chave(tx, ty));
    }

    private void carregar() {
        try {
            File ficheiro = new File("images/mundo/Tiled_files/Dungeon1.tmx");
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
                boolean colide = nome.equals("walls") || nome.equals("walls_under_water") ||
                        nome.equals("traps") || nome.equals("objects") || nome.equals("objects2");
                desenharCamadas(layer, tilesets, colide);
            }
        } catch (Exception erro) {
            System.out.println("Não foi possível carregar Dungeon1.tmx: " + erro);
        }
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
            t.imagePath = "mundo/Tiled_files/" + new File(img.getAttribute("source")).getName();
            resultado.add(t);
        }
        return resultado;
    }

    private void desenharCamadas(Element layer, ArrayList<Tileset> tilesets, boolean colide) {
        NodeList chunks = layer.getElementsByTagName("chunk");
        for (int c = 0; c < chunks.getLength(); c++) {
            Element chunk = (Element)chunks.item(c);
            int origemX = Integer.parseInt(chunk.getAttribute("x"));
            int origemY = Integer.parseInt(chunk.getAttribute("y"));
            int largura = Integer.parseInt(chunk.getAttribute("width"));
            String texto = chunk.getTextContent().replace('\n', ' ').replace('\r', ' ');
            String[] valores = texto.split(",");
            for (int i = 0; i < valores.length; i++) {
                String valor = valores[i].trim();
                if (valor.length() == 0) continue;
                long gid = Long.parseLong(valor) & 0x1fffffffL;
                if (gid == 0) continue;
                int tx = origemX + (i % largura);
                int ty = origemY + (i / largura);
                if (colide) bloqueados.add(chave(tx, ty));
                desenharTile((int)gid, tx, ty, tilesets);
            }
        }
    }

    private void desenharTile(int gid, int tx, int ty, ArrayList<Tileset> tilesets) {
        Tileset escolhido = null;
        for (Tileset t : tilesets) {
            if (t.firstGid <= gid && (escolhido == null || t.firstGid > escolhido.firstGid)) escolhido = t;
        }
        if (escolhido == null) return;
        try {
            GreenfootImage folha = new GreenfootImage(escolhido.imagePath);
            int local = gid - escolhido.firstGid;
            int sx = (local % escolhido.columns) * TILE;
            int sy = (local / escolhido.columns) * TILE;
            GreenfootImage tile = new GreenfootImage(TILE, TILE);
            tile.drawImage(folha, -sx, -sy);
            int px = (tx - MIN_X) * TILE;
            int py = (ty - MIN_Y) * TILE;
            if (px >= 0 && py >= 0 && px < imagem.getWidth() && py < imagem.getHeight()) {
                imagem.drawImage(tile, px, py);
            }
        } catch (Exception erro) {
            // Um tile em falta não impede o resto do mapa de ser jogável.
        }
    }

    private String chave(int x, int y) {
        return x + ":" + y;
    }
}
