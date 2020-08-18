package nodes.network;

import com.networking.node.NetworkServiceOuterClass.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jBeans.NodeInfo;

import java.io.IOException;

public class Receiver {
    private NetworkHandler networkHandler;
    private NodeInfo nodeInfo;
    private Server server;

    public Receiver(NetworkHandler networkHandler, NodeInfo nodeInfo) {
        this.networkHandler = networkHandler;
        this.nodeInfo = nodeInfo;
        server = ServerBuilder.forPort(nodeInfo.getPort()).addService(new NetworkServiceImpl(this)).build();
    }

    public void start() throws IOException {
        server.start();
        System.out.println("ID="+nodeInfo.getId()+"---------------------------------");
        System.out.println(
                "Network receiver (grpc server) started," +
                "listening on: "+ nodeInfo.getIp() + ":" + nodeInfo.getPort());
        System.out.println("---------------------------------------\n");
    }

    public void receiveToken(Token token) {
        networkHandler.receiveToken(token);
    }

    public void addNode(ProtoNodeInfo node) {
        networkHandler.addNodeToList(node);
    }
}
