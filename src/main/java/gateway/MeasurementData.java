package gateway;

import gateway.singleton.DataHandler;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/data")
public class MeasurementData {

    @GET
    @Produces("text/plain")
    public Response getData() {
        String lastData = DataHandler.getInstance().getLastData();
        return Response.ok(lastData).build();
    }
}
