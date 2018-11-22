package ru.shemplo.game.alchemy.logic.gfx;

import java.util.Objects;

import java.awt.Dimension;
import java.awt.Toolkit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import lombok.Getter;
import ru.shemplo.game.alchemy.logic.Game;

public class GameWindow extends Application {
    
    private static volatile GameWindow instance;
    @Getter private static volatile Stage stage;
    
    public static synchronized GameWindow getInstance () {
        if (instance == null) {
            synchronized (GameWindow.class) {
                while (instance == null) {
                    try   { GameWindow.class.wait (); } 
                    catch (InterruptedException e) { return null; }
                }
            }
        }
        
        return instance;
    }
    
    private Game game;
    
    public synchronized void setGameContext (Game game) {            
        if (this.game == null) { this.game = game; }
    }
    
    public void onError (String message) {
        
    }
    
    @Override
    public void start (Stage stage) throws Exception {
        synchronized (GameWindow.class) {            
            GameWindow.instance = this;
            GameWindow.stage = stage;
            
            GameWindow.class.notify ();
        }
        
        Dimension screen = Toolkit.getDefaultToolkit ().getScreenSize ();
        stage.getIcons ().add (new Image ("/gfx/philosophy.png"));
        stage.setY (100); stage.setX ((screen.width - 300) / 2);
        
        stage.setScene (new Scene (new Pane ()));
        stage.initStyle (StageStyle.UNDECORATED);
        stage.setTitle ("Test window");        
        switchScene (GameScene.MENU);
        stage.sizeToScene ();
        stage.show ();
    }
    
    public void switchScene (GameScene scene) {
        Objects.requireNonNull (scene);
        
        Parent parent = scene.isNeedReload ()
                      ? scene.reloadRoot ()
                      : scene.getRoot ();
        Platform.runLater (() -> {
            stage.getScene ().setRoot (parent);
            stage.sizeToScene ();
            
            if (scene.isNeedReload () || !scene.isInited ()) {
                scene.reloadListener (game, stage.getScene ());
            }
            
            scene.getListener ().onSceneShown ();
            stage.sizeToScene ();
        });
    }
    
}
