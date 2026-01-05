package javamon.entities;

import java.util.*;
import javamon.entities.moves.*;
import javamon.types.Type;

public class Pokemon {
    private final PokemonSpecies species;
    private int level;
    private String nickname;

    private int currentHp;
    private Stats stats; // Unmodified Stats (varies by level)
    private final StatModifiers modifiers; // Temporary battle buffs (+2 Attack, etc.)
    private final List<MoveSlot> moveSlots;
    private StatusCondition statusCondition;

    /**
     * Creates a Pokemon from species and level, initializes stats and full HP.
     * @param species base species definition
     * @param level level to use for stat calculation
     */
    public Pokemon(PokemonSpecies species, int level) {
        this.species = species;
        this.level = level;
        this.nickname = species.getName(); // Default nickname is species name
        this.modifiers = new StatModifiers();
        this.moveSlots = new ArrayList<>();
        this.statusCondition = StatusCondition.None;

        // Calculate stats immediately upon creation
        recalculateStats();
        
        // Full heal on creation
        this.currentHp = this.stats.get(Stat.Hp);
    }

    /**
     * Copy constructor to duplicate battle state (hp, moves, status, etc.).
     */
    public Pokemon(Pokemon other) {
        this(other.species, other.level);
        this.nickname = other.nickname; 
        this.currentHp = other.currentHp;
        if (other.stats != null) {
            this.stats = new Stats(other.stats);
        }
        this.moveSlots.addAll(other.moveSlots);
        this.statusCondition = other.statusCondition;
    }

    /**
     * Recalculates the stats based on Species Base Stats + Level.
     * Call this whenever the Pokemon levels up.
     */
    private void recalculateStats() {
        Stats base = species.getBaseStats();
        
        // Simple Gen 1 Style Formula
        // HP:   ( ( (Base + IV) * 2 + (Sqrt(EV)/4) ) * Level ) / 100 ) + Level + 10
        // We will stick to a simplified version without IV/EV for now:
        // TODO: IV/EV stat changes
        
        int hp = calcStat(base.get(Stat.Hp), level, true);
        int atk = calcStat(base.get(Stat.Attack), level, false);
        int def = calcStat(base.get(Stat.Defense), level, false);
        int spAtk = calcStat(base.get(Stat.SpecialAttack), level, false);
        int spDef = calcStat(base.get(Stat.SpecialDefense), level, false);
        int spd = calcStat(base.get(Stat.Speed), level, false);

        this.stats = new Stats(hp, atk, def, spAtk, spDef, spd);
    }

    /**
     * Computes a non-HP stat value using a simplified Gen 1 style formula.
     */
    private int calcStat(int base, int lvl, boolean isHp) {
        if (isHp) {
            return calcHp(base, lvl);
        }
        
        return (int) (((2 * base * lvl) / 100.0) + 5);
    }
    
    /**
     * Computes HP using a simplified Gen 1 style formula.
     */
    private int calcHp(int baseHP, int lvl) {
        return (int) (((2 * baseHP * lvl) / 100.0) + lvl + 10);
    }

    /**
     * Handles taking damage and returns a flavor string.
     */
    /**
     * Applies damage to current HP and returns a flavor message.
     */
    public String takeDamage(int dmg, DamageSource source) {
        if (dmg <= 0) {
            return "It has no effect on " + this.nickname + "!\n";
        }

        this.currentHp -= dmg;
        if (this.currentHp < 0) this.currentHp = 0;

        return this.nickname + " took " + dmg + " damage from " + source.getName() + "!\n";
    }

    /**
     * Heals HP up to the max HP.
     */
    public void heal(int amount) {
        this.currentHp += amount;
        int maxHp = this.stats.get(Stat.Hp);
        if (this.currentHp > maxHp) {
            this.currentHp = maxHp;
        }
    }

    /**
     * Returns the stats with Stage Multipliers applied (e.g. after Swords Dance).
     * Use THIS for damage calculation.
     * 
     * @return stats after temporary stage modifiers (for damage/speed checks).
     */
    public Stats getEffectiveStats() {
        Stats effectiveStats = new Stats(stats); // Start with copy of max stats

        for (Stat s : Stat.values()) {
            // HP, Accuracy, and Evasion do not use standard stage multipliers
            if (s == Stat.Hp || s == Stat.Accuracy || s == Stat.Evasion) continue;

            effectiveStats.set(s, getEffectiveStat(s));
        }
        return effectiveStats;
    }

    private int getEffectiveStat(Stat s) {
        return (int) (stats.get(s) * modifiers.getMultiplier(s));
    }

    /**
     * Adjusts a stat stage within bounds (-6 to +6).
     */
    public void modifyStat(Stat stat, int stages) {
        modifiers.modify(stat, stages);
    }

    /**
     * @return true if this Pokemon's effective speed exceeds the other's.
     */
    public boolean isFasterThan(Pokemon other) {
        return this.getEffectiveStat(Stat.Speed) > other.getEffectiveStat(Stat.Speed);
    }

    /**
     * @return true when current HP is zero or less.
     */
    public boolean isKnockedOut() {
        return this.currentHp <= 0;
    }

    // --- Status Conditions ---
    
    /**
     * @return current major status condition.
     */
    public StatusCondition getStatusCondition() {
        return statusCondition;
    }
    
    /**
     * Sets the major status condition.
     */
    public void setStatusCondition(StatusCondition status) {
        this.statusCondition = status;
    }
    
    /**
     * @return true if any non-None status is present.
     */
    public boolean hasStatusCondition() {
        return statusCondition != StatusCondition.None;
    }

    // --- Getters & Delegation ---

    /**
     * @return species name (not nickname).
     */
    public String getSpeciesName() {
        return species.getName();
    }

    /**
     * @return display nickname.
     */
    public String getNickname() {
        return nickname;
    }
    /**
     * Sets display nickname.
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    /**
     * @return current level.
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * @return current HP value.
     */
    public int getCurrentHp() {
        return currentHp;
    }

    /**
     * @return immutable species types list.
     */
    public List<Type> getTypes() {
        return species.getTypes();
    }
    
    /**
     * @return current learned moves (shallow copy of move data).
     */
    public List<Move> getMoves() {
        List<Move> moves = new ArrayList<>();
        for (MoveSlot ms : this.moveSlots) {
            moves.add(ms.getMoveData());
        }

        return moves;
    }
    /**
     * Adds a move to this Pokemon with full PP.
     */
    public void learnMove(Move move) {
        this.moveSlots.add(new MoveSlot(move));
    }
    /**
     * Checks if the Pokemon knows a move by name.
     */
    public boolean hasMoveByName(String moveName) {
        for (MoveSlot ms : this.moveSlots) {
            if (ms.getMoveData().getName().equals(moveName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return remaining PP for the named move.
     */
    public int getRemainingPP(String moveName) {
        for (MoveSlot ms : this.moveSlots) {
            if (ms.getMoveData().getName().equals(moveName)) {
                return ms.getRemainingPP();
            }
        }

        throw new IllegalArgumentException("Can't find remaining PP for move that this Pokemon doesn't know!");
    }

    /**
     * Decrements PP for the named move.
     */
    public void decrementPP(String moveName) {
        for (MoveSlot ms : this.moveSlots) {
            if (ms.getMoveData().getName().equals(moveName)) {
                ms.decrementPP();
                return;
            }
        }

        throw new IllegalArgumentException("Can't decrement PP for move that this Pokemon doesn't know!");
    }

    /**
     * @return defensive copy of unmodified stats.
     */
    public Stats getStats() {
        return new Stats(stats);
    }

    public static class MoveSlot {
        private Move move;
        private int pp;

        /**
         * Creates a move slot with full PP.
         */
        public MoveSlot(Move move) {
            this(move, move.getPP());
        }

        /**
         * Creates a move slot with specified remaining PP.
         */
        public MoveSlot(Move move, int remainingPP) {
            if (remainingPP < 0 || remainingPP > move.getPP()) {
                throw new IllegalArgumentException("Invalid remaining PP for move: " + move.getName());
            }

            this.move = move;
            this.pp = remainingPP;
        }

        /**
         * Decreases PP by one.
         */
        public void decrementPP() {
            this.pp--;
        }

        /**
         * @return current PP remaining.
         */
        public int getRemainingPP() {
            return pp;
        }

        /**
         * @return maximum PP for this move.
         */
        public int getMaxPP() {
            return move.getPP();
        }

        /**
         * @return underlying move data.
         */
        public Move getMoveData() {
            return move;
        }
    }

    public static class Stats {
        private final Map<Stat, Integer> values = new EnumMap<>(Stat.class);

        /**
         * Creates a stats container with concrete values.
         */
        public Stats(int hp, int atk, int def, int spAtk, int spDef, int spd) {
            values.put(Stat.Hp, hp);
            values.put(Stat.Attack, atk);
            values.put(Stat.Defense, def);
            values.put(Stat.SpecialAttack, spAtk);
            values.put(Stat.SpecialDefense, spDef);
            values.put(Stat.Speed, spd);
        }

        // Copy Constructor
        public Stats(Stats other) {
            this.values.putAll(other.values);
        }

        public int get(Stat s) { return values.getOrDefault(s, 0); }
        public void set(Stat s, int val) { values.put(s, val); }
    }

    public static class StatModifiers {
        private static final int MAX_STAGE = 6;
        private final Map<Stat, Integer> stages = new EnumMap<>(Stat.class);

        /**
         * Clamps and applies a stage change to the given stat.
         */
        public void modify(Stat stat, int amount) {
            int current = stages.getOrDefault(stat, 0);
            int next = Math.max(-MAX_STAGE, Math.min(MAX_STAGE, current + amount));
            stages.put(stat, next);
        }

        /**
         * @return stage multiplier for the stat (e.g., +1 => 1.5x, -1 => 0.66x).
         */
        public double getMultiplier(Stat stat) {
            int stage = stages.getOrDefault(stat, 0);
            if (stage >= 0) {
                return (2.0 + stage) / 2.0;
            } else {
                return 2.0 / (2.0 - stage);
            }
        }
    }
    
    public static enum StatusCondition {
        None,
        Burn,
        Freeze,
        Paralyze,
        Poison,
        Sleep
    }
}