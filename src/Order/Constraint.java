package Order;

public interface Constraint {
public boolean isSatisfied(FulfillmentPlan plan, Order order);
}
