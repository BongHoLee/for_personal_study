package shipInfo;

import static java.util.Objects.isNull;

public class Address {
    private String address;

    public Address(String address) {
        validationCheck(address);
        this.address = address;
    }

    private void validationCheck(String address) {
        if (isNull(address) || address.isEmpty())
            throw new IllegalArgumentException("Not Valid Address");
    }

    @Override
    public String toString() {
        return address;
    }
}
