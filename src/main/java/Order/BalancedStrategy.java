package Order;

public class BalancedStrategy implements FulfillmentStrategy{
	@Override 
	public double calculateScore(FulfillmentPlan plan, Order order) {
		int warehouseCount=plan.assignedWarehouses.size();
		double penalty=(warehouseCount -1 )*10;
		return penalty;
	}
	
}
