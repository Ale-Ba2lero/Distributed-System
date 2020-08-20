package nodes.network;

import com.networking.node.NetworkServiceOuterClass.*;
import jBeans.NodeInfo;

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

    public LinkedList<NodeInfo> getToAdd() {
        return toAdd;
    }

    public LinkedList<NodeInfo> getToRemove() {
        return toRemove;
    }
}
