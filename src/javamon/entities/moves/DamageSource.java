package javamon.entities.moves;

import java.util.*;
import javamon.types.Type;

public interface DamageSource {

    String getName();
    Type getType();
    List<DamageSourceType> getAttributes();

    public static enum DamageSourceType {
        Contact,
    }
}
