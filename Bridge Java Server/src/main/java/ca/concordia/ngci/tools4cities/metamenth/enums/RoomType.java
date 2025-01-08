package ca.concordia.ngci.tools4cities.metamenth.enums;

public enum RoomType {
    BEDROOM("Bedroom"),
    LIVING_ROOM("Living Room"),
    KITCHEN("Kitchen"),
    BATHROOM("Bathroom"),
    OFFICE("Office"),
    STUDY_ROOM("Study Room"),
    CLASS_ROOM("Class Room"),
    LIBRARY("Library"),
    FOOD_SERVICES("Food Services"),
	SEMINAR_ROOM("Seminar Room");

    private final String value;

    RoomType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}