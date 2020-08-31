package gateway.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gateway.singleton.DataHandler;
import nodes.sensor.Measurement;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/data")
public class MeasurementDataResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addMeasurement(String jsonMeasurementString) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            Measurement m = mapper.readValue(jsonMeasurementString, Measurement.class);
            DataHandler instance = DataHandler.getInstance();
            instance.addData(m);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getLastMeasurement() {
        ObjectMapper mapper = new ObjectMapper();

        Measurement lastMeasurement = DataHandler.getInstance().getMeasurements().get(0);
        String measurementJsonString;
        try {
            return mapper.writeValueAsString(lastMeasurement);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Error";
        }
    }
}
