package nodes.network;

import com.networking.node.NetworkServiceOuterClass.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jBeans.NodeInfo;

import java.io.IOException;

public class Receiver {
    private final NetworkHandler networkHandler;

    public Receiver(NetworkHandler networkHandler, NodeInfo nodeInfo) throws IOException {
        this.networkHandler = networkHandler;
        Server server = ServerBuilder.forPort(nodeInfo.getPort()).addService(new NetworkServiceImpl(this)).build();
        server.start();
        System.out.println("Network receiver (grpc server) started listening on: "+ nodeInfo.getIp() + ":" + nodeInfo.getPort());
    }

    public void receiveToken(ProtoToken token) {
        System.out.println("Token Received from " + token.getFrom() + " " + System.currentTimeMillis());
        networkHandler.receiveToken(token);
    }

    public void greeting(ProtoNodeInfo node) {
        System.out.println("Greeting received from " + node.getId());
        networkHandler.addNodeToList(node);
    }
}
