package game2048;

public class Block {
    private int exponent;
    private Boolean empty;
    private int value;

    public Block(int exponent) {
        this.exponent = exponent;
        this.empty = (exponent == 0);
        value = (int) Math.pow(2, exponent);
    }

    public void setExponent(int exponent) {
        this.exponent = exponent;
        this.value = (int) Math.pow(2, exponent);
    }

    public Boolean isEmpty() {
        return this.empty;
    }

    public int getExponent() {
        return this.exponent;
    }

    public int getValue() {
        return this.value;
    }
}