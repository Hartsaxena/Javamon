package ai.strategies;

import java.util.*;
import ai.BattleContext;
import javamon.battle.Turn;

/**
 * This strategy chooses a random move for each turn. Pokemon are never switched when not necessary.
 * When a pokemon faints, this strategy always sends out the next pokemon in the team.
 */
public class RandomStrategy implements Strategy {
    private final Random rng;
    
    public RandomStrategy() {
        this.rng = new Random();
    }
    
    public RandomStrategy(long seed) {
        this.rng = new Random(seed);
    }
    
    @Override
    public Turn decideTurn(BattleContext context) {
        List<String> validMoves = context.getValidMoves();
        String chosenMove = validMoves.get(rng.nextInt(validMoves.size()));
        return new Turn(context.getPlayerN(), chosenMove);
    }
}