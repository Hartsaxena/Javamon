package javamon.entities.moves;

import java.util.*;
import javamon.entities.*;
import javamon.types.*;

public final class DamagingMove extends Move {

    private final DamageType damageType;
    private final int power;

    public DamagingMove(String name, Type type, DamageType damageType, int power, int accuracy, int pp, List<DamageSourceType> attributes) {
        this(name, type, damageType, power, accuracy, pp, attributes, null, 0);
    }

    public DamagingMove(String name, Type type, DamageType damageType, int power, int accuracy, int pp, List<DamageSourceType> attributes, MoveEffect effect, int priority) {
        super(name, type, accuracy, pp, attributes, effect, priority);

        this.damageType = damageType;
        this.power = power;
    }

    @Override
    public String apply(Pokemon attacker, Pokemon defender) {
        int dmg = calculateDamage(attacker, defender);
        String damagedOutput = defender.takeDamage(dmg, this);
        String moveOutput = attacker.getNickname() + " used " + this.getName() + "!\n";
        
        // Check type effectiveness and add appropriate message
        double typeEffectiveness = TypeChart.getEffectiveness(this.getType(), defender.getTypes());
        String effectivenessMessage = "";
        if (typeEffectiveness > 1.0) {
            effectivenessMessage = "It was super effective!\n";
        } else if (typeEffectiveness < 1.0 && typeEffectiveness > 0.0) {
            effectivenessMessage = "It was not very effective...\n";
        }
        
        // Apply effect with actual damage dealt
        if (this.getEffect() != null) {
            this.getEffect().onHit(attacker, defender, dmg);
        }
        
        return moveOutput + effectivenessMessage + damagedOutput;
    }

    private int calculateDamage(Pokemon attacker, Pokemon defender) {
        // 1. Determine which stats to use based on DamageType
        int attackStat;
        int defenseStat;

        if (this.damageType == DamageType.Physical) {
            attackStat = attacker.getEffectiveStats().get(Stat.Attack);
            defenseStat = defender.getEffectiveStats().get(Stat.Defense);
        } else {
            attackStat = attacker.getEffectiveStats().get(Stat.SpecialAttack);
            defenseStat = defender.getEffectiveStats().get(Stat.SpecialDefense);
        }

        // 2. Base Damage Calculation
        int level = attacker.getLevel();
        double baseDamage = ((((2.0 * level) / 5.0) + 2.0) * this.power * ((double) attackStat / defenseStat) / 50.0) + 2.0;

        // 3. Modifiers
        double modifiers = calculateModifiers(attacker, defender);

        return (int) (baseDamage * modifiers);
    }

    private double calculateModifiers(Pokemon attacker, Pokemon defender) {
        double stab = 1.0;
        if (attacker.getTypes().contains(this.getType())) {
            stab = 1.5;
        }

        double typeEffectiveness = TypeChart.getEffectiveness(this.getType(), defender.getTypes());
        double random = 0.85 + (Math.random() * (1.0 - 0.85));

        return stab * typeEffectiveness * random;
    }

    public DamageType getDamageType() {
        return damageType;
    }

    public int getPower() {
        return power;
    }

    public static enum DamageType {
        Physical,
        Special
    }
}
