import greenfoot.*;
import java.util.List;
import java.util.function.IntSupplier;

/** Responsavel pelos textos e informacao visual do mundo. */
public class Hud {
    private final World mundo;
    private final List<Player> jogadores;
    private final IntSupplier faseAtual;
    private final int maxFases;
    private final String[] ultimasVidas;
    private String ultimaFase;
    private String ultimosControlos;
    private String ultimasInteracoes;

    public Hud(World mundo, List<Player> jogadores,
            IntSupplier faseAtual, int maxFases) {
        this.mundo = mundo;
        this.jogadores = jogadores;
        this.faseAtual = faseAtual;
        this.maxFases = maxFases;
        this.ultimasVidas = new String[jogadores.size()];
    }

    /** Atualiza todos os textos do HUD sem conhecer a logica do jogo. */
    public void atualizar() {
        for (int i = 0; i < jogadores.size(); i++) {
            Player jogador = jogadores.get(i);
            String vidas = formatarVidas(jogador, i);
            if (!vidas.equals(ultimasVidas[i])) {
                mundo.showText(vidas, posicaoJogador(i), 25);
                ultimasVidas[i] = vidas;
            }
        }

        String fase = "FASE " + faseAtual.getAsInt() + " / " + maxFases;
        if (!fase.equals(ultimaFase)) {
            mundo.showText(fase, mundo.getWidth() / 2, 25);
            ultimaFase = fase;
        }

        String controlos = "";
        for (int i = 0; i < jogadores.size(); i++) {
            if (!controlos.isEmpty()) {
                controlos += " | ";
            }
            controlos += formatarControlos(jogadores.get(i), i);
        }
        if (!controlos.isEmpty()) {
            controlos += " | ";
        }
        controlos += "N: Próxima Fase";
        String interacoes = "";
        for (int i = 0; i < jogadores.size(); i++) {
            Interagivel interagivel = jogadores.get(i).getInteragivelPerto();
            if (interagivel != null) {
                if (!interacoes.isEmpty()) {
                    interacoes += " | ";
                }
                interacoes += "P" + (i + 1) + ": "
                        + interagivel.getDescricao(jogadores.get(i));
            }
        }

        if (!controlos.equals(ultimosControlos)) {
            mundo.showText(controlos, mundo.getWidth() / 2, mundo.getHeight() - 20);
            ultimosControlos = controlos;
        }
        if (!interacoes.equals(ultimasInteracoes)) {
            mundo.showText(interacoes, mundo.getWidth() / 2, mundo.getHeight() - 44);
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
        if (indice == jogadores.size() - 1) {
            return mundo.getWidth() - 150;
        }
        return mundo.getWidth() * (indice + 1) / (jogadores.size() + 1);
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

        return "P" + (indiceJogador + 1) + ": " + coracoes
                + " Pedras: " + jogador.getPedras()
                + " Chaves: " + jogador.getNumeroChaves();
    }
}
