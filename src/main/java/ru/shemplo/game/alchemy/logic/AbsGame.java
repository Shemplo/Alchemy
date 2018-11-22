package ru.shemplo.game.alchemy.logic;

import static ru.shemplo.game.alchemy.GameParameters.Parameter.*;

import java.util.*;
import java.util.stream.Collectors;

import java.io.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.shemplo.game.alchemy.GameParameters;
import ru.shemplo.game.alchemy.GameParameters.Parameter;
import ru.shemplo.game.alchemy.logic.data.Entity;
import ru.shemplo.game.alchemy.logic.data.Recipe;
import ru.shemplo.game.alchemy.logic.data.RecipesPack;
import ru.shemplo.snowball.stuctures.Pair;
import ru.shemplo.snowball.utils.StringManip;

@Slf4j
public abstract class AbsGame implements Game {
    
    protected final GameParameters 
        PARAMS = GameParameters.instance ();
    
    @Getter protected final Set <Entity> opened = new HashSet <> ();
    protected final Set <Entity> unique = new HashSet <> ();
    protected final List <Pair <Entity, Recipe>> 
        recipes = new ArrayList <> ();
    
    @Override
    public final synchronized void setCurrentRecipesSet 
            (Collection <Pair <Entity, Recipe>> recipes) {
        this.recipes.clear ();
        recipes.stream  ()
               .filter  (p -> p.S != null)
               .forEach (this.recipes::add);
        
        this.opened.clear ();
        recipes.stream  ()
               .filter  (p -> p.S == null)
               .map     (p -> p.F)
               .forEach (this::addOpened);
        
        unique.addAll (this.recipes.stream ()
                           .map (p -> p.F)
                           .collect (Collectors.toList ()));
        unique.addAll (this.opened);
        
        this.saveFile = null; // May be new recipes pack
    }
    
    @Override
    public Optional <Entity> convenrtToEntity (String name) {
        return unique.stream ()
             . filter    (e -> e.getName ().equals (name))
             . findFirst ();
    }
    
    @Override
    public final Optional <Entity> getProduct (Recipe input) {
        return recipes.stream ()
             . filter    (p -> p.S.equals (input))
             . map       (Pair::getF)
             . findFirst ();
    }
    
    @Override
    public Optional <Recipe> getRecipeOf (Entity entity) {
        return recipes.stream ()
             . filter    (p -> p.F.equals (entity))
             . map       (Pair::getS)
             . findFirst ();
    }
    
    @Override
    public int getTotalProducts () {
        return unique.size ();
    }
    
    public final void addOpened (Entity entity) {
        if (opened.contains (entity)) { return; }
        entity.setIndex (this.opened.size ());
        this.opened.add (entity);
    }
    
    private Path saveFile = null;
    
    @Override
    public void saveGame () {
        if (saveFile == null) { 
            saveFile = createNewSave (); 
        }
        
        try (
            BufferedWriter bw = Files.newBufferedWriter (saveFile, 
                                          StandardCharsets.UTF_8);
            PrintWriter pw = new PrintWriter (bw);
        ) {
            pw.println (PARAMS.<RecipesPack> get (RECIPES).getFile ());
            StringJoiner sj = new StringJoiner (", ");
            getOpened ().stream ()
                        .sorted  ((a, b) -> Integer.compare (a.getIndex (), 
                                                             b.getIndex ()))
                        .map (Entity::getName).forEach (sj::add);
            pw.println (sj.toString ());
        } catch (IOException ioe) {
            ioe.printStackTrace ();
        }
    }
    
    private final DateFormat SAVE_FORMATTER 
        = new SimpleDateFormat ("dd.MM.yyyy hh-mm-ss");
    
    private Path createNewSave () {
        Date now = new Date (Instant.now ().toEpochMilli ());
        RecipesPack pack = PARAMS.get (Parameter.RECIPES);
        String fileName = String.format ("%s - %s.save", pack.getFile (), 
                                         SAVE_FORMATTER.format (now));
        Path path = prepareSaveDirectory ().resolve (fileName);
        if (!Files.exists (path)) { 
            try   { Files.createFile (path); } 
            catch (IOException e) { e.printStackTrace (); }
        }
        
        return path;
    }
    
    private Path prepareSaveDirectory () {
        String homePath = System.getProperty ("user.home");
        Path path = Paths.get (homePath).resolve (".alchemy");
        if (!Files.exists (path)) {
            try   { Files.createDirectory (path); } 
            catch (IOException e) { e.printStackTrace (); }
        }
        
        return path;
    }
    
    @Override
    public void loadGame (String fileName) throws IOException {
        Path path = prepareSaveDirectory ().resolve (fileName + ".save");
        if (!Files.exists (path)) {
            String message = String.format ("Save file `%s` not found", fileName);
            throw new IOException (message);
        }
        
        try (
            BufferedReader br = Files.newBufferedReader (path);
        ) {
            final String line = StringManip.fetchNonEmptyLine (br).trim ();
            RecipesPack pack = Arrays.stream (RecipesPack.values ())
                             . filter (p -> p.getFile ().equals (line))
                             . findFirst ().orElse (null);
            if (pack == null) {
                String message = String.format ("Unknown recipes pack `%s`", line);
                throw new IOException (message);
            }
            
            setCurrentRecipesSet (AbsGame.loadRecipes (pack.getFile ()));
            final String opened = StringManip.fetchNonEmptyLine (br);
            Arrays.stream  (opened.split (","))
                  .map     (String::trim)
                  .map     (Entity::new)
                  .forEach (this::addOpened);
        }
    }
    
    public List <Path> getSavesList () {
        String homePath = System.getProperty ("user.home");
        Path path = Paths.get (homePath).resolve (".alchemy");
        try {
            return Files.list (path)
                 . filter (Files::isRegularFile)
                 . filter (p -> p.getFileName ().toString ()
                                 .endsWith (".save"))
                 .map (f -> Pair.mp (f, lastModified (f)))
                 .sorted ((a, b) -> -Long.compare (a.S.toMillis (), 
                                                   b.S.toMillis ()))
                 .map (p -> p.F).collect (Collectors.toList ());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return new ArrayList <> ();
    }
    
    private static FileTime lastModified (Path path) {
        try   { return Files.getLastModifiedTime (path); } 
        catch (IOException e) { e.printStackTrace (); }
        
        return FileTime.fromMillis (0);
    }
    
    public static List <Pair <Entity, Recipe>> loadRecipes (String packName) throws IOException {
        final String resourcePath = String.format ("/data/%s.recipes", packName);
        log.debug (String.format ("Loading resource: %s", resourcePath));
        
        final List <Pair <Entity, Recipe>> recipes = new ArrayList <> ();
        final Set <Entity> initialEntities = new HashSet <> (),
                           knownEntities   = new HashSet <> ();
        try (
            InputStream is = AbsGame.class
                           . getResourceAsStream (resourcePath);
            Reader r = new InputStreamReader (is, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader (r);
        ) {
            String line = StringManip.fetchNonEmptyLine (br);
            
            if (line == null) {
                String message = String.format ("Wrong recipes file format: empty");
                throw new IOException (message);
            }
            
            Arrays.stream (line.split (",")).map (String::trim)
                  .map (String::toLowerCase).map (Entity::new)
                  .peek (knownEntities::add)
                  .forEach (initialEntities::add);
            
            while ((line = StringManip.fetchNonEmptyLine (br)) != null) {
                if (line.charAt (0) == '#') { continue; /* comment */ }
                
                final String [] parts = line.split ("=");
                if (parts.length < 2) {
                    String message = String.format ("Wrong recipes file format:"
                                   + " assignment not found in `%s`", line);
                    throw new IOException (message);
                }
                
                final Entity result = new Entity (parts [0].trim ().toLowerCase ());
                knownEntities.add (result);
                
                final List <Pair <Entity, Integer>> 
                    components = Arrays.stream (parts [1].split ("\\+"))
                               . map (String::trim)
                               . map (String::toLowerCase)
                               . filter (p -> p.length () > 0)
                               . map (Entity::new)
                               . collect (Collectors.groupingBy (a -> a))
                               . entrySet ().stream ()
                               . map (Pair::fromMapEntry)
                               . map (p -> Pair.mp (p.F, p.S.size ()))
                               . collect (Collectors.toList ());
                recipes.add (Pair.mp (result, new Recipe (components)));
            }
        }
        
        List <Entity> unknownEntities = recipes.stream ()
                                      . map     (Pair::getS)
                                      . map     (Recipe::getEntities)
                                      . flatMap (List::stream)
                                      . map     (p -> p.F)
                                      . filter  (e -> !knownEntities.contains (e))
                                      . distinct ()
                                      . collect (Collectors.toList ());
        if (unknownEntities.size () > 0) {
            String message = String.format ("Unknown entities found: %s", 
                                            unknownEntities.toString ());
            throw new IOException (message);
        }
        
        recipes.addAll (0, initialEntities.stream ()
                         . map (p -> Pair.mp (p, (Recipe) null))
                         . collect (Collectors.toList ()));
        return recipes;
    }
    
}
