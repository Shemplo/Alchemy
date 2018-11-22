package ru.shemplo.game.alchemy.logic.gfx;

import javafx.scene.Node;
import javafx.scene.Scene;

public interface SceneContainer {
    
    public String getId ();
    
    default public <R extends Node> R get (Scene scene) {
        String key = String.format ("#%s", getId ());
        @SuppressWarnings ("unchecked") R out 
            = (R) scene.lookup (key);
        return out;
    }
    
}
