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

    private final LinkedList<NodeInfo> greetingNodes;

    public NetworkHandler(NodeInfo nodeInfo, LinkedList<NodeInfo> nodes) throws IOException {
        this.node = nodeInfo;
        this.nodes = nodes;
        next = null;
        this.greetingNodes = new LinkedList<>();

        // Start the token receiver thread (gRPC server)
        new Receiver(this, nodeInfo);

        // Instantiate the token transmitter
        transmitter = new Transmitter(this, node);
        Thread transmitterThread = new Thread(transmitter);

        // If this is not the only node, start a token transmitter thread (gRPC client)
        if (nodes.size() > 1) {
            next = getNextNodeInNetwork(nodes, node);
            transmitter.greeting();
        } else {
            token = new Token(new LinkedList<>(), new LinkedList<>(), node);
        }

        transmitterThread.run();
    }

    public void receiveToken(ProtoToken protoToken) {
        //TODO handle token measurements

        //Get information about network changes from the token
        List<ProtoNodeInfo> toAdd = protoToken.getToAddList();
        List<ProtoNodeInfo> toRemove = protoToken.getToRemoveList();

        List<ProtoNodeInfo> tokenNodesToAdd = new LinkedList<>();
        List<ProtoNodeInfo> tokenNodesToRemove = new LinkedList<>();

        //If there are nodes to add, make a call to the network handler to add the new nodes
        if (toAdd.size() > 0) {
            System.out.println("Adding nodes");
            toAdd.forEach(protoNodeInfo -> {
                if (protoNodeInfo.getId() != node.getId()) {
                    nodes.add(new NodeInfo(protoNodeInfo.getId(), protoNodeInfo.getIp(), protoNodeInfo.getPort()));
                    tokenNodesToAdd.add(protoNodeInfo);
                }
            });

            Collections.sort(nodes);
            //update gRPC references
            updateNetworkReference();
        }

        token = new Token(tokenNodesToAdd, tokenNodesToRemove, node);

        if (greetingNodes.size() > 0) {
            addGreetingNodesToList();
        }

        //send the token
        tokenReady();

        //If there are nodes to remove, make a call to the network handler to remove the new nodes
        /*
        if (toRemove.size() > 0) {
            removeNodesFromList(toRemove);
        }*/
    }

    public void tokenReady() {
        synchronized (transmitter) {
            transmitter.notify();
        }
    }

    private synchronized void addGreetingNodesToList() {
        greetingNodes.forEach(node -> token.setNodeToAdd(node));
        greetingNodes.clear();
    }

    public synchronized void addNodeToList(ProtoNodeInfo protoNodeInfo) {
        NodeInfo newNode = new NodeInfo(protoNodeInfo.getId(), protoNodeInfo.getIp(), protoNodeInfo.getPort());
        nodes.add(newNode);
        greetingNodes.add(newNode);
        Collections.sort(nodes);

        //TODO update gRPC references
        updateNetworkReference();

        System.out.println("Adding " + protoNodeInfo.getId());
        System.out.println(nodes);
    }

    //Convert the protoNodeInfo obj into NodeInfo obj and remove them from the list

    /*public synchronized void removeNodesFromList(List<ProtoNodeInfo> toRemove) {
         toRemove.forEach((pni -> nodes.forEach(n -> {
            if (n.getId() == pni.getId() && n.getId() != node.getId()) {
                nodes.remove(n);
            } else if (n.getId() == pni.getId() && n.getId() == node.getId()) {
                quitNetwork();
            }
        })));

        //update gRPC references
        updateNetworkReference();
    }
    */


    public LinkedList<NodeInfo> getNodes() {
        return nodes;
    }

    public NodeInfo getTarget() {
        return next;
    }

    public Token getToken() {
        return token;
    }

    private void quitNetwork() {
        //TODO
    }

    private void updateNetworkReference() {
        NodeInfo newNext = getNextNodeInNetwork(nodes, this.node);
        if (next != null && newNext.getId() != next.getId()) {
            next = newNext;
            System.out.println("Updating target node to " + next.getId());
        } else if (next == null) {
            System.out.println("Starting token loop!");
            next = newNext;
            tokenReady();
        }
    }

    private static NodeInfo getNextNodeInNetwork(LinkedList<NodeInfo> nodes, NodeInfo node) {
        return nodes.get((getIndex(nodes, node) + 1) % nodes.size());
    }

    /*
    private static NodeInfo getPreviousNodeInNetwork(LinkedList<NodeInfo> nodes, NodeInfo node) {
        int idx = getIndex(nodes, node);
        if (idx == 0) {
            return nodes.get(nodes.size() - 1);
        } else {
            return nodes.get(idx - 1);
        }
    }
    */

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
