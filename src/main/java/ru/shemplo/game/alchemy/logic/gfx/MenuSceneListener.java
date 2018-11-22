package ru.shemplo.game.alchemy.logic.gfx;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.shemplo.game.alchemy.logic.Game;

public class MenuSceneListener extends AbsSceneListener {
    
    @AllArgsConstructor (access = AccessLevel.PRIVATE)
    private static enum MenuComponent implements SceneContainer {
        
        ROOT ("root"),
        
        NEW_GAME ("new"), LOAD_GAME ("load"), 
        EXIT ("exit");
        
        @Getter private final String id;
        
    }
    
    public MenuSceneListener (Game game, Scene scene) {
        super (game, scene);
        
        GameWindow.getStage ().setTitle ("Alchemy game");
        GameWindow.getStage ().setResizable (false);
        
        Button newGame = MenuComponent.NEW_GAME.get (scene);
        newGame.setOnAction (__ -> 
            switchSceneWithAnimation (GameScene.NEW_GAME));
        
        Button loadGame = MenuComponent.LOAD_GAME.get (scene);
        loadGame.setOnAction (__ ->
            switchSceneWithAnimation (GameScene.LOAD_GAME));
        
        Button exit = MenuComponent.EXIT.get (scene);
        exit.setOnAction (ae -> Platform.exit ());
    }
    
}
