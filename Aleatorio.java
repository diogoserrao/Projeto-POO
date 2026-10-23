import greenfoot.*;

/**
 * Estratégia de movimento aleatório com direção persistente.
 *
 * <p>Escolhe uma nova direção apenas quando a anterior termina ou fica
 * bloqueada. Assim, o inimigo não muda de direção a cada frame e todas as
 * tentativas continuam a respeitar {@link Inimigo#podeMoverPara(int, int)}.
 * A estratégia é composta por {@link Inimigo}, em vez de ser duplicada nas
 * subclasses.</p>
 */
public class Aleatorio implements Movimento {
    private static final int VELOCIDADE_POR_DEFEITO = EscalaJogo.px(2);
    private static final int DURACAO_DIRECAO_POR_DEFEITO = 18;
    private static final int FRAMES_ENTRE_TENTATIVAS = 8;

    private final int velocidade;
    private final int duracaoDirecao;
    private int contadorDirecao;

    public Aleatorio() {
        this(VELOCIDADE_POR_DEFEITO, DURACAO_DIRECAO_POR_DEFEITO);
    }

    /**
     * Cria a estratégia com velocidade e duração de direção configuráveis.
     * Valores inferiores a um são limitados para manter passos válidos.
     *
     * @param velocidade pixels percorridos por frame
     * @param duracaoDirecao número de frames durante os quais uma direção pode persistir
     */
    public Aleatorio(int velocidade, int duracaoDirecao) {
        this.velocidade = Math.max(1, velocidade);
        this.duracaoDirecao = Math.max(1, duracaoDirecao);
    }

    @Override
    public boolean mover(Inimigo inimigo) {
        // A direção atual é mantida enquanto ainda for possível avançar.
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
