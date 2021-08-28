package shipInfo;

public class ShipInfo {
    private final CustomerName name;
    private final Tel tel;
    private final Address address;

    public ShipInfo(CustomerName customerName, Tel tel, Address address) {
        this.name = customerName;
        this.tel = tel;
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    public CustomerName getName() {
        return name;
    }

    public Tel getTel() {
        return tel;
    }
}
