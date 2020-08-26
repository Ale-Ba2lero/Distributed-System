package nodes.network;

import com.networking.node.NetworkServiceOuterClass.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jBeans.NodeInfo;
import nodes.network.messages.GreetingMessage;
import nodes.network.messages.MessageType;
import nodes.network.messages.NetworkMessage;
import nodes.network.messages.Token;

import java.io.IOException;
import java.util.ArrayList;

public class Receiver {
    private final NetworkHandler networkHandler;
    private final ArrayList<NetworkMessage> messagesQueue;
    private final ArrayList<NetworkMessage> tokenQueue;

    private static Object tokenLoopLock;

    public Receiver(NetworkHandler networkHandler,NodeInfo nodeInfo) {
        tokenLoopLock = new Object();
        this.networkHandler = networkHandler;
        this.messagesQueue = new ArrayList<>();
        this.tokenQueue = new ArrayList<>();

        Server server = ServerBuilder.forPort(nodeInfo.getPort()).addService(new NetworkServiceImpl(this)).build();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Network receiver (grpc server) started listening on: "+ nodeInfo.getIp() + ":" + nodeInfo.getPort());
    }

    public void receiveToken(ProtoToken protoToken) {
        //System.out.println("[" + networkHandler.getNode().getId() + "] Token " + protoToken.getLoop() + " received from " + protoToken.getFrom().getId());
        Token token = new Token(
            MessageType.TOKEN,
            Token.fromProtoToNode(protoToken.getToAddList()),
            Token.fromProtoToNode(protoToken.getToRemoveList()),
            new NodeInfo(protoToken.getFrom().getId(), protoToken.getFrom().getIp(), protoToken.getFrom().getPort()),
            protoToken.getLoop()
        );

        synchronized (messagesQueue) {
            tokenQueue.add(token);
        }

        synchronized (networkHandler) {
            networkHandler.notify();
        }
    }

    public void greeting(ProtoNodeInfo node) {
        //System.out.println("Greeting received from " + node.getId());
        synchronized (messagesQueue) {
            messagesQueue.add(
                new GreetingMessage(
                    MessageType.GREETING,
                    new NodeInfo(
                        node.getId(),
                        node.getIp(),
                        node.getPort()
                    )
                )
            );
        }

        if (networkHandler.getNodeState() == NetworkHandler.NodeState.STARTING) {
            synchronized (networkHandler) {
                networkHandler.notify();
            }
        }
    }

    public ArrayList<NetworkMessage> getMessagesQueue() {
        synchronized (messagesQueue) {
            ArrayList<NetworkMessage> queue = new ArrayList<>(messagesQueue);
            messagesQueue.clear();
            return queue;
        }
    }

    public ArrayList<NetworkMessage> getTokenQueue() {
        synchronized (tokenQueue) {
            ArrayList<NetworkMessage> queue = new ArrayList<>(tokenQueue);
            tokenQueue.clear();
            return queue;
        }
    }
}
