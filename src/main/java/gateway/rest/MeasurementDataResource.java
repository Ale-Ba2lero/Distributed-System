package gateway.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gateway.singleton.DataHandler;
import nodes.sensor.Measurement;
import nodes.sensor.MixInMeasurement;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;

@Path("/data")
public class MeasurementDataResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addMeasurement(String jsonMeasurementString) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Measurement.class, MixInMeasurement.class);

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

        ArrayList<Measurement> m = DataHandler.getInstance().getMeasurements();
        ObjectMapper mapper = new ObjectMapper();
        String measurementJsonString;
        try {
            measurementJsonString = mapper.writeValueAsString(m);
        } catch (JsonProcessingException e) {
            measurementJsonString = null;
            e.printStackTrace();
        }

        return  measurementJsonString;
    }
}
