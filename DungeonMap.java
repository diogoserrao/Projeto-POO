import greenfoot.*;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 * Carrega o mapa do Tiled e cria uma colisão baseada nos pixels
 * realmente visíveis das paredes.
 *
 * Isto é importante neste mapa porque a layer "Walls" contém tiles
 * que podem ter partes transparentes/passagens. Não podemos tratar
 * cada tile 32x32 inteiro como uma parede.
 */
public class DungeonMap {
    private static final int SOURCE_TILE = 16;
    private static final int TILE = 32;

    private static final int MIN_X = -20;
    private static final int MIN_Y = -6;

    private static final int MAP_WIDTH = 40;
    private static final int MAP_HEIGHT = 30;

    private static final int WORLD_WIDTH = MAP_WIDTH * TILE;
    private static final int WORLD_HEIGHT = MAP_HEIGHT * TILE;

    /*
     * Portas/portões do tileset de portas.
     * As tiles deste tileset usadas na layer Walls são passagem.
     */
    private static final int DOOR_FIRST_GID = 5429;
    private static final int DOOR_LAST_GID = 5578;

    private final GreenfootImage imagem;

    /*
     * Um bit por pixel do mundo.
     * true = existe parte sólida visível de uma parede nesse pixel.
     */
    private final BitSet colisaoPixels =
        new BitSet(WORLD_WIDTH * WORLD_HEIGHT);

    private final Map<String, GreenfootImage> folhasCache =
        new HashMap<String, GreenfootImage>();

    private final int fase;

    private static class Tileset {
        int firstGid;
        int columns;
        String imagePath;
    }

    public DungeonMap() {
        this(1);
    }

    public DungeonMap(int fase) {
        this.fase = Math.max(1, Math.min(3, fase));

        imagem = new GreenfootImage(
            WORLD_WIDTH,
            WORLD_HEIGHT
        );

        imagem.setColor(
            new greenfoot.Color(13, 17, 24)
        );
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

    /**
     * Testa um pixel individual.
     *
     * Ao contrário da versão anterior, não considera o quadrado
     * inteiro de 32x32 como parede.
     */
    public boolean estaBloqueado(int x, int y) {
        if (x < 0 ||
            x >= WORLD_WIDTH ||
            y < 0 ||
            y >= WORLD_HEIGHT) {

            return true;
        }

        int indice =
            y * WORLD_WIDTH + x;

        return colisaoPixels.get(indice);
    }

    private void carregar() {
        try {
            File ficheiro = new File(
                "images/mundo/Tiled_files/Dungeon" +
                fase +
                ".tmx"
            );

            if (!ficheiro.exists()) {
                ficheiro = new File(
                    "images/mundo/Tiled_files/Dungeon1.tmx"
                );
            }

            Document doc =
                DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(ficheiro);

            Element mapa =
                doc.getDocumentElement();

            ArrayList<Tileset> tilesets =
                lerTilesets(mapa);

            NodeList layers =
                mapa.getChildNodes();

            for (int i = 0;
                 i < layers.getLength();
                 i++) {

                Node node =
                    layers.item(i);

                if (!(node instanceof Element) ||
                    !"layer".equals(node.getNodeName())) {

                    continue;
                }

                Element layer =
                    (Element) node;

                String nome =
                    layer.getAttribute("name")
                         .toLowerCase();

                /*
                 * APENAS Walls gera colisão.
                 *
                 * Objects, Objects2 e Objects_under_wall
                 * são visuais. Se os tratarmos como paredes,
                 * aparecem obstáculos invisíveis.
                 */
                boolean colide =
                    nome.equals("walls");

                desenharCamadas(
                    layer,
                    tilesets,
                    colide
                );
            }

        } catch (Exception erro) {
            System.out.println(
                "Erro ao carregar o mapa: " +
                erro.getMessage()
            );
        }
    }

    private void aplicarAmbiente() {
        greenfoot.Color cor;

        if (fase == 2) {
            cor =
                new greenfoot.Color(
                    22, 42, 58, 55
                );

        } else if (fase == 3) {
            cor =
                new greenfoot.Color(
                    62, 30, 45, 65
                );

        } else {
            cor =
                new greenfoot.Color(
                    8, 8, 18, 25
                );
        }

        imagem.setColor(cor);

        imagem.fillRect(
            0,
            0,
            imagem.getWidth(),
            imagem.getHeight()
        );
    }

    private ArrayList<Tileset> lerTilesets(
            Element mapa) {

        ArrayList<Tileset> resultado =
            new ArrayList<Tileset>();

        NodeList nodes =
            mapa.getChildNodes();

        for (int i = 0;
             i < nodes.getLength();
             i++) {

            Node node =
                nodes.item(i);

            if (!(node instanceof Element) ||
                !"tileset".equals(node.getNodeName())) {

                continue;
            }

            Element ts =
                (Element) node;

            Element img =
                (Element)
                ts.getElementsByTagName(
                    "image"
                ).item(0);

            if (img == null) {
                continue;
            }

            Tileset t =
                new Tileset();

            t.firstGid =
                Integer.parseInt(
                    ts.getAttribute(
                        "firstgid"
                    )
                );

            t.columns =
                Integer.parseInt(
                    ts.getAttribute(
                        "columns"
                    )
                );

            String src =
                img.getAttribute("source");

            t.imagePath =
                "images/mundo/Tiled_files/" +
                new File(src).getName();

            resultado.add(t);
        }

        return resultado;
    }

    private void desenharCamadas(
            Element layer,
            ArrayList<Tileset> tilesets,
            boolean colide) {

        NodeList chunks =
            layer.getElementsByTagName(
                "chunk"
            );

        if (chunks.getLength() > 0) {

            for (int c = 0;
                 c < chunks.getLength();
                 c++) {

                Element chunk =
                    (Element) chunks.item(c);

                int origemX =
                    Integer.parseInt(
                        chunk.getAttribute("x")
                    );

                int origemY =
                    Integer.parseInt(
                        chunk.getAttribute("y")
                    );

                int largura =
                    Integer.parseInt(
                        chunk.getAttribute(
                            "width"
                        )
                    );

                int altura =
                    Integer.parseInt(
                        chunk.getAttribute(
                            "height"
                        )
                    );

                processarCSV(
                    chunk.getTextContent(),
                    origemX,
                    origemY,
                    largura,
                    altura,
                    tilesets,
                    colide,
                    layer.getAttribute(
                        "name"
                    )
                );
            }

        } else {

            Element data =
                (Element)
                layer.getElementsByTagName(
                    "data"
                ).item(0);

            if (data != null) {

                int largura =
                    MAP_WIDTH;

                int altura =
                    MAP_HEIGHT;

                if (layer.hasAttribute("width")) {
                    largura =
                        Integer.parseInt(
                            layer.getAttribute(
                                "width"
                            )
                        );
                }

                if (layer.hasAttribute("height")) {
                    altura =
                        Integer.parseInt(
                            layer.getAttribute(
                                "height"
                            )
                        );
                }

                processarCSV(
                    data.getTextContent(),
                    0,
                    0,
                    largura,
                    altura,
                    tilesets,
                    colide,
                    layer.getAttribute(
                        "name"
                    )
                );
            }
        }
    }

    private void processarCSV(
            String texto,
            int origemX,
            int origemY,
            int largura,
            int altura,
            ArrayList<Tileset> tilesets,
            boolean colide,
            String nomeLayer) {

        String[] valores =
            texto
                .replace('\n', ' ')
                .replace('\r', ' ')
                .split(",");

        int limite =
            Math.min(
                valores.length,
                largura * altura
            );

        for (int i = 0;
             i < limite;
             i++) {

            String valor =
                valores[i].trim();

            if (valor.length() == 0) {
                continue;
            }

            long gidLong;

            try {
                /*
                 * Remove os bits de flip do Tiled.
                 */
                gidLong =
                    Long.parseLong(valor) &
                    0x1fffffffL;

            } catch (NumberFormatException erro) {
                continue;
            }

            if (gidLong == 0) {
                continue;
            }

            int tx =
                origemX +
                (i % largura);

            int ty =
                origemY +
                (i / largura);

            desenharTile(
                (int) gidLong,
                tx,
                ty,
                tilesets,
                colide,
                nomeLayer
            );
        }
    }

    private void desenharTile(
            int gid,
            int tx,
            int ty,
            ArrayList<Tileset> tilesets,
            boolean colide,
            String nomeLayer) {

        Tileset escolhido = null;

        for (Tileset t : tilesets) {

            if (t.firstGid <= gid &&
                (escolhido == null ||
                 t.firstGid >
                 escolhido.firstGid)) {

                escolhido = t;
            }
        }

        if (escolhido == null) {
            return;
        }

        try {

            GreenfootImage folha =
                folhasCache.get(
                    escolhido.imagePath
                );

            if (folha == null) {

                folha =
                    new GreenfootImage(
                        escolhido.imagePath
                    );

                folhasCache.put(
                    escolhido.imagePath,
                    folha
                );
            }

            int local =
                gid -
                escolhido.firstGid;

            int sx =
                (local %
                 escolhido.columns) *
                SOURCE_TILE;

            int sy =
                (local /
                 escolhido.columns) *
                SOURCE_TILE;

            GreenfootImage tile =
                new GreenfootImage(
                    SOURCE_TILE,
                    SOURCE_TILE
                );

            tile.drawImage(
                folha,
                -sx,
                -sy
            );

            int px =
                (tx - MIN_X) *
                TILE;

            int py =
                (ty - MIN_Y) *
                TILE;

            if (px < 0 ||
                py < 0 ||
                px >= WORLD_WIDTH ||
                py >= WORLD_HEIGHT) {

                return;
            }

            /*
             * Desenha o tile à escala final.
             */
            GreenfootImage tileFinal =
                new GreenfootImage(
                    tile
                );

            tileFinal.scale(
                TILE,
                TILE
            );

            imagem.drawImage(
                tileFinal,
                px,
                py
            );

            /*
             * COLISÃO PIXEL A PIXEL
             *
             * Só criamos colisão para a parte realmente visível
             * do tile da layer Walls.
             *
             * Isto permite que uma tile com uma abertura/porta
             * continue a ter uma passagem.
             */
            if (colide &&
                deveBloquear(
                    gid,
                    nomeLayer
                )) {

                construirMascaraColisao(
                    tile,
                    px,
                    py
                );
            }

        } catch (Exception erro) {

            System.out.println(
                "Erro no tile " +
                gid +
                ": " +
                erro.getMessage()
            );
        }
    }

    private void construirMascaraColisao(
            GreenfootImage tile,
            int px,
            int py) {

        /*
         * O tile original tem 16x16 e é apresentado
         * no mapa a 32x32. Cada pixel original ocupa
         * 2x2 pixels no mundo.
         *
         * Alpha >= 160 = parte visível/sólida.
         */
        for (int sy = 0;
             sy < SOURCE_TILE;
             sy++) {

            for (int sx = 0;
                 sx < SOURCE_TILE;
                 sx++) {

                greenfoot.Color cor =
                    tile.getColorAt(
                        sx,
                        sy
                    );

                if (cor.getAlpha() < 160) {
                    continue;
                }

                int wx =
                    px + sx * 2;

                int wy =
                    py + sy * 2;

                marcarPixel(wx, wy);
                marcarPixel(wx + 1, wy);
                marcarPixel(wx, wy + 1);
                marcarPixel(wx + 1, wy + 1);
            }
        }
    }

    private void marcarPixel(
            int x,
            int y) {

        if (x < 0 ||
            y < 0 ||
            x >= WORLD_WIDTH ||
            y >= WORLD_HEIGHT) {

            return;
        }

        colisaoPixels.set(
            y * WORLD_WIDTH + x
        );
    }

    private boolean deveBloquear(
            int gid,
            String nomeLayer) {

        String nome =
            nomeLayer.toLowerCase();

        /*
         * Tiles do conjunto de portas:
         * não são paredes sólidas.
         */
        if (nome.equals("walls") &&
            gid >= DOOR_FIRST_GID &&
            gid <= DOOR_LAST_GID) {

            return false;
        }

        return true;
    }
}
