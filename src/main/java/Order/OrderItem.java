package Order;

public class OrderItem {
    public Product product;
    public int requiredQuantity;

    public OrderItem(Product product, int requiredQuantity) {
        this.product = product;
        this.requiredQuantity = requiredQuantity;
    }
}