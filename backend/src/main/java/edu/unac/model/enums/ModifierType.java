package edu.unac.model.enums;

public enum ModifierType {

    PLUS_2("+2"),
    PLUS_4("+4"),
    PLUS_6("+6"),
    PLUS_8("+8"),
    PLUS_10("+10"),
    TIMES_2("x2");

    private final String value;

    ModifierType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}