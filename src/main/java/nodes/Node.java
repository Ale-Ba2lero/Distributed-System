package nodes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jBeans.NodeInfo;
import nodes.network.NetworkHandler;
import nodes.network.Receiver;
import nodes.network.Transmitter;
import nodes.sensor.PM10Simulator;

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

    public static void main(String[] args) {

        initNode();
        serverGreeting();

        InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        while (true) {
            System.out.println("----------------------------------------\n" +
                    "1 : Quit the network\n"
            );

            try {
                String line = bufferedReader.readLine();
                // Handle empty or space input
                if (!line.isEmpty() && !line.equals(" ")) {
                    int userInput = Integer.parseInt(line);
                    if (userInput == 1) {
                        stopNode();
                    } else {
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

    private static void initNode() {
        Random random = new Random();
        int id = random.nextInt(1000);
        String ip = "localhost";
        int port = 3000 + random.nextInt(1000);
        nodeInfo = new NodeInfo(id, ip, port);
        System.out.print("Init: Id= " + nodeInfo.getId() + " Ip= " + nodeInfo.getIp() + " Port= " + nodeInfo.getPort() + "\n");
    }

    private static void stopNode() {
        Response goodbyeResponse = ServerHandler.DELETENodeFromServer(nodeInfo.getId());
        //System.out.println("\n\nResponse status: " + goodbyeResponse.getStatus());
        //System.out.println(goodbyeResponse.readEntity(String.class));
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
                } catch (IOException e) {
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
        Transmitter transmitter = new Transmitter(networkHandler);
        Receiver receiver = new Receiver(networkHandler, nodeInfo);

        MeasurementsBuffer buffer = new MeasurementsBuffer();
        Thread sensor = new PM10Simulator(nodeInfo.getId() + "", buffer);

        networkHandler.init(nodes, transmitter, receiver, buffer);

        Thread transmitterThread = new Thread(transmitter);
        transmitterThread.start();
        Thread networkHandlerThread = new Thread(networkHandler);
        networkHandlerThread.start();
        sensor.start();
    }
}
