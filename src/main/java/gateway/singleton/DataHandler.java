package gateway.singleton;

import javafx.util.Pair;
import nodes.sensor.Measurement;

import java.util.ArrayList;
import java.util.List;

public class DataHandler {
    private static DataHandler instance = null;
    private final ArrayList<Measurement> data;

    private DataHandler() {
        data = new ArrayList<>();
    }

    public static synchronized DataHandler getInstance()
    {
        if (instance == null) {
            instance = new DataHandler();
        }

        return instance;
    }

    public synchronized void addData(Measurement m) {
        this.data.add(m);
    }

    public synchronized ArrayList<Measurement> getLastNMeasurements(int n) {
        return new ArrayList<>(this.data.subList(data.size() - n - 1, data.size() - 1));
    }

    public synchronized Pair<Double, Double> getStats(int n) {

        ArrayList<Measurement> sample = new ArrayList<>(data.subList(data.size() - n - 1, data.size() - 1));

        double average = sample.stream().mapToDouble(Measurement::getValue).average().getAsDouble();

        double stdDev = Math.sqrt(sample
            .stream()
            .mapToDouble(Measurement::getValue)
            .map(v -> Math.pow(v - average, 2))
            .sum() / sample.size());

        return new Pair<>(average, stdDev);

    }



}
