import greenfoot.*;
import java.util.List;
import java.util.function.IntSupplier;

/** Responsavel pelos textos e informacao visual do mundo. */
public class Hud {
    private final World mundo;
    private final List<Player> jogadores;
    private final IntSupplier faseAtual;
    private final int maxFases;

    public Hud(World mundo, List<Player> jogadores,
            IntSupplier faseAtual, int maxFases) {
        this.mundo = mundo;
        this.jogadores = jogadores;
        this.faseAtual = faseAtual;
        this.maxFases = maxFases;
    }

    /** Atualiza todos os textos do HUD sem conhecer a logica do jogo. */
    public void atualizar() {
        for (int i = 0; i < jogadores.size(); i++) {
            Player jogador = jogadores.get(i);
            mundo.showText(formatarVidas(jogador, i),
                    posicaoJogador(i), 25);
        }

        mundo.showText(
                "FASE " + faseAtual.getAsInt() + " / " + maxFases,
                mundo.getWidth() / 2,
                25);

        String controlos = "WASD: Jogador 1 | Setas: Jogador 2 | N: Próxima Fase";
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

        mundo.showText(controlos, mundo.getWidth() / 2,
                mundo.getHeight() - 20);
        mundo.showText(interacoes, mundo.getWidth() / 2,
                mundo.getHeight() - 44);
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

        return "P" + (indiceJogador + 1) + ": " + coracoes;
    }
}
