package ru.shemplo.game.alchemy.logic.gfx;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import java.nio.file.Path;

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

public class LoadGameSceneListener extends AbsSceneListener {

    @AllArgsConstructor (access = AccessLevel.PRIVATE)
    private static enum LoadGameComponent implements SceneContainer {
        
        ROOT ("root"),
        
        SAVE ("save"),
        
        LOAD ("load"), BACK ("back");
        
        @Getter private final String id;
        
    }
    
    private final AtomicReference <String> 
        selectedSave = new AtomicReference <> ();
    
    protected LoadGameSceneListener (Game game, Scene scene) {
        super (game, scene);
        
        Button load = LoadGameComponent.LOAD.get (scene);
        load.setDisable (true);
        load.setOnAction (__ -> {
            String saveName = selectedSave.get ();
            if (saveName == null) { return; }
            
            GameParameters.instance ().setValue (Parameter.LOAD, saveName);
            switchSceneWithAnimation (GameScene.GAME);
        });
        
        ChoiceBox <String> choice = LoadGameComponent.SAVE.get (scene);
        List <String> packs = game.getSavesList ().stream ()
                            . map (Path::getFileName)
                            . map (Path::toString)
                            . map (p -> p.substring (0, p.length () - 5))
                            . collect (Collectors.toList ());
        choice.setItems (FXCollections.observableArrayList (packs));
        choice.getSelectionModel ().selectedItemProperty ()
              .addListener ((all, down, up) -> {
                  Platform.runLater (() -> {                      
                      load.setDisable (up == null);
                      selectedSave.set (up);
                  });
              });
        choice.getSelectionModel ().select (0);
        
        Button back = LoadGameComponent.BACK.get (scene);
        back.setOnAction (__ -> 
            switchSceneWithAnimation (GameScene.MENU));
    }
    
}
