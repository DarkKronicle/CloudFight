package darkkronicle.github.io.cloudfight.game.games;

import darkkronicle.github.io.cloudfight.CloudFight;
import darkkronicle.github.io.cloudfight.game.GameState;
import darkkronicle.github.io.cloudfight.game.maps.ObjectiveMap;

public class ObjectiveGame extends GameInstance<ObjectiveMap> {

    public ObjectiveGame(CloudFight plugin, GameContainer container) {
        super(plugin, container);
    }

    @Override
    public GameState.Mode getType() {
        return GameState.Mode.OBJECTIVE;
    }


}
