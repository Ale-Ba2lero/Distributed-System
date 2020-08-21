package nodes.network;

import com.networking.node.NetworkServiceGrpc;
import com.networking.node.NetworkServiceGrpc.*;
import com.networking.node.NetworkServiceOuterClass.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jBeans.NodeInfo;

public class Transmitter implements Runnable {

    private final NetworkHandler networkHandler;
    private final NodeInfo node;
    private ManagedChannel channel;

    public Transmitter(NetworkHandler networkHandler, NodeInfo node) {
        this.networkHandler = networkHandler;
        this.node = node;
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

            if (channel != null) {
                channel.shutdownNow();
            }
            //plaintext channel on the address (ip/port) which offers the GreetingService service
            channel = ManagedChannelBuilder.forTarget(networkHandler.getTarget().getIp() + ":" + networkHandler.getTarget().getPort()).usePlaintext(true).build();
            NetworkServiceBlockingStub stub = NetworkServiceGrpc.newBlockingStub(channel);
            stub.sendTheToken(Token.tokenBuild(networkHandler.getToken(), node));

            // The following instruction could be the cause of issues with the token
            //System.out.println("[" + node.getId() + "] Token sent to " + networkHandler.getTarget().getId() + " " + System.currentTimeMillis());
        }
    }

    //contact a node on the list and inform it about the new node
    public void greeting() {
        System.out.println("Greeting to " + networkHandler.getTarget().getId());
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
