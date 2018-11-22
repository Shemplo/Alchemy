package ru.shemplo.game.alchemy.logic.gfx;

import java.util.Objects;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.util.Duration;

import lombok.Getter;
import ru.shemplo.game.alchemy.GameParameters;
import ru.shemplo.game.alchemy.logic.Game;

public abstract class AbsSceneListener implements SceneListener {
    
    protected static final int ANIMATION_FRAMES = 100;
    
    protected final GameParameters 
        PARAMS = GameParameters.instance ();
    
    @Getter protected final Scene scene;
    @Getter protected final Game game;
    
    protected final Timeline 
        fadeIN = new Timeline (
            new KeyFrame (Duration.ZERO, __ -> {
                double delta = 1.0 / ANIMATION_FRAMES;
                Parent root = getScene ().getRoot ();
                double current = root.getOpacity ();
                
                current = Math.min (current, 1.0 - delta);
                root.setOpacity (current + delta);
            }),
            new KeyFrame (Duration.millis (3))
        ),
        fadeOUT = new Timeline (
            new KeyFrame (Duration.ZERO, __ -> {
                double delta = 1.0 / ANIMATION_FRAMES;
                Parent root = getScene ().getRoot ();
                double current = root.getOpacity ();
                
                current = Math.max (current, delta);
                root.setOpacity (current - delta);
            }),
            new KeyFrame (Duration.millis (3))
        );
    
    private Point2D capture = null;
    
    protected AbsSceneListener (Game game, Scene scene) {
        this.game = game; this.scene = scene;
        
        scene.setOnMousePressed (me -> { capture = new Point2D (me.getSceneX (), me.getSceneY ()); });
        scene.setOnMouseDragged (me -> {
            final Stage stage = GameWindow.getStage ();
            if (!Objects.isNull (capture) && MouseButton.PRIMARY.equals (me.getButton ())) {
                stage.setX (me.getScreenX () - capture.getX ());
                stage.setY (me.getScreenY () - capture.getY ());
            }
        });
    }
    
    protected void switchSceneWithAnimation (GameScene to) {
        fadeOUT.setCycleCount (ANIMATION_FRAMES);
        fadeOUT.playFromStart ();
        
        new Thread (() -> {
            try { Thread.sleep (500); } catch (Exception e) { return; }
            GameWindow.getInstance ().switchScene (to);
        }).start ();;
    }
    
    @Override
    public void onSceneShown () {
        fadeIN.setCycleCount (ANIMATION_FRAMES);
        fadeIN.playFromStart ();
    }
    
}
