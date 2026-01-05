package ai.strategies;

import java.util.*;
import ai.BattleContext;
import javamon.battle.Turn;

public interface Strategy {
    /**
     * Decides the next turn based on battle state.
     * @param context Read-only wrapper around battle state
     * @return The Turn to execute (move or switch)
     */
    Turn decideTurn(BattleContext context);
    
    /**
     * Optional: Called when bot's Pokemon faints (must switch).
     * Default: picks first available non-fainted Pokemon.
     */
    default Turn decideForceSwitch(BattleContext context) {
        List<Integer> validSwitches = context.getValidSwitchTargets();
        if (validSwitches.isEmpty()) {
            throw new IllegalStateException("No valid switches available!");
        }
        return new Turn(context.getBotPlayerNumber(), validSwitches.get(0));
    }
}