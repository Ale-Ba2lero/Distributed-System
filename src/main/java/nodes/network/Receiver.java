package nodes.network;

import com.networking.node.NetworkServiceOuterClass.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jBeans.NodeInfo;

import java.io.IOException;
import java.util.ArrayList;

public class Receiver {
    private final NetworkHandler networkHandler;
    private final ArrayList<NodeInfo> greetingsQueue;
    private static Token token;

    private final Object tokenLock;

    public Receiver(NetworkHandler networkHandler,NodeInfo nodeInfo) {
        tokenLock = new Object();
        this.networkHandler = networkHandler;
        this.greetingsQueue = new ArrayList<>();

        Server server = ServerBuilder.forPort(nodeInfo.getPort()).addService(new NetworkServiceImpl(this)).build();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Network receiver (grpc server) started listening on: "+ nodeInfo.getIp() + ":" + nodeInfo.getPort());
    }

    public void receiveToken(ProtoToken protoToken) {
        //System.out.println("[" + networkHandler.getNode().getId() + "] Token received from " + protoToken.getFrom().getId());
        synchronized (tokenLock) {
            this.token = new Token(
                    Token.fromProtoToNode(protoToken.getToAddList()),
                    Token.fromProtoToNode(protoToken.getToRemoveList()),
                    new NodeInfo(protoToken.getFrom().getId(), protoToken.getFrom().getIp(), protoToken.getFrom().getPort()),
                    new NodeInfo(protoToken.getTo().getId(), protoToken.getTo().getIp(), protoToken.getTo().getPort())
            );
        }

        synchronized (networkHandler) {
            networkHandler.notify();
        }
    }

    public void greeting(ProtoNodeInfo node) {
        //System.out.println("Greeting received from " + node.getId());
        synchronized (greetingsQueue) {
            greetingsQueue.add(
                new NodeInfo(
                    node.getId(),
                    node.getIp(),
                    node.getPort()
                )
            );
        }

        if (networkHandler.getNodeState() == NetworkHandler.NodeState.STARTING) {
            synchronized (networkHandler) {
                networkHandler.notify();
            }
        }
    }

    public ArrayList<NodeInfo> getGreetingsQueue() {
        synchronized (greetingsQueue) {
            ArrayList<NodeInfo> queue = new ArrayList<>(greetingsQueue);
            greetingsQueue.clear();
            return queue;
        }
    }

    public Token getToken() {
        synchronized (tokenLock) {
            return token;
        }
    }
}
