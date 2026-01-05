package javamon.types;

import java.util.*;
import static javamon.types.Type.*;

public class TypeChart {

    // Outer Map: Attacker -> Inner Map
    // Inner Map: Defender -> Multiplier
    private static final Map<Type, Map<Type, Double>> chart = new EnumMap<>(Type.class);

    static {
        // Initialize inner map for every type
        for (Type t : Type.values()) {
            chart.put(t, new EnumMap<>(Type.class));
        }

        // 1. SUPER EFFECTIVE (2.0x)
        register(Fire, 2.0, Grass, Ice, Bug, Steel);
        register(Water, 2.0, Fire, Ground, Rock);
        register(Grass, 2.0, Water, Ground, Rock);
        register(Electric, 2.0, Water, Flying);
        register(Ice, 2.0, Grass, Ground, Flying, Dragon);
        register(Fighting, 2.0, Normal, Ice, Rock, Dark, Steel);
        register(Poison, 2.0, Grass, Fairy);
        register(Ground, 2.0, Fire, Electric, Poison, Rock, Steel);
        register(Flying, 2.0, Grass, Fighting, Bug);
        register(Psychic, 2.0, Fighting, Poison);
        register(Bug, 2.0, Grass, Psychic, Dark);
        register(Rock, 2.0, Fire, Ice, Flying, Bug);
        register(Ghost, 2.0, Psychic, Ghost);
        register(Dragon, 2.0, Dragon);
        register(Steel, 2.0, Ice, Rock, Fairy);
        register(Dark, 2.0, Psychic, Ghost);
        register(Fairy, 2.0, Fighting, Dragon, Dark);

        // 2. NOT VERY EFFECTIVE (0.5x)
        register(Normal, 0.5, Rock, Steel);
        register(Fire, 0.5, Fire, Water, Rock, Dragon);
        register(Water, 0.5, Water, Grass, Dragon);
        register(Grass, 0.5, Fire, Grass, Poison, Flying, Bug, Dragon, Steel);
        register(Electric, 0.5, Electric, Grass, Dragon);
        register(Ice, 0.5, Fire, Water, Ice, Steel);
        register(Fighting, 0.5, Poison, Flying, Psychic, Bug, Fairy);
        register(Poison, 0.5, Poison, Ground, Rock, Ghost);
        register(Ground, 0.5, Grass, Bug);
        register(Flying, 0.5, Electric, Rock, Steel);
        register(Psychic, 0.5, Psychic, Steel);
        register(Bug, 0.5, Fire, Fighting, Poison, Flying, Ghost, Steel, Fairy);
        register(Rock, 0.5, Fighting, Ground, Steel);
        register(Ghost, 0.5, Dark);
        register(Dragon, 0.5, Steel);
        register(Steel, 0.5, Fire, Water, Electric, Steel);
        register(Dark, 0.5, Fighting, Dark, Fairy);
        register(Fairy, 0.5, Fire, Poison, Steel);

        // 3. IMMUNITIES (0.0x)
        register(Normal, 0.0, Ghost);
        register(Electric, 0.0, Ground);
        register(Fighting, 0.0, Ghost);
        register(Poison, 0.0, Steel);
        register(Ground, 0.0, Flying);
        register(Psychic, 0.0, Dark);
        register(Ghost, 0.0, Normal);
        register(Dragon, 0.0, Fairy);
    }

    private static void register(Type attacker, double multi, Type... defenders) {
        for (Type defender : defenders) {
            chart.get(attacker).put(defender, multi);
        }
    }

    public static double getEffectiveness(Type attacker, List<Type> defenderTypes) {
        double multiplier = 1.0;

        for (Type defender : defenderTypes) {
            multiplier *= getMultiplier(attacker, defender);
        }
        
        return multiplier;
    }

    /**
     * Helper to safely get a multiplier. 
     * Returns 1.0 if no specific interaction was registered.
     */
    public static double getMultiplier(Type attacker, Type defender) {
        return chart.get(attacker).getOrDefault(defender, 1.0);
    }
}