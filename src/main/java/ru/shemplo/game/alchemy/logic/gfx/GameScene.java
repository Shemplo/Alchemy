package ru.shemplo.game.alchemy.logic.gfx;

import java.util.function.BiFunction;

import java.io.IOException;

import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import lombok.Getter;
import ru.shemplo.game.alchemy.logic.Game;

public enum GameScene {
 
    MENU      ("menu",     false, MenuSceneListener::new),
    NEW_GAME  ("newGame",  true,  NewGameSceneListener::new),
    LOAD_GAME ("loadGame", true,  LoadGameSceneListener::new),
    GAME      ("game",     true,  GameSceneListener::new);
    
    private final BiFunction <Game, Scene, SceneListener> init;
    @Getter private final boolean needReload;
    @Getter private SceneListener listener;
    @Getter private final String packName;
    
    @Getter private boolean inited;
    private Parent root;
    
    private GameScene (final String packName, final boolean needReload,
                       BiFunction <Game, Scene, SceneListener> listener) {
        this.needReload = needReload;
        this.packName = packName;
        this.init = listener;
    }
    
    public Parent getRoot () {
        if (this.root == null) { reloadRoot (); }
        return this.root;
    }
    
    public Parent reloadRoot () {
        String resourcePath = String.format ("/fxml/%s.fxml", getPackName ());
        URL url = GameScene.class.getResource (resourcePath);
        
        String stylesPath = String.format ("/css/%s.css", getPackName ());
        try {
            root = FXMLLoader.load (url);
            root.getStylesheets ()
                .add (stylesPath);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        return root;
    }
    
    public SceneListener reloadListener (Game game, Scene scene) {
        inited = true; listener = init.apply (game, scene);
        return listener;
    }
    
}
