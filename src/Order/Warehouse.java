package Order;

import java.util.HashMap;
import java.util.Map;

public class Warehouse {
    String warehouseId;
    Map<Product, Integer> inventory;

    public Warehouse(String warehouseId) {
        this.warehouseId = warehouseId;
        this.inventory = new HashMap<>();
    }

    public boolean hasStock(Product p, int quantity) {
        return inventory.getOrDefault(p, 0) >= quantity;
    }

    @Override
    public String toString() {
        return warehouseId;
    }

    public synchronized boolean reserveInventory(Product p, int quantity) {
        boolean present = hasStock(p, quantity);
        if (!present) {
            return false;
        }
        inventory.put(p, inventory.get(p) - quantity);
        return true;
    }
}