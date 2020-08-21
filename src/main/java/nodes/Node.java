package nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jBeans.NodeInfo;
import nodes.network.NetworkHandler;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Random;

public final class Node {
    private static NodeInfo nodeInfo;

    private static NetworkHandler networkHandler;

    private Node(){}

    public static void main(String[] args) throws IOException, InterruptedException {

        nodeInit();
        serverGreeting();
        /*InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        while (true) {
            System.out.println("----------------------------------------\n" +
                "0 : start node" +
                "\n1 : show node info" +
                "\n2 : start token loop"
            );

            try {
                String line = bufferedReader.readLine();
                // Handle empty or space input
                if (!line.isEmpty() && !line.equals(" ")) {
                    int userInput = Integer.parseInt(line);
                    switch (userInput) {
                        case 0:
                            nodeInit();
                            serverGreeting();
                            break;
                        case 1:
                            displayNodeInfo();
                            break;
                        case 2:
                            networkHandler.startTokenLoop();
                            break;
                        default:
                            System.out.println("Wrong input.\n");
                    }
                } else {
                    System.out.println("Wrong input.\n");
                }
            } catch (IOException  e) {
                e.printStackTrace();
            }
        }*/
    }

    private static void displayNodeInfo() {
        System.out.println("Node info: Id= " + nodeInfo.getId() + " Ip= " + nodeInfo.getIp() + " Port= " + nodeInfo.getPort());
        System.out.println("Nodes list: " + networkHandler.getNodes());
        System.out.println("Next node: " + networkHandler.getTarget());
    }

    private static void nodeInit() {
        Random random = new Random();
        int id = random.nextInt(1000);
        String ip = "localhost";
        int port = 3000 + random.nextInt(1000);
        nodeInfo = new NodeInfo(id, ip, port);
        System.out.print("Init: Id= " + nodeInfo.getId() + " Ip= " + nodeInfo.getIp() + " Port= " + nodeInfo.getPort() + "\n");
    }

    private static void serverGreeting() throws IOException, InterruptedException {
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
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                networkHandler = new NetworkHandler(nodeInfo, nodes);
            } else {
                System.out.println("\nResponse status: " + greetingResponse.getStatus());
                System.out.println(greetingResponse.readEntity(String.class) + "\n");
            }
        } else {
            System.out.println("Error!!: Node not initialized!\n");
        }
    }
}
