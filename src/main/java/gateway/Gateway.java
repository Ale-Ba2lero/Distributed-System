package gateway;

import nodes.Node;


import java.util.ArrayList;
import java.util.List;

public class Gateway {

    private static Gateway instance = null;
    private List<Node> nodes;

    private Gateway() {
        nodes = new ArrayList<Node>();
    }

    public static synchronized Gateway getInstance()
    {
        if (instance == null)
            instance = new Gateway();

        return instance;
    }

    public synchronized void addNode(Node node) {
        this.nodes.add(node);
    }

    //a shallow copy is returned instead of the reference to be thread safe
    public synchronized List<Node> getNodesList() {
        return new ArrayList<Node>(this.nodes);
    }
}
