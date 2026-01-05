package javamon.battle;

public class Turn {

    private final int playerN;
    private final TurnType type;
    private final int newPokemon;
    private final String moveName;

    public Turn(int playerN, int newPokemon) {
        this(playerN, TurnType.Switch, newPokemon, null);
    }

    public Turn(int playerN, String moveName) {
        this(playerN, TurnType.Move, -1, moveName);
    }

    private Turn(int playerN, TurnType type, int newPokemon, String moveName) {
        if (playerN != 1 && playerN != 2) {
            throw new IllegalArgumentException();
        }

        this.playerN = playerN;
        this.type = type;
        this.newPokemon = newPokemon;
        this.moveName = moveName;
    }

    public int getPlayerN() {
        return playerN;
    }

    public TurnType getType() {
        return type;
    }

    public String getMoveName() {
        if (this.getType() == TurnType.Move) {
            return moveName;
        } else {
            return "";
        }
    }

    public int getNewPokemon() {
        if (this.getType() == TurnType.Switch) {
            return newPokemon;
        } else {
            return -1;
        }
    }

    public static enum TurnType {
        Switch,
        Move,
    }
}
