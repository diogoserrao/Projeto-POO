import java.awt.Rectangle;

/** Porta especial que conclui a fase apenas com os dois jogadores juntos. */
public class Saida extends Porta {
    public Saida(String nome, Rectangle area) {
        super(nome, area);
    }

    @Override
    public boolean abrir(Player jogador) {
        if (jogador == null || estaAberta()) return estaAberta();
        MyWorld mundo = jogador.getMundo();
        if (mundo == null || !mundo.podePedirProximaFase()) return false;
        if (!jogadoresEstaoJuntos(jogador)) return false;
        if (!consumirChaveCorreta(jogador)) return false;

        concluirAbertura(jogador);
        mundo.pedirProximaFase();
        return true;
    }

    /** Usa a mesma zona de proximidade definida pela área da saída. */
    protected boolean jogadoresEstaoJuntos(Player jogador) {
        MyWorld mundo = jogador == null ? null : jogador.getMundo();
        if (mundo == null || mundo.getJogadores().size() < 2) return false;
        for (Player outro : mundo.getJogadores()) {
            Rectangle proximidade = new Rectangle(outro.getX() - 24, outro.getY() - 24, 48, 48);
            if (!getArea().intersects(proximidade)) return false;
        }
        return true;
    }

    @Override
    public String getDescricao(Player jogador) {
        if (estaAberta()) return "Saída aberta";
        String indicacaoTecla = jogador == null
                ? ""
                : "Premir " + jogador.getTeclaInteracao().toUpperCase() + " para ";
        return indicacaoTecla + "sair (os dois jogadores devem estar juntos)";
    }
}
