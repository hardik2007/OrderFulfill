package Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Setup Mock Database
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

        // 2. Setup Scanner for Terminal Input
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Order Fulfillment CLI ===");
        
        System.out.print("Enter new Order ID (e.g., ORD-100): ");
        String orderId = scanner.nextLine();
        Order order = new Order(orderId);

        while (true) {
            System.out.print("Enter product name (Laptop or Mouse) OR type 'done' to finish: ");
            String productName = scanner.nextLine();
            
            if (productName.equalsIgnoreCase("done")) {
                break;
            }

            System.out.print("Enter quantity needed: ");
            int quantity = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            order.items.add(new OrderItem(new Product(productName), quantity));
        }
        scanner.close();

        System.out.println("\nProcessing Order: " + order.orderId + "...");

        // 3. Execute Engine
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new InventoryConstraint());
        FulfillmentStrategy strategy = new BalancedStrategy();
        
        FulfillmentPlan finalPlan = FulfillmentEngine.selectBestPlan(allWarehouses, order, constraints, strategy);
        
        if (finalPlan == null) {
            System.out.println("FAILED: Insufficient stock or no valid plan found.");
            return;
        }
        
        System.out.println("SUCCESS: Selected Warehouses: " + finalPlan.assignedWarehouses);

        // 4. Reserve and Print Output
        boolean allReserved = true;
        for (OrderItem item : order.items) {
            int remaining = item.requiredQuantity;
            for (Warehouse w : finalPlan.assignedWarehouses) {
                int available = w.inventory.getOrDefault(item.product, 0);
                int quantityToReserve = Math.min(remaining, available);
                if (quantityToReserve > 0) {
                    boolean success = w.reserveInventory(item.product, quantityToReserve);
                    if (success) {
                        remaining -= quantityToReserve;
                        System.out.println(" -> Reserved " + quantityToReserve + " " + item.product.productId + "(s) at " + w.warehouseId);
                    }
                }
                if (remaining == 0) {
                    break;
                }
            }
            if (remaining > 0) {
                allReserved = false;
                System.out.println(" -> ERROR: Could not reserve complete quantity of " + item.product.productId);
            }
        }
        System.out.println("All items completely reserved: " + allReserved);
    }
}