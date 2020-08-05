package nodes;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jBeans.NodeInfo;

import java.io.IOException;

public class NetworkReceiver implements Runnable{
    private NodeInfo nodeInfo;

    public NetworkReceiver(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    @Override
    public void run() {
        try {

            Server server = ServerBuilder.forPort(nodeInfo.getPort()).addService(new TokenServiceImpl()).build();

            server.start();
            System.out.println("Network Receiver------------------------");
            System.out.println("Network receiver (grpc server) started!");
            System.out.print("Id= " + nodeInfo.getId() + "\nIp= " + nodeInfo.getIp() + "\nPort= " + nodeInfo.getPort() + "\n");
            System.out.println("---------------------------------------\n");
            server.awaitTermination();

        } catch (IOException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            e.printStackTrace();

        }
    }
}
