package ru.shemplo.game.alchemy.logic.gfx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.shemplo.game.alchemy.GameParameters.Parameter;
import ru.shemplo.game.alchemy.logic.AbsGame;
import ru.shemplo.game.alchemy.logic.Game;
import ru.shemplo.game.alchemy.logic.data.Entity;
import ru.shemplo.game.alchemy.logic.data.Recipe;
import ru.shemplo.game.alchemy.logic.data.RecipesPack;
import ru.shemplo.snowball.stuctures.Pair;

public class GameSceneListener extends AbsSceneListener {

    @AllArgsConstructor (access = AccessLevel.PRIVATE)
    private static enum GameComponent implements SceneContainer {
        
        ROOT ("root"),
        
        OPENED ("opened"), TOTAL ("total"),
        
        TABLE ("table"), RESULT ("result"), 
        INVENTORY ("inventory"),
        
        ALCHEMY ("alchemy"), CLEAN ("clean"),
        BACK ("back"), SAVE ("save"), HELP ("help"),
        SUGGEST ("suggest");
        
        @Getter private final String id;
        
    }
    
    private AtomicReference <Entity> opened = new AtomicReference <> ();
    private final List <Entity> onTable = new ArrayList <> ();
    
    protected GameSceneListener (Game game, Scene scene) {
        super (game, scene);
        
        try {
            if (PARAMS.get (Parameter.LOAD) == null) {                
                String packName = PARAMS
                                . <RecipesPack> get (Parameter.RECIPES)
                                . getFile ();
                game.setCurrentRecipesSet (AbsGame.loadRecipes (packName));
            } else {
                game.loadGame (PARAMS.get (Parameter.LOAD));
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
        
        GameWindow.getStage ().setTitle ("Do alchemy");
        GameWindow.getStage ().setResizable (true);
        
        Button alchemy = GameComponent.ALCHEMY.get (scene);
        alchemy.setOnAction (__ -> {
            Platform.runLater (() -> alchemy.setDisable (true));
            List <Pair <Entity, Integer>> 
                list = onTable.stream ()
                     . collect (Collectors.groupingBy (a -> a))
                     . entrySet ().stream ().map (Pair::fromMapEntry)
                     . map (p -> Pair.mp (p.F, p.S.size ()))
                     . collect (Collectors.toList ());
            onTable.clear ();
            game.getProduct (new Recipe (list)).ifPresent (e -> {
                game.addOpened (e); opened.set (e);
            });
            Platform.runLater (() -> {
                updateInventory (); updateTable ();
                alchemy.setDisable (false);
            });
        });
        
        Button clean = GameComponent.CLEAN.get (scene);
        clean.setOnAction (__ -> {
            onTable.clear ();
            Platform.runLater (() -> {
                updateInventory (); updateTable ();
            });
        });
        
        Image backIcon = new Image ("/gfx/logout.png");
        ImageView backView = new ImageView (backIcon);
        Button back = GameComponent.BACK.get (scene);
        back.setGraphic (backView);
        back.setOnAction (__ -> 
            switchSceneWithAnimation (GameScene.MENU));
        
        Image saveIcon = new Image ("/gfx/save.png");
        ImageView saveView = new ImageView (saveIcon);
        Button save = GameComponent.SAVE.get (scene);
        save.setGraphic (saveView);
        save.setOnAction (__ -> game.saveGame ());
        
        Button help = GameComponent.HELP.get (scene);
        help.setOnAction (__ -> {
            VBox table = GameComponent.TABLE.get (scene);
            Platform.runLater (() -> {
                table.getChildren ().clear ();
                
                Label label = new Label (
                    "Alchemy - manual\n"
                    + "\n"
                    + "Try to open as many elements as possible"
                    + " by combining avaliable ones. All available"
                    + " items are placed in your Inventory."
                    + " Items for next craft are shown on Table."
                    + " To make attempt just click on Alchemy."
                ); label.setWrapText (true);
                table.getChildren ().add (label);
            });
        });
        
        Button suggest = GameComponent.SUGGEST.get (scene);
        suggest.setOnAction (__ -> {
            onTable.clear (); opened.set (null);
            List <Entity> list = new ArrayList <> (game.getOpened ());
            int limit = 1 + RANDOM.nextInt (3);
            for (int i = 0; i < limit; i++) {
                onTable.add (list.get (RANDOM.nextInt (list.size ())));
            }
            
            Platform.runLater (this::updateTable);
        });
        
        updateInventory ();
        updateTable ();
    }
    
    private final EventHandler <ActionEvent> tableListener = ae -> {
        final Button source = (Button) ae.getSource ();
        game.convenrtToEntity (source.getText ()).ifPresent (e -> {
            int index = onTable.indexOf (e);
            if (index == -1) { return; } 
            
            onTable.remove (index);
            updateTable ();
        });
    };
    
    private void updateTable () {
        VBox table  = GameComponent.TABLE.get (scene);
        VBox result = GameComponent.RESULT.get (scene);
        Label done  = GameComponent.OPENED.get (scene),
              total = GameComponent.TOTAL.get (scene);
        Platform.runLater (() -> {
            table.getChildren ().clear ();
            table.getChildren ().addAll (
                onTable.stream  ()
                       .map     (e -> new Button (e.getName ()))
                       .peek    (b -> b.setOnAction (tableListener))
                       .collect (Collectors.toList ())
            );
            
            result.getChildren ().clear ();
            if (opened.get () != null) {
                Entity entity = opened.get ();
                result.getChildren ()
                      .add (new Label (entity.getName ()));
            }
            
            done.setText ("" + game.getOpened ().size ());
            total.setText ("" + game.getTotalProducts ());
        });
    }
    
    private final EventHandler <ActionEvent> inventoryListener = ae -> {
        final Button source = (Button) ae.getSource ();
        VBox result = GameComponent.RESULT.get (scene);
        game.convenrtToEntity (source.getText ()).ifPresent (e -> {
            onTable.add (e); updateTable ();
            result.getChildren ().clear ();
            opened.set (null);
        });
    };
    
    private final EventHandler <MouseEvent> recipeListener = me -> {
        if (!me.getButton ().equals (MouseButton.SECONDARY)) { return; }
        final Button source = (Button) me.getSource ();
        game.convenrtToEntity (source.getText ()).ifPresent (e ->
            game.getRecipeOf (e).ifPresent (recipe -> {
                VBox table = GameComponent.TABLE.get (scene);
                final List <Pair <Entity, Integer>> 
                    list = recipe.getEntities ();
                
                Platform.runLater (() -> {
                    onTable.clear (); //updateTable ();
                    table.getChildren ().clear ();
                    table.getChildren ().addAll (
                        list.stream ()
                            .flatMap (p -> Stream.generate (() -> p.F).limit (p.S))
                            .map (Entity::getName).map (Button::new)
                            .peek (b -> b.setDisable (true))
                            .collect (Collectors.toList ())
                    );
                });
            })
        );
    };
    
    private void updateInventory () {
        VBox inventory = GameComponent.INVENTORY.get (scene);
        Platform.runLater (() -> {
            inventory.getChildren ().clear ();
            inventory.getChildren ().addAll (
                game.getOpened ().stream ()
                    .sorted  ((a, b) -> Integer.compare (a.getIndex (), b.getIndex ()))
                    .map     (e -> new Button (e.getName ()))
                    .peek    (b -> b.setOnAction (inventoryListener))
                    .peek    (b -> b.setOnMouseClicked (recipeListener))
                    .collect (Collectors.toList ())
            );
        });
    }
    
}
