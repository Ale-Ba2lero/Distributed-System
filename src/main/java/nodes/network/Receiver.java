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
    private ArrayList<NetworkMessage> messagesQueue;

    public Receiver(NetworkHandler networkHandler,NodeInfo nodeInfo) throws IOException {
        this.networkHandler = networkHandler;
        this.messagesQueue = new ArrayList<>();
        Server server = ServerBuilder.forPort(nodeInfo.getPort()).addService(new NetworkServiceImpl(this)).build();
        server.start();
        System.out.println("Network receiver (grpc server) started listening on: "+ nodeInfo.getIp() + ":" + nodeInfo.getPort());
    }

    public void receiveToken(ProtoToken protoToken) {
        Token token = new Token(
            MessageType.TOKEN,
            Token.fromProtoToNode(protoToken.getToAddList()),
            Token.fromProtoToNode(protoToken.getToRemoveList()),
            new NodeInfo(protoToken.getFrom().getId(), protoToken.getFrom().getIp(), protoToken.getFrom().getPort())
        );

        synchronized (messagesQueue) {
            messagesQueue.add(token);
        }

        synchronized (networkHandler) {
            networkHandler.notify();
        }
    }

    public void greeting(ProtoNodeInfo node) {
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

        synchronized (networkHandler){
            networkHandler.notify();
        }
    }

    public synchronized ArrayList<NetworkMessage> getMessagesQueue() {
        ArrayList<NetworkMessage> queue = new ArrayList<>(messagesQueue);
        messagesQueue.clear();
        return queue;
    }
}
