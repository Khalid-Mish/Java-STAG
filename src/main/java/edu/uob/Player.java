package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class Player extends Character {

    private String location;
    private int health;
    private List<Artefact> inventory;
    private static List<Location> unlocked_locations = new ArrayList<>();

    public Player(String name) {
        super(name, "A player");
        this.health = 3;
        this.inventory = new ArrayList<>();
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void addInventory(Artefact artefact) {
        this.inventory.add(artefact);
    }

    public void removeInventory(String artefact) {
        this.inventory.removeIf(obj -> obj.getName().equals(artefact));
    }

    public static void addUnlockedLocation(Location location) {
        unlocked_locations.add(location);
    }

    public static List<Location> getUnlocked_locations() {
        return unlocked_locations;
    }

    public List<Artefact> getInventory() {
        return inventory;
    }

    public void healthUp() {
        if (this.health < 3) {
            this.health++;
        }
    }

    public void healthDrop() {
        if (this.health > 0) {
            this.health--;
        }
    }

    public int getHealth() {
        return health;
    }

    public String displayInventory() {
        if(inventory.size() == 0) {
            return "Your inventory is empty\n";
        }

        String output = "Inventory contains the following items: \n";
        for (Artefact artefact : inventory) {
            output += " * " + artefact.toString() + "\n";
        }
        return output;
    }
}
