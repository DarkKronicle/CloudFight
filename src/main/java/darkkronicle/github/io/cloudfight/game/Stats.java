package darkkronicle.github.io.cloudfight.game;

import darkkronicle.github.io.cloudfight.game.games.GameContainer;
import darkkronicle.github.io.cloudfight.utility.Counter;
import org.bukkit.entity.Player;

public class Stats {
    private final GameContainer container;
    private Counter<Player> blocks = new Counter<>();

    public Stats(GameContainer gameContainer) {
        this.container = gameContainer;
    }

    public void incrementBlocks(Player player) {
        blocks.increment(player);
    }

    public int getBlocks(Player player) {
        return blocks.get(player);
    }

}
