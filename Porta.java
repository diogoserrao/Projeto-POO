import greenfoot.Greenfoot;
import java.awt.Rectangle;

/** Representa uma porta trancada definida no mapa Tiled. */
public class Porta implements Interagivel {
    private final String identificador;
    private final Rectangle area;
    private boolean aberta;

    public Porta(String nome, Rectangle area) {
        String normalizado = nome == null ? "" : nome.trim().toLowerCase();
        int separador = normalizado.indexOf('_');
        identificador = separador >= 0 && separador + 1 < normalizado.length()
                ? normalizado.substring(separador + 1) : normalizado;
        this.area = area;
    }

    public String getIdentificador() { return identificador; }
    public boolean estaAberta() { return aberta; }
    public boolean estaFechada() { return !aberta; }

    /** A Porta 3 é a saída cooperativa da fase. */
    private boolean eSaidaCooperativa() {
        return "3".equals(identificador);
    }

    /** Abre apenas com a chave desta porta e consome-a do jogador. */
    public boolean abrir(Player jogador) {
        if (aberta) return true;
        if (jogador == null) return false;
        Chave chave = jogador.encontrarChave(this);
        if (chave == null || !chave.abre(this)) return false;

        if (eSaidaCooperativa()) {
            if (!jogadoresEstaoJuntos(jogador)) return false;
            ((MyWorld) jogador.getMundo()).pedirProximaFase();
            return true;
        }

        if (!jogador.consumirChave(chave)) return false;
        aberta = true;
        // Só é emitido depois de a chave ser consumida e a porta abrir.
        Greenfoot.playSound("win.wav");
        DungeonMap mapa = jogador.getMapa();
        if (mapa != null) mapa.redesenharObjetos();
        return true;
    }

    private boolean jogadoresEstaoJuntos(Player jogador) {
        if (!(jogador.getMundo() instanceof MyWorld)) return false;
        MyWorld mundo = (MyWorld) jogador.getMundo();
        if (mundo.getJogadores().size() < 2) return false;
        for (Player outro : mundo.getJogadores()) {
            Rectangle proximidade = new Rectangle(outro.getX() - 24, outro.getY() - 24, 48, 48);
            if (!area.intersects(proximidade)) return false;
        }
        return true;
    }

    @Override public Rectangle getArea() { return area; }
    @Override public boolean temAnimacao() { return true; }
    @Override public int getFrameVisual() {
        return aberta ? ObjetoInteragivel.FRAME_FINAL : 0;
    }
    @Override public String getDescricao() {
        return aberta ? "Porta aberta" : "Porta trancada (chave " + identificador + ")";
    }
    @Override public String getDescricao(Player jogador) {
        if (aberta) return "Porta aberta";
        if (eSaidaCooperativa() && (jogador == null || !jogador.temChave(identificador))) {
            return "Precisas da chave";
        }
        return eSaidaCooperativa()
                ? "Premir " + jogador.getTeclaInteracao().toUpperCase()
                    + " para sair (os dois jogadores devem estar juntos)"
                : "Porta trancada: precisa da chave " + identificador;
    }
    @Override public void interagir(Player jogador) { abrir(jogador); }
}
