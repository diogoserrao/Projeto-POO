import greenfoot.*;

/** Implementacao de teste: escolhe uma direcao nova em cada tentativa. */
public class Aleatorio implements Movimento {
    private static final int VELOCIDADE = 2;

    @Override
    public boolean mover(Enemy inimigo) {
        Direcao[] direcoes = Direcao.values();
        Direcao direcao = direcoes[Greenfoot.getRandomNumber(direcoes.length)];
        int novoX = inimigo.getX();
        int novoY = inimigo.getY();

        novoX += direcao.getDx() * VELOCIDADE;
        novoY += direcao.getDy() * VELOCIDADE;

        inimigo.definirDirecaoMovimento(direcao);
        if (!inimigo.podeMoverPara(novoX, novoY)) {
            return false;
        }

        inimigo.moverPara(novoX, novoY);
        return true;
    }
}
