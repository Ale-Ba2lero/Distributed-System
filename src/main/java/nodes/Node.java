package nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jBeans.NodeInfo;
import nodes.network.NetworkHandler;
import nodes.network.Receiver;
import nodes.network.Transmitter;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Random;

public final class Node {
    private static NodeInfo nodeInfo;

    private static NetworkHandler networkHandler;
    private static Transmitter networkOut;
    private static Receiver networkIn;

    private Node(){}

    public static void main(String args[]) throws IOException {

        boolean run = true;
        InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        while (run) {
            System.out.println("Node Main ----------------------------------------\n" +
                "1: Init.\n" +
                "2: Greet the server.\n" +
                "3: Show node info.\n"
            );

            try {
                String line = bufferedReader.readLine();
                // Handle empty or space input
                if (!line.isEmpty() && !line.equals(" ")) {
                    int userInput = Integer.parseInt(line);
                    switch (userInput) {
                        case 1:
                            init();
                            break;
                        case 2:
                            greeting();
                            break;
                        case 3:
                            displayNodeInfo();
                            break;
                        default:
                            System.out.println("Wrong input.\n");
                    }
                } else {
                    System.out.println("Wrong input.\n");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void displayNodeInfo() {
        System.out.print("Id= " + nodeInfo.getId() + "\nIp= " + nodeInfo.getIp() + "\nPort= " + nodeInfo.getPort() + "\n\n");
    }

    private static void init() {
        System.out.println("\nInit -------------------------");
        Random random = new Random();
        int id = random.nextInt(1000);
        String ip = "http://localhost/";
        int port = 3000 + random.nextInt(1000);
        nodeInfo = new NodeInfo(id, ip, port);

        System.out.print("Id= " + nodeInfo.getId() + "\nIp= " + nodeInfo.getIp() + "\nPort= " + nodeInfo.getPort() + "\n\n");
    }

    private static void greeting() throws IOException, InterruptedException {
        if (nodeInfo != null) {
            Response greetingResponse = ServerHandler.POSTServerGreeting(nodeInfo);
            if (greetingResponse.getStatus() == 200) {
                // Get the node list
                LinkedList<NodeInfo> nodes = new LinkedList<>();
                Response nodeListResponse = ServerHandler.GETServerNodeList();
                String jsonNodeList = nodeListResponse.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                try {
                    nodes = mapper.readValue(jsonNodeList, new TypeReference<LinkedList<NodeInfo>>() {});
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                networkHandler = new NetworkHandler(nodeInfo, nodes);

            } else {
                System.out.println("\nResponse status: " + greetingResponse.getStatus());
                System.out.println(greetingResponse.readEntity(String.class) + "\n");
            }
        } else {
            System.out.println("Initiation needed before greeting to the server!\n");
        }
    }

}
