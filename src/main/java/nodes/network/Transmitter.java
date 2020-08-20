package nodes.network;

import com.networking.node.NetworkServiceGrpc;
import com.networking.node.NetworkServiceGrpc.*;
import com.networking.node.NetworkServiceOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import jBeans.NodeInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class Transmitter implements Runnable {

    private final NetworkHandler networkHandler;
    private ManagedChannel channel;
    private NodeInfo node;

    public Transmitter(NetworkHandler networkHandler, NodeInfo node) {
        this.networkHandler = networkHandler;
        this.node = node;
    }

    //initialize or reinitialize the target node reference that will receive the tokens
    public void init() {
        if (channel != null) {
            channel.shutdownNow();
        }
        //plaintext channel on the address (ip/port) which offers the GreetingService service
        channel = ManagedChannelBuilder.forTarget(networkHandler.getTarget().getIp() + ":" + networkHandler.getTarget().getPort()).usePlaintext(true).build();
    }

    @Override
    public void run() {
        System.out.println("Transmitter is running");

        while (true) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long start = System.currentTimeMillis();
            init();
            long stop = System.currentTimeMillis();

            System.out.println(stop-start);

            NetworkServiceBlockingStub stub = NetworkServiceGrpc.newBlockingStub(channel);
            System.out.println("Send token");
            stub.sendTheToken(tokenBuild((networkHandler.getToken())));
        }
    }

    //contact a node on the list and inform it about the new node
    public void greeting() {
        System.out.println("So i started greeting");

        init();

        NetworkServiceBlockingStub stub = NetworkServiceGrpc.newBlockingStub(channel);

        ProtoNodeInfo info = ProtoNodeInfo.newBuilder().setIp(node.getIp()).setId(node.getId()).setPort(node.getPort()).build();

        Message message = stub.greeting(info);

        System.out.println(message.getMessage());
    }

    public ProtoToken tokenBuild(Token token) {
        LinkedList<NodeInfo> toAdd = token.getToAdd();
        LinkedList<NodeInfo> toRemove = token.getToRemove();

        ArrayList<ProtoNodeInfo> protoToAdd = new ArrayList<>();
        toAdd.forEach((nodeInfo -> {
            protoToAdd.add(ProtoNodeInfo
                    .newBuilder()
                    .setId(nodeInfo.getId())
                    .setIp(nodeInfo.getIp())
                    .setPort(nodeInfo.getPort())
                    .build());
        }));

        ArrayList<ProtoNodeInfo> protoToRemove = new ArrayList<>();
        toRemove.forEach((nodeInfo -> {
            protoToRemove.add(ProtoNodeInfo
                    .newBuilder()
                    .setId(nodeInfo.getId())
                    .setIp(nodeInfo.getIp())
                    .setPort(nodeInfo.getPort())
                    .build());
        }));

        ProtoToken.Builder protoToken = ProtoToken.newBuilder();
        protoToken.addAllToAdd(protoToAdd);
        protoToken.addAllToRemove(protoToRemove);

        //TODO build token sensor data field

        return protoToken.build();
    }
}
