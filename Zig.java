import greenfoot.*;

public class Zig extends Actor {
    private static final int LARGURA_FRAME = 16;
    private static final int ALTURA_FRAME = 16;

    private static final int FRAMES_ANDAR = 8;
    private static final int FRAMES_PARADO = 4;

    private GreenfootImage parado;
    private GreenfootImage andarBaixo;
    private GreenfootImage andarCima;
    private GreenfootImage andarEsquerda;
    private GreenfootImage andarDireita;

    private String direcao = "baixo";

    private int frame = 0;
    private int contadorAnimacao = 0;

    private int velocidade = 3;

    public Zig() {
        parado = new GreenfootImage(
                "soldier_02_spritesheet_idle.png");

        andarBaixo = new GreenfootImage(
                "soldier_02_spritesheet_walking_down.png");

        andarCima = new GreenfootImage(
                "soldier_02_spritesheet_walking_up.png");

        andarEsquerda = new GreenfootImage(
                "soldier_02_spritesheet_walking_left.png");

        andarDireita = new GreenfootImage(
                "soldier_02_spritesheet_walking_right.png");

        mostrarFrame(parado, 0);
    }

    public void act() {
        boolean movimento = false;

        if (Greenfoot.isKeyDown("up")) {
            moverParaCima();
            movimento = true;
        }

        if (Greenfoot.isKeyDown("down")) {
            moverParaBaixo();
            movimento = true;
        }

        if (Greenfoot.isKeyDown("left")) {
            moverParaEsquerda();
            movimento = true;
        }

        if (Greenfoot.isKeyDown("right")) {
            moverParaDireita();
            movimento = true;
        }

        if (!movimento) {
            animarParado();
        }
    }

    private void moverParaCima() {
        tentarMover(getX(), getY() - velocidade);
        direcao = "cima";
        animarAndar();
    }

    private void moverParaBaixo() {
        tentarMover(getX(), getY() + velocidade);
        direcao = "baixo";
        animarAndar();
    }

    private void moverParaEsquerda() {
        tentarMover(getX() - velocidade, getY());
        direcao = "esquerda";
        animarAndar();
    }

    private void moverParaDireita() {
        tentarMover(getX() + velocidade, getY());
        direcao = "direita";
        animarAndar();
    }

    private void tentarMover(int x, int y) {
        MyWorld mundo = (MyWorld)getWorld();
        if (mundo != null && mundo.podeMover(this, x, y)) setLocation(x, y);
    }

    private void animarAndar() {
        contadorAnimacao++;

        if (contadorAnimacao >= 5) {
            contadorAnimacao = 0;

            frame++;

            if (frame >= FRAMES_ANDAR) {
                frame = 0;
            }

            if (direcao.equals("cima")) {
                mostrarFrame(andarCima, frame);
            } else if (direcao.equals("baixo")) {
                mostrarFrame(andarBaixo, frame);
            } else if (direcao.equals("esquerda")) {
                mostrarFrame(andarEsquerda, frame);
            } else if (direcao.equals("direita")) {
                mostrarFrame(andarDireita, frame);
            }
        }
    }

    private void animarParado() {
        contadorAnimacao++;

        if (contadorAnimacao >= 10) {
            contadorAnimacao = 0;

            frame++;

            if (frame >= FRAMES_PARADO) {
                frame = 0;
            }

            mostrarFrame(parado, frame);
        }
    }

    private void mostrarFrame(GreenfootImage spritesheet, int frame) {
        GreenfootImage imagem = new GreenfootImage(
                LARGURA_FRAME,
                ALTURA_FRAME);

        imagem.drawImage(
                spritesheet,
                -frame * LARGURA_FRAME,
                0);

        imagem.scale(48, 48);

        setImage(imagem);
    }
}
