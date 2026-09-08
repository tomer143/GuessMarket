package Engine.External;

import java.util.List;

public class OrderResult {
    private final List<FillRecord> fills;
    private final int unfilledQuantity;

    public OrderResult(List<FillRecord> fills, int unfilledQuantity) {
        this.fills = fills;
        this.unfilledQuantity = unfilledQuantity;
    }

    public List<FillRecord> fills() {
        return fills;
    }

    public int unfilledQuantity() {
        return unfilledQuantity;
    }
}
