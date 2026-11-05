package Order;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Product laptop = new Product("Laptop");
        Product mouse = new Product("Mouse");

        Warehouse w1 = new Warehouse("Noida");
        w1.inventory.put(laptop, 5);

        Warehouse w2 = new Warehouse("Delhi");
        w2.inventory.put(mouse, 10);

        Warehouse w3 = new Warehouse("Jaipur");

        List<Warehouse> allWarehouses = new ArrayList<>();
        allWarehouses.add(w1);
        allWarehouses.add(w2);
        allWarehouses.add(w3);

        Order order = new Order("ORD-001");
        order.items.add(new OrderItem(laptop, 2));
        order.items.add(new OrderItem(mouse, 3));

        System.out.println("Processing Order: " + order.orderId);

        FulfillmentPlan finalPlan = FulfillmentEngine.greedyFallback(allWarehouses, order);
        System.out.println("Selected Warehouses: " + finalPlan.assignedWarehouses);

        boolean allReserved = true;
        for (OrderItem item : order.items) {
            for (Warehouse w : finalPlan.assignedWarehouses) {
                if (w.hasStock(item.product, item.requiredQuantity)) {
                    boolean success = w.reserveInventory(item.product, item.requiredQuantity);
                    if (success) {
                        System.out.println("Reserved " + item.requiredQuantity + " " + item.product.productId + "(s) at " + w.warehouseId);
                    }
                }
            }
        }
        
        System.out.println("Engine execution complete.");
    }
}