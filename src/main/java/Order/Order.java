package Order;

import java.util.ArrayList;
import java.util.List;

public class Order {
    String orderId;
    public List<OrderItem> items;

    public Order(String orderId) {
        this.orderId = orderId;
        items = new ArrayList<>();
    }
}