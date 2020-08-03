package nodes;

import jBeans.NodeInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Random;


import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Node {

    private static String URI = "http://localhost:8080/sdp_project_red_war_exploded/";
    private static NodeInfo nodeInfo;

    LinkedList<NodeInfo> nodes;

    public static void main(String args[]) throws IOException {
        boolean run = true;
        InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        while (run) {
            System.out.println(
                "1: Init.\n" +
                "2: Greet the server.\n"
            );

            try {
                int userInput = Integer.parseInt(bufferedReader.readLine());
                if (userInput == 1) {
                    init();
                } else if (userInput == 2) {
                    greeting();
                }
                else {
                    System.out.println("Wrong input.\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void init() {
        Random random = new Random();
        int id = random.nextInt(1000);
        String ip = "http://localhost/";
        int port = 3000 + random.nextInt(1000);
        Node.nodeInfo = new NodeInfo(id, ip, port);

        System.out.print("Id= " + nodeInfo.getId() + "\nIp= " + nodeInfo.getIp() + "\nPort= " + nodeInfo.getPort() + "\n\n");
    }

    private static void greeting() {
        NodeInfo nodeInfo = Node.nodeInfo;

        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(Node.URI).path("node");
        Response response = webTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(nodeInfo, MediaType.APPLICATION_JSON));
        System.out.println("\nResponse status: " + response.getStatus());
        System.out.println(response.readEntity(String.class));
    }
}
