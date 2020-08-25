package nodes.network.messages;

import com.networking.node.NetworkServiceOuterClass.*;
import jBeans.NodeInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Token extends NetworkMessage {
    private final NodeInfo node;
    private final LinkedList<NodeInfo> toAdd;
    private final LinkedList<NodeInfo> toRemove;
    private final Long loop;

    public Token(MessageType type, List<NodeInfo> toAddNodeList, List<NodeInfo> toRemoveNodeList, NodeInfo node, long loop) {
        super(type);

        this.loop = loop;
        this.node = node;
        toAdd = new LinkedList<>();
        toRemove = new LinkedList<>();

        toAddNodeList.forEach(protoNodeInfo -> {
            if (protoNodeInfo.getId() != node.getId()) {
                toAdd.add(new NodeInfo(protoNodeInfo.getId(), protoNodeInfo.getIp(),protoNodeInfo.getPort()));
            }
        });

        toRemoveNodeList.forEach(protoNodeInfo -> {
            if (protoNodeInfo.getId() != node.getId()) {
                toRemove.add(new NodeInfo(protoNodeInfo.getId(), protoNodeInfo.getIp(),protoNodeInfo.getPort()));
            }
        });
    }

    public LinkedList<NodeInfo> getToAdd() {
        return toAdd;
    }

    public LinkedList<NodeInfo> getToRemove() {
        return toRemove;
    }

    public static ProtoToken tokenBuild(Token token, NodeInfo node) {
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
                    .setId(node.getId())
                    .setIp(node.getIp())
                    .setPort(node.getPort())
                    .build());
        }));

        ProtoToken.Builder protoToken = ProtoToken.newBuilder();
        protoToken.addAllToAdd(protoToAdd);
        protoToken.addAllToRemove(protoToRemove);

        protoToken.setFrom(ProtoNodeInfo
                .newBuilder()
                .setId(node.getId())
                .setIp(node.getIp())
                .setPort(node.getPort())
                .build());

        protoToken.setLoop(token.loop);

        //TODO build token sensor data field
        return protoToken.build();
    }

    public NodeInfo getNode() {
        return node;
    }

    public static LinkedList<NodeInfo> fromProtoToNode (List<ProtoNodeInfo> protoNodeInfo) {
        LinkedList<NodeInfo> nodeList = new LinkedList<>();

        protoNodeInfo.forEach(pni -> {
            nodeList.add(new NodeInfo(pni.getId(), pni.getIp(), pni.getPort()));
        });

        return nodeList;
    }

    public long getLoop () {
        return loop;
    }
}
