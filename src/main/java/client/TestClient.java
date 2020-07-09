package client;

import gateway.NodeInfo;
import org.glassfish.jersey.client.ClientResponse;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestClient {

    private static String URI = "http://localhost:8080/sdp_project_red_war_exploded/";

    public static void main(String args[]) throws IOException {
        boolean run = true;
        InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        while (run) {
            System.out.println("0: (POST) insert new node infos.\n" +
                    "1: (GET) obtain list of nodes from server.");

            try {
                int userInput = Integer.parseInt(bufferedReader.readLine());
                if (userInput == 0) {
                    POSTNewNodeInfo();
                } else if (userInput == 1) {
                    GETServerNodesInfo();
                } else {
                    System.out.println("Wrong input.\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("\nPress any key to continue");
                bufferedReader.readLine();
            }
        }
    }

    private static void POSTNewNodeInfo() {
        NodeInfo newNode = consoleRequestNodeInfo();
        System.out.println(newNode.toString());

        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("nodes");
        Response response = webTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(newNode, MediaType.APPLICATION_JSON));
        System.out.println("\nResponse status: " + response.getStatus());
        String responseEntity = response.readEntity(String.class);
        System.out.println(responseEntity);
    }

    private static void GETServerNodesInfo() {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("nodes");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN);
        Response response = invocationBuilder.get();

        System.out.println(response.getStatus());
        System.out.println(response.readEntity(String.class));
    }

    private static NodeInfo consoleRequestNodeInfo() {

        InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        int id = -1, port = -1;
        String ip = "";

        try {
            System.out.print("Enter node ID: ");
            id = Integer.parseInt(bufferedReader.readLine());
            System.out.print("Enter node IP: ");
            ip = bufferedReader.readLine();
            System.out.print("Enter node PORT: ");
            port = Integer.parseInt(bufferedReader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

        NodeInfo newNode = new NodeInfo(id, ip, port);
        return newNode;
    }
}
