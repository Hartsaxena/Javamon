package javamon.entities.moves;

import java.util.*;
import javamon.entities.Pokemon;
import javamon.types.Type;

public class Move implements DamageSource {
    
    private final String name;
    private final Type type;
    private final int accuracy;
    private final int pp;
    private final List<DamageSourceType> attributes;
    private final int priority;
    private final MoveEffect effect;

    public Move(String name, Type type, int accuracy, int pp, List<DamageSourceType> attributes) {
        this(name, type, accuracy, pp, attributes, null, 0);
    }

    public Move(String name, Type type, int accuracy, int pp, List<DamageSourceType> attributes, MoveEffect effect) {
        this(name, type, accuracy, pp, attributes, effect, 0);
    }

    public Move(String name, Type type, int accuracy, int pp, List<DamageSourceType> attributes, MoveEffect effect, int priority) {
        this.name = name;
        this.type = type;
        this.accuracy = accuracy;
        this.pp = pp;
        this.attributes = attributes;
        this.effect = effect;
        this.priority = priority;
    }

    public String apply(Pokemon attacker, Pokemon defender) {
        if (this.effect != null) {
            this.effect.onHit(attacker, defender, 0);
        }
        return attacker.getNickname() + " used " + this.name + "!\n";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        Move other = (Move) obj;
        return this.name.equals(other.name);
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getPP() {
        return pp;
    }

    public int getPriority() {
        return priority;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public MoveEffect getEffect() {
        return effect;
    }
    
    public List<DamageSourceType> getAttributes() {
        return new ArrayList<>(attributes);
    }

    @FunctionalInterface
    public static interface MoveEffect {
        void onHit(Pokemon attacker, Pokemon defender, int damageDealt);
    }
}
