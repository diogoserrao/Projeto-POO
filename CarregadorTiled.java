import greenfoot.*;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    private File ficheiroMapa;
    private final GreenfootImage imagem;
    private GreenfootImage imagemBase;
    private final ArrayList<Interagivel> interagiveis = new ArrayList<Interagivel>();
    private final ArrayList<Armadilha> armadilhas = new ArrayList<Armadilha>();
    private final ArrayList<Chave> chaves = new ArrayList<Chave>();
    private final ArrayList<Alavanca> alavancas = new ArrayList<Alavanca>();
    private final Map<Integer, TileAnimation> animacoesTiles = new HashMap<Integer, TileAnimation>();
    private final Map<String, Rectangle> percursos = new HashMap<String, Rectangle>();
    private final ArrayList<Porta> portas = new ArrayList<Porta>();
    private final Map<Interagivel, ArrayList<int[]>> tilesDosObjetos = new HashMap<Interagivel, ArrayList<int[]>>();
    private final ArrayList<ConfiguracaoInimigo> configuracoesInimigos = new ArrayList<ConfiguracaoInimigo>();
    private final ArrayList<Point> posicoesPedras = new ArrayList<Point>();
    private final ArrayList<Point> spawnsJogadores = new ArrayList<Point>();
    private final ArrayList<Rectangle> zonasVazio = new ArrayList<Rectangle>();
    private final ArrayList<Plataforma> plataformas = new ArrayList<Plataforma>();
    private final boolean[][] colisaoComum = new boolean[MAP_WIDTH][MAP_HEIGHT];
    private static final Map<String, GreenfootImage> folhasCache = new HashMap<String, GreenfootImage>();
    private static final Map<String, GreenfootImage> tilesCache = new HashMap<String, GreenfootImage>();
    private final ArrayList<Tileset> tilesetsAtuais = new ArrayList<Tileset>();

    static final class Tileset {
        private int firstGid;
        private int columns;
        private String imagePath;

        Tileset() { }

        Tileset(Tileset original) {
            firstGid = original.firstGid;
            columns = original.columns;
            imagePath = original.imagePath;
        }
    }

    static final class TileAnimation {
        private int[] frames;
        private int[] duracoes;

        TileAnimation() { }

        TileAnimation(TileAnimation original) {
            frames = original.frames == null ? null : original.frames.clone();
            duracoes = original.duracoes == null ? null : original.duracoes.clone();
        }
    }

    public CarregadorTiled(int fase) {
        this.fase = Math.max(1, Math.min(2, fase));
        imagem = new GreenfootImage(WORLD_WIDTH, WORLD_HEIGHT);
        imagem.setColor(new greenfoot.Color(13, 17, 24));
        imagem.fill();
        carregar();
        prepararRecursosDasArmadilhas();
        if (MOSTRAR_COLISAO) depurarColisao();
        aplicarAmbiente(imagem, 0, 0, imagem.getWidth(), imagem.getHeight());
        imagemBase = new GreenfootImage(imagem);
        redesenharObjetos(getDados());
    }

    public DadosMapa getDados() {
        return new DadosMapa(imagem, imagemBase, interagiveis, armadilhas, portas, tilesDosObjetos,
                configuracoesInimigos, posicoesPedras, spawnsJogadores, zonasVazio, plataformas, colisaoComum,
                tilesetsAtuais, animacoesTiles, fase);
    }

    public static final class DadosMapa {
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
        private final ArrayList<Tileset> tilesets;
        private final Map<Integer, TileAnimation> animacoesTiles;
        private final int fase;

        DadosMapa(GreenfootImage imagem, GreenfootImage imagemBase,
                ArrayList<Interagivel> interagiveis, ArrayList<Armadilha> armadilhas, ArrayList<Porta> portas,
                Map<Interagivel, ArrayList<int[]>> tilesDosObjetos,
                ArrayList<ConfiguracaoInimigo> configuracoesInimigos,
                ArrayList<Point> posicoesPedras,
                ArrayList<Point> spawnsJogadores, ArrayList<Rectangle> zonasVazio,
                ArrayList<Plataforma> plataformas, boolean[][] colisaoComum,
                ArrayList<Tileset> tilesets, Map<Integer, TileAnimation> animacoesTiles, int fase) {
            this.imagem = imagem;
            this.imagemBase = imagemBase;
            this.interagiveis = interagiveis;
            this.armadilhas = armadilhas;
            this.portas = portas;
            this.tilesDosObjetos = tilesDosObjetos;
            this.configuracoesInimigos = configuracoesInimigos;
            this.posicoesPedras = posicoesPedras;
            this.spawnsJogadores = spawnsJogadores;
            this.zonasVazio = zonasVazio;
            this.plataformas = plataformas;
            this.colisaoComum = colisaoComum;
            this.tilesets = tilesets;
            this.animacoesTiles = animacoesTiles;
            this.fase = fase;
        }

        /** Devolve uma cópia para impedir alterações à imagem interna. */
        public GreenfootImage getImagem() { return new GreenfootImage(imagem); }

        /** Devolve uma cópia para impedir alterações à imagem interna. */
        public GreenfootImage getImagemBase() { return new GreenfootImage(imagemBase); }

        public List<Interagivel> getInteragiveis() {
            return Collections.unmodifiableList(interagiveis);
        }

        public List<Armadilha> getArmadilhas() {
            return Collections.unmodifiableList(armadilhas);
        }

        public List<Porta> getPortas() {
            return Collections.unmodifiableList(portas);
        }

        public Map<Interagivel, List<int[]>> getTilesDosObjetos() {
            Map<Interagivel, List<int[]>> copia =
                    new HashMap<Interagivel, List<int[]>>();
            for (Map.Entry<Interagivel, ArrayList<int[]>> entrada
                    : tilesDosObjetos.entrySet()) {
                ArrayList<int[]> tiles = new ArrayList<int[]>();
                for (int[] tile : entrada.getValue()) {
                    tiles.add(tile.clone());
                }
                copia.put(entrada.getKey(), Collections.unmodifiableList(tiles));
            }
            return Collections.unmodifiableMap(copia);
        }

        public List<ConfiguracaoInimigo> getConfiguracoesInimigos() {
            return Collections.unmodifiableList(configuracoesInimigos);
        }

        public List<Point> getPosicoesPedras() {
            return copiarPontos(posicoesPedras);
        }

        public List<Point> getSpawnsJogadores() {
            return copiarPontos(spawnsJogadores);
        }

        public List<Rectangle> getZonasVazio() {
            ArrayList<Rectangle> copia = new ArrayList<Rectangle>();
            for (Rectangle zona : zonasVazio) copia.add(new Rectangle(zona));
            return Collections.unmodifiableList(copia);
        }

        public List<Plataforma> getPlataformas() {
            return Collections.unmodifiableList(plataformas);
        }

        public boolean[][] getColisaoComum() {
            boolean[][] copia = new boolean[colisaoComum.length][];
            for (int i = 0; i < colisaoComum.length; i++) {
                copia[i] = colisaoComum[i].clone();
            }
            return copia;
        }

        public List<Tileset> getTilesets() {
            ArrayList<Tileset> copia = new ArrayList<Tileset>();
            for (Tileset tileset : tilesets) {
                copia.add(new Tileset(tileset));
            }
            return Collections.unmodifiableList(copia);
        }

        public Map<Integer, TileAnimation> getAnimacoesTiles() {
            Map<Integer, TileAnimation> copia = new HashMap<Integer, TileAnimation>();
            for (Map.Entry<Integer, TileAnimation> entrada
                    : animacoesTiles.entrySet()) {
                copia.put(entrada.getKey(), new TileAnimation(entrada.getValue()));
            }
            return Collections.unmodifiableMap(copia);
        }

        private static List<Point> copiarPontos(List<Point> pontos) {
            ArrayList<Point> copia = new ArrayList<Point>();
            for (Point ponto : pontos) {
                copia.add(new Point(ponto));
            }
            return Collections.unmodifiableList(copia);
        }

        public int getFase() { return fase; }
    }

    private void carregar() {
        File ficheiro = new File("images/mundo/Mundo" + fase + "/Tiled_files/Dungeon" + fase + ".tmx");
        ficheiroMapa = ficheiro;
        if (!ficheiro.isFile()) {
            throw new IllegalStateException("Mapa Tiled em falta: " + ficheiro.getPath()
                    + " (fase " + fase + ")");
        }
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ficheiro);
            Element mapa = doc.getDocumentElement();
            ArrayList<Tileset> tilesets = lerTilesets(mapa, ficheiro.getParentFile());
            tilesetsAtuais.addAll(tilesets);
            lerAnimacoesTiles(mapa, tilesets);
            NodeList filhos = mapa.getChildNodes();
            // Regista primeiro todos os objetos, independentemente da ordem no TMX.
            for (int i = 0; i < filhos.getLength(); i++) {
                Node node = filhos.item(i);
                if (!(node instanceof Element) || !"objectgroup".equals(node.getNodeName())) continue;
                Element grupo = (Element) node;
                String nome = grupo.getAttribute("name").trim().toLowerCase();
                if (nome.equals("interacoesobjetos")) lerInteracoes(grupo);
                else if (nome.equals("inimigos")) lerInimigos(grupo);
                else if (nome.equals("pedras") || nome.equals("pedra")) lerPedras(grupo);
                else if (nome.equals("jogadores")) lerSpawnJogadores(grupo);
                else if (nome.equals("vazio")) lerVazios(grupo);
                else if (nome.equals("percurso")) lerPercursos(grupo);
            }
            lerPlataformas(filhos);
            associarChavesEPortas();
            for (int i = 0; i < filhos.getLength(); i++) {
                Node node = filhos.item(i);
                if (!(node instanceof Element)) continue;
                Element elemento = (Element) node;
                String nome = elemento.getAttribute("name").toLowerCase();
                if ("objectgroup".equals(node.getNodeName())) {
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
            boolean ponto = !objeto.hasAttribute("width") || !objeto.hasAttribute("height");
            if (nome.toLowerCase().startsWith("armadilha")) {
                registarArmadilha(new Armadilha(nome, converterParaMundo(x, y, largura, altura, ponto)));
                continue;
            }
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
            if (tipo.startsWith("armadilha")) registarArmadilha(new Armadilha(nome, area));
            else if (tipo.startsWith("bau")) registarInteragivel(new Bau(nome, area));
            else if (tipo.startsWith("chave")) {
                Chave chave = new Chave(nome, area);
                chaves.add(chave);
                registarInteragivel(chave);
            } else if (tipo.startsWith("porta")) {
                Porta porta = tipo.equals("porta_3")
                        ? new Saida(nome, area)
                        : new Porta(nome, area);
                portas.add(porta);
                registarInteragivel(porta);
            } else if (tipo.startsWith("alavanca")) {
                Alavanca alavanca = new Alavanca(nome, area);
                alavancas.add(alavanca);
                registarInteragivel(alavanca);
            } else if (tipo.startsWith("plataforma")) {
                // Criadas em lerPlataformas, depois de todos os percursos serem lidos.
            } else System.out.println("Objeto interativo desconhecido ignorado: " + nome);
        }
    }

    private void lerVazios(Element grupo) {
        NodeList objetos = grupo.getElementsByTagName("object");
        for (int i = 0; i < objetos.getLength(); i++) {
            Element objeto = (Element) objetos.item(i);
            if (!objeto.hasAttribute("width") || !objeto.hasAttribute("height")) continue;
            zonasVazio.add(converterParaMundo(Double.parseDouble(objeto.getAttribute("x")),
                    Double.parseDouble(objeto.getAttribute("y")),
                    Double.parseDouble(objeto.getAttribute("width")),
                    Double.parseDouble(objeto.getAttribute("height")), false));
        }
    }

    private void lerPercursos(Element grupo) {
        NodeList objetos = grupo.getElementsByTagName("object");
        for (int i = 0; i < objetos.getLength(); i++) {
            Element objeto = (Element) objetos.item(i);
            if (!objeto.hasAttribute("width") || !objeto.hasAttribute("height")) continue;
            String nome = objeto.getAttribute("name").trim();
            String id = nome.toLowerCase().startsWith("percurso_")
                    ? nome.substring("percurso_".length()) : "1";
            percursos.put(id, converterParaMundo(Double.parseDouble(objeto.getAttribute("x")),
                    Double.parseDouble(objeto.getAttribute("y")),
                    Double.parseDouble(objeto.getAttribute("width")),
                    Double.parseDouble(objeto.getAttribute("height")), false));
        }
    }

    private void lerPlataformas(NodeList filhos) {
        for (int i = 0; i < filhos.getLength(); i++) {
            Node node = filhos.item(i);
            if (!(node instanceof Element) || !"objectgroup".equals(node.getNodeName())) continue;
            NodeList objetos = ((Element) node).getElementsByTagName("object");
            for (int j = 0; j < objetos.getLength(); j++) {
                Element objeto = (Element) objetos.item(j);
                String nome = objeto.getAttribute("name").trim();
                String tipo = nome.toLowerCase();
                if (!tipo.startsWith("plataforma") || !objeto.hasAttribute("x") || !objeto.hasAttribute("y")) continue;
                boolean ponto = !objeto.hasAttribute("width") || !objeto.hasAttribute("height");
                Rectangle area = converterParaMundo(Double.parseDouble(objeto.getAttribute("x")),
                        Double.parseDouble(objeto.getAttribute("y")), SOURCE_TILE, SOURCE_TILE, ponto);
                if (tipo.startsWith("plataformamovel")) {
                    String id = nome.contains("_") ? nome.substring(nome.indexOf('_') + 1) : "1";
                    Rectangle percurso = percursos.get(id);
                    if (percurso == null && percursos.size() == 1) percurso = percursos.values().iterator().next();
                    if (percurso != null) plataformas.add(new PlataformaMovel(area, percurso));
                    else System.out.println("Aviso: plataforma móvel sem percurso: " + nome);
                } else if (tipo.startsWith("plataformatemporaria")) {
                    plataformas.add(new PlataformaTemporaria(area));
                }
            }
        }
    }

    /** Extrai o tile de caixa de madeira já existente no tileset Objects. */
    static GreenfootImage criarImagemPlataforma(int largura, int altura) {
        try {
            GreenfootImage folha = new GreenfootImage("images/mundo/Mundo1/Tiled_files/Objects.png");
            GreenfootImage tile = new GreenfootImage(SOURCE_TILE, SOURCE_TILE);
            int local = 128; // GID 5707 no mapa: tile de caixa do tileset Objects.
            tile.drawImage(folha, -(local % 24) * SOURCE_TILE, -(local / 24) * SOURCE_TILE);
            tile.scale(Math.max(1, largura), Math.max(1, altura));
            return tile;
        } catch (Exception erro) {
            throw new IllegalStateException("Não foi possível reutilizar a caixa do tileset Objects", erro);
        }
    }

    private void associarChavesEPortas() {
        Map<String, Porta> porId = new HashMap<String, Porta>();
        for (Porta porta : portas) porId.put(porta.getIdentificador(), porta);
        for (Chave chave : chaves) {
            Porta porta = porId.get(chave.getTipo());
            if (porta == null) {
                System.out.println("Aviso: chave sem porta correspondente: chave_" + chave.getTipo());
            } else {
                chave.associarPorta(porta);
            }
        }
        for (Porta porta : portas) {
            boolean encontrada = false;
            for (Chave chave : chaves)
                if (chave.getTipo().equals(porta.getIdentificador())) encontrada = true;
            if (!encontrada) System.out.println("Aviso: porta sem chave correspondente: porta_" + porta.getIdentificador());
        }
        Map<String, Alavanca> alavancasPorId = new HashMap<String, Alavanca>();
        for (Alavanca alavanca : alavancas)
            alavancasPorId.put(alavanca.getIdentificador(), alavanca);
        for (Armadilha armadilha : armadilhas) {
            if (!armadilha.podeSerDesativada()) continue;
            String numero = armadilha.getIdentificador().substring("armadilha_".length());
            Alavanca alavanca = alavancasPorId.get("alavanca_" + numero);
            if (alavanca == null) {
                System.out.println("Aviso: armadilha sem alavanca correspondente: " + armadilha.getIdentificador());
            } else {
                alavanca.associarArmadilha(armadilha);
            }
        }
    }

    private void registarInteragivel(Interagivel objeto) {
        if (objeto == null || interagiveis.contains(objeto)) return;
        interagiveis.add(objeto);
        tilesDosObjetos.put(objeto, new ArrayList<int[]>());
    }

    private void registarArmadilha(Armadilha armadilha) {
        if (armadilha == null || armadilhas.contains(armadilha)) return;
        armadilhas.add(armadilha);
        registarInteragivel(armadilha);
    }

    private Rectangle converterParaMundo(double x, double y, double largura, double altura, boolean ponto) {
        int mundoX = (int) Math.round((x * TILE / SOURCE_TILE) - (MIN_X * TILE));
        int mundoY = (int) Math.round((y * TILE / SOURCE_TILE) - (MIN_Y * TILE));
        int mundoLargura = ponto ? TAMANHO_OBJETO_PONTO : (int) Math.round(largura * TILE / SOURCE_TILE);
        int mundoAltura = ponto ? TAMANHO_OBJETO_PONTO : (int) Math.round(altura * TILE / SOURCE_TILE);
        return new Rectangle(ponto ? mundoX - mundoLargura / 2 : mundoX, ponto ? mundoY - mundoAltura / 2 : mundoY, mundoLargura, mundoAltura);
    }

    private ArrayList<Tileset> lerTilesets(Element mapa, File pastaReferencia) {
        ArrayList<Tileset> resultado = new ArrayList<Tileset>();
        NodeList nodes = mapa.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (!(node instanceof Element) || !"tileset".equals(node.getNodeName())) continue;
            Element referencia = (Element) node;
            Element ts = referencia;
            File pastaTileset = pastaReferencia;
            Document documentoTileset = null;
            if (referencia.hasAttribute("source")) {
                File ficheiroTileset = resolverFicheiro(pastaReferencia, referencia.getAttribute("source"));
                exigirFicheiro(ficheiroTileset, "tileset referenciado pelo mapa");
                try {
                    documentoTileset = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ficheiroTileset);
                } catch (Exception erro) {
                    throw new IllegalStateException("Não foi possível ler o tileset: " + ficheiroTileset.getPath(), erro);
                }
                ts = documentoTileset.getDocumentElement();
                pastaTileset = ficheiroTileset.getParentFile();
            }
            Element img = (Element) ts.getElementsByTagName("image").item(0);
            if (img == null || !img.hasAttribute("source")) {
                throw new IllegalStateException("Tileset sem imagem: "
                        + (referencia.hasAttribute("source") ? referencia.getAttribute("source") : "incorporado")
                        + " no mapa " + ficheiroMapa.getPath());
            }
            Tileset t = new Tileset();
            t.firstGid = Integer.parseInt(referencia.getAttribute("firstgid"));
            t.columns = ts.hasAttribute("columns") ? Integer.parseInt(ts.getAttribute("columns")) : 0;
            File caminhoImagem = resolverFicheiro(pastaTileset, img.getAttribute("source"));
            exigirFicheiro(caminhoImagem, "imagem do tileset");
            t.imagePath = caminhoImagem.getPath().replace('\\', '/');
            if (t.columns <= 0) {
                throw new IllegalStateException("Tileset sem número de colunas: " + caminhoImagem.getPath());
            }
            resultado.add(t);
        }
        return resultado;
    }

    private static File resolverFicheiro(File pastaReferencia, String caminho) {
        File indicado = new File(caminho.replace('/', File.separatorChar));
        return indicado.isAbsolute() ? indicado : new File(pastaReferencia, caminho);
    }

    private static void exigirFicheiro(File ficheiro, String tipo) {
        if (!ficheiro.isFile()) {
            throw new IllegalStateException("Falta " + tipo + ": " + ficheiro.getPath());
        }
    }

    private void lerAnimacoesTiles(Element mapa, ArrayList<Tileset> tilesets) {
        NodeList nodes = mapa.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (!(nodes.item(i) instanceof Element) || !"tileset".equals(nodes.item(i).getNodeName())) continue;
            Element referencia = (Element) nodes.item(i);
            Element ts = referencia;
            if (referencia.hasAttribute("source")) {
                File ficheiroTileset = resolverFicheiro(ficheiroMapa.getParentFile(), referencia.getAttribute("source"));
                exigirFicheiro(ficheiroTileset, "tileset referenciado pelo mapa");
                try {
                    ts = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ficheiroTileset).getDocumentElement();
                } catch (Exception erro) {
                    throw new IllegalStateException("Não foi possível ler as animações do tileset: "
                            + ficheiroTileset.getPath(), erro);
                }
            }
            int firstGid = Integer.parseInt(referencia.getAttribute("firstgid"));
            NodeList tiles = ts.getElementsByTagName("tile");
            for (int j = 0; j < tiles.getLength(); j++) {
                Element tile = (Element) tiles.item(j);
                Element anim = (Element) tile.getElementsByTagName("animation").item(0);
                if (anim == null) continue;
                NodeList frames = anim.getElementsByTagName("frame");
                TileAnimation resultado = new TileAnimation();
                resultado.frames = new int[frames.getLength()];
                resultado.duracoes = new int[frames.getLength()];
                for (int k = 0; k < frames.getLength(); k++) {
                    Element frame = (Element) frames.item(k);
                    resultado.frames[k] = firstGid + Integer.parseInt(frame.getAttribute("tileid"));
                    int milissegundos = Integer.parseInt(frame.getAttribute("duration"));
                    resultado.duracoes[k] = Math.max(1, (milissegundos + 8) / 16);
                }
                animacoesTiles.put(firstGid + Integer.parseInt(tile.getAttribute("id")), resultado);
            }
        }
    }

    private void desenharCamada(Element layer, ArrayList<Tileset> tilesets) {
        boolean armadilhasLayer = "traps".equalsIgnoreCase(layer.getAttribute("name").trim());
        boolean objetos = "objects".equalsIgnoreCase(layer.getAttribute("name"))
                || "objects2".equalsIgnoreCase(layer.getAttribute("name"));
        boolean paredes = "walls".equalsIgnoreCase(layer.getAttribute("name"));
        for (int[] t : listarTiles(layer)) {
            if (armadilhasLayer && guardarTileDeArmadilha(t)) continue;
            if ((objetos && guardarTileDeObjeto(t, false))
                    || (paredes && guardarTileDeObjeto(t, true))) continue;
            desenharTile(t[2], t[0], t[1], tilesets);
        }
    }

    private boolean guardarTileDeArmadilha(int[] tile) {
        int px = (tile[0] - MIN_X) * TILE;
        int py = (tile[1] - MIN_Y) * TILE;
        Rectangle tileArea = new Rectangle(px, py, TILE, TILE);
        Armadilha escolhida = null;
        int maiorIntersecao = 0;
        for (Armadilha armadilha : armadilhas) {
            Rectangle intersecao = armadilha.getArea().intersection(tileArea);
            int areaIntersecao = Math.max(0, intersecao.width) * Math.max(0, intersecao.height);
            if (areaIntersecao > maiorIntersecao) {
                maiorIntersecao = areaIntersecao;
                escolhida = armadilha;
            }
        }
        if (escolhida == null) return false;
        TileAnimation animacao = animacoesTiles.get(tile[2]);
        if (animacao != null) escolhida.configurarAnimacao(animacao.duracoes);
        ArrayList<int[]> tiles = tilesDosObjetos.get(escolhida);
        tiles.add(new int[] { tile[0], tile[1], tile[2] });
        return true;
    }

    /** Guarda tiles de objetos normais ou, especificamente, de portas nas paredes. */
    private boolean guardarTileDeObjeto(int[] tile, boolean apenasPortas) {
        Tileset tileset = escolherTileset(tile[2], tilesetsAtuais);
        Interagivel objetoMaisProximo = null;
        int menorDistancia = Integer.MAX_VALUE;

        for (Map.Entry<Interagivel, ArrayList<int[]>> entrada : tilesDosObjetos.entrySet()) {
            Interagivel objeto = entrada.getKey();
            if (apenasPortas != portas.contains(objeto)) continue;
            if (objeto.temAnimacao() && (tileset == null
                    || !new File(tileset.imagePath).getName().equalsIgnoreCase("doors_lever_chest_animation.png"))) continue;

            int ancoraX = (int) Math.floor(objeto.getArea().getCenterX() / TILE + MIN_X);
            int ancoraY = (int) Math.floor(objeto.getArea().getCenterY() / TILE + MIN_Y);
            int distancia = Math.abs(tile[0] - ancoraX) + Math.abs(tile[1] - ancoraY);
            if (Math.abs(tile[0] - ancoraX) <= objeto.getRaioSprite()
                    && Math.abs(tile[1] - ancoraY) <= objeto.getRaioSprite()
                    && distancia <= objeto.getRaioSprite() * 2
                    && distancia < menorDistancia) {
                objetoMaisProximo = objeto;
                menorDistancia = distancia;
            }
        }

        if (objetoMaisProximo == null) return false;

        ArrayList<int[]> tiles = tilesDosObjetos.get(objetoMaisProximo);
        for (int[] existente : tiles) {
            if (existente[0] == tile[0] && existente[1] == tile[1]) return true;
        }
        tiles.add(new int[] { tile[0], tile[1], tile[2] });
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
        Tileset escolhido = escolherTileset(gid, tilesets);
        if (escolhido == null) return;
        try {
            int px = (tx - MIN_X) * TILE, py = (ty - MIN_Y) * TILE;
            if (px < 0 || py < 0 || px >= WORLD_WIDTH || py >= WORLD_HEIGHT) return;
            GreenfootImage tileFinal = obterTile(escolhido, gid);
            if (tileFinal == null) return;
            imagem.drawImage(tileFinal, px, py);
        } catch (Exception erro) {
            throw new IllegalStateException("Erro ao desenhar o tile " + gid
                    + " usando a imagem " + escolhido.imagePath, erro);
        }
    }

    /** Compõe uma vez os quadros dos atores, sem tocar na imagem de fundo. */
    private void prepararRecursosDasArmadilhas() {
        for (Armadilha armadilha : armadilhas) {
            ArrayList<int[]> tiles = tilesDosObjetos.get(armadilha);
            if (tiles == null || tiles.isEmpty()) continue;

            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            int numeroQuadros = 1;
            int[] duracoes = null;
            for (int[] tile : tiles) {
                minX = Math.min(minX, tile[0]);
                minY = Math.min(minY, tile[1]);
                maxX = Math.max(maxX, tile[0]);
                maxY = Math.max(maxY, tile[1]);
                TileAnimation animacao = animacoesTiles.get(tile[2]);
                if (animacao != null && animacao.frames.length > 0) {
                    numeroQuadros = Math.max(numeroQuadros, animacao.frames.length);
                    if (duracoes == null) duracoes = animacao.duracoes.clone();
                }
            }
            if (duracoes != null) armadilha.configurarAnimacao(duracoes);

            int largura = (maxX - minX + 1) * TILE;
            int altura = (maxY - minY + 1) * TILE;
            GreenfootImage[] imagens = new GreenfootImage[numeroQuadros];
            for (int quadro = 0; quadro < numeroQuadros; quadro++) {
                imagens[quadro] = new GreenfootImage(largura, altura);
                for (int[] tile : tiles) {
                    int gid = tile[2];
                    TileAnimation animacao = animacoesTiles.get(gid);
                    if (animacao != null && animacao.frames.length > 0)
                        gid = animacao.frames[quadro % animacao.frames.length];
                    Tileset tileset = escolherTileset(gid, tilesetsAtuais);
                    GreenfootImage imagemTile = tileset == null ? null : obterTile(tileset, gid);
                    if (imagemTile != null)
                        imagens[quadro].drawImage(imagemTile,
                                (tile[0] - minX) * TILE, (tile[1] - minY) * TILE);
                }
            }
            int centroX = (minX - MIN_X) * TILE + largura / 2;
            int centroY = (minY - MIN_Y) * TILE + altura / 2;
            armadilha.configurarRecursosVisuais(imagens, centroX, centroY);
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

    static void redesenharObjetos(DadosMapa dados) {
        dados.imagem.drawImage(dados.imagemBase, 0, 0);
        for (Map.Entry<Interagivel, ArrayList<int[]>> entrada : dados.tilesDosObjetos.entrySet()) {
            Interagivel objeto = entrada.getKey();
            if (!objeto.deveDesenharTiles()) continue;
            if (!objeto.estaVisivel()) continue;
            for (int[] tile : entrada.getValue()) {
                int gid = tile[2];
                if (objeto.temAnimacao()) {
                    gid += objeto.getFrameVisual() * 30;
                }
                desenharTileEstatica(dados.imagem, gid, tile[0], tile[1], dados.tilesets);
            }
        }
    }

    private static void desenharTileEstatica(GreenfootImage destino, int gid, int tx, int ty, ArrayList<Tileset> tilesets) {
        Tileset escolhido = escolherTileset(gid, tilesets);
        if (escolhido == null) return;
        GreenfootImage tileFinal = obterTile(escolhido, gid);
        if (tileFinal == null) return;
        destino.drawImage(tileFinal, (tx - MIN_X) * TILE, (ty - MIN_Y) * TILE);
    }

    private static Tileset escolherTileset(int gid, ArrayList<Tileset> tilesets) {
        Tileset escolhido = null;
        for (Tileset t : tilesets) if (t.firstGid <= gid
                && (escolhido == null || t.firstGid > escolhido.firstGid)) escolhido = t;
        return escolhido;
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
            throw new IllegalStateException("Erro ao carregar o tile " + gid
                    + " a partir de " + tileset.imagePath, erro);
        }
    }
}
