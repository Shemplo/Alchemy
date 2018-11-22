package ru.shemplo.game.alchemy.logic;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import java.io.IOException;

import java.nio.file.Path;

import ru.shemplo.game.alchemy.logic.data.Entity;
import ru.shemplo.game.alchemy.logic.data.Recipe;
import ru.shemplo.snowball.stuctures.Pair;

public interface Game {
    
    void setCurrentRecipesSet (Collection <Pair <Entity, Recipe>> recipes);
    
    Optional <Entity> convenrtToEntity (String name);
    
    Optional <Recipe> getRecipeOf (Entity entity);
    
    Optional <Entity> getProduct (Recipe input);
    
    void addOpened (Entity entity);
    
    Set <Entity> getOpened ();
    
    int getTotalProducts ();
    
    void startNewGame ();
    
    void saveGame ();
    
    List <Path> getSavesList ();
    
    void loadGame (String fileName) throws IOException;
    
}
