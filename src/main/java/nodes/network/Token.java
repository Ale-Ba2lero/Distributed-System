package nodes.network;

import com.networking.node.NetworkServiceOuterClass.*;
import jBeans.NodeInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Token{
    private final NodeInfo from;
    private final NodeInfo to;
    private final LinkedList<NodeInfo> toAdd;
    private final LinkedList<NodeInfo> toRemove;

    public Token(List<NodeInfo> toAdd, List<NodeInfo> toRemove, NodeInfo from, NodeInfo to) {
        this.from = from;
        this.to = to;
        this.toAdd = new LinkedList<>(toAdd);
        this.toRemove = new LinkedList<>(toRemove);
    }

    public LinkedList<NodeInfo> getToAdd() {
        return toAdd;
    }

    public LinkedList<NodeInfo> getToRemove() {
        return toRemove;
    }

    public static ProtoToken tokenBuild(Token token, NodeInfo from, NodeInfo to) {
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

        protoToken.setFrom(ProtoNodeInfo
                .newBuilder()
                .setId(from.getId())
                .setIp(from.getIp())
                .setPort(from.getPort())
                .build());

        protoToken.setTo(ProtoNodeInfo
                .newBuilder()
                .setId(to.getId())
                .setIp(to.getIp())
                .setPort(to.getPort())
                .build());

        //TODO build token sensor data field
        return protoToken.build();
    }

    public NodeInfo getFrom() {
        return from;
    }

    public NodeInfo getTo() {return to;}

    public static LinkedList<NodeInfo> fromProtoToNode (List<ProtoNodeInfo> protoNodeInfo) {
        LinkedList<NodeInfo> nodeList = new LinkedList<>();

        protoNodeInfo.forEach(pni -> {
            nodeList.add(new NodeInfo(pni.getId(), pni.getIp(), pni.getPort()));
        });

        return nodeList;
    }
}
