package base;

import runner.Main;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Tester {
    private static final List<Result> resultsAcc = new ArrayList<>();
    private final List<Double> accuracy;
    private final List<Double> reduction;
    private final List<Double> time;
    private File folder;
    private String name;
    private String algorithm;

    private Tester() {
        accuracy = new ArrayList<>();
        reduction = new ArrayList<>();
        time = new ArrayList<>();
    }

    public Tester(String folderName, String algorithm) {
        this();
        this.folder = new File(folderName);
        this.name = folder.getName();
        this.algorithm = algorithm;
    }

    public void begin() {
        processDirectory(folder);
        Map.Entry<Double, Double> accuracyStats  = applyStatistics(accuracy);
        Map.Entry<Double, Double> timeStats      = applyStatistics(time);

        System.out.println(accuracyStats.getKey() + " --- " + accuracyStats.getValue());
        //System.out.println(timeStats.getKey() + " --- " + timeStats.getValue());

        Result result = new Result(name, algorithm, accuracyStats.getKey());
        resultsAcc.add(result);
    }

    private void processDirectory(File folder) {
        //noinspection ConstantConditions
        List<File> files = Arrays.asList(folder.listFiles());
        files.forEach(this::processFile);
    }

    private void processFile(File file) {
        if (file.isDirectory()) {
            //System.out.println("Directory: " + file.getName());
            if (!file.getName().startsWith(name)) {
                System.out.println(file.getName() + " is in " + name);
                System.exit(0);
                return;
            }

            processDirectory(file);
            return;
        }

        if (!file.getName().endsWith(".arff")) {
            //System.out.println("File: " + file.getName());
            return;
        }

        testFile(file);
    }

    private void testFile(File fileReduced) {
        int number = Integer.parseInt(fileReduced.getName().split("-")[2].split("red")[0]);
        File fileTest = Paths.get(Main.ALL_THINGS_FOLDER + "\\" + name + "\\" + name + "-10-" + number + "tst.arff").toFile();

        //System.out.println("Red: " + fileReduced.getName() + " - Tst: " + fileTest.getName());

        try {
            Instances reduced = new Instances(new FileReader(fileReduced));
            Instances test    = new Instances(new FileReader(fileTest));
            if (reduced.classIndex() == -1) reduced.setClassIndex(reduced.numAttributes() - 1);
            if (test.classIndex()    == -1) test.setClassIndex(test.numAttributes() - 1);

            //Creates a Knn instance
            IBk knn = new IBk(5);
            double accuracy;

            //Evaluates it
            long start = System.currentTimeMillis();
            Evaluation evaluation = new Evaluation(reduced);
            knn.buildClassifier(reduced);
            //Classify the test into the reduced
            evaluation.evaluateModel(knn, test);

            //Gets the number of correct classifications of test
            accuracy = evaluation.correct();
            long end = System.currentTimeMillis();

            Long time = end - start;
            accuracy = accuracy / test.numInstances();
            this.accuracy.add(accuracy);
            this.time.add(time.doubleValue()/1000);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private Map.Entry<Double, Double> applyStatistics(List<Double> values) {
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(-1);
        double sum  = values.stream().mapToDouble(value -> Math.pow(value - mean, 2)).sum();
        if (mean == -1) {
            throw new RuntimeException("Everything is wrong");
        }
        return new AbstractMap.SimpleEntry<>(mean, Math.sqrt(sum/values.size()));
    }

    public static void createValuesData() {
        Map<String, List<Result>> resultMap = resultsAcc.stream().collect(Collectors.groupingBy(Result::getName));
        System.out.println(resultMap);

        System.out.println("Accuracy Array");

        resultMap.forEach((key, value) -> {
            System.out.print("{");
            for (int i = 0; i < value.size(); i++) {
                Result result = value.get(i);
                if (i == 0) System.out.print(result.getValue());
                else System.out.print("," + result.getValue());
            }
            System.out.println("},");
        });
    }

    private static class Result {
        private final double value;
        private final String name;
        private final String algorithm;

        Result(String name, String algorithm, double value) {
            this.value = value;
            this.name = name;
            this.algorithm = algorithm;
        }

        public double getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public String getAlgorithm() {
            return algorithm;
        }
    }
}
