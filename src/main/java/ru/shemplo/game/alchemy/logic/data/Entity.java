package ru.shemplo.game.alchemy.logic.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@EqualsAndHashCode(exclude = {"index"})
public class Entity {

    @Getter private final String name;
    @Getter @Setter private int index;
    
    @Override
    public String toString () {
        return getName ();
    }
    
}
