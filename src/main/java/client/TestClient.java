package client;

import gateway.NodeInfo;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestClient {

    private static String serverURL = "http://localhost:8080/sdp_project_red_war_exploded/";

    public static void main(String args[]) throws IOException {
        NodeInfo nodeInfo = consoleRequestNodeInfo();
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(serverURL).path("nodes/helloworld");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN);
        Response response = invocationBuilder.get();
        System.out.println(response.getStatus());
        System.out.println(response.readEntity(String.class));
    }

    private static NodeInfo consoleRequestNodeInfo() throws IOException {
        NodeInfo newNode = new NodeInfo();
        InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        System.out.print("Enter node ID: ");
        newNode.setId(Integer.parseInt(bufferedReader.readLine()));
        System.out.print("Enter node IP: ");
        newNode.setIp(bufferedReader.readLine());
        System.out.print("Enter node PORT: ");
        newNode.setPort(Integer.parseInt(bufferedReader.readLine()));
        return newNode;
    }
}
