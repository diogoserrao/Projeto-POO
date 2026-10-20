----------------------------------------------------------------------------
Este é o arquivo README do projeto. Você deve descrever aqui o seu projeto.
Informe ao usuário (alguém que não sabe nada sobre este projeto!) tudo que
ele/ela precisa saber. Os comentários devem incluir, pelo menos:
------------------------------------------------------------------------

TÍTULO DO PROJETO: Dungeon Escape
OBJETIVO DO PROJETO: Jogo cooperativo para dois jogadores explorarem três fases.
VERSÃO ou DATA: 23/09/2026
COMO INICIAR O PROJETO: Abrir a pasta no Greenfoot, compilar e executar MyWorld.
AUTORES: Projeto POO
INSTRUÇÕES PARA O USUÁRIO:
Zag usa WASD e Zig usa as setas. E/Enter servem para interagir e atacar.
Os ataques só acertam inimigos pelas costas. A colisão é calculada pela grelha do mapa,
considerando apenas a zona dos pés dos personagens. Pressione N para avançar
para a fase seguinte. Os mapas usados são:
images/mundo/Mundo1/Tiled_files/Dungeon1.tmx e
images/mundo/Mundo2/Tiled_files/Dungeon2.tmx.

INIMIGOS:
Cada fase contém inimigos parados e inimigos patrulheiros. Os patrulheiros
movem-se apenas dentro da zona configurada para eles e mudam de direção
quando chegam ao limite ou encontram uma parede. Todos usam sprites da pasta
images/jogador/PNG, que contém os sprites transparentes do mesmo conjunto
de personagens, e respeitam a mesma colisão do mapa usada pelos jogadores.
No Tiled, cada objeto Spawn_N cria um inimigo; se existir também uma
Patrulha_N com o mesmo número, esse inimigo patrulha essa zona. Um Spawn_N
sem Patrulha_N cria um inimigo parado.
