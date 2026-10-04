/** Configuração das teclas usadas por um jogador. */
public class Controlos {
    private final String esquerda;
    private final String direita;
    private final String cima;
    private final String baixo;

    public Controlos(String esquerda, String direita, String cima, String baixo) {
        this.esquerda = esquerda;
        this.direita = direita;
        this.cima = cima;
        this.baixo = baixo;
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
}
