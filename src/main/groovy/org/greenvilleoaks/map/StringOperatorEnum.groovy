package org.greenvilleoaks.map

public enum StringOperatorEnum {
    contains   ("contains"),
    startsWith ("startsWith"),
    endsWith   ("endsWith"),
    equals     ("=="),
    notEquals  ("!=")

    private final String value

    StringOperatorEnum(final String value) {
        this.value = value
    }
    
    public String toString() { return value }
}
