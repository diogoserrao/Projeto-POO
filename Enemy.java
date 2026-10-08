import greenfoot.*;
import java.awt.Rectangle;
import java.util.List;

/** Um inimigo delega o movimento para a implementacao recebida. */
public class Enemy extends Personagem {
    private static final int RAIO_VISAO = 100;
    private static final double ANGULO_VISAO = 60.0;
    private static final int COOLDOWN_ATAQUE = 60;
    private static final boolean MOSTRAR_VISAO = true;
    private final Movimento movimento;
    private int contadorAtaque;

    public Enemy(int personagem, Movimento movimento) {
        super(personagem);
        if (movimento == null) {
            throw new IllegalArgumentException("O movimento nao pode ser null");
        }
        this.movimento = movimento;
    }

    @Override
    public void act() {
        boolean estaAMover = movimento.mover(this);
        atualizarAnimacao(estaAMover);
        atualizarAtaque();
    }

    private void atualizarAtaque() {
        if (contadorAtaque > 0) {
            contadorAtaque--;
        }

        Player alvo = encontrarAlvo();
        if (alvo != null && contadorAtaque == 0) {
            alvo.perderVida();
            contadorAtaque = COOLDOWN_ATAQUE;
        }
    }

    /** Escolhe o jogador vivo, visivel e mais proximo. */
    private Player encontrarAlvo() {
        World mundo = getWorld();
        if (mundo == null) {
            return null;
        }

        List<Player> jogadores = mundo.getObjects(Player.class);
        Player maisProximo = null;
        double menorDistancia = Double.MAX_VALUE;

        for (Player jogador : jogadores) {
            if (jogador == null || !jogador.estaVivo()
                    || jogador.getNivel() != getNivel()) {
                continue;
            }

            double distancia = distanciaA(jogador);
            if (distancia <= RAIO_VISAO
                    && distancia < menorDistancia
                    && estaNoConeDeVisao(jogador)
                    && temLinhaDeVisao(jogador)) {
                maisProximo = jogador;
                menorDistancia = distancia;
            }
        }

        return maisProximo;
    }

    private double distanciaA(Player jogador) {
        double dx = jogador.getX() - getX();
        double dy = jogador.getY() - getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private boolean estaNoConeDeVisao(Player jogador) {
        double dx = jogador.getX() - getX();
        double dy = jogador.getY() - getY();
        double comprimento = Math.sqrt(dx * dx + dy * dy);

        if (comprimento == 0) {
            return true;
        }

        double direcaoX = 0;
        double direcaoY = 0;
        switch (getDirecao()) {
        case DIREITA:
            direcaoX = 1;
            break;
        case ESQUERDA:
            direcaoX = -1;
            break;
        case CIMA:
            direcaoY = -1;
            break;
        default:
            direcaoY = 1;
            break;
        }

        double produto = (dx * direcaoX + dy * direcaoY) / comprimento;
        double angulo = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, produto))));
        return angulo <= ANGULO_VISAO / 2.0;
    }

    /** Percorre a linha entre os atores e consulta a colisao do nivel atual. */
    private boolean temLinhaDeVisao(Player jogador) {
        DungeonMap mapa = getMapa();
        if (mapa == null) {
            return false;
        }

        double distancia = distanciaA(jogador);
        int passos = Math.max(1, (int) Math.ceil(distancia / 4.0));
        for (int passo = 1; passo < passos; passo++) {
            double fracao = (double) passo / passos;
            int x = (int) Math.round(getX() + (jogador.getX() - getX()) * fracao);
            int y = (int) Math.round(getY() + (jogador.getY() - getY()) * fracao);
            if (mapa.estaBloqueado(new Rectangle(x, y, 1, 1), getNivel())) {
                return false;
            }
        }
        return true;
    }

    /** Acrescenta o cone a seguir ao frame normal, sem afetar colisoes. */
    @Override
    protected void mostrarFrame() {
        super.mostrarFrame();
        if (!MOSTRAR_VISAO) {
            return;
        }

        GreenfootImage personagem = getImage();
        int tamanho = RAIO_VISAO * 2 + 4;
        GreenfootImage imagem = new GreenfootImage(tamanho, tamanho);
        int centro = tamanho / 2;
        int[] xs = new int[23];
        int[] ys = new int[23];
        xs[0] = centro;
        ys[0] = centro;
        double direcao = direcaoEmGraus();
        for (int i = 0; i <= 21; i++) {
            double graus = Math.toRadians(direcao - ANGULO_VISAO / 2.0
                    + ANGULO_VISAO * i / 21.0);
            xs[i + 1] = centro + (int) Math.round(Math.cos(graus) * RAIO_VISAO);
            ys[i + 1] = centro + (int) Math.round(Math.sin(graus) * RAIO_VISAO);
        }
        imagem.setColor(new greenfoot.Color(255, 220, 70, 45));
        imagem.fillPolygon(xs, ys, xs.length);
        imagem.setColor(new greenfoot.Color(255, 220, 70, 150));
        imagem.drawPolygon(xs, ys, xs.length);
        imagem.drawImage(personagem, centro - personagem.getWidth() / 2,
                centro - personagem.getHeight() / 2);
        setImage(imagem);
    }

    private double direcaoEmGraus() {
        switch (getDirecao()) {
        case DIREITA:
            return 0;
        case CIMA:
            return -90;
        case ESQUERDA:
            return 180;
        default:
            return 90;
        }
    }

    public boolean podeMoverPara(int x, int y) {
        DungeonMap mapa = getMapa();
        return mapa != null && !mapa.estaBloqueado(getPes(x, y), getNivel());
    }

    public void moverPara(int x, int y) {
        setLocation(x, y);
    }

    public void definirDirecaoMovimento(int direcao) {
        definirDirecao(direcao);
    }
}
