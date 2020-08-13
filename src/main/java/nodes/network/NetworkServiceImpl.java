package nodes.network;

import com.networking.node.NetworkServiceGrpc.*;
import com.networking.node.NetworkServiceOuterClass.*;
import io.grpc.stub.StreamObserver;
import jBeans.NodeInfo;
import nodes.Node;

import java.util.LinkedList;

public class NetworkServiceImpl extends NetworkServiceImplBase {
    Receiver receiver;

    public NetworkServiceImpl(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public StreamObserver<Token> sendTheToken(StreamObserver<Message> responseObserver) {
        return new StreamObserver<Token>() {

            @Override
            public void onNext(Token token) {
                receiveToken(token);
            }

            @Override
            public void onError(Throwable throwable) {
                //TODO implement
            }

            @Override
            public void onCompleted() {
                //TODO implement
            }
        };
    }

    @Override
    public void greeting(GreetingMessage request, StreamObserver<Message> responseObserver) {
        super.greeting(request, responseObserver);
    }

    private void receiveToken(Token token) {
        receiver.receiveToken(token);
    }
}
