package nodes;

import jBeans.NodeInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Random;

public class Node {
    private static ServerHandler serverHandler;

    private static NodeInfo nodeInfo;
    private LinkedList<NodeInfo> nodes;

    public static void main(String args[]) throws IOException {
        boolean run = true;
        InputStreamReader streamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        while (run) {
            System.out.println(
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
                        serverHandler.greeting(nodeInfo);
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
        Random random = new Random();
        int id = random.nextInt(1000);
        String ip = "http://localhost/";
        int port = 3000 + random.nextInt(1000);
        Node.nodeInfo = new NodeInfo(id, ip, port);

        System.out.print("Id= " + nodeInfo.getId() + "\nIp= " + nodeInfo.getIp() + "\nPort= " + nodeInfo.getPort() + "\n\n");
    }


}
