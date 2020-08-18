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
    StreamObserver<Token> tokenStream;

    @Override
    public void run() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendToken(new LinkedList<NodeInfo>(), new LinkedList<NodeInfo>());
    }

    public void greeting(NodeInfo nodeInfo, NodeInfo targetNode) throws InterruptedException {

        //plaintext channel on the address (ip/port) which offers the GreetingService service
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(targetNode.getIp() + ":" + targetNode.getPort()).usePlaintext(true).build();

        //creating an asynchronous stub on the channel
        NetworkServiceStub stub = NetworkServiceGrpc.newStub(channel);

        //creating the HelloResponse object which will be provided as input to the RPC method
        ProtoNodeInfo info = ProtoNodeInfo.newBuilder().setIp(nodeInfo.getIp()).setId(nodeInfo.getId()).setPort(nodeInfo.getPort()).build();

        //calling the RPC method. since it is asynchronous, we need to define handlers
        stub.greeting(info , new StreamObserver<Message>() {

            @Override
            public void onNext(Message message) {
                System.out.println(message.getMessage());
            }

            //if there are some errors, this method will be called
            public void onError(Throwable throwable) {

                System.out.println("Error! "+throwable.getMessage());

            }

            //when the stream is completed (the server called "onCompleted") just close the channel
            public void onCompleted() {
                channel.shutdownNow();
            }

        });

        //you need this. otherwise the method will terminate before that answers from the server are received
        channel.awaitTermination(5, TimeUnit.SECONDS);
    }

    public void init(NodeInfo node) {
        //plaintext channel on the address (ip/port) which offers the GreetingService service
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(node.getIp() + ":" + node.getPort()).usePlaintext(true).build();

        //creating an asynchronous stub on the channel
        NetworkServiceStub stub = NetworkServiceGrpc.newStub(channel);

        tokenStream = stub.sendTheToken(new StreamObserver<Message>() {
            @Override
            public void onNext(Message message) {
                //TODO
            }

            @Override
            public void onError(Throwable throwable) {
                //TODO
            }

            @Override
            public void onCompleted() {
                //TODO
            }
        });
    }

    public Token tokenBuild(LinkedList<NodeInfo> toAdd, LinkedList<NodeInfo> toRemove) {
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

        Token.Builder token = Token.newBuilder();
        token.addAllToAdd(protoToAdd);
        token.addAllToRemove(protoToRemove);

        //TODO build token sensor data field

        return token.build();
    }

    private void sendToken(LinkedList<NodeInfo> toAdd, LinkedList<NodeInfo> toRemove) {
        tokenStream.onNext(tokenBuild(toAdd, toRemove));
    }

    public void closeConnection() {
        tokenStream.onCompleted();
    }
}
