package hu.nem3d.zincity.Cell;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import hu.nem3d.zincity.Screen.StatUI;

/**
 * Provides base class for cell tiles.
 * @see com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell
 *
 */
public abstract class CityCell extends TiledMapTileLayer.Cell {
    //CLASS FOR EACH TILE ON THE MAP
    //should contain logic data.
    //equivalent with "tile" class in UML diagram. Renamed to avoid confusion.

    protected int x, y; //necessary for selecting tiles

    //It may need a rework, but now for efficiency purposes, those are 2 booleans
    //Obviously, each tile starts with no wire, electricity
    protected boolean isElectrified = false;
    protected boolean isWired = false;

    /**
     * Constructs a CityCell with coordinates of origin
     */
    public CityCell() {
        super();
        this.x = 0;
        this.y = 0;
    }

    /**
     * Constructs a CityCell with parameters set as the coordinates
     * @param x The distance of this from the origin on the horizontal axis
     * @param y The distance of this from the origin on the vertical axis
     */
    public CityCell(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x value of the coordinate of this
     * @return The distance of this from the origin on the horizontal axis
     */
    public int getX() {return x;}

    /**
     * Changes the x value of this to the value of the parameter
     * @param x The distance of this from the origin on the horizontal axis
     */
    public void setX(int x) {this.x = x;}

    /**
     * Gets the y value of the coordinate of this
     * @return The distance of this from the origin on the vertical axis
     */
    public int getY() {return y;}

    /**
     * Changes the x value of this to the value of the parameter
     * @param y The distance of this from the origin on the vertical axis
     */
    public void setY(int y) {this.y = y;}

    /**
     * Returns true if this cell has wires
     * @return True, if this cell has wires
     */
    public boolean isWired() {return isWired;}

    /**
     * Changes the value of isWired to the value of the parameter
     * @param wired The value, which isWired will be set to
     */
    public void setWired(boolean wired) {isWired = wired;}

    /**
     * Returns a true if this cell gets electricity from one of the neighbour of this
     * @return True, if this cell is electrified
     */
    public boolean isElectrified() {return isElectrified;}

    /**
     * Changes the value of isElectrified to the value of the parameter (if this has wires)
     * @param elect The value, which isElectrified will be set to (if it has wires)
     * @return True, if this action is successful
     */
    public boolean setElectrified(boolean elect) {
        if(elect){
            if (isWired){  //Electricity requires wires to flow through
                isElectrified = true;
                return true;
            }else{
                return false;
            }
        }else{
            isElectrified = false;
            return true;
        }

    }

    //Result of merging the Position and the Tile classes from UML

    /**
     * Calculates the distance between two CityCells by air
     * @param c The other Citycell from the same map
     * @return The value of the distance by air of this and c
     */
    public double auraRadiusSize(CityCell c) {
        return Math.sqrt(Math.pow((c.getX() - this.x), 2) + Math.pow((c.getY() - this.y), 2));
    }

    /**
     * Calls the StatUI to call a statCall on this
     * @param statscreen The StatUI that will be called on this
     */
    public void statCall(StatUI statscreen){
        statscreen.statCall(this);
    }

}