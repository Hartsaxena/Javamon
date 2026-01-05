package javamon.entities;

import java.util.*;
import javamon.types.Type;
import javamon.entities.Pokemon.Stats; // Re-use your existing Stats class

public class PokemonSpecies {
    private final String name;
    private final List<Type> types;
    private final Stats baseStats;

    public PokemonSpecies(String name, List<Type> types, int hp, int atk, int def, int spAtk, int spDef, int spd) {
        this.name = name;
        this.types = List.copyOf(types); // Immutable copy
        this.baseStats = new Stats(hp, atk, def, spAtk, spDef, spd);
    }

    /**
     * The Factory Method. This creates a specific individual of this species.
     */
    public Pokemon create(int level) {
        // TODO: Calculate the actual stats for this level based on baseStats
        return new Pokemon(this, level); 
    }

    public String getName() {
        return name;
    }

    public List<Type> getTypes() {
        return new ArrayList<>(types);
    }

    public Stats getBaseStats() {
        return new Stats(baseStats);
    }
}