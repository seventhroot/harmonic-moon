package io.github.lucariatias.harmonicmoon.world;

import io.github.lucariatias.harmonicmoon.HarmonicMoon;
import io.github.lucariatias.harmonicmoon.block.Block;
import io.github.lucariatias.harmonicmoon.character.CharacterWorldInfo;
import io.github.lucariatias.harmonicmoon.door.Door;
import io.github.lucariatias.harmonicmoon.tile.Tile;
import io.github.lucariatias.harmonicmoon.tile.TileLayer;
import io.github.lucariatias.harmonicmoon.tile.TileSheet;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class World {

    private HarmonicMoon harmonicMoon;
    private String name;
    private Map<TileLayer, BufferedImage> tileMaps;
    private BufferedImage objectMap;
    private TileSheet tileSheet;

    private Map<TileLayer, Set<Tile>> tiles = new EnumMap<>(TileLayer.class);
    private Set<WorldObject> objects = new HashSet<>();

    public World(HarmonicMoon harmonicMoon, String name, Map<TileLayer, BufferedImage> tileMaps, BufferedImage objectMap, TileSheet tileSheet) {
        this.harmonicMoon = harmonicMoon;
        this.name = name;
        this.tileMaps = tileMaps;
        this.objectMap = objectMap;
        this.tileSheet = tileSheet;
    }

    public String getName() {
        return name;
    }

    public void onTick() {
        for (WorldObject object : objects) {
            object.onTick();
        }
    }

    public BufferedImage getTileMap(TileLayer layer) {
        return tileMaps.get(layer);
    }

    public Set<Tile> getTiles(TileLayer layer) {
        if (tiles.get(layer) == null) {
            tiles.put(layer, new HashSet<Tile>());
        }
        return tiles.get(layer);
    }

    public void render(Graphics graphics) {
        for (Tile tile : getTiles(TileLayer.BACK)) {
            tile.render(graphics, TileLayer.BACK);
        }
        for (Tile tile : getTiles(TileLayer.BACK_TOP)) {
            tile.render(graphics, TileLayer.BACK_TOP);
        }
        WorldObject[] sortedObjects = objects.toArray(new WorldObject[objects.size()]);
        quickSort(sortedObjects);
        for (WorldObject object : sortedObjects) {
            if (harmonicMoon.getCamera().getLocation().distanceSquared(object.getLocation()) <= 640000 && object.getLocation().getX() >= harmonicMoon.getCamera().getLocation().getX() - 32 && object.getLocation().getY() >= harmonicMoon.getCamera().getLocation().getY() - 32) {
                object.render(graphics);
            }
        }
        for (Tile tile : getTiles(TileLayer.FRONT)) {
            tile.render(graphics, TileLayer.FRONT);
        }
        for (Tile tile : getTiles(TileLayer.FRONT_TOP)) {
            tile.render(graphics, TileLayer.FRONT_TOP);
        }
    }

    public Set<WorldObject> getObjects() {
        return objects;
    }

    public void addObject(WorldObject object) {
        objects.add(object);
        if (object instanceof CharacterWorldInfo) harmonicMoon.debug(((CharacterWorldInfo) object).getCharacter().getName() + " added to " + getName());
    }

    public void removeObject(WorldObject object) {
        objects.remove(object);
        if (object instanceof CharacterWorldInfo) harmonicMoon.debug(((CharacterWorldInfo) object).getCharacter().getName() + " removed from " + getName());
    }

    private int partition(WorldObject[] objects, int left, int right) {
        int i = left, j = right;
        WorldObject tmp;
        WorldObject pivot = objects[(left + right) / 2];
        while (i <= j) {
            while (objects[i].getLocation().getY() < pivot.getLocation().getY()) i++;
            while (objects[j].getLocation().getY() > pivot.getLocation().getY()) j--;
            if (i <= j) {
                tmp = objects[i];
                objects[i] = objects[j];
                objects[j] = tmp;
                i++;
                j--;
            }
        }
        return i;
    }

    private void quickSort(WorldObject[] objects, int left, int right) {
        int index = partition(objects, left, right);
        if (left < index - 1) quickSort(objects, left, index - 1);
        if (index < right) quickSort(objects, index, right);
    }

    private void quickSort(WorldObject[] objects) {
        quickSort(objects, 0, objects.length - 1);
    }

    public void populate() {
        populateTiles(TileLayer.BACK);
        populateTiles(TileLayer.BACK_TOP);
        populateObjects();
        populateTiles(TileLayer.FRONT);
        populateTiles(TileLayer.FRONT_TOP);
    }

    private void populateTiles(TileLayer layer) {
        BufferedImage tileMap = getTileMap(layer);
        int width = tileMap.getWidth();
        int height = tileMap.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = tileMap.getRGB(x, y);
                Color colour = new Color((pixel >> 16) & 0xff, (pixel >> 8) & 0xff, pixel & 0xff);
                if (!colour.equals(Color.BLACK) || layer == TileLayer.BACK) {
                    Tile tile = tileSheet.getTile(colour.getRed(), colour.getGreen());
                    tile.getLocations(layer).add(new WorldLocation(this, x * 16, y * 16));
                    getTiles(layer).add(tile);
                }
            }
        }
        tileMap.flush();
    }

    private void populateObjects() {
        int width = objectMap.getWidth();
        int height = objectMap.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = objectMap.getRGB(x, y);
                Color colour = new Color((pixel >> 16) & 0xff, (pixel >> 8) & 0xff, pixel & 0xff);
                WorldObject object = getObjectFromColour(colour);
                if (object != null) {
                    object.setLocation(new WorldLocation(this, x * 16, y * 16));
                    addObject(object);
                }
            }
        }
        objectMap.flush();
    }

    private WorldObject getObjectFromColour(Color colour) {
        switch (colour.getRed()) {
            case 0:
                switch (colour.getGreen()) {
                    case 0:
                        switch (colour.getBlue()) {
                            case 0: return null;
                            case 1: return new Block();
                            case 2: return new Door(harmonicMoon);
                            default: return null;
                        }
                    case 1:
                        switch (colour.getBlue()) {
                            case 0: return harmonicMoon.getCharacterManager().getCharacter("lonyre").world();
                            case 1: return harmonicMoon.getCharacterManager().getCharacter("tivor").world();
                            case 2: return harmonicMoon.getCharacterManager().getCharacter("kesowa").world();
                            case 3: return harmonicMoon.getCharacterManager().getCharacter("namapo").world();
                            case 4: return harmonicMoon.getCharacterManager().getCharacter("syalae").world();
                            case 5: return harmonicMoon.getCharacterManager().getCharacter("anaria").world();
                            case 6: return harmonicMoon.getCharacterManager().getCharacter("idain").world();
                            case 7: return harmonicMoon.getCharacterManager().getCharacter("seuri").world();
                        }
                    default: return null;
                }
            default: return null;
        }
    }

}
