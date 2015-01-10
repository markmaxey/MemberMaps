package org.greenvilleoaks.map

public enum StringOperatorEnum {
    contains   ("contains"),
    startsWith ("starts with"),
    endsWith   ("ends with"),
    equals     ("=="),
    notEquals  ("!=")

    private final String value

    StringOperatorEnum(final String value) {
        this.value = value
    }
}
