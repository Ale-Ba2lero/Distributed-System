package gateway.singleton;

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

    public synchronized ArrayList<Measurement> getMeasurements() {
        return new ArrayList<>(this.data);
    }
}
