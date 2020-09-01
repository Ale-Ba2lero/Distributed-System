package nodes.sensor;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MixInMeasurement {
    public MixInMeasurement(@JsonProperty("id") String id,
                            @JsonProperty("type") String type,
                            @JsonProperty("value") double value,
                            @JsonProperty("timestamp")long timestamp) {
    }
}
