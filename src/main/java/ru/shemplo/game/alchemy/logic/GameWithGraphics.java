package ru.shemplo.game.alchemy.logic;

import java.io.IOException;

import javafx.application.Application;

import lombok.extern.slf4j.Slf4j;
import ru.shemplo.game.alchemy.GameParameters.Parameter;
import ru.shemplo.game.alchemy.logic.gfx.GameWindow;

@Slf4j
public class GameWithGraphics extends AbsGame {
    
    private final GameWindow window;
    
    public GameWithGraphics () {
        new Thread (() -> Application.launch (GameWindow.class), 
                    "Game-Window-Initializer").start ();
        this.window = GameWindow.getInstance ();
        window.setGameContext (this);
    }
    
    @Override
    public void startNewGame () {
        try {
            String pack = PARAMS.get (Parameter.RECIPES);
            setCurrentRecipesSet (loadRecipes (pack));
        } catch (IOException ioe) {
            window.onError (ioe.getMessage ());
            log.error (ioe.getMessage ());
            return;
        }
    }
    
}
