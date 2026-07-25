abstract public class Event {
    int id;
    int mmId;
    String name;
    String description;
    FeeMethod feeMethod;
    FeeCollection feeCollection;
    Option[] options;
    boolean isActive;

    abstract float getPrice(int amount);
}
