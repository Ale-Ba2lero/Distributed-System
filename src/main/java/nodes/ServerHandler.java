package nodes;

import jBeans.NodeInfo;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ServerHandler {
    private static String URI = "http://localhost:8080/sdp_project_red_war_exploded/";

    public static void greeting(NodeInfo nodeInfo) {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("node");
        Response response = webTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(nodeInfo, MediaType.APPLICATION_JSON));
        System.out.println("\nResponse status: " + response.getStatus());
        System.out.println(response.readEntity(String.class) + "\n");
    }
}
