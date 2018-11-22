package ru.shemplo.game.alchemy.logic.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import ru.shemplo.snowball.stuctures.Pair;

public class Recipe {
    
    @Getter (value = AccessLevel.PROTECTED) 
    private final Map <Entity, Integer> values = new HashMap <> ();
    @Getter private final List <Pair <Entity, Integer>> entities;
    
    public Recipe (List <Pair <Entity, Integer>> entities) {
        entities.forEach (p -> values.put (p.F, p.S));
        this.entities = entities;
    }
    
    @SafeVarargs
    public Recipe (Pair <Entity, Integer>... entities) {
        this (Arrays.asList (entities));
    }
    
    @Override
    public String toString () {
        return String.format ("Recipe %s", entities.toString ());
    }
    
    private int getEntitiesNumber () {
        return getValues ().size ();
    }
    
    private Integer getValue (Entity key) {
        return getValues ().get (key);
    }
    
    @Override
    public final boolean equals (Object obj) {
        return obj == null 
             ? false
             : ((obj instanceof Recipe) 
                 ? strucureEquals ((Recipe) obj) 
                 : false);
    }
    
    private final boolean strucureEquals (Recipe recipe) {
        if (getEntitiesNumber () != recipe.getEntitiesNumber ()) {
            return false; // Different sizes
        }
        
        final Set <Entity> entities = new HashSet <> (values.keySet ());
        if (entities.retainAll (recipe.values.keySet ())) {
            return false; // Something was removed
        };
        
        long same = getValues ().keySet ().stream ()
                  . map (k -> Pair.mp (this.getValue   (k), 
                                       recipe.getValue (k)))
                  . filter (Pair::same)
                  . count ();
        
        return same == entities.size ();
    }
    
    @Override
    public final int hashCode () {
        return getValues ().hashCode ();
    }
    
}
