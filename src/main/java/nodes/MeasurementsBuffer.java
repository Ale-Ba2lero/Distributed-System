package nodes;

import nodes.sensor.Buffer;
import nodes.sensor.Measurement;

import java.util.ArrayList;

public class MeasurementsBuffer implements Buffer {

    private ArrayList<Measurement> buffer;

    public MeasurementsBuffer() {
        this.buffer = new ArrayList<>();
    }

    @Override
    public void addMeasurement(Measurement m) {

        buffer.add(m);

        if (buffer.size() == 12) {
            System.out.println(buffer);
            buffer = new ArrayList<> (buffer.subList(6, 11));
        }
    }
}
