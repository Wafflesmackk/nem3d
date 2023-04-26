package hu.nem3d.zincity.Cell;

public class ArenaCell extends BuildingCell {

    public ArenaCell(int x, int y, int range, int maintenanceFee) {
        super(x, y, range, false);
        this.name = "Arena";
        this.price = 50;
        this.upkeepCost = 20;
    }

    public ArenaCell(int x, int y, int range, int maintenanceFee, BuildingPart part) {
        super(x, y, range, maintenanceFee, part);
        this.name = "Arena";
        this.price = 50;
        this.upkeepCost = 20;
    }
}
