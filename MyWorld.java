import greenfoot.*;

public class MyWorld extends World
{
    private DungeonMap mapa;

    public MyWorld()
    {
        super(640, 480, 1, false);
        mapa = new DungeonMap();
        setBackground(mapa.getImagem());

        Zag jogador1 = new Zag();
        Zig jogador2 = new Zig();

        addObject(jogador1, 376, 120);
        addObject(jogador2, 424, 120);
    }

    public boolean podeMover(Actor jogador, int novoX, int novoY) {
        int raioX = Math.max(8, jogador.getImage().getWidth() / 2 - 8);
        int raioY = Math.max(8, jogador.getImage().getHeight() / 2 - 8);
        return !mapa.estaBloqueado(novoX - raioX, novoY - raioY) &&
               !mapa.estaBloqueado(novoX + raioX, novoY - raioY) &&
               !mapa.estaBloqueado(novoX - raioX, novoY + raioY) &&
               !mapa.estaBloqueado(novoX + raioX, novoY + raioY);
    }
}
