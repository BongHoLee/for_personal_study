package shipInfo;

import static java.util.Objects.isNull;

public class CustomerName {
    private String name;

    public CustomerName(String name) {
        validationCheck(name);
        this.name = name;
    }

    private void validationCheck(String name) {
        if (isNull(name) || name.isEmpty())
            throw new IllegalArgumentException("Not Valid Name");
    }

    @Override
    public String toString() {
        return name;
    }
}
