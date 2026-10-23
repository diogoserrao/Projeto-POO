import greenfoot.*;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Responsável pela informação textual apresentada sobre o mundo de jogo.
 *
 * <p>Consulta os jogadores e o mundo, mas não altera a lógica da partida.
 * Guarda os últimos textos para redesenhar apenas o que mudou e usa
 * {@link IntSupplier} para obter a fase atual sem duplicar o seu estado.</p>
 */
public class Hud {
    private final MyWorld mundo;
    private final IntSupplier faseAtual;
    private final int maxFases;
    private final String[] ultimasVidas;
    private final String[] ultimosInventarios;
    private String ultimaFase;
    private String ultimaPontuacao;
    private String ultimosControlos;
    private String ultimasInteracoes;

    /** Liga o HUD ao mundo e à fonte que fornece a fase atual. */
    public Hud(MyWorld mundo, IntSupplier faseAtual, int maxFases) {
        this.mundo = mundo;
        this.faseAtual = faseAtual;
        this.maxFases = maxFases;
        this.ultimasVidas = new String[mundo.getJogadores().size()];
        this.ultimosInventarios = new String[mundo.getJogadores().size()];
    }

    /** Obtém os jogadores atuais sem duplicar a coleção mantida pelo mundo. */
    private List<Player> jogadores() {
        return mundo.getJogadores();
    }

    /** Atualiza todos os textos do HUD sem conhecer a logica do jogo. */
    /**
     * Atualiza vidas, fase, pontuação, controlos e indicação de interação.
     * Só chama {@code showText} quando o texto correspondente mudou, reduzindo
     * trabalho visual sem alterar a informação apresentada.
     */
    public void atualizar() {
        for (int i = 0; i < jogadores().size(); i++) {
            Player jogador = jogadores().get(i);
            String vidas = formatarVidas(jogador, i);
            if (!vidas.equals(ultimasVidas[i])) {
                mundo.showText(vidas, posicaoJogador(i), 22);
                ultimasVidas[i] = vidas;
            }
            String inventario = "Pedras: " + jogador.getPedras()
                    + "  Chaves: " + jogador.getNumeroChaves();
            if (!inventario.equals(ultimosInventarios[i])) {
                mundo.showText(inventario, posicaoJogador(i), 42);
                ultimosInventarios[i] = inventario;
            }
        }

        String fase = "FASE " + faseAtual.getAsInt() + " / " + maxFases;
        if (!fase.equals(ultimaFase)) {
            mundo.showText(fase, mundo.getWidth() / 2, 22);
            ultimaFase = fase;
        }

        String pontuacao = "Pontuação: " + mundo.getPontuacao();
        if (!pontuacao.equals(ultimaPontuacao)) {
            mundo.showText(pontuacao, mundo.getWidth() / 2, 44);
            ultimaPontuacao = pontuacao;
        }

        String controlos = "";
        for (int i = 0; i < jogadores().size(); i++) {
            if (!controlos.isEmpty()) {
                controlos += " | ";
            }
            controlos += formatarControlos(jogadores().get(i), i);
        }
        String interacoes = "";
        for (int i = 0; i < jogadores().size(); i++) {
            Interagivel interagivel = jogadores().get(i).getInteragivelPerto();
            if (interagivel != null) {
                if (!interacoes.isEmpty()) {
                    interacoes += " | ";
                }
                interacoes += "P" + (i + 1) + ": "
                        + interagivel.getDescricao(jogadores().get(i));
            }
        }

        if (!controlos.equals(ultimosControlos)) {
            mundo.showText(controlos, mundo.getWidth() / 2, mundo.getHeight() - 18);
            ultimosControlos = controlos;
        }
        if (!interacoes.equals(ultimasInteracoes)) {
            mundo.showText(interacoes, mundo.getWidth() / 2, mundo.getHeight() - 42);
            ultimasInteracoes = interacoes;
        }
    }

    private String formatarControlos(Player jogador, int indiceJogador) {
        Controlos controlos = jogador.getControlos();
        String nome;
        if ("a".equals(controlos.getEsquerda())
                && "d".equals(controlos.getDireita())
                && "w".equals(controlos.getCima())
                && "s".equals(controlos.getBaixo())) {
            nome = "WASD";
        } else if ("left".equals(controlos.getEsquerda())
                && "right".equals(controlos.getDireita())
                && "up".equals(controlos.getCima())
                && "down".equals(controlos.getBaixo())) {
            nome = "Setas";
        } else {
            nome = "Cima: " + controlos.getCima()
                    + ", Baixo: " + controlos.getBaixo()
                    + ", Esquerda: " + controlos.getEsquerda()
                    + ", Direita: " + controlos.getDireita();
        }
        return nome + ", Lançar: " + controlos.getLancar()
                + ": Jogador " + (indiceJogador + 1);
    }

    private int posicaoJogador(int indice) {
        if (indice == 0) {
            return 150;
        }
        if (indice == jogadores().size() - 1) {
            return mundo.getWidth() - 150;
        }
        return mundo.getWidth() * (indice + 1) / (jogadores().size() + 1);
    }

    /** Usa a mesma representacao visual de vidas existente no jogo. */
    private String formatarVidas(Player jogador, int indiceJogador) {
        String coracoes = "";
        for (int i = 0; i < jogador.getMaxVidas(); i++) {
            coracoes += i < jogador.getVidas() ? "[X]" : "[ ]";
            if (i < jogador.getMaxVidas() - 1) {
                coracoes += " ";
            }
        }

        return "P" + (indiceJogador + 1) + ": " + coracoes;
    }
}
