WordsWithRobots
===============

Christopher Mullins <http://christophermullins.net>

WordsWithRobots is a move generator for Words With Friends. It uses [wwf_api](http://github.com/sidoh/wwf_api) to communicate with the game and manipulate game state.

## Move Generation

WordsWithRobots uses a [GADDAG](http://en.wikipedia.org/wiki/GADDAG) to generate all possible moves for a given board. Since wwf_api allows access to the opponent's board, it also employs alpha-beta pruning to look ahead a few moves.

## Usage

Running the `Robot` class starts the robot. You either need to pass it your WWF access token or enter your facebook login credentials. You can configure the default settings in `RobotSettingsKey`. The GADDAG takes a lot of memory, so when you launch it, you'll need to allow the JVM an appropriate amount of RAM (1-2GB seems to be plenty).

You should be able to compile/run it with the following:

```bash
mvn clean compile assembly:single
java -classpath $CLASSPATH:target/words_with_robots-0.1-jar-with-dependencies.jar \
  org.sidoh.words_with_robots.robot.Robot <wwf_access_token>
```
