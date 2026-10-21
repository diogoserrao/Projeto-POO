import java.awt.Rectangle;

/** Porta especial que conclui a fase apenas com os dois jogadores juntos. */
public class Saida extends Porta {
    public Saida(String nome, Rectangle area) {
        super(nome, area);
    }

    @Override
    public boolean abrir(Player jogador, DungeonMap mapa, MyWorld mundo) {
        if (jogador == null || estaAberta()) return estaAberta();
        if (mundo == null || !mundo.podePedirProximaFase()) return false;
        if (!jogadoresEstaoJuntos(jogador, mundo)) return false;
        if (!consumirChaveCorreta(jogador)) return false;

        concluirAbertura(mapa);
        mundo.pedirProximaFase();
        return true;
    }

    /** Usa a mesma zona de proximidade definida pela área da saída. */
    protected boolean jogadoresEstaoJuntos(Player jogador, MyWorld mundo) {
        if (mundo == null || mundo.getJogadores().size() < 2) return false;
        for (Player outro : mundo.getJogadores()) {
            Rectangle proximidade = new Rectangle(outro.getX() - 24, outro.getY() - 24, 48, 48);
            if (!getArea().intersects(proximidade)) return false;
        }
        return true;
    }

    @Override
    public void interagir(Player jogador, DungeonMap mapa, MyWorld mundo) {
        abrir(jogador, mapa, mundo);
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
