package gateway.singleton;

import jBeans.NodeInfo;

import java.util.Collections;
import java.util.LinkedList;

public class NodeHandler {

    private static NodeHandler instance = null;
    private static LinkedList<NodeInfo> nodes;

    private NodeHandler() {
        nodes = new LinkedList<NodeInfo>();
    }

    public static synchronized NodeHandler getInstance() {
        if (instance == null)
            instance = new NodeHandler();

        return instance;
    }

    public synchronized void addNode(NodeInfo nodeInfo) {
        nodes.add(nodeInfo);

        //Keep the list to reference node sorted by the id
        Collections.sort(nodes);
    }

    public synchronized LinkedList<NodeInfo> getNodesList() {
        return new LinkedList<NodeInfo>(nodes);
    }

    public synchronized boolean deleteNode (NodeInfo node) {
        return nodes.remove(node);
    }

    public synchronized int getNodesSize() {
        return nodes.size();
    }
}
