package nodes.network.messages;

import jBeans.NodeInfo;

public class GreetingMessage  extends NetworkMessage {
    private NodeInfo nodeInfo;

    public GreetingMessage(MessageType type, NodeInfo nodeInfo) {
        super(type);
        this.nodeInfo = nodeInfo;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }
}
