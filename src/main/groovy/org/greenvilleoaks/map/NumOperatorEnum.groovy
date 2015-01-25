package org.greenvilleoaks.map

public enum NumOperatorEnum {
    lessThan         ("<"),
    lessThanEqual    ("<="),
    greaterThan      (">"),
    greaterThanEqual (">="),
    equals           ("=="),
    notEquals        ("!=")

    private final String value

    NumOperatorEnum(final String value) {
        this.value = value
    }

    public String toString() { return value }
}