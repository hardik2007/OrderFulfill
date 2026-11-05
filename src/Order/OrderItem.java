package Order;

public class OrderItem {
    Product product;
    int requiredQuantity;

    public OrderItem(Product product, int requiredQuantity) {
        this.product = product;
        this.requiredQuantity = requiredQuantity;
    }
}