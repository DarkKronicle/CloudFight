package darkkronicle.github.io.cloudfight.game;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.game.games.CaptureGame;
import darkkronicle.github.io.cloudfight.game.games.GameContainer;
import darkkronicle.github.io.cloudfight.game.games.GameInstance;
import darkkronicle.github.io.cloudfight.game.games.ObjectiveGame;
import darkkronicle.github.io.cloudfight.utility.Bag;

import java.util.ArrayList;

public class GameState {

    public static class RandomMode {

        public static Bag<GameState.Mode> modeBag = null;

        public RandomMode() {
            if (modeBag == null) {
                ArrayList<Mode> modes = new ArrayList<>();
                for (GameState.Mode mode : GameState.Mode.values()) {
                    if (mode != GameState.Mode.RANDOM) {
                        modes.add(mode);
                    }
                }
                System.out.println(modes.size());
                modeBag = Bag.fromCollection(modes);
            }
        }

        public Mode get() {
            return RandomMode.modeBag.get();
        }
    }

    public enum State {
        READY,
        COUNTDOWN,
        STARTED,
        RESETTING
    }

    public enum Mode {
        CAPTURE(CaptureGame::new),
        OBJECTIVE(ObjectiveGame::new),
        RANDOM(null);

        public GameLaunch supplier;

        Mode(GameLaunch supplier) {
            this.supplier = supplier;
        }

        public interface GameLaunch {

            GameInstance newGame(CloudFight plugin, GameContainer container);

        }
    }
}
