package ru.shemplo.game.alchemy.logic.gfx;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.shemplo.game.alchemy.GameParameters;
import ru.shemplo.game.alchemy.GameParameters.Parameter;
import ru.shemplo.game.alchemy.logic.Game;
import ru.shemplo.game.alchemy.logic.data.RecipesPack;

public class NewGameSceneListener extends AbsSceneListener {

    @AllArgsConstructor (access = AccessLevel.PRIVATE)
    private static enum NewGameComponent implements SceneContainer {
        
        ROOT ("root"),
        
        PACK ("pack"),
        
        START ("start"), BACK ("back");
        
        @Getter private final String id;
        
    }
    
    private final AtomicReference <RecipesPack> 
        selectedPack = new AtomicReference <> ();
    
    protected NewGameSceneListener (Game game, Scene scene) {
        super (game, scene);
        
        GameWindow.getStage ().setTitle ("Start alchemy");
        GameWindow.getStage ().setResizable (false);
        
        Button start = NewGameComponent.START.get (scene);
        start.setDisable (true);
        start.setOnAction (__ -> {
            RecipesPack pack = selectedPack.get ();
            if (pack == null) { return; }
            
            GameParameters.instance ().setValue (Parameter.RECIPES, pack);
            switchSceneWithAnimation (GameScene.GAME);
        });
        
        Button back = NewGameComponent.BACK.get (scene);
        back.setOnAction (__ -> 
            switchSceneWithAnimation (GameScene.MENU));
        
        ChoiceBox <RecipesPack> choice = NewGameComponent.PACK.get (scene);
        List <RecipesPack> packs = Arrays.asList (RecipesPack.values ());
        choice.setItems (FXCollections.observableArrayList (packs));
        choice.getSelectionModel ().selectedItemProperty ()
              .addListener ((all, down, up) -> {
                  Platform.runLater (() -> {                      
                      start.setDisable (up == null);
                      selectedPack.set (up);
                  });
              });
        choice.getSelectionModel ().select (0);
    }
    
}
