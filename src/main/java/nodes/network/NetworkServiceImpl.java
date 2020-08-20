package nodes.network;

import com.networking.node.NetworkServiceGrpc.*;
import com.networking.node.NetworkServiceOuterClass.*;
import io.grpc.stub.StreamObserver;

public class NetworkServiceImpl extends NetworkServiceImplBase {
    Receiver receiver;

    public NetworkServiceImpl(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void sendTheToken(ProtoToken token, StreamObserver<Message> responseObserver) {
        receiver.receiveToken(token);

        responseObserver.onNext(Message.newBuilder().setMessage("Token received").build());

        responseObserver.onCompleted();
    }

    @Override
    public void greeting(ProtoNodeInfo node, StreamObserver<Message> responseObserver) {
        receiver.addNode(node);

        responseObserver.onNext(Message.newBuilder().setMessage("Greeting!! :)").build());

        responseObserver.onCompleted();
    }

    private void receiveToken(ProtoToken token) {
        receiver.receiveToken(token);
    }
}
