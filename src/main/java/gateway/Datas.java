package gateway;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("datas")
public class Datas {

    @GET
    @Produces("text/plain")
    public String getData() {
        String lastData = DataStatsHandler.getInstance().getLastData();
        return lastData;
    }
}
