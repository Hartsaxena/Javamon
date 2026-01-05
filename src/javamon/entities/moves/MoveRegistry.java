package javamon.entities.moves;

import java.util.*;
import javamon.entities.*;
import static javamon.types.Type.*;
import static javamon.entities.Stat.*;
import static javamon.entities.moves.DamagingMove.DamageType.*;

public class MoveRegistry {
    private static final Map<String, Move> moves = new TreeMap<>();
    
    // Helper methods for creating common move effects
    
    /**
     * Creates a status condition effect with a given probability.
     * @param status The status condition to apply
     * @param probability The probability (0.0 to 1.0) of applying the status
     * @return A MoveEffect that applies the status condition
     */
    private static Move.MoveEffect statusEffect(Pokemon.StatusCondition status, double probability) {
        return (attacker, defender, damageDealt) -> {
            if (Math.random() < probability) {
                if (!defender.hasStatusCondition()) {
                    defender.setStatusCondition(status);
                }
            }
        };
    }
    
    /**
     * Creates a stat modification effect.
     * @param stat The stat to modify
     * @param stages The number of stages to change (positive = raise, negative = lower)
     * @param targetOpponent If true, modifies opponent's stat; if false, modifies user's stat
     * @param probability The probability (0.0 to 1.0) of applying the stat change
     * @return A MoveEffect that modifies the stat
     */
    private static Move.MoveEffect statChange(Stat stat, int stages, boolean targetOpponent, double probability) {
        return (attacker, defender, damageDealt) -> {
            if (Math.random() < probability) {
                Pokemon target = targetOpponent ? defender : attacker;
                target.modifyStat(stat, stages);
            }
        };
    }

    private static Move.MoveEffect chainEffect(Move.MoveEffect... effects) {
        return (attacker, defender, damageDealt) -> {
            for (Move.MoveEffect effect : effects) {
                effect.onHit(attacker, defender, damageDealt);
            }
        };
    }
    

    // Boring moves with no effects
    static {
        System.out.print("Loading Move Registry... ");

        moves.put("Alluring Voice", new DamagingMove(
            "Alluring Voice",
            Fairy,
            Special,
            80,  // power
            100, // accuracy
            10,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Aqua Tail", new DamagingMove(
            "Aqua Tail",
            Water,
            Physical,
            90,  // power
            90, // accuracy
            10,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Branch Poke", new DamagingMove(
            "Branch Poke",
            Grass,
            Physical,
            40,  // power
            100, // accuracy
            40,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Burning Bulwark", new Move(
            "Burning Bulwark",
            Fire,
            Integer.MAX_VALUE, // accuracy
            10,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Cut", new DamagingMove(
            "Cut",
            Normal,
            Physical,
            50,  // power
            95, // accuracy
            30,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Dragon Cheer", new Move(
            "Dragon Cheer",
            Dragon,
            Integer.MAX_VALUE, // accuracy
            15,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Dragon Claw", new DamagingMove(
            "Dragon Claw",
            Dragon,
            Physical,
            80,  // power
            100, // accuracy
            15,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Dragon Pulse", new DamagingMove(
            "Dragon Pulse",
            Dragon,
            Special,
            85,  // power
            100, // accuracy
            10,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Drill Peck", new DamagingMove(
            "Drill Peck",
            Flying,
            Physical,
            80,  // power
            100, // accuracy
            20,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Egg Bomb", new DamagingMove(
            "Egg Bomb",
            Normal,
            Physical,
            100,  // power
            75, // accuracy
            10,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Fairy Wind", new DamagingMove(
            "Fairy Wind",
            Fairy,
            Special,
            40,  // power
            100, // accuracy
            30,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Hard Press", new Move(
            "Hard Press",
            Steel,
            100, // accuracy
            10,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Horn Attack", new DamagingMove(
            "Horn Attack",
            Normal,
            Physical,
            65,  // power
            100, // accuracy
            25,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Hydro Pump", new DamagingMove(
            "Hydro Pump",
            Water,
            Special,
            110,  // power
            80, // accuracy
            5,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Hyper Voice", new DamagingMove(
            "Hyper Voice",
            Normal,
            Special,
            90,  // power
            100, // accuracy
            10,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Land's Wrath", new DamagingMove(
            "Land's Wrath",
            Ground,
            Physical,
            90,  // power
            100, // accuracy
            10,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Mega Kick", new DamagingMove(
            "Mega Kick",
            Normal,
            Physical,
            120,  // power
            75, // accuracy
            5,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Mega Punch", new DamagingMove(
            "Mega Punch",
            Normal,
            Physical,
            80,  // power
            85, // accuracy
            20,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Megahorn", new DamagingMove(
            "Megahorn",
            Bug,
            Physical,
            120,  // power
            85, // accuracy
            10,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Mighty Cleave", new DamagingMove(
            "Mighty Cleave",
            Rock,
            Physical,
            95,  // power
            100, // accuracy
            5,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Peck", new DamagingMove(
            "Peck",
            Flying,
            Physical,
            35,  // power
            100, // accuracy
            35,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Pound", new DamagingMove(
            "Pound",
            Normal,
            Physical,
            40,  // power
            100, // accuracy
            35,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Power Gem", new DamagingMove(
            "Power Gem",
            Rock,
            Special,
            80,  // power
            100, // accuracy
            20,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Power Whip", new DamagingMove(
            "Power Whip",
            Grass,
            Physical,
            120,  // power
            85, // accuracy
            10,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Rock Throw", new DamagingMove(
            "Rock Throw",
            Rock,
            Physical,
            50,  // power
            90, // accuracy
            15,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Scratch", new DamagingMove(
            "Scratch",
            Normal,
            Physical,
            40,  // power
            100, // accuracy
            35,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Seed Bomb", new DamagingMove(
            "Seed Bomb",
            Grass,
            Physical,
            80,  // power
            100, // accuracy
            15,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Slam", new DamagingMove(
            "Slam",
            Normal,
            Physical,
            80,  // power
            75, // accuracy
            20,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Strength", new DamagingMove(
            "Strength",
            Normal,
            Physical,
            80,  // power
            100, // accuracy
            15,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Supercell Slam", new DamagingMove(
            "Supercell Slam",
            Electric,
            Physical,
            100,  // power
            95, // accuracy
            15,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Tackle", new DamagingMove(
            "Tackle",
            Normal,
            Physical,
            40,  // power
            100, // accuracy
            35,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Temper Flare", new DamagingMove(
            "Temper Flare",
            Fire,
            Physical,
            75,  // power
            100, // accuracy
            10,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Tera Starstorm", new DamagingMove(
            "Tera Starstorm",
            Normal,
            Special,
            120,  // power
            100, // accuracy
            5,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Vine Whip", new DamagingMove(
            "Vine Whip",
            Grass,
            Physical,
            45,  // power
            100, // accuracy
            25,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Vise Grip", new DamagingMove(
            "Vise Grip",
            Normal,
            Physical,
            55,  // power
            100, // accuracy
            30,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Water Gun", new DamagingMove(
            "Water Gun",
            Water,
            Special,
            40,  // power
            100, // accuracy
            25,  // pp
            Collections.emptyList()
        ));
        
        moves.put("Wing Attack", new DamagingMove(
            "Wing Attack",
            Flying,
            Physical,
            60,  // power
            100, // accuracy
            35,  // pp
            Collections.emptyList()
        ));
        
        moves.put("X-Scissor", new DamagingMove(
            "X-Scissor",
            Bug,
            Physical,
            80,  // power
            100, // accuracy
            15,  // pp
            Collections.emptyList()
        ));
    }

    // Priority-only moves
    static {
        moves.put("Accelerock", new DamagingMove(
            "Accelerock",
            Rock,
            Physical,
            40,  // power
            100, // accuracy
            20,  // pp
            Collections.emptyList(),
            null,  // effect
            1 // priority
        ));
        
        moves.put("Aqua Jet", new DamagingMove(
            "Aqua Jet",
            Water,
            Physical,
            40,  // power
            100, // accuracy
            20,  // pp
            Collections.emptyList(),
            null,  // effect
            1 // priority
        ));
        
        moves.put("Bullet Punch", new DamagingMove(
            "Bullet Punch",
            Steel,
            Physical,
            40,  // power
            100, // accuracy
            30,  // pp
            Collections.emptyList(),
            null,  // effect
            1 // priority
        ));
        
        moves.put("Extreme Speed", new DamagingMove(
            "Extreme Speed",
            Normal,
            Physical,
            80,  // power
            100, // accuracy
            5,  // pp
            Collections.emptyList(),
            null,  // effect
            2 // priority
        ));
        
        moves.put("Ice Shard", new DamagingMove(
            "Ice Shard",
            Ice,
            Physical,
            40,  // power
            100, // accuracy
            30,  // pp
            Collections.emptyList(),
            null,  // effect
            1 // priority
        ));
        
        moves.put("Jet Punch", new DamagingMove(
            "Jet Punch",
            Water,
            Physical,
            60,  // power
            100, // accuracy
            15,  // pp
            Collections.emptyList(),
            null,  // effect
            1 // priority
        ));
        
        moves.put("Mach Punch", new DamagingMove(
            "Mach Punch",
            Fighting,
            Physical,
            40,  // power
            100, // accuracy
            30,  // pp
            Collections.emptyList(),
            null,  // effect
            1 // priority
        ));
        
        moves.put("Quick Attack", new DamagingMove(
            "Quick Attack",
            Normal,
            Physical,
            40,  // power
            100, // accuracy
            30,  // pp
            Collections.emptyList(),
            null,  // effect
            1 // priority
        ));
        
        moves.put("Shadow Sneak", new DamagingMove(
            "Shadow Sneak",
            Ghost,
            Physical,
            40,  // power
            100, // accuracy
            30,  // pp
            Collections.emptyList(),
            null,  // effect
            1 // priority
        ));
        
        moves.put("Vacuum Wave", new DamagingMove(
            "Vacuum Wave",
            Fighting,
            Special,
            40,  // power
            100, // accuracy
            30,  // pp
            Collections.emptyList(),
            null,  // effect
            1 // priority
        ));
    }

    // Misc
    static {
        // Ember - 10% chance to burn
        moves.put("Ember", new DamagingMove(
            "Ember",
            Fire,
            Special,
            40,
            100,
            25,
            Collections.emptyList(),
            statusEffect(Pokemon.StatusCondition.Burn, 0.10),
            0 // priority
        ));

        moves.put("Growl", new Move(
            "Growl",
            Normal,
            100,
            40,
            Collections.emptyList(),
            statChange(Attack, -1, true, 1.0)
        ));
    }
    static {
        System.out.println("Done!");
    }


    public static Move get(String name) {
        return moves.get(name);
    }
}
