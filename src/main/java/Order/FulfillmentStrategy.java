package Order;

public interface FulfillmentStrategy {
	public double calculateScore(FulfillmentPlan plan, Order order);
}
