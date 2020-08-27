package nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jBeans.NodeInfo;
import nodes.network.NetworkHandler;
import nodes.network.Receiver;
import nodes.network.Transmitter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
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

    public static void main(String[] args) throws IOException {

        nodeInit();
        serverGreeting();

        InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        while (true) {
            System.out.println("----------------------------------------\n" +
                "1 : node info\n" +
                "2 : stop node\n"
            );

            try {
                String line = bufferedReader.readLine();
                // Handle empty or space input
                if (!line.isEmpty() && !line.equals(" ")) {
                    int userInput = Integer.parseInt(line);
                    switch (userInput) {
                        case 1:
                            System.out.print("Init: Id= " + nodeInfo.getId() + " Ip= " + nodeInfo.getIp() + " Port= " + nodeInfo.getPort() + "\n");
                            break;
                        case 2:
                            nodeStop();
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
        }
    }

    private static void nodeInit() {
        Random random = new Random();
        int id = random.nextInt(1000);
        String ip = "localhost";
        int port = 3000 + random.nextInt(1000);
        nodeInfo = new NodeInfo(id, ip, port);
        System.out.print("Init: Id= " + nodeInfo.getId() + " Ip= " + nodeInfo.getIp() + " Port= " + nodeInfo.getPort() + "\n");
    }

    private static void nodeStop() {
        Response goodbyeResponse = ServerHandler.DELETENodeFromServer(nodeInfo.getId());
        System.out.println("\n\nResponse status: " + goodbyeResponse.getStatus());
        System.out.println(goodbyeResponse.readEntity(String.class));
        // Once the node has been removed from the gateway list remove it from the network.
        if ( goodbyeResponse.getStatus() == 200) {
            networkHandler.removeNodeFromNetwork();
        }
    }

    private static void serverGreeting(){
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

                nodeStart(nodes);
            } else {
                System.out.println("\nResponse status: " + greetingResponse.getStatus());
                System.out.println(greetingResponse.readEntity(String.class) + "\n");
            }
        } else {
            System.out.println("Error!!: Node not initialized!\n");
        }
    }

    private static void nodeStart(LinkedList<NodeInfo> nodes) {
        networkHandler = new NetworkHandler(nodeInfo);

        Transmitter transmitter = new Transmitter(networkHandler, nodeInfo);
        Receiver receiver = new Receiver(networkHandler, nodeInfo);

        networkHandler.init(nodes, transmitter, receiver);

        Thread transmitterThread = new Thread(transmitter);
        transmitterThread.start();
        Thread networkHandlerThread = new Thread(networkHandler);
        networkHandlerThread.start();
    }
}
