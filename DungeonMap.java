import greenfoot.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.awt.Point;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.awt.Rectangle;

/**
 * Carrega o mapa do Tiled e constrói a colisão.
 *
 * COMO A COLISÃO É CONSTRUÍDA
 * ---------------------------
 * As layers "Colisao" e "Colisao_N" do Tiled definem as células bloqueadas.
 * As zonas "passagem" são lidas para detetar escadas.
 *
 * NÍVEIS (ANDARES)
 * ----------------
 * Layers de tiles opcionais no Tiled, com QUALQUER tile pintado nas células:
 * "Colisao" -> bloqueia sempre (todos os andares)
 * "Colisao_0" -> bloqueia só quem está no andar 0
 * "Colisao_1" -> bloqueia só quem está no andar 1 (Colisao_-1, etc.)
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

    /** true = pinta a colisão a vermelho por cima do mapa (para depurar). */
    private static final boolean MOSTRAR_COLISAO = false;

    private static final int TAMANHO_OBJETO_PONTO = 48;

    private final ArrayList<Rectangle> escadas = new ArrayList<Rectangle>();

    private final GreenfootImage imagem;
    private GreenfootImage imagemBase;

    private final ArrayList<Interagivel> interagiveis = new ArrayList<Interagivel>();
    private final Map<Chave, ArrayList<int[]>> tilesDasChaves =
            new HashMap<Chave, ArrayList<int[]>>();

    private final ArrayList<ConfiguracaoInimigo> configuracoesInimigos = new ArrayList<ConfiguracaoInimigo>();

    private final ArrayList<Point> spawnsJogadores = new ArrayList<Point>();

    /* Colisão por células (layers "Colisao" e "Colisao_N" do Tiled). */
    private final boolean[][] colisaoComum = new boolean[MAP_WIDTH][MAP_HEIGHT];
    private final Map<Integer, boolean[][]> colisaoPorNivel = new HashMap<Integer, boolean[][]>();

    private final Map<String, GreenfootImage> folhasCache = new HashMap<String, GreenfootImage>();
    private static final Map<String, GreenfootImage> tilesCache =
            new HashMap<String, GreenfootImage>();

    private final int fase;
    private final ArrayList<Tileset> tilesetsAtuais = new ArrayList<Tileset>();

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

        carregar();

        if (MOSTRAR_COLISAO) {
            depurarColisao();
        }

        aplicarAmbiente();
        imagemBase = new GreenfootImage(imagem);
        redesenharChavesVisuais();
    }

    public GreenfootImage getImagem() {
        return imagem;
    }

    public ArrayList<Interagivel> getInteragiveis() {
        return interagiveis;
    }

    /** Remove um objeto interagível que deixou de existir no mapa. */
    public void removerInteragivel(Interagivel interagivel) {
        interagiveis.remove(interagivel);
        if (interagivel instanceof Chave) {
            redesenharChavesVisuais();
        }
    }

    public ArrayList<ConfiguracaoInimigo> getConfiguracoesInimigos() {
        return configuracoesInimigos;
    }

    // ------------------------------------------------------------------
    // CONSULTAS DE COLISÃO
    // ------------------------------------------------------------------

    /**
     * Colisão numa área para um jogador que está no andar "nivel".
     * Fora do mapa conta sempre como bloqueado.
     */
    public boolean temColisaoNaZona(int x1, int y1, int x2, int y2, int nivel) {

        if (x1 < 0 || y1 < 0 || x2 >= WORLD_WIDTH || y2 >= WORLD_HEIGHT) {
            return true;
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

    /** Indica se a caixa dos pés está bloqueada no nível indicado. */
    public boolean estaBloqueado(Rectangle pes, int nivel) {
        if (pes == null) {
            return true;
        }

        return temColisaoNaZona(pes.x, pes.y,
                pes.x + pes.width - 1,
                pes.y + pes.height - 1, nivel);
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

    /** Pinta a colisão por cima do mapa (só para depuração). */
    private void depurarColisao() {

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
            tilesetsAtuais.clear();
            tilesetsAtuais.addAll(tilesets);
            NodeList filhos = mapa.getChildNodes();

            /*
             * O objectgroup aparece depois das layers no TMX atual. As
             * chaves precisam de ser conhecidas antes de desenhar a layer
             * Objects, para que os seus tiles não fiquem presos no fundo.
             */
            for (int i = 0; i < filhos.getLength(); i++) {
                Node node = filhos.item(i);
                if (node instanceof Element
                        && "objectgroup".equals(node.getNodeName())) {
                    Element elemento = (Element) node;
                    String nome = elemento.getAttribute("name").trim();
                    if ("interacoesobjetos".equalsIgnoreCase(nome)) {
                        lerInteracoes(elemento);
                    }
                }
            }

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

                    if (nome.equals("inimigos")) {
                        lerInimigos(elemento);
                    }

                    if ("jogadores".equalsIgnoreCase(nome)) {
                        lerSpawnJogadores(elemento);
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

                desenharCamada(elemento, tilesets);
            }

        } catch (Exception erro) {
            System.out.println("Erro ao carregar o mapa: " + erro.getMessage());
        }
    }

    /**
     * Layers "Colisao" e "Colisao_N": qualquer tile pintado bloqueia essa célula.
     */
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

    private void lerInimigos(Element objectGroup) {

        NodeList objetos = objectGroup.getElementsByTagName("object");

        Map<String, Rectangle> patrulhas = new HashMap<String, Rectangle>();
        Map<String, Rectangle> spawns = new HashMap<String, Rectangle>();

        for (int i = 0; i < objetos.getLength(); i++) {

            Element objeto = (Element) objetos.item(i);

            if (!objeto.hasAttribute("x")
                    || !objeto.hasAttribute("y")) {
                continue;
            }

            String nome = objeto.getAttribute("name").trim();

            if (nome.isEmpty()) {
                continue;
            }

            double x = Double.parseDouble(objeto.getAttribute("x"));
            double y = Double.parseDouble(objeto.getAttribute("y"));

            boolean ePatrulha = nome.startsWith("Patrulha_");

            double largura = objeto.hasAttribute("width")
                    ? Double.parseDouble(objeto.getAttribute("width"))
                    : SOURCE_TILE;

            double altura = objeto.hasAttribute("height")
                    ? Double.parseDouble(objeto.getAttribute("height"))
                    : SOURCE_TILE;

            Rectangle area;

            if (ePatrulha) {

                area = converterParaMundo(
                        x,
                        y,
                        largura,
                        altura,
                        false);

                patrulhas.put(
                        nome.substring("Patrulha_".length()),
                        area);

            } else if (nome.startsWith("Spawn_")) {

                area = converterParaMundo(
                        x,
                        y,
                        SOURCE_TILE,
                        SOURCE_TILE,
                        true);

                spawns.put(
                        nome.substring("Spawn_".length()),
                        area);
            }
        }

        /*
         * Um spawn sem patrulha representa um inimigo parado. Assim,
         * todos os Spawn_N definidos no Tiled são válidos por si só.
         */
        for (Map.Entry<String, Rectangle> entrada : spawns.entrySet()) {
            String numero = entrada.getKey();
            Rectangle spawn = entrada.getValue();
            Rectangle patrulha = patrulhas.get(numero);

            configuracoesInimigos.add(new ConfiguracaoInimigo(spawn, patrulha));
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
            Rectangle area = converterParaMundo(x, y, largura, altura, false);
            String nome = objeto.getAttribute("name").trim().toLowerCase();

            if (nome.startsWith("escada")) {
                escadas.add(area);
            }
        }
    }

    private void lerInteracoes(Element objectGroup) {

        NodeList objetos = objectGroup.getElementsByTagName("object");

        for (int i = 0; i < objetos.getLength(); i++) {

            Element objeto = (Element) objetos.item(i);

            if (!objeto.hasAttribute("x") || !objeto.hasAttribute("y")) {
                continue;
            }

            double x = Double.parseDouble(objeto.getAttribute("x"));
            double y = Double.parseDouble(objeto.getAttribute("y"));
            double largura = objeto.hasAttribute("width")
                    ? Double.parseDouble(objeto.getAttribute("width"))
                    : SOURCE_TILE;
            double altura = objeto.hasAttribute("height")
                    ? Double.parseDouble(objeto.getAttribute("height"))
                    : SOURCE_TILE;
            boolean objetoPonto = !objeto.hasAttribute("width")
                    || !objeto.hasAttribute("height");

            Rectangle area = converterParaMundo(x, y, largura, altura, objetoPonto);

            String nome = objeto.getAttribute("name").trim();

            if (nome.isEmpty()) {
                continue;
            }

            String tipo = nome.toLowerCase();
            if (tipo.equals("bau_1") || tipo.equals("bau_2")
                    || tipo.equals("bau_3") || tipo.equals("bau_4")) {
                interagiveis.add(new Bau(nome, area));
            } else if (tipo.equals("chave_1") || tipo.equals("chave_2")) {
                Chave chave = new Chave(nome, area);
                interagiveis.add(chave);
                tilesDasChaves.put(chave, new ArrayList<int[]>());
            } else if (tipo.equals("alavanca_1") || tipo.equals("alavanca_2")) {
                interagiveis.add(new Alavanca(nome, area));
            } else {
                System.out.println("Objeto interativo desconhecido ignorado: " + nome);
            }
        }
    }

    private Rectangle converterParaMundo(double x, double y,
            double largura, double altura, boolean objetoPonto) {
        int mundoX = (int) Math.round((x * TILE / SOURCE_TILE) - (MIN_X * TILE));
        int mundoY = (int) Math.round((y * TILE / SOURCE_TILE) - (MIN_Y * TILE));
        int mundoLargura = objetoPonto
                ? TAMANHO_OBJETO_PONTO
                : (int) Math.round(largura * TILE / SOURCE_TILE);
        int mundoAltura = objetoPonto
                ? TAMANHO_OBJETO_PONTO
                : (int) Math.round(altura * TILE / SOURCE_TILE);

        return new Rectangle(
                objetoPonto ? mundoX - mundoLargura / 2 : mundoX,
                objetoPonto ? mundoY - mundoAltura / 2 : mundoY,
                mundoLargura,
                mundoAltura);
    }

    private void aplicarAmbiente() {
        aplicarAmbienteNaZona(imagem, 0, 0, imagem.getWidth(), imagem.getHeight());
    }

    private void aplicarAmbienteNaZona(GreenfootImage destino,
            int x, int y, int largura, int altura) {
        greenfoot.Color cor;

        if (fase == 2) {
            cor = new greenfoot.Color(22, 42, 58, 55);
        } else if (fase == 3) {
            cor = new greenfoot.Color(62, 30, 45, 65);
        } else {
            cor = new greenfoot.Color(8, 8, 18, 25);
        }

        destino.setColor(cor);
        destino.fillRect(x, y, largura, altura);
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

    private void desenharCamada(Element layer, ArrayList<Tileset> tilesets) {
        boolean eCamadaDeObjetos = "objects".equalsIgnoreCase(layer.getAttribute("name"));
        for (int[] t : listarTiles(layer)) {
            if (eCamadaDeObjetos && guardarTileDeChave(t)) {
                continue;
            }
            desenharTile(t[2], t[0], t[1], tilesets);
        }
    }

    /** Guarda os tiles das chaves para os desenhar fora da imagem-base. */
    private boolean guardarTileDeChave(int[] tile) {
        int tileX = tile[0];
        int tileY = tile[1];

        for (Map.Entry<Chave, ArrayList<int[]>> entrada : tilesDasChaves.entrySet()) {
            Chave chave = entrada.getKey();
            int ancoraX = (int) Math.floor(chave.getArea().getCenterX() / TILE + MIN_X);
            int ancoraY = (int) Math.floor(chave.getArea().getCenterY() / TILE + MIN_Y);

            /* Os sprites das chaves ocupam no máximo uma área de 3x3 tiles. */
            if (Math.abs(tileX - ancoraX) <= 2 && Math.abs(tileY - ancoraY) <= 2) {
                entrada.getValue().add(new int[] { tileX, tileY, tile[2] });
                return true;
            }
        }

        return false;
    }

    /** Repõe o fundo e redesenha apenas as chaves ainda disponíveis. */
    private void redesenharChavesVisuais() {
        if (imagemBase == null) {
            return;
        }

        imagem.drawImage(imagemBase, 0, 0);
        for (Map.Entry<Chave, ArrayList<int[]>> entrada : tilesDasChaves.entrySet()) {
            if (entrada.getKey().estaRecolhida()) {
                continue;
            }
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (int[] tile : entrada.getValue()) {
                desenharTile(tile[2], tile[0], tile[1], tilesetsAtuais);
                minX = Math.min(minX, tile[0]);
                minY = Math.min(minY, tile[1]);
                maxX = Math.max(maxX, tile[0]);
                maxY = Math.max(maxY, tile[1]);
            }
            if (minX != Integer.MAX_VALUE) {
                aplicarAmbienteNaZona(imagem,
                        (minX - MIN_X) * TILE,
                        (minY - MIN_Y) * TILE,
                        (maxX - minX + 1) * TILE,
                        (maxY - minY + 1) * TILE);
            }
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

    private void desenharTile(int gid, int tx, int ty, ArrayList<Tileset> tilesets) {

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

            int px = (tx - MIN_X) * TILE;
            int py = (ty - MIN_Y) * TILE;

            if (px < 0 || py < 0 || px >= WORLD_WIDTH || py >= WORLD_HEIGHT) {
                return;
            }

            String chave = escolhido.imagePath + "#" + gid;
            GreenfootImage tileFinal = tilesCache.get(chave);
            if (tileFinal == null) {
                int local = gid - escolhido.firstGid;
                int sx = (local % escolhido.columns) * SOURCE_TILE;
                int sy = (local / escolhido.columns) * SOURCE_TILE;
                GreenfootImage tile = new GreenfootImage(SOURCE_TILE, SOURCE_TILE);
                tile.drawImage(folha, -sx, -sy);
                tileFinal = new GreenfootImage(tile);
                tileFinal.scale(TILE, TILE);
                tilesCache.put(chave, tileFinal);
            }
            imagem.drawImage(tileFinal, px, py);

        } catch (Exception erro) {
            System.out.println("Erro no tile " + gid + ": " + erro.getMessage());
        }
    }

    private void lerSpawnJogadores(Element objectGroup) {

        NodeList objetos = objectGroup.getElementsByTagName("object");
        TreeMap<String, Point> porNome = new TreeMap<String, Point>();

        for (int i = 0; i < objetos.getLength(); i++) {

            Element objeto = (Element) objetos.item(i);

            if (!objeto.hasAttribute("x") || !objeto.hasAttribute("y")) {
                continue;
            }

            double x = Double.parseDouble(objeto.getAttribute("x"));
            double y = Double.parseDouble(objeto.getAttribute("y"));
            Rectangle area = converterParaMundo(x, y, 0, 0, true);

            porNome.put(objeto.getAttribute("name").trim(),
                    new Point(area.x + area.width / 2, area.y + area.height / 2));
        }

        spawnsJogadores.addAll(porNome.values());
    }

    public ArrayList<Point> getSpawnsJogadores() {
        return spawnsJogadores;
    }
}
