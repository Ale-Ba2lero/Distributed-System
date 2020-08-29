package nodes;

import nodes.sensor.Buffer;
import nodes.sensor.PM10Simulator;

public class SensorMain {

    public static void main(String[] args) {
        Buffer buffer = new MeasurementsBuffer();
        Thread sensor = new PM10Simulator("PM10", buffer);

        sensor.start();
    }
}
