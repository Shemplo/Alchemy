package ru.shemplo.game.alchemy;

import ru.shemplo.game.alchemy.GameParameters.Parameter;

public class RunAlchemy {
    
    private static final GameParameters 
        PARAMS = GameParameters.instance ();
    
    public static void main (String ... args) {
        PARAMS.addConsoleArguments (args); PARAMS.validate ();
        PARAMS.<GameMode> get (Parameter.MODE).run ();
    }
    
}
