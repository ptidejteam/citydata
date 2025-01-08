package ca.concordia.ngci.tools4cities.metamenth.enums;

public enum FloorType {
    REGULAR("Regular"),
    BASEMENT("Basement"),
    ROOFTOP("Rooftop");

    private final String value;

    FloorType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
