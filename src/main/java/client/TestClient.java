package client;

import Beans.NodeInfo;

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
            System.out.println(
                "1: (POST) insert new node infos.\n" +
                "2: (GET) obtain list of nodes from server.\n" +
                "3: (DELETE) remove a node\n"
            );

            try {
                int userInput = Integer.parseInt(bufferedReader.readLine());
                if (userInput == 1) {
                    POSTNewNodeInfo();
                } else if (userInput == 2) {
                    GETServerNodesInfo();
                } else if (userInput == 3) {
                    DELETENodeInfo();
                }
                else {
                    System.out.println("Wrong input.\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void POSTNewNodeInfo() {
        NodeInfo newNode = consoleRequestNodeInfo();

        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("node");
        Response response = webTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(newNode, MediaType.APPLICATION_JSON));
        System.out.println("\n\nResponse status: " + response.getStatus());
        System.out.println(response.readEntity(String.class));
    }

    private static void GETServerNodesInfo() {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(URI).path("node");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN);
        Response response = invocationBuilder.get();

        System.out.println(response.getStatus());
        System.out.println(response.readEntity(String.class));
    }

    private static void DELETENodeInfo() {
        Client client = ClientBuilder.newClient();
        int nodeId = consoleRequestNodeId();
        WebTarget webTarget = client.target(URI).path("node/" + nodeId);
        Response response = webTarget
                .request(MediaType.TEXT_PLAIN)
                .delete();
        System.out.println("\n\nResponse status: " + response.getStatus());
        System.out.println(response.readEntity(String.class));
    }

    private static int consoleRequestNodeId() {
        InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        int id = 0;
        try {
            System.out.print("Enter node ID: ");
            id = Integer.parseInt(bufferedReader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return id;
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
        System.out.println(newNode.toString());
        return newNode;
    }
}
