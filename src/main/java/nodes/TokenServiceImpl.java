package nodes;

import com.networking.node.TokenServiceGrpc.*;
import com.networking.node.TokenServiceOuterClass.*;
import io.grpc.stub.StreamObserver;

public class TokenServiceImpl extends TokenServiceImplBase {
    @Override
    public StreamObserver<Token> sendTheToken(StreamObserver<Ack> responseObserver) {
        return super.sendTheToken(responseObserver);


    }
}
