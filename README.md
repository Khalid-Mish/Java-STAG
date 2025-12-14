# Java Simple Text Adventure Game

This project folder contains a text-based adventure game inspired by classic games like Zork.

## How to use:
This project consists of a server and a client, both of which must be run in order to start the server and have the user connect to it. Both of these can be run from the command line. The game features multiplayer and multiple clients can connect to the server if run with different arguments, which will serve as the player's name.
### Connect to the server from the command line:
    ./mvnw exec:java@server
### Connect to the client from the command line:
    ./mvnw exec:java@client
  The server does not save the game when terminated. The world. all characters, items and states are reset when the server is restarted.

## Playing the game!

## Commands

### Basic Commands
- **inventory** or **inv**: Displays the items currently in your inventory.
- **get <artefact>** or **take <artefact>**: Picks up the specified artefact from the current location and adds it to your inventory.
- **drop <artefact>**: Removes the specified artefact from your inventory and places it in the current location.
- **goto <location>**: Moves your player to the specified connected location.
- **look**: Shows a description of the current location, including any entities present.
- **health**: Displays your current health points.

### Extended Commands

Many other extended commands exist in the game which can be used contextually depending on which location the player is in, what items are present and what action they would like to perform, such as attacking a monster, chopping down a tree or drinking a health potion.

