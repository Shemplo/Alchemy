package ru.shemplo.game.alchemy.logic.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor (access = AccessLevel.PRIVATE)
public enum RecipesPack {
    
    CLASSIC ("classic");
    
    @Getter private final String file;
    
    @Override
    public String toString () {
        return getFile ();
    }
    
}
