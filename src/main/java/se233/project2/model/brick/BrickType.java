package se233.project2.model.brick;

// The letter each brick type uses in a stage layout.
public enum BrickType {
    NORMAL('N'),
    STRONG('S'),
    BONUS('B'),
    UNBREAKABLE('U');

    private final char symbol;

    BrickType(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }
}
