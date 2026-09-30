import greenfoot.*;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.awt.Rectangle;

/**
 * Carrega o mapa do Tiled e constrói a colisão.
 *
 * COMO A COLISÃO É CONSTRUÍDA
 * ---------------------------
 * 1. Só as layers de "chão + paredes" (ver ehLayerLogica) são desenhadas numa
 *    imagem lógica (1 pixel por pixel do tileset). Objetos, luzes, janelas e
 *    armadilhas ficam de fora, por isso nunca criam obstáculos invisíveis.
 * 2. Cada pixel dessa imagem é classificado pela COR: chão, água ou parede/vazio.
 *    (A layer "Walls" sozinha não chega: há paredes desenhadas noutras layers.)
 * 3. As linhas finas de sombra/rebordo (até LINHA_FINA pixels) são removidas,
 *    para não funcionarem como paredes. Pilares e paredes reais mantêm-se.
 * 4. As zonas "passagem" do Tiled abrem a colisão (escadas, ligações, etc.).
 *
 * NÍVEIS (ANDARES)
 * ----------------
 * Layers de tiles opcionais no Tiled, com QUALQUER tile pintado nas células:
 *   "Colisao"     -> bloqueia sempre (todos os andares)
 *   "Colisao_0"   -> bloqueia só quem está no andar 0
 *   "Colisao_1"   -> bloqueia só quem está no andar 1   (Colisao_-1, etc.)
 * Estas layers nunca são desenhadas no jogo.
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

    // Imagem lógica: 1 pixel por pixel do tileset (metade do mundo).
    private static final int LOGICA_W = MAP_WIDTH * SOURCE_TILE;
    private static final int LOGICA_H = MAP_HEIGHT * SOURCE_TILE;

    /** true = as poças/piscinas são água pouco funda e dá para atravessar. */
    private static final boolean AGUA_CAMINHAVEL = true;

    /** true = pinta a colisão a vermelho por cima do mapa (para depurar). */
    private static final boolean MOSTRAR_COLISAO = true;

    /** Espessura máxima (em pixels do tileset) das linhas que NÃO contam como parede. */
    private static final int LINHA_FINA = 4;

    /** Valor de "nível" que ignora as layers Colisao_N. */
    public static final int SEM_NIVEL = Integer.MIN_VALUE;

    private final ArrayList<Rectangle> escadas = new ArrayList<Rectangle>();
    private final ArrayList<Rectangle> passagens = new ArrayList<Rectangle>();

    private final GreenfootImage imagem;
    private final GreenfootImage logica;

    /* Um bit por pixel do mundo. */
    private final BitSet colisaoPixels = new BitSet(WORLD_WIDTH * WORLD_HEIGHT);
    private final BitSet aguaPixels = new BitSet(WORLD_WIDTH * WORLD_HEIGHT);

    /* Colisão por células (layers "Colisao" e "Colisao_N" do Tiled). */
    private final boolean[][] colisaoComum = new boolean[MAP_WIDTH][MAP_HEIGHT];
    private final Map<Integer, boolean[][]> colisaoPorNivel = new HashMap<Integer, boolean[][]>();

    private final Map<String, GreenfootImage> folhasCache = new HashMap<String, GreenfootImage>();

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

        imagem = new GreenfootImage(WORLD_WIDTH, WORLD_HEIGHT);
        imagem.setColor(new greenfoot.Color(13, 17, 24));
        imagem.fill();

        logica = new GreenfootImage(LOGICA_W, LOGICA_H);
        logica.setColor(new greenfoot.Color(13, 17, 24));
        logica.fill();

        carregar();
        // construirMascara();
        abrirZonasDePassagem();

        if (MOSTRAR_COLISAO) {
            depurarColisao();
        }

        aplicarAmbiente();
    }

    public GreenfootImage getImagem() {
        return imagem;
    }

    public int getFase() {
        return fase;
    }

    // ------------------------------------------------------------------
    // CONSULTAS DE COLISÃO
    // ------------------------------------------------------------------

    /** Testa um pixel individual (ignora os níveis). */
    public boolean estaBloqueado(int x, int y) {
        return temColisaoNaZona(x, y, x, y, SEM_NIVEL);
    }

    /** Colisão numa área, sem olhar ao andar. */
    public boolean temColisaoNaZona(int x1, int y1, int x2, int y2) {
        return temColisaoNaZona(x1, y1, x2, y2, SEM_NIVEL);
    }

    /**
     * Colisão numa área para um jogador que está no andar "nivel".
     * Fora do mapa conta sempre como bloqueado.
     */
    public boolean temColisaoNaZona(int x1, int y1, int x2, int y2, int nivel) {

        if (x1 < 0 || y1 < 0 || x2 >= WORLD_WIDTH || y2 >= WORLD_HEIGHT) {
            return true;
        }

        for (int y = y1; y <= y2; y++) {
            int inicio = y * WORLD_WIDTH + x1;
            int fim = y * WORLD_WIDTH + x2;
            int bloqueado = colisaoPixels.nextSetBit(inicio);
            if (bloqueado >= 0 && bloqueado <= fim) {
                return true;
            }
        }

        boolean[][] doNivel = colisaoPorNivel.get(nivel);

        for (int cx = x1 / TILE; cx <= x2 / TILE; cx++) {
            for (int cy = y1 / TILE; cy <= y2 / TILE; cy++) {
                if (colisaoComum[cx][cy] || (doNivel != null && doNivel[cx][cy])) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Indica se os pés do jogador estão sobre água. */
    public boolean estaNaAgua(int x, int y) {
        return existeNoBitSet(aguaPixels, x - 6, y + 18, x + 6, y + 28);
    }

    /** Indica se os pés do jogador estão dentro de uma escada. */
    public boolean estaNaEscada(int x, int y) {
        Rectangle zonaPes = new Rectangle(x - 6, y + 18, 12, 12);
        for (Rectangle escada : escadas) {
            if (escada.intersects(zonaPes)) {
                return true;
            }
        }
        return false;
    }

    public boolean estaNaPassagem(int x, int y) {
        Rectangle zonaJogador = new Rectangle(x - 6, y + 18, 12, 12);
        for (Rectangle passagem : passagens) {
            if (passagem.intersects(zonaJogador)) {
                return true;
            }
        }
        return false;
    }

    private boolean existeNoBitSet(BitSet mascara, int x1, int y1, int x2, int y2) {

        x1 = Math.max(0, x1);
        y1 = Math.max(0, y1);
        x2 = Math.min(WORLD_WIDTH - 1, x2);
        y2 = Math.min(WORLD_HEIGHT - 1, y2);

        if (x1 > x2 || y1 > y2) {
            return false;
        }

        for (int y = y1; y <= y2; y++) {
            int inicio = y * WORLD_WIDTH + x1;
            int fim = y * WORLD_WIDTH + x2 + 1;
            int primeiro = mascara.nextSetBit(inicio);
            if (primeiro >= 0 && primeiro < fim) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // CONSTRUÇÃO DA MÁSCARA
    // ------------------------------------------------------------------

    /** Classifica cada pixel da imagem lógica: chão, água ou parede/vazio. */
    private void construirMascara() {

        boolean[][] parede = new boolean[LOGICA_H][LOGICA_W];
        boolean[][] agua = new boolean[LOGICA_H][LOGICA_W];

        for (int y = 0; y < LOGICA_H; y++) {
            for (int x = 0; x < LOGICA_W; x++) {

                greenfoot.Color c = logica.getColorAt(x, y);
                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();

                boolean ehAgua = (b - r) > 60;
                double luminosidade = 0.3 * r + 0.59 * g + 0.11 * b;
                boolean chao = !ehAgua && ((g - r) >= 10 || luminosidade >= 145);

                agua[y][x] = ehAgua;
                parede[y][x] = !ehAgua && !chao;
            }
        }

        parede = removerLinhasFinas(parede);
        agua = removerLinhasFinas(agua);

        for (int y = 0; y < LOGICA_H; y++) {
            for (int x = 0; x < LOGICA_W; x++) {

                if (agua[y][x]) {
                    marcar(aguaPixels, x, y);
                }

                if (parede[y][x] || (!AGUA_CAMINHAVEL && agua[y][x])) {
                    marcar(colisaoPixels, x, y);
                }
            }
        }
    }

    /** Cada pixel do tileset ocupa 2x2 pixels no mundo. */
    private void marcar(BitSet mascara, int sx, int sy) {
        int wx = sx * 2;
        int wy = sy * 2;
        mascara.set(wy * WORLD_WIDTH + wx);
        mascara.set(wy * WORLD_WIDTH + wx + 1);
        mascara.set((wy + 1) * WORLD_WIDTH + wx);
        mascara.set((wy + 1) * WORLD_WIDTH + wx + 1);
    }

    /**
     * Abertura morfológica: mantém apenas as zonas onde cabe um quadrado
     * LINHA_FINA x LINHA_FINA. Remove contornos e sombras finas.
     */
    private boolean[][] removerLinhasFinas(boolean[][] m) {

        int h = m.length;
        int w = m[0].length;
        int k = LINHA_FINA;

        // 1) Erosão: célula fica ligada se o quadrado k x k a partir dela está todo ligado.
        boolean[][] erodida = new boolean[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean todos = true;
                for (int dy = 0; dy < k && todos; dy++) {
                    for (int dx = 0; dx < k; dx++) {
                        int yy = y + dy;
                        int xx = x + dx;
                        if (yy < h && xx < w && !m[yy][xx]) {
                            todos = false;
                            break;
                        }
                    }
                }
                erodida[y][x] = todos;
            }
        }

        // 2) Dilatação: volta a "engordar" o que sobreviveu.
        boolean[][] resultado = new boolean[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!erodida[y][x]) {
                    continue;
                }
                for (int dy = 0; dy < k; dy++) {
                    for (int dx = 0; dx < k; dx++) {
                        if (y + dy < h && x + dx < w) {
                            resultado[y + dy][x + dx] = true;
                        }
                    }
                }
            }
        }

        return resultado;
    }

    /** Abre a colisão dentro das zonas "passagem" do Tiled. */
    private void abrirZonasDePassagem() {

        for (Rectangle passagem : passagens) {

            int xInicial = Math.max(0, passagem.x);
            int yInicial = Math.max(0, passagem.y);
            int xFinal = Math.min(WORLD_WIDTH, passagem.x + passagem.width);
            int yFinal = Math.min(WORLD_HEIGHT, passagem.y + passagem.height);

            for (int y = yInicial; y < yFinal; y++) {
                int inicio = y * WORLD_WIDTH + xInicial;
                int fim = y * WORLD_WIDTH + xFinal;
                if (fim > inicio) {
                    colisaoPixels.clear(inicio, fim);
                }
            }
        }
    }

    /** Pinta a colisão por cima do mapa (só para depuração). */
    private void depurarColisao() {

        imagem.setColor(new greenfoot.Color(255, 0, 0, 110));

        for (int i = colisaoPixels.nextSetBit(0); i >= 0; i = colisaoPixels.nextSetBit(i + 1)) {
            imagem.fillRect(i % WORLD_WIDTH, i / WORLD_WIDTH, 1, 1);
        }

        imagem.setColor(new greenfoot.Color(255, 0, 255, 110));

        for (int cx = 0; cx < MAP_WIDTH; cx++) {
            for (int cy = 0; cy < MAP_HEIGHT; cy++) {
                boolean porNivel = false;
                for (boolean[][] grelha : colisaoPorNivel.values()) {
                    porNivel |= grelha[cx][cy];
                }
                if (colisaoComum[cx][cy] || porNivel) {
                    imagem.fillRect(cx * TILE, cy * TILE, TILE, TILE);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // LEITURA DO TMX
    // ------------------------------------------------------------------

    private void carregar() {
        try {
            File ficheiro = new File("images/mundo/Tiled_files/Dungeon" + fase + ".tmx");

            if (!ficheiro.exists()) {
                ficheiro = new File("images/mundo/Tiled_files/Dungeon1.tmx");
            }

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ficheiro);
            Element mapa = doc.getDocumentElement();

            ArrayList<Tileset> tilesets = lerTilesets(mapa);
            NodeList filhos = mapa.getChildNodes();

            for (int i = 0; i < filhos.getLength(); i++) {

                Node node = filhos.item(i);

                if (!(node instanceof Element)) {
                    continue;
                }

                Element elemento = (Element) node;
                String nome = elemento.getAttribute("name").toLowerCase();

                if ("objectgroup".equals(node.getNodeName())) {
                    if (nome.equals("passagem")) {
                        lerPassagens(elemento);
                    }
                    continue;
                }

                if (!"layer".equals(node.getNodeName())) {
                    continue;
                }

                if (nome.equals("colisao") || nome.startsWith("colisao_")) {
                    lerCamadaColisao(elemento, nome);
                    continue;
                }

                desenharCamada(elemento, tilesets, nome);
            }

        } catch (Exception erro) {
            System.out.println("Erro ao carregar o mapa: " + erro.getMessage());
        }
    }

    /** Layers "Colisao" e "Colisao_N": qualquer tile pintado bloqueia essa célula. */
    private void lerCamadaColisao(Element layer, String nome) {

        boolean[][] destino = colisaoComum;

        if (nome.startsWith("colisao_")) {
            try {
                int nivel = Integer.parseInt(nome.substring("colisao_".length()).trim());
                destino = colisaoPorNivel.get(nivel);
                if (destino == null) {
                    destino = new boolean[MAP_WIDTH][MAP_HEIGHT];
                    colisaoPorNivel.put(nivel, destino);
                }
            } catch (NumberFormatException erro) {
                return;
            }
        }

        for (int[] t : listarTiles(layer)) {
            int cx = t[0] - MIN_X;
            int cy = t[1] - MIN_Y;
            if (cx >= 0 && cy >= 0 && cx < MAP_WIDTH && cy < MAP_HEIGHT) {
                destino[cx][cy] = true;
            }
        }
    }

    private void lerPassagens(Element objectGroup) {

        NodeList objetos = objectGroup.getElementsByTagName("object");

        for (int i = 0; i < objetos.getLength(); i++) {

            Element objeto = (Element) objetos.item(i);

            if (!objeto.hasAttribute("x") || !objeto.hasAttribute("y")
                    || !objeto.hasAttribute("width") || !objeto.hasAttribute("height")) {
                continue;
            }

            double x = Double.parseDouble(objeto.getAttribute("x"));
            double y = Double.parseDouble(objeto.getAttribute("y"));
            double largura = Double.parseDouble(objeto.getAttribute("width"));
            double altura = Double.parseDouble(objeto.getAttribute("height"));

            /*
             * Os objetos do Tiled usam o mapa original (16 px por tile),
             * o jogo desenha com 32 px por tile.
             */
            int mundoX = (int) Math.round((x * TILE / SOURCE_TILE) - (MIN_X * TILE));
            int mundoY = (int) Math.round((y * TILE / SOURCE_TILE) - (MIN_Y * TILE));
            int mundoLargura = (int) Math.round(largura * TILE / SOURCE_TILE);
            int mundoAltura = (int) Math.round(altura * TILE / SOURCE_TILE);

            Rectangle area = new Rectangle(mundoX, mundoY, mundoLargura, mundoAltura);
            passagens.add(area);

            String nome = objeto.getAttribute("name").trim().toLowerCase();

            if (nome.startsWith("escada")) {
                escadas.add(area);
            }
        }
    }

    private void aplicarAmbiente() {
        greenfoot.Color cor;

        if (fase == 2) {
            cor = new greenfoot.Color(22, 42, 58, 55);
        } else if (fase == 3) {
            cor = new greenfoot.Color(62, 30, 45, 65);
        } else {
            cor = new greenfoot.Color(8, 8, 18, 25);
        }

        imagem.setColor(cor);
        imagem.fillRect(0, 0, imagem.getWidth(), imagem.getHeight());
    }

    private ArrayList<Tileset> lerTilesets(Element mapa) {

        ArrayList<Tileset> resultado = new ArrayList<Tileset>();
        NodeList nodes = mapa.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {

            Node node = nodes.item(i);

            if (!(node instanceof Element) || !"tileset".equals(node.getNodeName())) {
                continue;
            }

            Element ts = (Element) node;
            Element img = (Element) ts.getElementsByTagName("image").item(0);

            if (img == null) {
                continue;
            }

            Tileset t = new Tileset();
            t.firstGid = Integer.parseInt(ts.getAttribute("firstgid"));
            t.columns = Integer.parseInt(ts.getAttribute("columns"));

            String src = img.getAttribute("source");
            t.imagePath = "images/mundo/Tiled_files/" + new File(src).getName();

            resultado.add(t);
        }

        return resultado;
    }

    // ------------------------------------------------------------------
    // DESENHO DAS LAYERS
    // ------------------------------------------------------------------

    /** Layers que definem onde se pode andar (chão, água e paredes). */
    private boolean ehLayerLogica(String nome) {
        return nome.equals("water_floor3")
                || nome.equals("floor2_darker_surface")
                || nome.equals("floor2_pool")
                || nome.equals("floor")
                || nome.equals("floor_darker_surface")
                || nome.equals("walls");
    }

    private void desenharCamada(Element layer, ArrayList<Tileset> tilesets, String nome) {
        boolean logicaDaLayer = ehLayerLogica(nome);

        for (int[] t : listarTiles(layer)) {
            desenharTile(t[2], t[0], t[1], tilesets, logicaDaLayer);
        }
    }

    /** Devolve {tileX, tileY, gid} de todos os tiles não vazios da layer. */
    private ArrayList<int[]> listarTiles(Element layer) {

        ArrayList<int[]> tiles = new ArrayList<int[]>();
        NodeList chunks = layer.getElementsByTagName("chunk");

        if (chunks.getLength() > 0) {

            for (int c = 0; c < chunks.getLength(); c++) {
                Element chunk = (Element) chunks.item(c);
                lerCSV(chunk.getTextContent(),
                        Integer.parseInt(chunk.getAttribute("x")),
                        Integer.parseInt(chunk.getAttribute("y")),
                        Integer.parseInt(chunk.getAttribute("width")),
                        tiles);
            }

        } else {

            Element data = (Element) layer.getElementsByTagName("data").item(0);

            if (data != null) {
                int largura = layer.hasAttribute("width")
                        ? Integer.parseInt(layer.getAttribute("width"))
                        : MAP_WIDTH;
                lerCSV(data.getTextContent(), 0, 0, largura, tiles);
            }
        }

        return tiles;
    }

    private void lerCSV(String texto, int origemX, int origemY, int largura, ArrayList<int[]> saida) {

        String[] valores = texto.replace('\n', ' ').replace('\r', ' ').split(",");

        for (int i = 0; i < valores.length; i++) {

            String valor = valores[i].trim();

            if (valor.length() == 0) {
                continue;
            }

            long gid;

            try {
                // Remove os bits de flip do Tiled.
                gid = Long.parseLong(valor) & 0x1fffffffL;
            } catch (NumberFormatException erro) {
                continue;
            }

            if (gid != 0) {
                saida.add(new int[] { origemX + (i % largura), origemY + (i / largura), (int) gid });
            }
        }
    }

    private void desenharTile(int gid, int tx, int ty, ArrayList<Tileset> tilesets, boolean naLogica) {

        Tileset escolhido = null;

        for (Tileset t : tilesets) {
            if (t.firstGid <= gid && (escolhido == null || t.firstGid > escolhido.firstGid)) {
                escolhido = t;
            }
        }

        if (escolhido == null) {
            return;
        }

        try {
            GreenfootImage folha = folhasCache.get(escolhido.imagePath);

            if (folha == null) {
                folha = new GreenfootImage(escolhido.imagePath);
                folhasCache.put(escolhido.imagePath, folha);
            }

            int local = gid - escolhido.firstGid;
            int sx = (local % escolhido.columns) * SOURCE_TILE;
            int sy = (local / escolhido.columns) * SOURCE_TILE;

            GreenfootImage tile = new GreenfootImage(SOURCE_TILE, SOURCE_TILE);
            tile.drawImage(folha, -sx, -sy);

            int px = (tx - MIN_X) * TILE;
            int py = (ty - MIN_Y) * TILE;

            if (px < 0 || py < 0 || px >= WORLD_WIDTH || py >= WORLD_HEIGHT) {
                return;
            }

            GreenfootImage tileFinal = new GreenfootImage(tile);
            tileFinal.scale(TILE, TILE);
            imagem.drawImage(tileFinal, px, py);

            if (naLogica) {
                logica.drawImage(tile, px / 2, py / 2);
            }

        } catch (Exception erro) {
            System.out.println("Erro no tile " + gid + ": " + erro.getMessage());
        }
    }
}