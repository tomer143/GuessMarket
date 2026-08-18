package Engine;

import Engine.External.GuessMarketException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class StateFileManager {
    private static final String EXTENSION = ".data";

    static void save(String basePath) throws GuessMarketException {
        if (basePath == null || basePath.isBlank())
            throw new GuessMarketException("No file path was provided.");

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(resolvePath(basePath)))) {
            out.writeObject(Manager.getInstance());
        } catch (IOException exception) {
            throw new GuessMarketException("Could not save the state: " + exception.getMessage());
        }
    }

    static void load(String basePath) throws GuessMarketException {
        if (basePath == null || basePath.isBlank())
            throw new GuessMarketException("No file path was provided.");

        File file = new File(resolvePath(basePath));
        if (!file.isFile())
            throw new GuessMarketException("The file \"" + file.getPath() + "\" does not exist.");

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Manager restored = (Manager) in.readObject();
            Manager.restoreInstance(restored);
        } catch (IOException | ClassNotFoundException | ClassCastException exception) {
            throw new GuessMarketException("The file is not a valid saved state: " + exception.getMessage());
        }
    }

    private static String resolvePath(String basePath) {
        return basePath.toLowerCase().endsWith(EXTENSION) ? basePath : basePath + EXTENSION;
    }
}
