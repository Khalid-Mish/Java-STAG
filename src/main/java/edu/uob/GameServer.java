package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;

import javax.swing.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;

public final class GameServer {

    private Map<String, Location> clusters_map;
    private Map<String, Player> players_map;
    private HashMap<String, HashSet<GameAction>> actions = new HashMap<>();
    private String startLocation;
    private static final char END_OF_TRANSMISSION = 4;

    public static void main(String[] args) throws IOException {
        File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Instanciates a new server instance, specifying a game with some configuration files
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    */
    public GameServer(File entitiesFile, File actionsFile) {

        this.clusters_map = new HashMap<>();
        this.players_map = new HashMap<>();

        // parsing the entities file
        FileReader fileReader = null;
        try {
            // entities file
            fileReader = new FileReader(entitiesFile);

            // parse
            Parser p = new Parser();
            p.parse(fileReader);

            Graph layout = p.getGraphs().get(0);

            // locations
            Graph locations = layout.getSubgraphs().get(0);
            // paths
            ArrayList<Edge> paths = layout.getSubgraphs().get(1).getEdges();

            // get clusters (locations)
            ArrayList<Graph> clusters = locations.getSubgraphs();
            // iterate one by one
            for (Graph cluster : clusters) {
                Node locationDetails = cluster.getNodes(true).get(0);

                // location name
                String locationName = locationDetails.getId().getId();
                // description
                String locationDescription = locationDetails.getAttribute("description");

                // if this is the first location
                // mark it as the default location
                Location location = new Location(locationName, locationDescription);
                if (startLocation == null) {
                    startLocation = locationName;
                }

                // finding sub-graphs for characters, artefacts, and furniture
                ArrayList<Graph> entities = cluster.getSubgraphs();
                Graph characters = null;
                Graph artefacts = null;
                Graph furnitures = null;
                for (Graph entity : entities) {
                    switch (entity.getId().getId()) {
                        // characters sub-graph
                        case "characters": {
                            characters = entity;
                            break;
                        }
                        // artefacts sub-graph
                        case "artefacts": {
                            artefacts = entity;
                            break;
                        }
                        // furniture sub-graph
                        case "furniture": {
                            furnitures = entity;
                            break;
                        }
                    }
                }


                // extract data from characters sub-graph
                if (characters != null && characters.getNodes(true).size() != 0) {
                    for (int i = 0; i < characters.getNodes(true).size(); i++) {
                        Node characterDetails = characters.getNodes(true).get(i);
                        // character name
                        String characterName = characterDetails.getId().getId();
                        // description
                        String characterDescription = characterDetails.getAttribute("description");
                        // create new character instance
                        Character character = new Character(characterName, characterDescription);
                        // add to the location
                        location.addCharacter(character);
                    }
                }

                // extract data from artefacts sub-graph
                if (artefacts != null && artefacts.getNodes(true).size() != 0) {
                    for (int i = 0; i < artefacts.getNodes(true).size(); i++) {
                        Node artefactDetails = artefacts.getNodes(true).get(i);
                        String artefactName = artefactDetails.getId().getId();
                        String artefactDescription = artefactDetails.getAttribute("description");
                        // create new artefact instance
                        Artefact artefact = new Artefact(artefactName, artefactDescription);
                        // add to the location
                        location.addArtefact(artefact);
                    }
                }

                // extract data from furniture sub-graph
                if (furnitures != null && furnitures.getNodes(true).size() != 0) {
                    for (int i = 0; i < furnitures.getNodes(true).size(); i++) {
                        Node furnitureDetails = furnitures.getNodes(true).get(i);
                        String furnitureName = furnitureDetails.getId().getId();
                        String furnitureDescription = furnitureDetails.getAttribute("description");
                        // create new furniture instance
                        Furniture furniture = new Furniture(furnitureName, furnitureDescription);
                        // add to the location
                        location.addFurniture(furniture);
                    }
                }

                // add the location to the map
                clusters_map.put(location.getName(), location);

            }

            // for each path
            for (Edge path : paths) {
                // from
                Node fromLocation = path.getSource().getNode();
                String fromName = fromLocation.getId().getId();
                // to
                Node toLocation = path.getTarget().getNode();
                String toName = toLocation.getId().getId();

                // if both names are correct
                if (clusters_map.containsKey(fromName) && clusters_map.containsKey(toName)) {
                    // find locations form the map
                    Location from = clusters_map.get(fromName);
                    Location to = clusters_map.get(toName);
                    // add paths
                    from.addTo(to);
                    to.addFrom(from);
                    // save in the map
                    clusters_map.put(fromName, from);
                    clusters_map.put(toName, to);
                }
            }
        } catch (FileNotFoundException e) {
        } catch (ParseException e) {
        }


        // parsing the actions file
        try {
            // parsing the XML actions config
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(actionsFile);

            // get actions list
            NodeList actionList = document.getElementsByTagName("action");

            for (int i = 0; i < actionList.getLength(); i++) {

                // create a new action instance
                GameAction action = new GameAction();

                // action element
                Element actionEle = (Element) actionList.item(i);

                // Parse <subjects> tag
                Element subjectsTag = (Element) actionEle.getElementsByTagName("subjects").item(0);
                NodeList subjects = subjectsTag.getElementsByTagName("entity");
                for (int j = 0; j < subjects.getLength(); j++) {
                    String subject = subjects.item(j).getTextContent();
                    action.getSubjects().add(subject.replaceAll("\\s", ""));
                }

                // Parse <consumed> tag
                Element consumedTag = (Element) actionEle.getElementsByTagName("consumed").item(0);
                NodeList consumed = consumedTag.getElementsByTagName("entity");
                for (int j = 0; j < consumed.getLength(); j++) {
                    String consumedEntity = consumed.item(j).getTextContent();
                    action.getConsumed().add(consumedEntity.replaceAll("\\s", ""));
                }

                // Parse <produced> tag
                Element producedTag = (Element) actionEle.getElementsByTagName("produced").item(0);
                NodeList produced = producedTag.getElementsByTagName("entity");
                for (int j = 0; j < produced.getLength(); j++) {
                    String producedEntity = produced.item(j).getTextContent();
                    action.getProduced().add(producedEntity.replaceAll("\\s", ""));
                }

                // Parse <narration> tag
                String narration = actionEle.getElementsByTagName("narration").item(0).getTextContent();
                action.setNarration(narration);


                // Parse <triggers> tag
                Element triggersTag = (Element) actionEle.getElementsByTagName("triggers").item(0);
                NodeList triggers = triggersTag.getElementsByTagName("keyphrase");
                for (int j = 0; j < triggers.getLength(); j++) {
                    String trigger = triggers.item(j).getTextContent().replaceAll("\\s", "");
                    GameAction temp = action.copy();
                    temp.setTrigger(trigger);
                    if (this.actions.containsKey(trigger)) {
                        // Key exists
                        HashSet<GameAction> actionSet = actions.get(trigger);
                        actionSet.add(temp);
                    } else {
                        // Key doesn't exist
                        HashSet<GameAction> newActionSet = new HashSet<>();
                        newActionSet.add(temp);
                        this.actions.put(trigger, newActionSet);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * This method handles all incoming game commands and carries out the corresponding actions.</p>
    *
    * @param command The incoming command to be processed
    */
    public String handleCommand(String command) {

        // output
        String output = "[Error]: Invalid action, please try again\n"; // initialize with error message

        String[] temp = command.toLowerCase().split(": ");
        String userName = temp[0];

        Player player = null; // to store the player
        Location location = null; // to store the player's current location

        // If player already exists
        if (players_map.containsKey(userName)) {
            player = players_map.get(userName);
            location = clusters_map.get(player.getLocation());
        }
        // If player does not exist
        else {
            player = new Player(userName);
            player.setLocation(startLocation);
            location = clusters_map.get(startLocation);
            location.addCharacter(player);

            players_map.put(userName, player);
            clusters_map.put(startLocation, location);
        }

        // Basic actions
        boolean basicAction = Arrays.stream(new String[]{"inventory", "inv", "get", "take", "drop", "goto", "look", "health"}).anyMatch(temp[1]::contains);

        if (basicAction) {
            // error handling for basic commands
            boolean validBasicAction = isValidBasicAction(temp);
            if (!validBasicAction) {
                output = "[Error]: Invalid command, please try again\n";
            }
            // health command
            else if (temp[1].contains("health")){
                output = "You have " + player.getHealth() + " health points";
            }
            // inventory & inv commands
            else if (temp[1].contains("inventory") || temp[1].contains("inv")) {
                // set output to player's inventory
                output = player.displayInventory();
            }
            // get & take commands
            else if (temp[1].contains("get") || temp[1].contains("take")) {
                Artefact selectedArtefact = null;
                for (Artefact artefact : location.getArtefacts()) {
                    if (temp[1].contains(artefact.getName())) {
                        selectedArtefact = artefact;
                        break;
                    }
                }
                if (selectedArtefact != null) {
                    player.addInventory(selectedArtefact);
                    location.removeArtefact(selectedArtefact.getName());
                    clusters_map.put(location.getName(), location);
                    output = selectedArtefact.getName() + " is added to the inventory\n";
                } else {
                    output = "[Error]: Can't find the artefact, please try again\n";
                }
            }
            // drop command
            else if (temp[1].contains("drop")) {
                Artefact selectedArtefact = null;
                for (Artefact artefact : player.getInventory()) {
                    if (temp[1].contains(artefact.getName())) {
                        selectedArtefact = artefact;
                        break;
                    }
                }
                if (selectedArtefact != null) {
                    player.removeInventory(selectedArtefact.getName());
                    location.addArtefact(selectedArtefact);
                    clusters_map.put(location.getName(), location);
                    output = selectedArtefact.getName() + " is dropped at " + location.getName() + "\n";
                } else {
                    output = "[Error]: Can't find the artefact in your inventory, please try again\n";
                }

            }
            // goto command
            else if (temp[1].contains("goto")) {
                Location nextLocation = null;
                for (Location nl : location.getTo()) {
                    if (temp[1].contains(nl.getName())) {
                        nextLocation = nl;
                        break;
                    }
                }
                for (Location nl : Player.getUnlocked_locations()) {
                    if (temp[1].contains(nl.getName())) {
                        nextLocation = nl;
                        break;
                    }
                }

                // if that location is available
                if (nextLocation != null) {
                    // set player's current location
                    player.setLocation(nextLocation.getName());
                    // add player to the next location
                    nextLocation.addCharacter(player);
                    // remove player from the previous location
                    location.removeCharacter(player.getName());

                    // update
                    clusters_map.put(nextLocation.getName(), nextLocation);
                    clusters_map.put(location.getName(), location);
                    output = "You have moved to " + nextLocation.getName() + "\n";
                } else {
                    boolean locked = false;
                    for (String ln : clusters_map.keySet()) {
                        if (temp[1].contains(ln)) {
                            locked = true;
                            break;
                        }
                    }
                    if (locked) {
                        output = "[Error]: The target location is locked, please unlock it first\n";
                    } else {
                        output = "[Error]: Can't find a path to the target location, please try again\n";
                    }
                }

            }
            // look command
            else if (temp[1].contains("look")) {
                // set out put to location's info
                output = location.toString();
            }
        }

        // if the command is not a basic command
        else {

            // get all triggers
            Set<String> triggers = this.actions.keySet();
            for (String trigger : triggers) {

                // match correct command
                if (temp[1].contains(trigger)) {

                    // all the entity in the current location and player's inventory
                    List<GameEntity> items = new ArrayList<>();
                    items.addAll(player.getInventory());
                    items.addAll(location.getArtefacts());
                    items.addAll(location.getFurniture());
                    items.addAll(location.getCharacters());

                    // get all actions related to the trigger
                    HashSet<GameAction> actionsSet = this.actions.get(trigger);

                    // iterate one by one
                    for (GameAction action : actionsSet) {

                        boolean validExtendedAction = isValidExtendedAction(temp, action);

                        if(!validExtendedAction) {
                            output = "[Error]: Invalid command, please try again\n";
                        }
                        else {
                            // subjects
                            List<String> subjects = action.getSubjects();
                            // consumed
                            List<String> consumed = action.getConsumed();
                            // produced
                            List<String> produced = action.getProduced();

                            // if items contains the required subjects
                            // then, this the correct Action
                            if (items.stream().map(GameEntity::getName).toList().containsAll(subjects)) {
                                Location storeroom = clusters_map.get("storeroom");

                                // remove the consumed entities
                                for (String con : consumed) {

                                    if(con.equals("health")) {
                                        player.healthDrop();
                                    }

                                    // if health == 0
                                    // drop inventory
                                    // and move back to the starting location
                                    if(player.getHealth() == 0) {
                                        List<Artefact> inv = player.getInventory();
                                        List<Artefact> toBeRemoved = new ArrayList<>();
                                        for(Artefact artefact: inv) {
                                            // add artefact back to the current location
                                            clusters_map.get(location.getName()).addArtefact(artefact);
                                            toBeRemoved.add(artefact);
                                        }
                                        for (Artefact artefact: toBeRemoved) {
                                            // drop from the inv
                                            player.removeInventory(artefact.getName());
                                        }

                                        // reset health
                                        player.healthUp();
                                        player.healthUp();
                                        player.healthUp();
                                        // move player back to the original location
                                        player.setLocation(startLocation);
                                        clusters_map.get(location.getName()).removeCharacter(player.getName());
                                        clusters_map.get(startLocation).addCharacter(player);

                                        return "You have died and lost all the items in your inventory\n";

                                    }
                                    // otherwise
                                    else {

                                        for (Artefact tmpArtefact : location.getArtefacts()) {
                                            if (tmpArtefact.getName().equals(con)) {
                                                // move to store room
                                                if (storeroom != null) {
                                                    storeroom.addArtefact(tmpArtefact);
                                                }
                                                location.removeArtefact(con);
                                                break;
                                            }
                                        }
                                        for (Character tmpCharacter : location.getCharacters()) {
                                            if (tmpCharacter.getName().equals(con)) {
                                                // move to store room
                                                if (storeroom != null) {
                                                    storeroom.addCharacter(tmpCharacter);
                                                }
                                                location.removeCharacter(con);
                                                break;
                                            }
                                        }
                                        for (Furniture tmpFurniture : location.getFurniture()) {
                                            if (tmpFurniture.getName().equals(con)) {
                                                // move to store room
                                                if (storeroom != null) {
                                                    storeroom.addFurniture(tmpFurniture);
                                                }
                                                location.removeFurniture(con);
                                                break;
                                            }
                                        }
                                        for (Artefact tmpArtefact : player.getInventory()) {
                                            if (tmpArtefact.getName().equals(con)) {
                                                // move to store room
                                                if (storeroom != null) {
                                                    storeroom.addArtefact(tmpArtefact);
                                                }
                                                player.removeInventory(con);
                                                break;
                                            }
                                        }
                                    }
                                }

                                // update the store room
                                if (storeroom != null) {
                                    clusters_map.put(storeroom.getName(), storeroom);
                                }

                                for (String prod : produced) {

                                    if(prod.equals("health")) {
                                        player.healthUp();
                                    }
                                    if (clusters_map.containsKey(prod)) {
                                        Location newLocation = clusters_map.get(prod);
                                        location.addTo(newLocation);
                                        newLocation.addFrom(location);
                                        player.addUnlockedLocation(newLocation);
                                        clusters_map.put(location.getName(), location);
                                        clusters_map.put(newLocation.getName(), newLocation);
                                    }

                                    GameEntity entity = null;
                                    if (clusters_map.containsKey(prod)) {
                                        player.addUnlockedLocation(clusters_map.get(prod));
                                        players_map.put(player.getName(), player);
                                    } else {
                                        for (Location l : clusters_map.values()) {
                                            List<Artefact> a = l.getArtefacts();
                                            List<Furniture> f = l.getFurniture();
                                            List<Character> c = l.getCharacters();

                                            // if the entity is an artefact
                                            for (Artefact t : a) {
                                                if (t.getName().equals(prod)) {
                                                    entity = t;
                                                    l.removeArtefact(prod);
                                                    location.addArtefact(t);
                                                    break;
                                                }
                                            }
                                            // if the entity is a furniture
                                            if (entity == null) {
                                                for (Furniture t : f) {
                                                    if (t.getName().equals(prod)) {
                                                        entity = t;
                                                        l.removeFurniture(prod);
                                                        location.addFurniture(t);
                                                        break;
                                                    }
                                                }
                                            }
                                            // if the entity is a character
                                            if (entity == null) {
                                                for (Character t : c) {
                                                    if (t.getName().equals(prod)) {
                                                        entity = t;
                                                        l.removeCharacter(prod);
                                                        location.addCharacter(t);
                                                        break;
                                                    }
                                                }
                                            }

                                            if (entity != null) {
                                                // update the current location
                                                clusters_map.put(location.getName(), location);
                                                break;
                                            }
                                        }
                                    }
                                }
                                // set the output
                                output = action.getNarration() + "\n";
                                break;
                            } else {
                                output = "[Error]: Please collect all the required entities to trigger the command: " + trigger + " \n";
                            }

                        }
                    }
                    break;
                }
            }

        }

        return output;
    }

    private static boolean isValidBasicAction(String[] command) {
        String[] parts = command[1].split(" ");
        boolean validInv = (parts[0].equals("inventory") || parts[0].equals("inv")) && (parts.length == 1);
        boolean validGetTake = (parts[0].equals("get") || parts[0].equals("take")) && (parts.length == 2);
        boolean validDrop = parts[0].equals("drop") && (parts.length == 2);
        boolean validGoTo = parts[0].equals("goto") && (parts.length == 2);
        boolean validLook = parts[0].equals("look") && (parts.length == 1);
        boolean validhealth = parts[0].equals("health") && (parts.length == 1);
        return validInv || validGetTake || validDrop || validGoTo || validLook || validhealth;
    }

    private static boolean isValidExtendedAction(String[] command, GameAction action) {
        List<String> parts = new ArrayList<>(Arrays.asList(command[1].split(" ")));
        parts.removeAll(Arrays.asList("with", "the", "using", "use", "of", "a", "at", "to", "this", "that", "my", "please", " ", null));
        parts.remove(0);
        List<String> subjects = action.getSubjects();
        List<String> consumed = action.getConsumed();

        boolean conditionOne = parts.size() == 1 || parts.size() == 2;
        boolean conditionTwo = parts.size() == 1 && subjects.containsAll(parts);
        boolean conditionThree = parts.size() == 2 && subjects.containsAll(parts) && (consumed.contains(parts.get(1)) || consumed.contains(parts.get(0)));

        return conditionOne && (conditionTwo || conditionThree);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Starts a *blocking* socket server listening for new connections.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Handles an incoming connection from the socket server.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println("Received message from " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
