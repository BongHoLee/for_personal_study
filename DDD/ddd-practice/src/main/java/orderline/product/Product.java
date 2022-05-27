package orderline.product;

public class Product {
    private final String productName;
    private final String productCode;
    private final Volume volume;
    private final Price price;

    public Product(String productName, String productCode, Volume volume, Price price) {
        this.productName = productName;
        this.productCode = productCode;
        this.volume = volume;
        this.price = price;
    }

    public Price price() {
        return Price.of(price.value() * volume.getVolume());
    }
}
