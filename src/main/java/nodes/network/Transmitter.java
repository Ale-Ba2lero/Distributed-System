package nodes.network;

import com.networking.node.NetworkServiceGrpc;
import com.networking.node.NetworkServiceGrpc.*;
import com.networking.node.NetworkServiceOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import jBeans.NodeInfo;

public class Transmitter implements Runnable {

    private final NetworkHandler networkHandler;
    private NodeInfo target;
    private ManagedChannel channel;

    public Transmitter(NetworkHandler networkHandler, NodeInfo node) {
        this.networkHandler = networkHandler;
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Token token = networkHandler.getToken();
            NodeInfo newTarget = token.getTo();

            if (target == null || target.getId() != newTarget.getId()) {
                target = newTarget;

                if (channel != null) {
                    channel.shutdownNow();
                }

                channel = ManagedChannelBuilder
                        .forTarget(target.getIp() + ":" + target.getPort())
                        .usePlaintext(true).build();
            }

            //plaintext channel on the address (ip/port) which offers the GreetingService service
            NetworkServiceStub stub = NetworkServiceGrpc.newStub(channel);
            stub.sendTheToken(Token.tokenBuild(token, token.getFrom(), token.getTo()), new StreamObserver<Message>() {
                @Override
                public void onNext(Message message) {

                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println(throwable.getMessage());
                }

                @Override
                public void onCompleted() {

                }
            });

            //System.out.println("[" + node.getId() + "] Token sent to " + networkHandler.getTarget().getId());
        }
    }

    //contact a node on the list and inform it about the new node
    public void greeting(NodeInfo node) {
        //System.out.println("Greeting to " + networkHandler.getTarget().getId());
        if (channel != null) {
            channel.shutdownNow();
        }

        //plaintext channel on the address (ip/port) which offers the GreetingService service
        channel = ManagedChannelBuilder.forTarget(networkHandler.getTarget().getIp() + ":" + networkHandler.getTarget().getPort()).usePlaintext(true).build();
        NetworkServiceBlockingStub stub = NetworkServiceGrpc.newBlockingStub(channel);
        ProtoNodeInfo info = ProtoNodeInfo.newBuilder().setIp(node.getIp()).setId(node.getId()).setPort(node.getPort()).build();
        Message message = stub.greeting(info);
        System.out.println(message.getMessage());
    }
}
