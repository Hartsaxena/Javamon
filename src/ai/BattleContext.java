package ai;

import java.util.*;
import javamon.battle.*;
import javamon.entities.*;
import javamon.entities.moves.*;

public class BattleContext {
    private final BattleEngine engine;
    private final int playerN;
    
    /**
     * Creates a battle context that reads from the given battle engine.
     * 
     * @param engine The {@code BattleEngine} to read from
     * @param playerN An {@code int} representing from which perspective to read data (which player is the active player).
     */
    public BattleContext(BattleEngine engine, int playerN) {
        this.engine = engine;
        this.playerN = playerN;
    }

    @Override
    public String toString() {
        Pokemon opp = getOpponentActivePokemon();
        Pokemon player = getMyActivePokemon();
        String output = "Turn " + getTurnN() + "\n"
                      + "Player " + getOpponentPlayerN() + ": " + getOpponentTeam().size() + " Pokemon\n"
                      + "\n";

        return output;
    }
    
    public int getPlayerN() {
        return playerN;
    }
    
    public int getOpponentPlayerN() {
        return (playerN == 1) ? 2 : 1;
    }
    
    public Pokemon getMyActivePokemon() {
        return engine.getActivePokemon(playerN);
    }
    
    public Pokemon getOpponentActivePokemon() {
        return engine.getActivePokemon(getOpponentPlayerN());
    }
    
    public List<Pokemon> getMyTeam() {
        return engine.getPokemon(playerN);
    }
    
    public List<Pokemon> getOpponentTeam() {
        return engine.getPokemon(getOpponentPlayerN());
    }
    
    public int getTurnN() {
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