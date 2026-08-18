package Engine;

class Option {
    private final int id;
    private final String name;

    public Option(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }
}
