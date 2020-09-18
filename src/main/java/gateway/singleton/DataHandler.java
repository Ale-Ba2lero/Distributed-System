package gateway.singleton;

import client.Pair;
import nodes.sensor.Measurement;

import java.util.ArrayList;

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
        if (n > data.size()) {
            return new ArrayList<>(this.data);
        } else {
            return new ArrayList<>(this.data.subList(data.size() - n , data.size()));
        }
    }

    public synchronized Pair<Double> getStats(int n) {
        if (n > data.size()) n = data.size();
        ArrayList<Measurement> sample = new ArrayList<>(this.data.subList(Math.max(data.size() - n, 0) , data.size()));

        double average = sample.stream().mapToDouble(Measurement::getValue).average().getAsDouble();

        double stdDev = Math.sqrt(sample
            .stream()
            .mapToDouble(Measurement::getValue)
            .map(v -> Math.pow(v - average, 2))
            .sum() / sample.size());

        return new Pair<>(average, stdDev);
    }
}
