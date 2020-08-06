package nodes;

import com.networking.node.TokenServiceGrpc;
import com.networking.node.TokenServiceGrpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import jBeans.NodeInfo;

import java.util.concurrent.TimeUnit;

public class NetworkTransmitter implements Runnable{

    private NodeInfo nextInNetwork;

    @Override
    public void run() {
        if (nextInNetwork != null) {

        }
    }

    public void setNextNodeInNetwork(NodeInfo nextNode) {
        this.nextInNetwork = nextNode;
    }

    public void transmitToken() throws InterruptedException {
        //plaintext channel on the address (ip/port) which offers the GreetingService service
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(nextInNetwork.getIp() + ":" + nextInNetwork.getPort()).usePlaintext(true).build();

        //creating an asynchronous stub on the channel
        TokenServiceStub stub = TokenServiceGrpc.newStub(channel);

    }
}
