package nodes.network;

import com.networking.node.NetworkServiceOuterClass.*;
import jBeans.NodeInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Token {
    private final LinkedList<NodeInfo> toAdd;
    private final LinkedList<NodeInfo> toRemove;

    public Token(List<ProtoNodeInfo> toAddNodeList, List<ProtoNodeInfo> toRemoveNodeList, NodeInfo node) {
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

    public void setNodeToAdd(NodeInfo nodeInfo) {
        toAdd.add(nodeInfo);
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
                    .setId(nodeInfo.getId())
                    .setIp(nodeInfo.getIp())
                    .setPort(nodeInfo.getPort())
                    .build());
        }));

        ProtoToken.Builder protoToken = ProtoToken.newBuilder();
        protoToken.addAllToAdd(protoToAdd);
        protoToken.addAllToRemove(protoToRemove);

        protoToken.setFrom(node.getId());

        //TODO build token sensor data field

        return protoToken.build();
    }
}
