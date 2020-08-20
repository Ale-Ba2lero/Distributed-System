package nodes.network;

import com.networking.node.NetworkServiceOuterClass.*;
import jBeans.NodeInfo;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class NetworkHandler {
    private final Transmitter transmitter;
    private final NodeInfo node;
    private final LinkedList<NodeInfo> nodes;

    private NodeInfo next;
    private Token token;


    public NetworkHandler(NodeInfo nodeInfo, LinkedList<NodeInfo> nodes) throws IOException, InterruptedException {
        this.node = nodeInfo;
        this.nodes = nodes;
        next = null;

        // Start the token receiver thread (gRPC server)
        Receiver receiver = new Receiver(this, nodeInfo);

        // Instantiate the token transmitter
        transmitter = new Transmitter(this, node);

        // If this is not the only node, start a token transmitter thread (gRPC client)
        if (nodes.size() > 1) {
            next = getNextNodeInNetwork(nodes, node);
            transmitter.greeting();
        }

        transmitter.run();
    }

    public void receiveToken(ProtoToken ptotoToken) {
        //TODO handle token measurements

        //Get information about network changes from the token
        List<ProtoNodeInfo> toAdd = ptotoToken.getToAddList();
        List<ProtoNodeInfo> toRemove = ptotoToken.getToRemoveList();

        //If there are nodes to add, make a call to the network handler to add the new nodes
        if (toAdd.size() > 0) {
            addNodesToList(toAdd);
        }

        //prepare the token
        token = new Token(toAdd, toRemove, node);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //send the token
        tokenReady();

        //If there are nodes to remove, make a call to the network handler to remove the new nodes
        if (toRemove.size() > 0) {
            removeNodesFromList(toRemove);
        }
    }

    public void tokenReady() {
        synchronized (transmitter) {
            transmitter.notify();
        }
    }

    //Convert the protoNodeInfo obj into NodeInfo obj and add them to the list
    public synchronized void addNodesToList(List<ProtoNodeInfo> toAdd) {
        toAdd.forEach((pni -> {
            if (pni.getId() != node.getId()) {
                nodes.add(new NodeInfo(pni.getId(), pni.getIp(), pni.getPort()));
            }
        }));

        Collections.sort(nodes);
        //update gRPC references
        updateNetworkReference();
    }

    public synchronized void addNodeToList(ProtoNodeInfo pnode) {
        nodes.add(new NodeInfo(pnode.getId(), pnode.getIp(), pnode.getPort()));

        Collections.sort(nodes);

        //TODO update gRPC references
        updateNetworkReference();
    }

    //Convert the protoNodeInfo obj into NodeInfo obj and remove them from the list
    public synchronized void removeNodesFromList(List<ProtoNodeInfo> toRemove) {
        toRemove.forEach((pni -> {
            nodes.forEach(n -> {
                if (n.getId() == pni.getId() && n.getId() != node.getId()) {
                    nodes.remove(n);
                } else if (n.getId() == pni.getId() && n.getId() == node.getId()) {
                    quitNetwork();
                }
            });
        }));

        //update gRPC references
        updateNetworkReference();
    }

    public LinkedList<NodeInfo> getNodes() {
        return nodes;
    }

    public NodeInfo getTarget() {
        return next;
    }

    public Token getToken() {
        return token;
    }

    public void startTokenLoop() {
        token = new Token(new LinkedList<>(), new LinkedList<>(), node);
        tokenReady();
        System.out.println("Starting token loop");
    }

    private void quitNetwork() {
        //TODO
    }

    private void updateNetworkReference() {
        NodeInfo newNext = getNextNodeInNetwork(nodes, this.node);
        if (next != null && newNext.getId() != next.getId()) {
            next = newNext;
        } else if (next == null) {
            next = newNext;
            transmitter.run();
            tokenReady();
        }
    }

    private static NodeInfo getNextNodeInNetwork(LinkedList<NodeInfo> nodes, NodeInfo node) {
        return nodes.get((getIndex(nodes, node) + 1) % nodes.size());
    }

    private static NodeInfo getPreviousNodeInNetwork(LinkedList<NodeInfo> nodes, NodeInfo node) {
        int idx = getIndex(nodes, node);
        if (idx == 0) {
            return nodes.get(nodes.size() - 1);
        } else {
            return nodes.get(idx - 1);
        }
    }

    private static int getIndex(LinkedList<NodeInfo> nodes, NodeInfo node) {
        int i;
        for (i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId() == node.getId()) {
                return i;
            }
        }
        return -1;
    }
}
