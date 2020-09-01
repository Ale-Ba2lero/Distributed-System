package nodes;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jBeans.NodeInfo;
import jdk.nashorn.internal.parser.JSONParser;
import nodes.sensor.Measurement;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ServerHandler {
    // if error "server already running on port 8080" use this on command line:
    // ./glassfish5/glassfish/bin/asadmin stop-domain
    private static final String URI = "http://localhost:8080/sdp_project_red_war_exploded/";

    public static Response POSTServerGreeting(NodeInfo nodeInfo) {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("node");
        return webTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(nodeInfo, MediaType.APPLICATION_JSON));
    }

    public static Response POSTMeasurement(Measurement m) {

        ObjectMapper mapper = new ObjectMapper();
        String measurementJsonString;
        try {
            measurementJsonString = mapper.writeValueAsString(m);
            System.out.println(measurementJsonString);
        } catch (JsonProcessingException e) {
            measurementJsonString = null;
            e.printStackTrace();
        }
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("data");
        return webTarget
                .request()
                .post(Entity.json(measurementJsonString));
    }

    public static Response GETServerNodeList() {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("node");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN);
        return invocationBuilder.get();
    }

    public static Response DELETENodeFromServer(int nodeId) {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("node/" + nodeId);
        return webTarget.request(MediaType.TEXT_PLAIN).delete();
    }


}
