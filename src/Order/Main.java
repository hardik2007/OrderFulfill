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

        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new InventoryConstraint());
        FulfillmentStrategy strategy = new BalancedStrategy();
        FulfillmentPlan finalPlan = FulfillmentEngine.selectBestPlan(allWarehouses, order, constraints, strategy);
        
        if (finalPlan == null) {
            System.out.println("No valid fulfillment plan found.");
            return;
        }
        
        
        System.out.println("Selected Warehouses: " + finalPlan.assignedWarehouses);

        boolean allReserved = true;
        for (OrderItem item : order.items) {
        	int remaining=item.requiredQuantity;
            for (Warehouse w : finalPlan.assignedWarehouses) {
                int available=w.inventory.getOrDefault(item.product, 0);
                int quantityToReserve=Math.min(remaining, available);
                if(quantityToReserve>0) {
                	boolean success=w.reserveInventory(item.product, quantityToReserve);
                	if(success)
                	{
                		remaining-= quantityToReserve;
                		
                		System.out.println("Reserved "+quantityToReserve+" "+item.product.productId+ "(s) at "+w.warehouseId);
                	}
                }
                if(remaining == 0) {
                	break;
                }
            }
            if(remaining>0)
            {
            	allReserved =false;
            	System.out.println("Could not reserve complete quantity of " + item.product.productId);
            }
        }
        System.out.println("All items reserved: " + allReserved);
        System.out.println("Engine execution complete.");
    }
}