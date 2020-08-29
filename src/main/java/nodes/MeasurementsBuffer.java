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
            buffer.add(computeAverage(rawBuffer));
            System.out.println(buffer.pop());
            rawBuffer = new ArrayList<> (rawBuffer.subList(RAW_BUFFER_SIZE / 2, RAW_BUFFER_SIZE - 1));
        }
    }

    private Measurement computeAverage(ArrayList<Measurement> buffer) {
        return new Measurement(
            buffer.get(buffer.size() - 1).getId(),
            buffer.get(buffer.size() - 1).getType(),
            buffer.stream().mapToDouble(Measurement::getValue).average().getAsDouble(),
            buffer.get(buffer.size() - 1).getTimestamp());
    }

    public synchronized Measurement pop() {
        try {
            return buffer.pop();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
