package nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jBeans.NodeInfo;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Random;

public class Node {
    private static ServerHandler serverHandler;
    private static NodeInfo nodeInfo;
    private static LinkedList<NodeInfo> nodes;

    private static NetworkTransmitter networkOut;
    private static NetworkReceiver networkIn;

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
                int userInput = Integer.parseInt(bufferedReader.readLine());

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
            } catch (IOException e) {
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

    private static void greeting() {
        Response greetingResponse = serverHandler.POSTServerGreeting(nodeInfo);
        if (greetingResponse.getStatus() == 200) {

            // Star the token receiver thread (gRPC server)
            Thread receiver = new Thread(new NetworkReceiver(nodeInfo));
            receiver.start();

            // Get the node list
            Response nodeListResponse = serverHandler.GETServerNodeList();
            String jsonNodeList = nodeListResponse.readEntity(String.class);

            ObjectMapper mapper = new ObjectMapper();
            try {
                nodes = mapper.readValue(jsonNodeList, new TypeReference<LinkedList<NodeInfo>>() {
                });
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            // If this is not the only node, start a token transmitter thread(gRPC client)
            if (nodes.size() > 1) {

            }

        } else {
            System.out.println("\nResponse status: " + greetingResponse.getStatus());
            System.out.println(greetingResponse.readEntity(String.class) + "\n");
        }
    }

    public synchronized void getNextNodeInNetwork(NetworkTransmitter networkOut) {

    }
}
