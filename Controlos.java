/**
 * Encapsula o mapeamento das teclas de um jogador.
 *
 * <p>Esta classe separa a configuração dos controlos da lógica de {@link Player}:
 * o jogador consulta os comandos através dos getters, sem depender de teclas
 * fixas. Os construtores sobrecarregados permitem usar os controlos predefinidos
 * para interagir e lançar pedras ou substituí-los quando necessário.</p>
 */
public class Controlos {
    private final String esquerda;
    private final String direita;
    private final String cima;
    private final String baixo;
    private final String interagir;
    private final String lancar;

    public Controlos(String esquerda, String direita, String cima, String baixo) {
        this(esquerda, direita, cima, baixo, "e");
    }

    public Controlos(String esquerda, String direita, String cima, String baixo,
            String interagir) {
        this(esquerda, direita, cima, baixo, interagir, "q");
    }

    /** Cria a configuração completa, incluindo interação e lançamento. */
    public Controlos(String esquerda, String direita, String cima, String baixo,
            String interagir, String lancar) {
        this.esquerda = esquerda;
        this.direita = direita;
        this.cima = cima;
        this.baixo = baixo;
        this.interagir = interagir;
        this.lancar = lancar;
    }

    /** Devolve a tecla de movimento para a esquerda. */
    public String getEsquerda() {
        return esquerda;
    }

    /** Devolve a tecla de movimento para a direita. */
    public String getDireita() {
        return direita;
    }

    /** Devolve a tecla de movimento para cima. */
    public String getCima() {
        return cima;
    }

    /** Devolve a tecla de movimento para baixo. */
    public String getBaixo() {
        return baixo;
    }

    /** Devolve a tecla de interação. */
    public String getInteragir() {
        return interagir;
    }

    /** Devolve a tecla usada para lançar pedras. */
    public String getLancar() {
        return lancar;
    }
}
