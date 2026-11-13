package Order;

public class InventoryConstraint implements Constraint {
    @Override
    public boolean isSatisfied(FulfillmentPlan plan, Order order) {
        for (OrderItem item : order.items) {
            int totalAvailableStock = 0;
            for (Warehouse w : plan.assignedWarehouses) {
                totalAvailableStock += w.inventory.getOrDefault(item.product, 0);
            }
            if (totalAvailableStock < item.requiredQuantity) {
                return false; 
            }
        }
        return true; 
    }
}