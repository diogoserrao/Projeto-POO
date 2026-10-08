import greenfoot.*;

/** Implementacao de teste: escolhe uma direcao nova em cada tentativa. */
public class Aleatorio implements Movimento {
    private static final int VELOCIDADE = 2;

    @Override
    public boolean mover(Enemy inimigo) {
        int direcao = Greenfoot.getRandomNumber(4);
        int novoX = inimigo.getX();
        int novoY = inimigo.getY();

        switch (direcao) {
        case Personagem.CIMA:
            novoY -= VELOCIDADE;
            break;
        case Personagem.ESQUERDA:
            novoX -= VELOCIDADE;
            break;
        case Personagem.DIREITA:
            novoX += VELOCIDADE;
            break;
        default:
            novoY += VELOCIDADE;
            break;
        }

        inimigo.definirDirecaoMovimento(direcao);
        if (!inimigo.podeMoverPara(novoX, novoY)) {
            return false;
        }

        inimigo.moverPara(novoX, novoY);
        return true;
    }
}
