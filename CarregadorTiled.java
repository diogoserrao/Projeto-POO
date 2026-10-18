import greenfoot.*;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/** Lê um mapa TMX e prepara os dados usados pelo jogo. */
public class CarregadorTiled {
    private static final int SOURCE_TILE = 16;
    private static final int TILE = 32;
    private static final int MIN_X = -20;
    private static final int MIN_Y = -6;
    private static final int MAP_WIDTH = 40;
    private static final int MAP_HEIGHT = 30;
    private static final int WORLD_WIDTH = MAP_WIDTH * TILE;
    private static final int WORLD_HEIGHT = MAP_HEIGHT * TILE;
    private static final int TAMANHO_OBJETO_PONTO = 48;
    private static final boolean MOSTRAR_COLISAO = false;

    private final int fase;
    private final GreenfootImage imagem;
    private GreenfootImage imagemBase;
    private final ArrayList<Interagivel> interagiveis = new ArrayList<Interagivel>();
    private final Map<Chave, ArrayList<int[]>> tilesDasChaves = new HashMap<Chave, ArrayList<int[]>>();
    private final ArrayList<ConfiguracaoInimigo> configuracoesInimigos = new ArrayList<ConfiguracaoInimigo>();
    private final ArrayList<Point> posicoesPedras = new ArrayList<Point>();
    private final ArrayList<Point> spawnsJogadores = new ArrayList<Point>();
    private final boolean[][] colisaoComum = new boolean[MAP_WIDTH][MAP_HEIGHT];
    private static final Map<String, GreenfootImage> folhasCache = new HashMap<String, GreenfootImage>();
    private static final Map<String, GreenfootImage> tilesCache = new HashMap<String, GreenfootImage>();
    private final ArrayList<Tileset> tilesetsAtuais = new ArrayList<Tileset>();

    static class Tileset {
        int firstGid;
        int columns;
        String imagePath;
    }

    public CarregadorTiled(int fase) {
        this.fase = Math.max(1, Math.min(3, fase));
        imagem = new GreenfootImage(WORLD_WIDTH, WORLD_HEIGHT);
        imagem.setColor(new greenfoot.Color(13, 17, 24));
        imagem.fill();
        carregar();
        if (MOSTRAR_COLISAO) depurarColisao();
        aplicarAmbiente(imagem, 0, 0, imagem.getWidth(), imagem.getHeight());
        imagemBase = new GreenfootImage(imagem);
        redesenharChaves(getDados());
    }

    public DadosMapa getDados() {
        return new DadosMapa(imagem, imagemBase, interagiveis, tilesDasChaves,
                configuracoesInimigos, posicoesPedras, spawnsJogadores, colisaoComum,
                tilesetsAtuais, fase);
    }

    public static final class DadosMapa {
        final GreenfootImage imagem;
        final GreenfootImage imagemBase;
        final ArrayList<Interagivel> interagiveis;
        final Map<Chave, ArrayList<int[]>> tilesDasChaves;
        final ArrayList<ConfiguracaoInimigo> configuracoesInimigos;
        final ArrayList<Point> posicoesPedras;
        final ArrayList<Point> spawnsJogadores;
        final boolean[][] colisaoComum;
        final ArrayList<Tileset> tilesets;
        final int fase;

        DadosMapa(GreenfootImage imagem, GreenfootImage imagemBase,
                ArrayList<Interagivel> interagiveis,
                Map<Chave, ArrayList<int[]>> tilesDasChaves,
                ArrayList<ConfiguracaoInimigo> configuracoesInimigos,
                ArrayList<Point> posicoesPedras,
                ArrayList<Point> spawnsJogadores, boolean[][] colisaoComum,
                ArrayList<Tileset> tilesets, int fase) {
            this.imagem = imagem;
            this.imagemBase = imagemBase;
            this.interagiveis = interagiveis;
            this.tilesDasChaves = tilesDasChaves;
            this.configuracoesInimigos = configuracoesInimigos;
            this.posicoesPedras = posicoesPedras;
            this.spawnsJogadores = spawnsJogadores;
            this.colisaoComum = colisaoComum;
            this.tilesets = tilesets;
            this.fase = fase;
        }
    }

    private void carregar() {
        File ficheiro = new File("images/mundo/Tiled_files/Dungeon" + fase + ".tmx");
        if (!ficheiro.exists()) ficheiro = new File("images/mundo/Tiled_files/Dungeon1.tmx");
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ficheiro);
            Element mapa = doc.getDocumentElement();
            ArrayList<Tileset> tilesets = lerTilesets(mapa);
            tilesetsAtuais.addAll(tilesets);
            NodeList filhos = mapa.getChildNodes();
            for (int i = 0; i < filhos.getLength(); i++) {
                Node node = filhos.item(i);
                if (node instanceof Element && "objectgroup".equals(node.getNodeName())
                        && "interacoesobjetos".equalsIgnoreCase(((Element) node).getAttribute("name").trim())) {
                    lerInteracoes((Element) node);
                }
            }
            for (int i = 0; i < filhos.getLength(); i++) {
                Node node = filhos.item(i);
                if (!(node instanceof Element)) continue;
                Element elemento = (Element) node;
                String nome = elemento.getAttribute("name").toLowerCase();
                if ("objectgroup".equals(node.getNodeName())) {
                    if (nome.equals("inimigos")) lerInimigos(elemento);
                    if (nome.equals("pedras") || nome.equals("pedra")) lerPedras(elemento);
                    if (nome.equals("jogadores")) lerSpawnJogadores(elemento);
                    continue;
                }
                if (!"layer".equals(node.getNodeName())) continue;
                if (nome.equals("colisao")) {
                    lerCamadaColisao(elemento);
                } else {
                    desenharCamada(elemento, tilesets);
                }
            }
        } catch (Exception erro) {
            throw new IllegalStateException("Erro ao carregar o mapa Tiled: " + ficheiro.getPath(), erro);
        }
    }

    private void lerCamadaColisao(Element layer) {
        for (int[] t : listarTiles(layer)) {
            int cx = t[0] - MIN_X, cy = t[1] - MIN_Y;
            if (cx >= 0 && cy >= 0 && cx < MAP_WIDTH && cy < MAP_HEIGHT) colisaoComum[cx][cy] = true;
        }
    }

    private void lerInimigos(Element grupo) {
        NodeList objetos = grupo.getElementsByTagName("object");
        Map<String, Rectangle> patrulhas = new HashMap<String, Rectangle>();
        Map<String, Rectangle> spawns = new HashMap<String, Rectangle>();
        for (int i = 0; i < objetos.getLength(); i++) {
            Element objeto = (Element) objetos.item(i);
            if (!objeto.hasAttribute("x") || !objeto.hasAttribute("y")) continue;
            String nome = objeto.getAttribute("name").trim();
            if (nome.isEmpty()) continue;
            double x = Double.parseDouble(objeto.getAttribute("x"));
            double y = Double.parseDouble(objeto.getAttribute("y"));
            double largura = objeto.hasAttribute("width") ? Double.parseDouble(objeto.getAttribute("width")) : SOURCE_TILE;
            double altura = objeto.hasAttribute("height") ? Double.parseDouble(objeto.getAttribute("height")) : SOURCE_TILE;
            if (nome.startsWith("Patrulha_")) {
                patrulhas.put(nome.substring("Patrulha_".length()), converterParaMundo(x, y, largura, altura, false));
            } else if (nome.startsWith("Spawn_")) {
                spawns.put(nome.substring("Spawn_".length()), converterParaMundo(x, y, SOURCE_TILE, SOURCE_TILE, true));
            }
        }
        for (Map.Entry<String, Rectangle> entrada : spawns.entrySet()) {
            configuracoesInimigos.add(new ConfiguracaoInimigo(entrada.getValue(), patrulhas.get(entrada.getKey())));
        }
    }

    private void lerPedras(Element grupo) {
        NodeList objetos = grupo.getElementsByTagName("object");
        for (int i = 0; i < objetos.getLength(); i++) {
            Element objeto = (Element) objetos.item(i);
            if (!objeto.hasAttribute("x") || !objeto.hasAttribute("y")) continue;

            double x = Double.parseDouble(objeto.getAttribute("x"));
            double y = Double.parseDouble(objeto.getAttribute("y"));
            Rectangle area = converterParaMundo(x, y, SOURCE_TILE, SOURCE_TILE, true);
            posicoesPedras.add(new Point((int) area.getCenterX(), (int) area.getCenterY()));
        }
    }

    private void lerInteracoes(Element grupo) {
        NodeList objetos = grupo.getElementsByTagName("object");
        for (int i = 0; i < objetos.getLength(); i++) {
            Element objeto = (Element) objetos.item(i);
            if (!objeto.hasAttribute("x") || !objeto.hasAttribute("y")) continue;
            double x = Double.parseDouble(objeto.getAttribute("x"));
            double y = Double.parseDouble(objeto.getAttribute("y"));
            double largura = objeto.hasAttribute("width") ? Double.parseDouble(objeto.getAttribute("width")) : SOURCE_TILE;
            double altura = objeto.hasAttribute("height") ? Double.parseDouble(objeto.getAttribute("height")) : SOURCE_TILE;
            boolean ponto = !objeto.hasAttribute("width") || !objeto.hasAttribute("height");
            Rectangle area = converterParaMundo(x, y, largura, altura, ponto);
            String nome = objeto.getAttribute("name").trim();
            if (nome.isEmpty()) continue;
            String tipo = nome.toLowerCase();
            if (tipo.startsWith("bau")) interagiveis.add(new Bau(nome, area));
            else if (tipo.startsWith("chave")) {
                Chave chave = new Chave(nome, area);
                interagiveis.add(chave);
                tilesDasChaves.put(chave, new ArrayList<int[]>());
            } else if (tipo.startsWith("alavanca")) interagiveis.add(new Alavanca(nome, area));
            else System.out.println("Objeto interativo desconhecido ignorado: " + nome);
        }
    }

    private Rectangle converterParaMundo(double x, double y, double largura, double altura, boolean ponto) {
        int mundoX = (int) Math.round((x * TILE / SOURCE_TILE) - (MIN_X * TILE));
        int mundoY = (int) Math.round((y * TILE / SOURCE_TILE) - (MIN_Y * TILE));
        int mundoLargura = ponto ? TAMANHO_OBJETO_PONTO : (int) Math.round(largura * TILE / SOURCE_TILE);
        int mundoAltura = ponto ? TAMANHO_OBJETO_PONTO : (int) Math.round(altura * TILE / SOURCE_TILE);
        return new Rectangle(ponto ? mundoX - mundoLargura / 2 : mundoX, ponto ? mundoY - mundoAltura / 2 : mundoY, mundoLargura, mundoAltura);
    }

    private ArrayList<Tileset> lerTilesets(Element mapa) {
        ArrayList<Tileset> resultado = new ArrayList<Tileset>();
        NodeList nodes = mapa.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (!(node instanceof Element) || !"tileset".equals(node.getNodeName())) continue;
            Element ts = (Element) node;
            Element img = (Element) ts.getElementsByTagName("image").item(0);
            if (img == null) continue;
            Tileset t = new Tileset();
            t.firstGid = Integer.parseInt(ts.getAttribute("firstgid"));
            t.columns = Integer.parseInt(ts.getAttribute("columns"));
            t.imagePath = "images/mundo/Tiled_files/" + new File(img.getAttribute("source")).getName();
            resultado.add(t);
        }
        return resultado;
    }

    private void desenharCamada(Element layer, ArrayList<Tileset> tilesets) {
        boolean objetos = "objects".equalsIgnoreCase(layer.getAttribute("name"));
        for (int[] t : listarTiles(layer)) {
            if (objetos && guardarTileDeChave(t)) continue;
            desenharTile(t[2], t[0], t[1], tilesets);
        }
    }

    private boolean guardarTileDeChave(int[] tile) {
        Chave chaveMaisProxima = null;
        int menorDistancia = Integer.MAX_VALUE;
        for (Map.Entry<Chave, ArrayList<int[]>> entrada : tilesDasChaves.entrySet()) {
            Chave chave = entrada.getKey();
            int ancoraX = (int) Math.floor(chave.getArea().getCenterX() / TILE + MIN_X);
            int ancoraY = (int) Math.floor(chave.getArea().getCenterY() / TILE + MIN_Y);
            int distancia = Math.abs(tile[0] - ancoraX) + Math.abs(tile[1] - ancoraY);
            if (Math.abs(tile[0] - ancoraX) <= 2 && Math.abs(tile[1] - ancoraY) <= 2
                    && distancia < menorDistancia) {
                chaveMaisProxima = chave;
                menorDistancia = distancia;
            }
        }
        if (chaveMaisProxima == null) return false;
        tilesDasChaves.get(chaveMaisProxima).add(new int[] { tile[0], tile[1], tile[2] });
        return true;
    }

    private ArrayList<int[]> listarTiles(Element layer) {
        ArrayList<int[]> tiles = new ArrayList<int[]>();
        NodeList chunks = layer.getElementsByTagName("chunk");
        if (chunks.getLength() > 0) {
            for (int c = 0; c < chunks.getLength(); c++) {
                Element chunk = (Element) chunks.item(c);
                lerCSV(chunk.getTextContent(), Integer.parseInt(chunk.getAttribute("x")), Integer.parseInt(chunk.getAttribute("y")), Integer.parseInt(chunk.getAttribute("width")), tiles);
            }
        } else {
            Element data = (Element) layer.getElementsByTagName("data").item(0);
            if (data != null) lerCSV(data.getTextContent(), 0, 0, layer.hasAttribute("width") ? Integer.parseInt(layer.getAttribute("width")) : MAP_WIDTH, tiles);
        }
        return tiles;
    }

    private void lerCSV(String texto, int origemX, int origemY, int largura, ArrayList<int[]> saida) {
        String[] valores = texto.replace('\n', ' ').replace('\r', ' ').split(",");
        for (int i = 0; i < valores.length; i++) {
            String valor = valores[i].trim();
            if (valor.length() == 0) continue;
            try {
                long gid = Long.parseLong(valor) & 0x1fffffffL;
                if (gid != 0) saida.add(new int[] { origemX + (i % largura), origemY + (i / largura), (int) gid });
            } catch (NumberFormatException erro) { /* célula inválida/vazia */ }
        }
    }

    private void desenharTile(int gid, int tx, int ty, ArrayList<Tileset> tilesets) {
        Tileset escolhido = null;
        for (Tileset t : tilesets) if (t.firstGid <= gid && (escolhido == null || t.firstGid > escolhido.firstGid)) escolhido = t;
        if (escolhido == null) return;
        try {
            int px = (tx - MIN_X) * TILE, py = (ty - MIN_Y) * TILE;
            if (px < 0 || py < 0 || px >= WORLD_WIDTH || py >= WORLD_HEIGHT) return;
            GreenfootImage tileFinal = obterTile(escolhido, gid);
            if (tileFinal == null) return;
            imagem.drawImage(tileFinal, px, py);
        } catch (Exception erro) {
            System.out.println("Erro no tile " + gid + ": " + erro.getMessage());
        }
    }

    private void lerSpawnJogadores(Element grupo) {
        NodeList objetos = grupo.getElementsByTagName("object");
        TreeMap<String, Point> porNome = new TreeMap<String, Point>();
        for (int i = 0; i < objetos.getLength(); i++) {
            Element objeto = (Element) objetos.item(i);
            if (!objeto.hasAttribute("x") || !objeto.hasAttribute("y")) continue;
            Rectangle area = converterParaMundo(Double.parseDouble(objeto.getAttribute("x")), Double.parseDouble(objeto.getAttribute("y")), 0, 0, true);
            porNome.put(objeto.getAttribute("name").trim(), new Point(area.x + area.width / 2, area.y + area.height / 2));
        }
        spawnsJogadores.addAll(porNome.values());
    }

    private void aplicarAmbiente(GreenfootImage destino, int x, int y, int largura, int altura) {
        greenfoot.Color cor = fase == 2 ? new greenfoot.Color(22, 42, 58, 55)
                : fase == 3 ? new greenfoot.Color(62, 30, 45, 65)
                : new greenfoot.Color(8, 8, 18, 25);
        destino.setColor(cor);
        destino.fillRect(x, y, largura, altura);
    }

    private void depurarColisao() {
        imagem.setColor(new greenfoot.Color(255, 0, 255, 110));
        for (int cx = 0; cx < MAP_WIDTH; cx++) for (int cy = 0; cy < MAP_HEIGHT; cy++) {
            if (colisaoComum[cx][cy]) imagem.fillRect(cx * TILE, cy * TILE, TILE, TILE);
        }
    }

    static void redesenharChaves(DadosMapa dados) {
        dados.imagem.drawImage(dados.imagemBase, 0, 0);
        for (Map.Entry<Chave, ArrayList<int[]>> entrada : dados.tilesDasChaves.entrySet()) {
            if (entrada.getKey().estaRecolhida()) continue;
            for (int[] tile : entrada.getValue()) {
                desenharTileEstatica(dados.imagem, tile[2], tile[0], tile[1], dados.tilesets);
            }
        }
    }

    private static void desenharTileEstatica(GreenfootImage destino, int gid, int tx, int ty, ArrayList<Tileset> tilesets) {
        Tileset escolhido = null;
        for (Tileset t : tilesets) if (t.firstGid <= gid && (escolhido == null || t.firstGid > escolhido.firstGid)) escolhido = t;
        if (escolhido == null) return;
        GreenfootImage tileFinal = obterTile(escolhido, gid);
        if (tileFinal == null) return;
        destino.drawImage(tileFinal, (tx - MIN_X) * TILE, (ty - MIN_Y) * TILE);
    }

    /** Obtém o tile recortado e ampliado, carregando-o quando necessário. */
    private static GreenfootImage obterTile(Tileset tileset, int gid) {
        String chave = tileset.imagePath + "#" + gid;
        GreenfootImage tileFinal = tilesCache.get(chave);
        if (tileFinal != null) return tileFinal;
        try {
            GreenfootImage folha = folhasCache.get(tileset.imagePath);
            if (folha == null) {
                folha = new GreenfootImage(tileset.imagePath);
                folhasCache.put(tileset.imagePath, folha);
            }
            int local = gid - tileset.firstGid;
            GreenfootImage tile = new GreenfootImage(SOURCE_TILE, SOURCE_TILE);
            tile.drawImage(folha, -(local % tileset.columns) * SOURCE_TILE,
                    -(local / tileset.columns) * SOURCE_TILE);
            tileFinal = new GreenfootImage(tile);
            tileFinal.scale(TILE, TILE);
            tilesCache.put(chave, tileFinal);
            return tileFinal;
        } catch (Exception erro) {
            System.out.println("Erro no tile " + gid + ": " + erro.getMessage());
            return null;
        }
    }
}

