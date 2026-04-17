#!/bin/bash
# Run pure-JVM tests (no Android runtime needed)
set -e
cd "$(dirname "$0")"

echo "=== Compiling tests (pure JVM, no Android jar) ==="
mkdir -p testobj

# Compile only the game-logic sources (no android imports)
GAME_SOURCES="src/com/pacman/classic/game/Direction.java
src/com/pacman/classic/game/GhostMode.java
src/com/pacman/classic/game/Maze.java
src/com/pacman/classic/game/PacMan.java
src/com/pacman/classic/game/Ghost.java
src/com/pacman/classic/game/Blinky.java
src/com/pacman/classic/game/Pinky.java
src/com/pacman/classic/game/Inky.java
src/com/pacman/classic/game/Clyde.java
src/com/pacman/classic/game/GameEngine.java"

# GameEngine references android.content.Context via AudioHelper indirectly,
# but GameEngine itself is clean. Compile with a stub for Context.
# We use a stub android jar for test compilation.
STUB_JAR="/usr/lib/android-sdk/platforms/android-23/android.jar"

javac \
    --release 8 \
    -classpath "$STUB_JAR" \
    -d testobj \
    $GAME_SOURCES \
    test/com/pacman/classic/test/MazeTest.java \
    test/com/pacman/classic/test/GhostAITest.java \
    test/com/pacman/classic/test/ScoringTest.java \
    test/com/pacman/classic/test/PowerUpTest.java

echo "=== Running MazeTest ==="
java -cp testobj:"$STUB_JAR" com.pacman.classic.test.MazeTest

echo "=== Running GhostAITest ==="
java -cp testobj:"$STUB_JAR" com.pacman.classic.test.GhostAITest

echo "=== Running ScoringTest ==="
java -cp testobj:"$STUB_JAR" com.pacman.classic.test.ScoringTest

echo "=== Running PowerUpTest ==="
java -cp testobj:"$STUB_JAR" com.pacman.classic.test.PowerUpTest

echo ""
echo "=== All tests passed ==="
