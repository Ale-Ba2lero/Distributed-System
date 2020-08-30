package nodes;

import nodes.sensor.Buffer;
import nodes.sensor.Measurement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class MeasurementsBuffer implements Buffer {

    private final int RAW_BUFFER_SIZE = 12;

    private final LinkedList<Measurement> buffer;
    private ArrayList<Measurement> rawBuffer;

    public MeasurementsBuffer() {
        this.rawBuffer = new ArrayList<>();
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
            System.out.println(buffer.pop());
            rawBuffer = new ArrayList<> (rawBuffer.subList(RAW_BUFFER_SIZE / 2, RAW_BUFFER_SIZE - 1));
        }
    }

    public synchronized Measurement pop() {
        try {
            return buffer.pop();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
