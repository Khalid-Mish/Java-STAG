package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class Location extends GameEntity {

    private List<Character> characters;
    private List<Artefact> artefacts;
    private List<Furniture> furniture;
    private List<Location> from;
    private List<Location> to;

    public Location(String name, String description) {
        super(name, description);
        this.characters = new ArrayList<>();
        this.artefacts = new ArrayList<>();
        this.furniture = new ArrayList<>();
        this.from = new ArrayList<>();
        this.to = new ArrayList<>();
    }

    public void addCharacter(Character character) {
        this.characters.add(character);
    }

    public void removeCharacter(String character) {
        this.characters.removeIf(obj -> obj.getName().equals(character));
    }

    public void addArtefact(Artefact artefact) {
        this.artefacts.add(artefact);
    }

    public void removeArtefact(String artefactName) {
        artefacts.removeIf(obj -> obj.getName().equals(artefactName));
    }

    public void addFurniture(Furniture furniture) {
        this.furniture.add(furniture);
    }

    public void removeFurniture(String furniture) {
        this.furniture.removeIf(obj -> obj.getName().equals(furniture));
    }

    public void addTo(Location location) {
        if (!to.stream().map(GameEntity::getName).toList().contains(location.getName())) {
            this.to.add(location);
        }
    }

    public void addFrom(Location location) {
        if (!from.stream().map(GameEntity::getName).toList().contains(location.getName())) {
            this.from.add(location);
        }
    }


    public List<Character> getCharacters() {
        return characters;
    }

    public List<Artefact> getArtefacts() {
        return artefacts;
    }

    public List<Furniture> getFurniture() {
        return furniture;
    }

    public List<Location> getFrom() {
        return from;
    }

    public List<Location> getTo() {
        return to;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("Location: " + getName() + " (" + getDescription() + ")\n");

        if (!getArtefacts().isEmpty()) {
            output.append("  Artefacts you can see:\n");
            for (Artefact a : getArtefacts()) {
                output.append("   * ").append(a.toString()).append("\n");
            }
        }

        if (!getCharacters().isEmpty()) {
            output.append("  Characters you can see:\n");
            for (Character c : getCharacters()) {
                output.append("   * ").append(c.toString()).append("\n");
            }
        }

        if (!getFurniture().isEmpty()) {
            output.append("  Furniture you can see:\n");
            for (Furniture f : getFurniture()) {
                output.append("   * ").append(f.toString()).append("\n");
            }
        }

        if (!to.isEmpty()) {
            output.append("  From here you can go to:\n");
            for (Location c : to) {
                output.append("   * ").append(c.getName()).append(" (").append(c.getDescription()).append(")\n");
            }
        }

        return output.toString();
    }

}
