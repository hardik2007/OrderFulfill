package Order;

import java.util.ArrayList;
import java.util.List;

public class FulfillmentPlan {
    List<Warehouse> assignedWarehouses;

    public FulfillmentPlan(List<Warehouse> assignedWarehouses) {
        this.assignedWarehouses = new ArrayList<>(assignedWarehouses);
    }
}