package gateway.singleton;

import jBeans.NodeInfo;
import java.util.LinkedList;

public class NodeHandler {

    private static NodeHandler instance = null;
    private LinkedList<NodeInfo> nodes;

    private NodeHandler() {
        nodes = new LinkedList<NodeInfo>();
    }

    public static synchronized NodeHandler getInstance()
    {
        if (instance == null)
            instance = new NodeHandler();

        return instance;
    }

    public synchronized void addNode(NodeInfo nodeInfo) {
        this.nodes.add(nodeInfo);
    }

    public synchronized LinkedList<NodeInfo> getNodesList() {
        return new LinkedList<NodeInfo>(this.nodes);
    }

    public synchronized boolean deleteNode (NodeInfo node) {
        return nodes.remove(node);
    }
}
