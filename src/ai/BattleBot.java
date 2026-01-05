package ai;

import ai.strategies.*;
import javamon.battle.*;
import javamon.entities.*;

public class BattleBot {
    private final Strategy strategy;
    private final int playerNumber;
    
    public BattleBot(Strategy strategy, int playerNumber) {
        this.strategy = strategy;
        this.playerNumber = playerNumber;
    }
    
    /**
     * Makes a decision and queues it in the battle engine.
     */
    public void queueTurn(BattleEngine engine) {
        BattleContext context = new BattleContext(engine, playerNumber);
        
        // Check if forced to switch (Pokemon fainted)
        Pokemon active = context.getMyActivePokemon();
        if (active.isKnockedOut()) {
            Turn switchTurn = strategy.decideForceSwitch(context);
            engine.queueTurn(switchTurn.getPlayerN(), switchTurn.getNewPokemon());
            return;
        }
        
        // Normal decision
        Turn decision = strategy.decideTurn(context);
        
        // Queue the decision in the engine
        if (decision.getType() == Turn.TurnType.Move) {
            engine.queueTurn(decision.getPlayerN(), decision.getMoveName());
        } else {
            engine.queueTurn(decision.getPlayerN(), decision.getNewPokemon());
        }
    }
    
    public int getPlayerNumber() {
        return playerNumber;
    }
    
    public Strategy getStrategy() {
        return strategy;
    }
}