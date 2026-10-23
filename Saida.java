import java.awt.Rectangle;

/**
 * Porta final da fase, sujeita à cooperação dos dois jogadores.
 *
 * <p>Reutiliza a validação e o consumo da chave de {@link Porta}, mas só
 * conclui a abertura quando o mundo permite avançar e ambos os jogadores se
 * encontram na zona da saída.</p>
 */
public class Saida extends Porta {
    public Saida(String nome, Rectangle area) {
        super(nome, area);
    }

    @Override
    public boolean abrir(Player jogador, DungeonMap mapa, MyWorld mundo) {
        // A saída acrescenta as condições globais à validação normal da porta:
        // a fase não pode estar em transição e os dois jogadores têm de estar
        // dentro da zona definida pelo mapa.
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
            // A área de proximidade de cada jogador é comparada com a saída;
            // basta um jogador ficar fora para impedir a cooperação.
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
