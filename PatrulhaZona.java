import greenfoot.*;
import java.awt.Rectangle;

/** Move um inimigo para a frente e para tras dentro de uma zona. */
public class PatrulhaZona implements Movimento {
    private static final int TEMPO_PAUSA_NA_EXTREMIDADE = 8;
    private static final int DISTANCIA_DESACELERACAO = 4;

    private enum EstadoPatrulha {
        MOVENDO,
        PAUSANDO
    }

    private final Rectangle zona;
    private final boolean horizontal;
    private final int velocidade;
    private int sentido = 1;
    private EstadoPatrulha estadoPatrulha = EstadoPatrulha.MOVENDO;
    private int contadorPausa;

    public PatrulhaZona(Rectangle zona, int velocidade) {
        if (zona == null) {
            throw new IllegalArgumentException("A zona de patrulha nao pode ser null");
        }

        this.zona = zona;
        this.horizontal = zona.width >= zona.height;
        this.velocidade = Math.max(1, velocidade);
    }

    @Override
    public boolean mover(Enemy inimigo) {
        return patrulharZona(inimigo);
    }

    private boolean patrulharZona(Enemy inimigo) {
        int limite = horizontal
                ? (sentido > 0 ? zona.x + zona.width : zona.x)
                : (sentido > 0 ? zona.y + zona.height : zona.y);
        return patrulharAte(inimigo, limite);
    }

    /** Move ate ao extremo sem o ultrapassar e gere a pausa da viragem. */
    private boolean patrulharAte(Enemy inimigo, int limite) {
        if (estadoPatrulha == EstadoPatrulha.PAUSANDO) {
            contadorPausa--;
            if (contadorPausa <= 0) {
                sentido *= -1;
                estadoPatrulha = EstadoPatrulha.MOVENDO;
                inimigo.definirDirecaoMovimento(direcaoDoSentido());
            }
            return false;
        }

        int novoX = inimigo.getX();
        int novoY = inimigo.getY();
        int atual = horizontal ? novoX : novoY;
        int distancia = Math.abs(limite - atual);

        inimigo.definirDirecaoMovimento(direcaoDoSentido());

        if (distancia == 0) {
            iniciarPausa();
            return false;
        }

        int passo = velocidade;
        if (distancia <= DISTANCIA_DESACELERACAO) {
            passo = Math.max(1, velocidade / 2);
        }
        passo = Math.min(passo, distancia);

        if (horizontal) {
            novoX += sentido * passo;
        } else {
            novoY += sentido * passo;
        }

        if (!inimigo.podeMoverPara(novoX, novoY)) {
            iniciarPausa();
            return false;
        }

        inimigo.moverPara(novoX, novoY);

        if (passo == distancia) {
            iniciarPausa();
            return false;
        }
        return true;
    }

    private void iniciarPausa() {
        estadoPatrulha = EstadoPatrulha.PAUSANDO;
        contadorPausa = TEMPO_PAUSA_NA_EXTREMIDADE;
    }

    private int direcaoDoSentido() {
        if (horizontal) {
            return sentido > 0 ? Personagem.DIREITA : Personagem.ESQUERDA;
        }
        return sentido > 0 ? Personagem.BAIXO : Personagem.CIMA;
    }
}
