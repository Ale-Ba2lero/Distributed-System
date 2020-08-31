package nodes;

import nodes.sensor.Buffer;
import nodes.sensor.Measurement;

import java.util.ArrayList;
import java.util.LinkedList;

public class MeasurementsBuffer implements Buffer {

    private final int RAW_BUFFER_SIZE = 12;

    private final LinkedList<Measurement> buffer;
    private LinkedList<Measurement> rawBuffer;

    public MeasurementsBuffer() {
        this.rawBuffer = new LinkedList<>();
        this.buffer = new LinkedList<>();
    }

    @Override
    public synchronized void addMeasurement(Measurement m) {
        rawBuffer.add(m);

        if (rawBuffer.size() == RAW_BUFFER_SIZE) {
            buffer.add(new Measurement(
                    rawBuffer.get(rawBuffer.size() - 1).getId(),
                    rawBuffer.get(rawBuffer.size() - 1).getType(),
                    rawBuffer.stream().mapToDouble(Measurement::getValue).average().getAsDouble(),
                    rawBuffer.get(rawBuffer.size() - 1).getTimestamp()));
            rawBuffer = new LinkedList<> (rawBuffer.subList(RAW_BUFFER_SIZE / 2, RAW_BUFFER_SIZE - 1));
        }
    }

    public synchronized Measurement pop() {
        if (buffer.size() > 0) {
            Measurement pop = buffer.get(0);
            buffer.remove(0);
            return pop;
        }

        return null;
    }
}
