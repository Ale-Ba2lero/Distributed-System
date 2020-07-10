package gateway;

import java.util.ArrayList;
import java.util.List;

public class NodeHandler {

    private static NodeHandler instance = null;
    private List<NodeInfo> nodes;

    private NodeHandler() {
        nodes = new ArrayList<NodeInfo>();
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

    //a shallow copy is returned instead of the reference to be thread safe
    public synchronized List<NodeInfo> getNodesList() {
        return new ArrayList<NodeInfo>(this.nodes);
    }
}
