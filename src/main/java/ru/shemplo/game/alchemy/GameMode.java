package ru.shemplo.game.alchemy;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import ru.shemplo.game.alchemy.logic.Game;
import ru.shemplo.game.alchemy.logic.GameWithGraphics;

public enum GameMode {
    
    CONSOLE  (() -> null), 
    GRAPHICS (() -> new GameWithGraphics ());
    
    private final Supplier <Game> GAME_PRODUCER;
    
    private GameMode (Supplier <Game> producer) {
        this.GAME_PRODUCER = producer;
    }
    
    public Game run () { return GAME_PRODUCER.get (); }
    
    public static GameMode parse (String value) {
        Objects.requireNonNull (value);
        final String val = value.trim ()
                         . toUpperCase ()
                         . replace (' ', '_');
        return Arrays.stream (values ())
             . filter (m -> m.name ().equals (val))
             . findFirst ().orElse (null);
    }
    
}
