package gateway;

import java.util.ArrayList;
import java.util.List;

public class Gateway {

    private static Gateway instance = null;
    private List<NodeInfo> nodes;

    private Gateway() {
        nodes = new ArrayList<NodeInfo>();
    }

    public static synchronized Gateway getInstance()
    {
        if (instance == null)
            instance = new Gateway();

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
