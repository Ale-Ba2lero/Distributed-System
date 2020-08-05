package nodes;

import jBeans.NodeInfo;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ServerHandler {
    private static String URI = "http://localhost:8080/sdp_project_red_war_exploded/";

    public static Response POSTServerGreeting(NodeInfo nodeInfo) {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("node");
        Response response = webTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(nodeInfo, MediaType.APPLICATION_JSON));
        return response;
    }

    public static Response GETServerNodeList() {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("node");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN);
        Response response = invocationBuilder.get();
        return response;
    }
}
