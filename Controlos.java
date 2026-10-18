/** Configuração das teclas usadas por um jogador. */
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

    public Controlos(String esquerda, String direita, String cima, String baixo,
            String interagir, String lancar) {
        this.esquerda = esquerda;
        this.direita = direita;
        this.cima = cima;
        this.baixo = baixo;
        this.interagir = interagir;
        this.lancar = lancar;
    }

    public String getEsquerda() {
        return esquerda;
    }

    public String getDireita() {
        return direita;
    }

    public String getCima() {
        return cima;
    }

    public String getBaixo() {
        return baixo;
    }

    public String getInteragir() {
        return interagir;
    }

    public String getLancar() {
        return lancar;
    }
}
