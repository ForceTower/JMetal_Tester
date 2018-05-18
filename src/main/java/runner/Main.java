package runner;

import base.Tester;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static final String ALL_THINGS_FOLDER = "C:\\Users\\joaop\\Desktop\\Small";
    public static final String DATA_FOLDER = "C:\\Users\\joaop\\Desktop\\Small\\small_exec_all_second\\data - Copia";

    public static void main(String[] args) throws IOException {
        Path mainFolder = Paths.get(DATA_FOLDER);
        Files.list(mainFolder).forEach(path -> {
            File file = path.toFile();
            try {
                Files.list(path).forEach(subPath -> {
                    File data = subPath.toFile();
                    new Tester(data.getPath(), file.getName()).begin();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Tester.createValuesData();
    }
}
