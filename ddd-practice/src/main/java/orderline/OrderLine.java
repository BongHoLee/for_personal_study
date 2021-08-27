package orderline;

import java.util.List;
import orderline.product.Price;
import orderline.product.Product;

public class OrderLine {
    private final List<Product> products;

    public OrderLine(List<Product> products) {
        this.products = products;
    }

    public Price price() {
        Price price = Price.of(0);
        for (Product product : products) {
            price = price.sumWith(product.price());
        }

        return price;
    }
}
