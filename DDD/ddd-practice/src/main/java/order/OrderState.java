package order;

public enum OrderState {
    READY,
    CANCELED,
    RELEASE;

    public boolean canNotChangeOrderContents() {
        return this != READY;
    }

}
