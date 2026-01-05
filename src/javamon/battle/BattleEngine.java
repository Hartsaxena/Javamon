package javamon.battle;

import java.io.*;
import java.util.*;
import javamon.entities.*;
import javamon.entities.moves.*;

public class BattleEngine {
    
    private final Side side1;
    private final Side side2;

    private int turnN;
    private List<Turn> turnQueue;
    private Random random;

    public BattleEngine(List<Pokemon> team1, List<Pokemon> team2) {
        if (team1 == null || team2 == null) {
            throw new IllegalArgumentException("Battle must have two teams!");
        }
        if (team1.size() > 6 || team2.size() > 6) {
            throw new IllegalArgumentException("Team can only be maximum 6 Pokemon!");
        }

        this.side1 = new Side(team1);
        this.side2 = new Side(team2);
        this.turnN = 1;
        this.turnQueue = new LinkedList<>();
        this.random = new Random();
    }

    public void queueTurn(int playerN, String moveName) {
        Pokemon active = getActivePokemon(playerN);

        // Check if PP is exhausted - if so, force Struggle
        if (!moveName.equals("Struggle") && active.hasMoveByName(moveName)) {
            if (active.getRemainingPP(moveName) <= 0) {
                moveName = "Struggle";
            }
        }
        
        // Validate the move (Struggle doesn't need to be known by the Pokemon)
        if (!moveName.equals("Struggle") && !active.hasMoveByName(moveName)) {
            throw new IllegalArgumentException("Pokemon does not know move: " + moveName);
        }

        queueTurn(new Turn(playerN, moveName));
    }
    public void queueTurn(int playerN, int newPokemon) {
        if (playerN != 1 && playerN != 2) {
            throw new IllegalArgumentException();
        }

        Side side = (playerN == 1) ? side1 : side2;
        if (newPokemon >= side.getTeamSize()) {
            throw new IllegalArgumentException();
        }

        queueTurn(new Turn(playerN, newPokemon));
    }

    private void queueTurn(Turn turn) {
        if (turnQueue.size() >= 2) {
            throw new IllegalArgumentException("Only up to two turns can be queued per turn");
        }

        if (!isValidTurn(turn)) {
            throw new IllegalArgumentException("Turns are not valid for current battle state!");
        }

        for (int i = 0; i < turnQueue.size(); i++) {
            if (turnQueue.get(i).getPlayerN() == turn.getPlayerN()) {
                // Need to override turn
                turnQueue.remove(i);
                turnQueue.add(i, turn);
                return;
            }
        }

        turnQueue.add(turn);
    }

    public void playOutTurns(PrintStream output) {
        if (turnQueue.size() != 2) {
            throw new IllegalArgumentException("Must have two turns queued before playing out round");
        }

        Turn first = fasterMove(turnQueue.get(0), turnQueue.get(1));
        Turn second = (first == turnQueue.get(0)) ? turnQueue.get(1) : turnQueue.get(0);
        turnQueue.clear();
        turnQueue.addAll(List.of(first, second));

        for (Turn t : turnQueue) {
            if (t.getType() == Turn.TurnType.Switch) {
                playSwitch(t, output);
            } else {
                playMove(t, output);
            }
        }

        this.turnN++;
        this.turnQueue.clear();
    }

    public boolean isFinished() {
        return side1.isWiped() || side2.isWiped();
    }

    public boolean isValidTurn(Turn t) {
        if (t.getType() == Turn.TurnType.Switch) {
            // Validate switch turn
            int playerN = t.getPlayerN();
            Side side = (playerN == 1) ? side1 : side2;
            int newPokemonIndex = t.getNewPokemon();
            
            // Check that index is valid
            if (newPokemonIndex < 0 || newPokemonIndex >= side.getTeamSize()) {
                return false;
            }
            
            // Check that we're not switching to the currently active Pokemon
            if (newPokemonIndex == side.getActiveIndex()) {
                return false;
            }
            
            // Check that the Pokemon we're switching to is not knocked out
            Pokemon targetPokemon = side.getPokemon(newPokemonIndex);
            if (targetPokemon.isKnockedOut()) {
                return false;
            }
            
            return true;
        } else {
            // Validate move turn
            int playerN = t.getPlayerN();
            Side side = (playerN == 1) ? side1 : side2;
            Pokemon active = getActivePokemon(playerN);
            
            // Check that active Pokemon is not knocked out
            if (active.isKnockedOut()) {
                return false;
            }
            
            // If a switch is needed (Pokemon fainted), only switches are allowed
            if (side.needsSwitch()) {
                return false;
            }
            
            String moveName = t.getMoveName();
            
            // Check that move exists in registry
            Move move = MoveRegistry.get(moveName);
            if (move == null) {
                return false;
            }
            
            // Struggle doesn't need to be known by the Pokemon (it's a fallback move)
            if (!moveName.equals("Struggle") && !active.hasMoveByName(moveName)) {
                return false;
            }
            
            return true;
        }
    }

    public Pokemon getActivePokemon(int playerN) {
        if (playerN == 1) {
            return side1.activePokemon();
        } else if (playerN == 2) {
            return side2.activePokemon();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public List<Pokemon> getPokemon(int playerN) {
        List<Pokemon> pokes = new ArrayList<>();
        if (playerN == 1) {
            for (int i = 0; i < side1.getTeamSize(); i++) {
                pokes.add(side1.getPokemon(i));
            }
        } else if (playerN == 2) {
            for (int i = 0; i < side2.getTeamSize(); i++) {
                pokes.add(side2.getPokemon(i));
            }
        } else {
            throw new IllegalArgumentException();
        }

        return pokes;
    }

    public int getTurnN() {
        return turnN;
    }

    private void playSwitch(Turn t, PrintStream output) {
        int playerN = t.getPlayerN();
        Side side = (playerN == 1) ? side1 : side2;
        int newPokemonIndex = t.getNewPokemon();
        
        Pokemon currentPokemon = side.activePokemon();
        Pokemon newPokemon = side.getPokemon(newPokemonIndex);
        
        // Output the switch message
        output.println("Player " + playerN + " withdrew " + currentPokemon.getNickname() + ".\n");
        output.println("Player " + playerN + " sent out " + newPokemon.getNickname() + "!\n");
        
        // Switch the active Pokemon
        side.switchActive(newPokemonIndex);
    }

    private Turn fasterMove(Turn turn1, Turn turn2) {

        // Switches take priority (except for pursuit)
        if (turn1.getType() == Turn.TurnType.Switch) {
            return turn1;
        } else if (turn2.getType() == Turn.TurnType.Switch) {
            return turn2;
        }
        
        Move move1 = MoveRegistry.get(turn1.getMoveName());
        Move move2 = MoveRegistry.get(turn2.getMoveName());
        
        // Safety check: if moves don't exist in registry, this shouldn't happen after validation
        // but we'll handle it gracefully
        if (move1 == null || move2 == null) {
            throw new IllegalStateException("Move not found in registry: " + 
                (move1 == null ? turn1.getMoveName() : turn2.getMoveName()));
        }

        // Check move priority
        if (move1.getPriority() > move2.getPriority()) {
            return turn1;
        } else if (move1.getPriority() < move2.getPriority()) {
            return turn2;
        }

        // Check speed
        Pokemon poke1 = getActivePokemon(turn1.getPlayerN());
        Pokemon poke2 = getActivePokemon(turn2.getPlayerN());
        if (poke1.isFasterThan(poke2)) {
            return turn1;
        } else if (poke2.isFasterThan(poke1)) {
            return turn2;
        }

        // Randomly select one in case of speed tie
        return random.nextBoolean() ? turn1 : turn2;
    }

    private void playMove(Turn t, PrintStream output) {
        int playerN = t.getPlayerN();
        Pokemon attacker = getActivePokemon(playerN);
        
        // Get the opponent's active Pokemon
        int opponentN = (playerN == 1) ? 2 : 1;
        Pokemon defender = getActivePokemon(opponentN);
        
        // If attacker is knocked out, skip the move
        if (attacker.isKnockedOut()) {
            return;
        }
        
        // If defender is already knocked out, skip the move
        if (defender.isKnockedOut()) {
            return;
        }
        
        String moveName = t.getMoveName();
        Move move = MoveRegistry.get(moveName);
        
        if (move == null) {
            output.println("Error: Move " + moveName + " not found in registry!");
            return;
        }
        
        // Check accuracy (moves with 100+ accuracy always hit, otherwise roll)
        // Note: Integer.MAX_VALUE is used for moves that never miss
        boolean hit = true;
        int accuracy = move.getAccuracy();
        if (accuracy < 100) {
            int roll = random.nextInt(100);
            hit = roll < accuracy;
        }
        // If accuracy >= 100 or is Integer.MAX_VALUE, the move always hits
        
        if (!hit) {
            output.print(attacker.getNickname() + " used " + moveName + "!\n");
            output.println("But it missed!");
            return;
        }
        
        // Decrement PP (unless it's Struggle)
        if (!moveName.equals("Struggle")) {
            attacker.decrementPP(moveName);
        }
        
        // Apply the move
        String result = move.apply(attacker, defender);
        output.print(result);
        
        // Check if defender is knocked out
        if (defender.isKnockedOut()) {
            Side defenderSide = (opponentN == 1) ? side1 : side2;
            defenderSide.setNeedsSwitch(true);
            output.println(defender.getNickname() + " fainted!");
        }
        
        // Check if attacker is knocked out (e.g., from recoil damage)
        if (attacker.isKnockedOut()) {
            Side attackerSide = (playerN == 1) ? side1 : side2;
            attackerSide.setNeedsSwitch(true);
            output.println(attacker.getNickname() + " fainted!");
        }
    }

    private static class Side {
        private List<Pokemon> team;
        private int active;
        private boolean needsSwitch;

        public Side(List<Pokemon> team) {
            if (team == null || team.size() > 6) {
                throw new IllegalArgumentException();
            }

            this.team = new ArrayList<>();
            for (Pokemon p : team) {
                this.team.add(new Pokemon(p)); // Uses the copy constructor
            }

            this.active = 0;
        }

        public boolean needsSwitch() {
            return needsSwitch;
        }

        public void setNeedsSwitch(boolean needsSwitch) {
            this.needsSwitch = needsSwitch;
        }

        public Pokemon activePokemon() {
            return team.get(this.active);
        }

        public boolean isWiped() {
            return team.isEmpty() || team.stream().allMatch(Pokemon::isKnockedOut);
        }

        public int getTeamSize() {
            return team.size();
        }

        public Pokemon getPokemon(int index) {
            if (index < 0 || index >= team.size()) {
                throw new IndexOutOfBoundsException("Invalid Pokemon index: " + index);
            }
            return team.get(index);
        }

        public int getActiveIndex() {
            return active;
        }

        public void switchActive(int newActiveIndex) {
            if (newActiveIndex < 0 || newActiveIndex >= team.size()) {
                throw new IndexOutOfBoundsException("Invalid Pokemon index for switch: " + newActiveIndex);
            }
            if (newActiveIndex == active) {
                throw new IllegalArgumentException("Cannot switch to the currently active Pokemon");
            }
            if (team.get(newActiveIndex).isKnockedOut()) {
                throw new IllegalArgumentException("Cannot switch to a knocked out Pokemon");
            }
            this.active = newActiveIndex;
            this.needsSwitch = false; // Reset the flag after switching
        }
    }
}
