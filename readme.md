# :alembic: Alchemy - game of crafting and logic
> If you don't know how to waste time - just try to combine elephant and shell

### :book: Game description and rules

In the original game _Alchemy_ player was need to union different items to get new one; 
And the main goal was to open **all** of such items (elements). The difficulty was in
huge listing of options of these items; The tips were just in some kind of logic of
authors of recipes.

This version of game repeats all ideas of parent and gives some opportunities to authors
to make recipes of different kinds and different sizes (in original each recipe can
contain only 2 elements).

At the beginning player has several basic components that he can use. When he found
right combination of them, he gets new one... and this process repeats until all
elements of loaded pack are opened.

**:white_flower: For example:** 
* player has `water` and `fire` in inventory;
* there is a recipe: `steam = fire + water`

... so when player put on table `water` and `fire`, he would craft a `steam`

The main **rule** of this game is not to hack a recipes because the feature of
this game is in long searching of right combination of items in unlogic recipes :nerd_face:

### :hammer: How make it works

This game is written on Java and compatible with any popular platforms _(event tested on Windows, Ubuntu and Debian)_

If you don't want to change something in sources then you even can download build `jar` file from 
[releases](https://github.com/Shemplo/Alchemy/releases).

Game can be runned in several modes:
* **Console** - will not use graphics at all. All interaction with user will be through terminal.
Player would be needed to type commands to see the inventory and place items on table.
This mode is enabled by default (but it also can be enabled with flag `-m console`)
* **Graphics** - game will be displayed in as pretty window as possible (author is not a designer).
Controll of process in this mode is more efficient (all necessary information will be organized in window).
For rendering this mode uses JavaFX and can be enabled with flag `-m graphics`

The simplest way to run game:
> javaw -jar game.alchemy-[version].jar -m [mode]

:crystal_ball: In case of you want to build it by yourself <sub>(good luck)</sub> you will need to do some extra actions:

Necessary dependencies (exept one) are declared in `pom.xml` and can be easily installed 
by `update` of maven project in IDE.

One more dependency is [Snowball](https://github.com/AlienCoffee/Snowball) library of the same author. 
Problem is that this library is not an architype in maven and can't be pulled with it. 
It must be added to classpath by hands from original repository.

And also your IDE shoud support [Lobok project](https://projectlombok.org) annotations processor.

Now just add all of these things to project and **SUCESS** :tada:

### :mag: Format of file with recipes

All such files must be placed in `resources` directory of project (in `src/main/resources`).
Recommended to place them to `data` directory in `resources`.

The name of file has to end with `.recipes` and with `UTF-8` encoding.

The first non-emplty line of file declares items that will be available at the beginning 
(basic elements). Items should separated with `,` (comma) character.

**Example:**
> fire, water, earth, wind  
> ...

The next lines describes recipes in format `result = item [+ item]*`.

**Example:**
> ...  
> steam = fire + water  
> ice = water + wind + wind  
> chaos = fire + wind + earth + water

### :ambulance: How to contribute

If you have built project succesfully, please, put a :star: to this repository

Branch `master` is closed for directed push (even for collaborators), so fork this project, 
make changes and don't forget to send `pull request` to this repository. 

If you have questions or have ideas how to improve this project: create `issue`.
