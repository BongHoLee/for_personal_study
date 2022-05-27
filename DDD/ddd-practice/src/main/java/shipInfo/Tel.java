package shipInfo;

import static java.util.Objects.isNull;

public class Tel {
    private String tel;

    public Tel(String tel) {
        validationCheck(tel);
        this.tel = tel;
    }

    private void validationCheck(String tel) {
        if (isNull(tel) || tel.isEmpty())
            throw new IllegalArgumentException("Not Valid Tel");
    }

    @Override
    public String toString() {
        return tel;
    }
}
