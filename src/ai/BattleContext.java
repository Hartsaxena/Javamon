package ai;

import java.util.*;
import javamon.battle.*;
import javamon.entities.*;
import javamon.entities.moves.*;

public class BattleContext {
    private final BattleEngine engine;
    private final int botPlayerNumber;
    
    public BattleContext(BattleEngine engine, int botPlayerNumber) {
        this.engine = engine;
        this.botPlayerNumber = botPlayerNumber;
    }
    
    public int getBotPlayerNumber() {
        return botPlayerNumber;
    }
    
    public int getOpponentPlayerNumber() {
        return (botPlayerNumber == 1) ? 2 : 1;
    }
    
    public Pokemon getMyActivePokemon() {
        return engine.getActivePokemon(botPlayerNumber);
    }
    
    public Pokemon getOpponentActivePokemon() {
        return engine.getActivePokemon(getOpponentPlayerNumber());
    }
    
    public List<Pokemon> getMyTeam() {
        return engine.getPokemon(botPlayerNumber);
    }
    
    public List<Pokemon> getOpponentTeam() {
        return engine.getPokemon(getOpponentPlayerNumber());
    }
    
    public int getTurnNumber() {
        return engine.getTurnN();
    }
    
    public boolean isValidTurn(Turn turn) {
        return engine.isValidTurn(turn);
    }
    
    // Helper: Get all valid moves for current Pokemon
    public List<String> getValidMoves() {
        Pokemon active = getMyActivePokemon();
        List<String> validMoves = new ArrayList<>();
        
        for (Move move : active.getMoves()) {
            String moveName = move.getName();
            if (active.getRemainingPP(moveName) > 0) {
                validMoves.add(moveName);
            }
        }
        
        if (validMoves.isEmpty()) {
            validMoves.add("Struggle");
        }
        
        return validMoves;
    }
    
    // Helper: Get all valid switch targets
    public List<Integer> getValidSwitchTargets() {
        List<Integer> targets = new ArrayList<>();
        List<Pokemon> team = getMyTeam();
        Pokemon active = getMyActivePokemon();
        
        for (int i = 0; i < team.size(); i++) {
            Pokemon p = team.get(i);
            if (p != active && !p.isKnockedOut()) {
                targets.add(i);
            }
        }
        
        return targets;
    }
}