package Engine;

class Option {
    private final int id;
    private final String name;

    Option(int id, String name) {
        this.id = id;
        this.name = name;
    }

    int id() {
        return id;
    }

    String name() {
        return name;
    }
}
