package ru.shemplo.game.alchemy;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import ru.shemplo.game.alchemy.logic.data.RecipesPack;
import ru.shemplo.snowball.stuctures.Pair;
import ru.shemplo.snowball.utils.fun.StreamUtils;

public class GameParameters {
 
    private static volatile GameParameters parameters;
    
    public static GameParameters instance () {
        if (parameters == null) {
            synchronized (GameParameters.class) {
                if (parameters == null) {
                    parameters = new GameParameters ();
                }
            }
        }
        
        return parameters;
    }
    
    public static enum Parameter implements Function <String, Object> {
        
        MODE    ("Game mode", "-m", GameMode.CONSOLE, GameMode::parse),
        RECIPES ("Recipes pack", "-r", RecipesPack.CLASSIC),
        LOAD    ("Loading game", "-l", (Object) null),
        TREADS  ("Threads", "-ts", 1);
        
        private final Function <String, ?> TRANSFORMER;
        @Getter private final boolean isRequired;
        @Getter private final Object def;
        private final String TITLE, CUT;
        
        private Parameter (String title, String cut, Object def) {
            this.TITLE = title; this.CUT = cut;
            this.isRequired  = false;
            this.TRANSFORMER = a -> a;
            this.def = def;
        }
        
        private Parameter (String title, String cut, 
                           Function <String, ?> transformer) {
            this (title, cut, null, transformer);
        }
        
        private Parameter (String title, String cut, Object def,
                           Function <String, ?> transformer) {
            this.TITLE = title; this.CUT = cut;
            this.TRANSFORMER = transformer;
            this.isRequired  = true;
            this.def = def;
        }
        
        private Parameter (String title, String cut) {
            this (title, cut, a -> a);
        }
        
        public String getTitle () { return TITLE; }
        public String getCut   () { return CUT; }
        
        public static Parameter parse (final String cut) {
            Objects.requireNonNull (cut);
            
            return Arrays.stream (Parameter.values ())
                 . map (p -> Pair.mp (p.CUT, p))
                 . filter (p -> p.F.equals (cut.trim ().toLowerCase ()))
                 . map (p -> p.S)
                 . findAny ().orElse (null);
        }

        @Override
        public Object apply (String input) {
            return TRANSFORMER.apply (input);
        }
        
    }
    
    private final Map <Parameter, Object> VALUES = new HashMap <> ();
    
    private static final String FIRST_PARAMETER = "";
    
    public void addConsoleArguments (String ... args) {
        Map <String, Integer> starts = new HashMap <> ();
        if (FIRST_PARAMETER.length () > 0) {            
            starts.put (FIRST_PARAMETER, 0);
        }
        
        StreamUtils.zip (Arrays.stream (args), 
                         Stream.iterate (0, i -> i + 1), 
                         Pair::mp)
                   .filter  (Objects::nonNull)
                   .filter  (p -> p.F.length () > 0)
                   .filter  (p -> p.F.charAt (0) == '-')
                   .forEach (p -> starts.put (p.F, p.S));
        
        List <Integer> stops = starts.values ().stream ().sorted ()
                             . collect (Collectors.toList ());
        starts.entrySet ().stream ().map (Pair::fromMapEntry)
              .map     (p -> Pair.mp (Parameter.parse (p.F), p.S))
              .filter  (p -> p.F != null)
              .map     (p -> fetchValue (args, p, stops))
              .map     (p -> Pair.mp (p.F, p.F.apply (p.S)))
              .map     (p -> p.S != null ? p : Pair.mp (p.F, p.F.getDef ()))
              .forEach (p -> VALUES.put (p.F, p.S));
    }
    
    private Pair <Parameter, String> fetchValue (String [] args, 
            Pair <Parameter, Integer> pair, List <Integer> stops) {
        int from = pair.S, to = stops.stream ()
                              . filter (v -> v > from)
                              . min    (Integer::compare)
                              . orElse (args.length) - 1;
        StringJoiner sj = new StringJoiner (" ");
        Stream.iterate (from + 1, i -> i + 1)
              .limit   (to - from)
              .map     (i -> args [i])
              .forEach (sj::add);
        
        return Pair.mp (pair.F, sj.toString ());
    }
    
    public void validate () throws IllegalStateException {
        final StringJoiner sj = new StringJoiner (", ");
        final Function <Parameter, String> parameter2String = 
            p -> String.format ("[%s (%s)]", p.getTitle (), p.getCut ());
        Arrays.stream  (Parameter.values ())
              .filter  (p -> p.getDef () != null)
              .filter  (p -> !VALUES.containsKey (p))
              .forEach (p -> VALUES.putIfAbsent (p, p.getDef ()));
        Arrays.stream (Parameter.values ())
              .filter (Parameter::isRequired)
              .filter (p -> !VALUES.containsKey (p))
              .map (parameter2String)
              .forEach (sj::add);
        if (sj.length () > 0) {
            String message = String.format ("Missed required arguments: %s", 
                                            sj.toString ());
            throw new IllegalStateException (message);
        }
    }
    
    public <T> void setValue (Parameter parameter, T value) {
        Objects.requireNonNull (parameter);
        VALUES.put (parameter, value);
    }
    
    public <R> R get (Parameter parameter) {
        @SuppressWarnings ("unchecked") R value 
            = (R) VALUES.get (parameter);
        return value;
    }
    
}
