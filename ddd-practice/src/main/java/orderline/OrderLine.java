package orderline;

import java.util.List;
import orderline.product.Price;
import orderline.product.Product;

public class OrderLine {
    private static final String PRODUCTS_NOT_VALID_MESSAGE = "NOT_VALID_PRODUCTS";
    private final List<Product> products;

    public OrderLine(List<Product> products) {
        validationCheck(products);
        this.products = products;
    }

    private void validationCheck(List<Product> products) {
        if ( products == null || products.isEmpty() ) {
            throw new IllegalArgumentException(PRODUCTS_NOT_VALID_MESSAGE);
        }
    }

    public Price price() {
        Price price = Price.of(0);
        for (Product product : products) {
            price = price.sumWith(product.price());
        }

        return price;
    }
}
