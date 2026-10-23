import greenfoot.Actor;
import greenfoot.Color;
import greenfoot.GreenfootImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Ator visual que desenha o cone de visão de um inimigo.
 *
 * <p>A imagem é criada uma vez por combinação de raio e ângulo e depois é
 * reutilizada para as quatro direções. Esta classe só representa a camada
 * visual; a deteção efetiva permanece em {@link Inimigo}.</p>
 */
public final class ConeVisao extends Actor {
    private static final Color COR_PREENCHIMENTO = new Color(255, 220, 70, 45);
    private static final Color COR_CONTORNO = new Color(255, 220, 70, 150);
    private static final Map<String, GreenfootImage[]> CACHE =
            new HashMap<String, GreenfootImage[]>();

    /** Cria o ator visual com uma imagem inicial orientada para baixo. */
    public ConeVisao(int raio, double angulo) {
        setImage(obterImagem(raio, angulo, Direcao.BAIXO));
    }

    /** Atualiza a geometria visual quando o inimigo muda de direção ou perfil. */
    public void atualizar(int raio, double angulo, Direcao direcao) {
        setImage(obterImagem(raio, angulo, direcao));
    }

    /** Reutiliza a imagem em cache para a combinação geométrica recebida. */
    private static GreenfootImage obterImagem(int raio, double angulo, Direcao direcao) {
        String chave = raio + ":" + Double.toString(angulo);
        GreenfootImage[] imagens = CACHE.get(chave);
        if (imagens == null) {
            imagens = criarImagens(raio, angulo);
            CACHE.put(chave, imagens);
        }
        return imagens[direcao.ordinal()];
    }

    /** Desenha as quatro orientações do setor circular do cone de visão. */
    private static GreenfootImage[] criarImagens(int raio, double angulo) {
        GreenfootImage[] resultado = new GreenfootImage[Direcao.values().length];
        int tamanho = raio * 2 + 4;
        int centro = tamanho / 2;
        int[] xs = new int[23];
        int[] ys = new int[23];
        double meio = angulo / 2.0;

        for (Direcao direcao : Direcao.values()) {
            double orientacao = Math.toDegrees(Math.atan2(direcao.getDy(), direcao.getDx()));
            xs[0] = centro;
            ys[0] = centro;
            for (int i = 0; i <= 21; i++) {
                double graus = Math.toRadians(orientacao - meio + angulo * i / 21.0);
                xs[i + 1] = centro + (int) Math.round(Math.cos(graus) * raio);
                ys[i + 1] = centro + (int) Math.round(Math.sin(graus) * raio);
            }

            GreenfootImage imagem = new GreenfootImage(tamanho, tamanho);
            imagem.setColor(COR_PREENCHIMENTO);
            imagem.fillPolygon(xs, ys, xs.length);
            imagem.setColor(COR_CONTORNO);
            imagem.drawPolygon(xs, ys, xs.length);
            resultado[direcao.ordinal()] = imagem;
        }
        return resultado;
    }

    @Override
    public void act() {
        // A camada é atualizada pelo Inimigo; não tem lógica própria.
    }
}
