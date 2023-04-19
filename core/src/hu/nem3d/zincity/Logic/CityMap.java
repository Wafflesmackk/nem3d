package hu.nem3d.zincity.Logic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import hu.nem3d.zincity.Cell.*;
import hu.nem3d.zincity.Misc.OpenSimplex2S;
import hu.nem3d.zincity.Misc.TextureTiles;

import java.util.*;


public class CityMap {
    TiledMap map;
    TiledMapTileLayer baseLayer; //contains Cell objects that point to TiledMapTile objects

    TiledMapTileLayer buildingLayer; //more layers can be added on demand
    TiledMapTileSet tileSet; //contains TiledMapTile objects.
    Texture texture;
    public CityMap() {
        FileHandle handle = Gdx.files.internal("texture.png");
        texture = new Texture(handle.path());

        //create the tileset
        tileSet = new TiledMapTileSet();
        int i = 0, j = 0;
        for (TextureTiles item : TextureTiles.values()) {
            if (i > 9){ //since there are 10 columns in the texture file.
                i = 0;
                j++;
            }
            tileSet.putTile(item.ordinal(), new StaticTiledMapTile(new TextureRegion(texture, i*24, j*24, 24,24)));
            i++;

        }

        //create the layer
        baseLayer = new TiledMapTileLayer(30,20,24,24);
        buildingLayer = new TiledMapTileLayer(30,20,24,24);

        Random r = new Random();
        long seedWater = r.nextLong();
        long seedTrees = r.nextLong();
        for ( i = 0; i < 30; i++) {
            for ( j = 0; j < 20; j++) {
                boolean isEmpty = false;
                if (OpenSimplex2S.noise2(seedWater, i*0.05, j*0.05) > -0.3){ //change these threshold values to modify world gen
                    CityCell cell = new EmptyCell(i,j);
                    cell.setTile(tileSet.getTile(0));
                    cell.setX(i);
                    cell.setY(j);
                    baseLayer.setCell(i, j, cell);
                    isEmpty = true;
                }
                else{
                    CityCell cell = new BlockedCell(i,j,"");
                    cell.setTile(tileSet.getTile(1));
                    cell.setX(i);
                    cell.setY(j);
                    baseLayer.setCell(i, j, cell);
                }

                if (OpenSimplex2S.noise2(seedTrees, i*0.05, j*0.05) > 0.7  ){
                    //add dense forest
                    CityCell cell = new ForestCell(i,j);
                    cell.setTile((tileSet.getTile(3)));
                    cell.setX(i);
                    cell.setY(j);
                    buildingLayer.setCell(i,j,cell);
                }
                else if (OpenSimplex2S.noise2(seedTrees, i*0.05, j*0.05) > 0.6){
                    //add sparse forest
                    CityCell cell = new ForestCell(i,j);
                    cell.setTile((tileSet.getTile(2)));
                    cell.setX(i);
                    cell.setY(j);
                    buildingLayer.setCell(i,j,cell);
                }
                else if(isEmpty){
                    CityCell cell = new EmptyCell(i,j);
                    cell.setX(i);
                    cell.setY(j);
                    buildingLayer.setCell(i, j, cell);
                }
                else{
                    CityCell cell = new BlockedCell(i,j,"");
                    cell.setX(i);
                    cell.setY(j);
                    buildingLayer.setCell(i, j, cell);
                }



            }
        }

        //adds some tiles for testing purposes
        LivingZoneCell testLivingZoneCell = new LivingZoneCell(4);
        testLivingZoneCell.setTile(tileSet.getTile(5)); //tudhatná a saját textúráját...
        buildingLayer.setCell(0,0,testLivingZoneCell);
        testLivingZoneCell.setX(0);
        testLivingZoneCell.setY(0);


        IndustrialZoneCell testIndustrialZoneCell = new IndustrialZoneCell(2);
        testIndustrialZoneCell.setTile(tileSet.getTile(8));
        buildingLayer.setCell(1,0,testIndustrialZoneCell);
        testIndustrialZoneCell.setX(1);
        testIndustrialZoneCell.setY(0);


        ServiceZoneCell testServiceZoneCell = new ServiceZoneCell(2);
        testServiceZoneCell.setTile(tileSet.getTile(11));
        buildingLayer.setCell(2,0,testServiceZoneCell);
        testServiceZoneCell.setX(2);
        testServiceZoneCell.setY(0);

        for (i = 0; i < 3; i++){
            RoadCell testRoadCell = new RoadCell();
            testRoadCell.setTile(tileSet.getTile(4));
            testRoadCell.setX(i);
            testRoadCell.setY(1);
            buildingLayer.setCell(i,1,testRoadCell);
        }

        map = new TiledMap();
        map.getLayers().add(baseLayer);
        map.getLayers().add(buildingLayer);
    }
    public TiledMap getMap() {
        return map;
    }

    public TiledMapTileLayer getBuildingLayer() {
        return buildingLayer;
    }

    public void setBuildingLayer(TiledMapTileLayer buildingLayer) {
        this.buildingLayer = buildingLayer;
    }

    public TiledMapTileSet getTileSet() {
        return tileSet;
    }

    public ZoneCell closestWorkplaceFrom(LivingZoneCell home, boolean isIndustrial, boolean mustBeAvailable) {
        return (closestOfWorkplaceWithDistance(home,
                (isIndustrial ? IndustrialZoneCell.class : ServiceZoneCell.class), mustBeAvailable).getKey());
    }

    public int shortestWorkplaceDistanceFrom(LivingZoneCell home, boolean isIndustrial, boolean mustBeAvailable) {
        return (closestOfWorkplaceWithDistance(home,
                (isIndustrial ? IndustrialZoneCell.class : ServiceZoneCell.class), mustBeAvailable)).getValue();
    }

    private MapEntry<ZoneCell, Integer> closestOfWorkplaceWithDistance(LivingZoneCell home, Class<?> workplaceType,
                                                    boolean mustBeAvailable) {

        //Setting up the distances matrix with -1 values, this will represent the unreachable tiles
        int[][] distances = new int[buildingLayer.getWidth()][buildingLayer.getHeight()];
        for(int i = 0; i < buildingLayer.getWidth(); ++i) {
            Arrays.fill(distances[i], -1);
        }

        distances[home.getX()][home.getY()] = 0;

        ZoneCell destination = null;

        //Queue always has Citycells "sorted" by their distances in increasing order (It's standard FIFO queue.)
        LinkedList<CityCell> queue = new LinkedList<>();
        queue.add(home);

        while(!queue.isEmpty()) {
            CityCell current = queue.remove(0);

            if(current.getClass() == workplaceType) {
                destination = (ZoneCell) current;
                if(mustBeAvailable) {
                    if(!destination.isFull()){
                        break;
                    }else{
                        destination = null;
                    }
                }else{
                    break;
                }
            }

            queue.addAll(getGoodNeighbours(current, workplaceType, distances));

        }

        return (new MapEntry(destination, distances[destination.getX()][destination.getY()]));
    }



    private List<CityCell> getGoodNeighbours(CityCell me, Class<?> workplaceType, int[][] distances){
        ArrayList<CityCell> result = new ArrayList<>();

        int distNow = distances[me.getX()][me.getY()];

        CityCell current;

        if(isReachable(me.getX()+1, me.getY())) {
            if(distances[me.getX()+1][me.getY()] == -1) {
                current = (CityCell) buildingLayer.getCell(me.getX()+1, me.getY());
                if(current.getClass() == RoadCell.class || current.getClass() == workplaceType) {
                    distances[me.getX()+1][me.getY()] = distNow + 1;
                    result.add(current);
                }
            }
        }

        if(isReachable(me.getX()-1, me.getY())) {
            if(distances[me.getX()-1][me.getY()] == -1) {
                current = (CityCell) buildingLayer.getCell(me.getX()-1, me.getY());
                if(current.getClass() == RoadCell.class || current.getClass() == workplaceType) {
                    distances[me.getX()-1][me.getY()] = distNow + 1;
                    result.add(current);
                }
            }
        }

        if(isReachable(me.getX(), me.getY()+1)) {
            if(distances[me.getX()][me.getY()+1] == -1) {
                current = (CityCell) buildingLayer.getCell(me.getX(), me.getY()+1);
                if(current.getClass() == RoadCell.class || current.getClass() == workplaceType) {
                    distances[me.getX()][me.getY()+1] = distNow + 1;
                    result.add(current);
                }
            }
        }

        if(isReachable(me.getX(), me.getY()-1)) {
            if(distances[me.getX()][me.getY()-1] == -1) {
                current = (CityCell) buildingLayer.getCell(me.getX(), me.getY()-1);
                if(current.getClass() == RoadCell.class || current.getClass() == workplaceType) {
                    distances[me.getX()][me.getY()-1] = distNow + 1;
                    result.add(current);
                }
            }
        }

        return result;
    }

    public boolean isReachable(int x, int y) {
        return (x >= 0 && x < buildingLayer.getWidth() && y >= 0 && y < buildingLayer.getHeight());
    }

    private class MapEntry<K, V>{
        private K key;
        private V value;

        MapEntry(K key, V value){
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
