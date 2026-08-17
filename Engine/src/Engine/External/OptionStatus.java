package Engine.External;

public class OptionStatus {
    private final String name;
    private final double chance;
    private final int totalSharesBought;

    public OptionStatus(String name, double chance, int totalSharesBought) {
        this.name = name;
        this.chance = chance;
        this.totalSharesBought = totalSharesBought;
    }

    public String name() {
        return name;
    }

    public double chance() {
        return chance;
    }

    public int totalSharesBought() {
        return totalSharesBought;
    }
}
