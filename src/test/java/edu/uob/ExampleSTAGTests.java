package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

class ExampleSTAGTests {

  private GameServer server;

  // Create a new server _before_ every @Test
  @BeforeEach
  void setup() {
      File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
      File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
      server = new GameServer(entitiesFile, actionsFile);
  }

  String sendCommandToServer(String command) {
      // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
      return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
      "Server took too long to respond (probably stuck in an infinite loop)");
  }

  // A lot of tests will probably check the game state using 'look' - so we better make sure 'look' works well !
  @Test
  void testLook() {
    String response = sendCommandToServer("simon: look");
    response = response.toLowerCase();
    assertTrue(response.contains("cabin"), "Did not see the name of the current room in response to look");
    assertTrue(response.contains("log cabin"), "Did not see a description of the room in response to look");
    assertTrue(response.contains("magic potion"), "Did not see a description of artifacts in response to look");
    assertTrue(response.contains("wooden trapdoor"), "Did not see description of furniture in response to look");
    assertTrue(response.contains("forest"), "Did not see available paths in response to look");
  }

  // Test that we can pick something up and that it appears in our inventory
  @Test
  void testGet()
  {
      String response;
      sendCommandToServer("simon: get potion");
      response = sendCommandToServer("simon: inv");
      response = response.toLowerCase();
      assertTrue(response.contains("potion"), "Did not see the potion in the inventory after an attempt was made to get it");
      response = sendCommandToServer("simon: look");
      response = response.toLowerCase();
      assertFalse(response.contains("potion"), "Potion is still present in the room after an attempt was made to get it");
  }

  // Test that we can goto a different location (we won't get very far if we can't move around the game !)
  @Test
  void testGoto()
  {
      sendCommandToServer("simon: goto forest");
      String response = sendCommandToServer("simon: look");
      response = response.toLowerCase();
      assertTrue(response.contains("key"), "Failed attempt to use 'goto' command to move to the forest - there is no key in the current location");
  }

    @Test
    void testDrop()
    {
        String response;
        sendCommandToServer("simon: get potion");
        response = sendCommandToServer("simon: inv");
        response = response.toLowerCase();
        assertTrue(response.contains("potion"), "Did not see the potion in the inventory after an attempt was made to get it");
        sendCommandToServer("simon: drop potion");
        response = sendCommandToServer("simon: inv");
        response = response.toLowerCase();
        assertFalse(response.contains("potion"), "Potion is still present in the inventory after an attempt was made to drop it");
        response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertTrue(response.contains("potion"), "Did not see the potion in the room after an attempt was made to drop it");
    }

    @Test
    void testUnlockCommand() {
        String response;
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        response = sendCommandToServer("simon: unlock with key");
        assertTrue(response.toLowerCase().contains("cellar"), "Failed to unlock the trapdoor with the key");
    }

    @Test
    void openCellarTest() {
        String response;
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: unlock trapdoor with key");
        sendCommandToServer("simon: goto cellar");
        response = sendCommandToServer("simon: look");
        assertTrue(response.contains("cellar"), "Simon should be in the cellar");
    }

    @Test
    void testTwoPlayers()
    {
        String response;
        sendCommandToServer("simon: look");
        sendCommandToServer("simon: inv");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("sion: look");
        sendCommandToServer("sion: goto forest");
        response = sendCommandToServer("sion: look");
        response = response.toLowerCase();
        assertTrue(response.contains("simon"), "Simon isn't here?!? Simon is invisible!");
    }

    @Test
    void testAxePresent()
    {
        String response;
        response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertTrue(response.contains("axe"), "There is supposed to be an axe in the cabin but one was not found");
    }

    @Test
    void testCoinPresent()
    {
        String response;
        response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertTrue(response.contains("coin"), "There is supposed to be a coin in the cabin but one was not found");
    }

    @Test
    void testPayElf()
    {
        String response;
        sendCommandToServer("simon: get coin");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: unlock trapdoor");
        sendCommandToServer("simon: goto cellar");
        sendCommandToServer("simon: pay elf");
        response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertTrue(response.contains("shovel"), "You are supposed to receive a shovel after paying the elf");
    }

    @Test
    void testTreeChop()
    {
        String response;
        sendCommandToServer("simon: get axe");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: chop tree");
        sendCommandToServer("simon: get log");
        response = sendCommandToServer("simon: inv");
        response = response.toLowerCase();
        assertTrue(response.contains("log"), "A log is supposed to be in your inventory but was not found");
    }

    @Test
    void testInvalidGoto() {
        String response;
        sendCommandToServer("simon: goto moon");
        response = sendCommandToServer("simon: look");
        assertTrue(response.toLowerCase().contains("cabin"), "Should not be able to go to an invalid location");
    }

    @Test
    void testThreePlayers()
    {
        String response;
        sendCommandToServer("simon: look");
        sendCommandToServer("simon: inv");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("sion: look");
        sendCommandToServer("sion: goto forest");
        sendCommandToServer("mish: look");
        sendCommandToServer("mish: goto forest;");
        response = sendCommandToServer("sion: look");
        response = response.toLowerCase();
        assertTrue(response.contains("simon"), "Simon isn't here?!? Simon is invisible!");
        assertTrue(response.contains("mish"), "Mish isn't here?!? Mish is invisible!");
    }

    @Test
    void testCharacterProduced()
    {
        String response;
        sendCommandToServer("simon: look");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: goto riverbank");
        sendCommandToServer("simon: get horn");
        sendCommandToServer("simon: blow horn");
        response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertTrue(response.contains("lumberjack"), "The lumberjack should have appeared after blowing the horn");

    }

    @Test
    void testLocationProduced()
    {
        String response;
        sendCommandToServer("simon: look");
        sendCommandToServer("simon: get axe");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: chop tree");
        sendCommandToServer("simon: get log");
        sendCommandToServer("simon: goto riverbank");
        sendCommandToServer("simon: bridge river with log");
        response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertTrue(response.contains("clearing"), "The clearing should have been produced after bridging the river");

    }

    @Test
    void testDecoratedCommand()
    {
        String response;
        sendCommandToServer("simon: look");
        sendCommandToServer("simon: get axe");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: please chop the tree using the axe");
        response = sendCommandToServer("simon: look");
        assertFalse(response.contains("tree"), "The tree should have been chopped down");
    }

    @Test
    void testPlayerHealthDrop()
    {
        String response;
        sendCommandToServer("simon: look");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: unlock trapdoor");
        sendCommandToServer("simon: goto cellar");
        sendCommandToServer("simon: attack elf");
        response = sendCommandToServer("simon: health");
        assertTrue(response.contains("2"), "The players health should have decreased");
    }

    @Test
    void testPlayerHealthGain()
    {
        String response;
        sendCommandToServer("simon: look");
        sendCommandToServer("simon: get potion");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: unlock trapdoor");
        sendCommandToServer("simon: goto cellar");
        sendCommandToServer("simon: attack elf");
        sendCommandToServer("simon: attack elf");
        sendCommandToServer("simon: drink potion");
        response = sendCommandToServer("simon: health");
        assertTrue(response.contains("2"), "The players health should be on 2 after drinking the potion");
    }

    @Test
    void testPlayerMaximumHealth()
    {
        String response;
        sendCommandToServer("simon: look");
        sendCommandToServer("simon: get potion");
        sendCommandToServer("simon: drink potion");
        response = sendCommandToServer("simon: health");
        assertTrue(response.contains("3"), "The players health should not increase past 3");
        assertFalse(response.contains("4"), "The players health should not increase past 3");
    }

    @Test
    void testPlayerDeathLocationReset()
    {
        String response;

        sendCommandToServer("simon: look");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: unlock trapdoor");
        sendCommandToServer("simon: goto cellar");
        sendCommandToServer("simon: attack elf");
        sendCommandToServer("simon: attack elf");
        sendCommandToServer("simon: attack elf");
        response = sendCommandToServer("simon: look");

        assertTrue(response.contains("cabin"), "The player should have died and been returned to the cabin");
    }

    @Test
    void testPlayerDeathInvReset()
    {
        String response;

        sendCommandToServer("simon: look");
        sendCommandToServer("simon: get axe");
        sendCommandToServer("simon: get potion");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: unlock trapdoor");
        sendCommandToServer("simon: goto cellar");
        sendCommandToServer("simon: attack elf");
        sendCommandToServer("simon: attack elf");
        sendCommandToServer("simon: attack elf");
        sendCommandToServer("simon: look");
        response = sendCommandToServer("simon: inv");

        assertFalse(response.contains("axe"), "The player should have died and dropped their inventory");
        assertFalse(response.contains("potion"), "The player should have died and dropped their inventory");
    }
}
