import greenfoot.*;

/** Movimento aleatorio com direcoes persistentes e verificacao de colisao. */
public class Aleatorio implements Movimento {
    private static final int VELOCIDADE_POR_DEFEITO = 2;
    private static final int DURACAO_DIRECAO_POR_DEFEITO = 18;
    private static final int FRAMES_ENTRE_TENTATIVAS = 8;

    private final int velocidade;
    private final int duracaoDirecao;
    private int contadorDirecao;

    public Aleatorio() {
        this(VELOCIDADE_POR_DEFEITO, DURACAO_DIRECAO_POR_DEFEITO);
    }

    /** Permite ajustar a velocidade e a fluidez de cada tipo de inimigo. */
    public Aleatorio(int velocidade, int duracaoDirecao) {
        this.velocidade = Math.max(1, velocidade);
        this.duracaoDirecao = Math.max(1, duracaoDirecao);
    }

    @Override
    public boolean mover(Inimigo inimigo) {
        Direcao direcaoAtual = inimigo.getDirecao();
        if (contadorDirecao > 0 && podeAvancar(inimigo, direcaoAtual)) {
            contadorDirecao--;
            return avancar(inimigo, direcaoAtual);
        }

        Direcao[] direcoes = Direcao.values();
        int inicio = Greenfoot.getRandomNumber(direcoes.length);
        for (int i = 0; i < direcoes.length; i++) {
            Direcao candidata = direcoes[(inicio + i) % direcoes.length];
            if (podeAvancar(inimigo, candidata)) {
                inimigo.definirDirecaoMovimento(candidata);
                contadorDirecao = duracaoDirecao - 1;
                return avancar(inimigo, candidata);
            }
        }

        // Num canto fechado, evita escolher uma direcao impossivel todos os frames.
        contadorDirecao = FRAMES_ENTRE_TENTATIVAS;
        return false;
    }

    private boolean podeAvancar(Inimigo inimigo, Direcao direcao) {
        int novoX = inimigo.getX() + direcao.getDx() * velocidade;
        int novoY = inimigo.getY() + direcao.getDy() * velocidade;
        return inimigo.podeMoverPara(novoX, novoY);
    }

    private boolean avancar(Inimigo inimigo, Direcao direcao) {
        inimigo.moverPara(inimigo.getX() + direcao.getDx() * velocidade,
                inimigo.getY() + direcao.getDy() * velocidade);
        return true;
    }
}
