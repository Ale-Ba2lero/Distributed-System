package nodes.network;

import jBeans.NodeInfo;

import java.util.*;

public class NetworkHandler implements Runnable{
    private final NodeInfo node;
    private volatile NodeState nodeState;

    private final LinkedList<NodeInfo> nodes;
    private Transmitter transmitter;
    private Receiver receiver;

    private final Object networkTargetLock;
    private NodeInfo target;

    private final Object tokenLock;
    private Token token;

    private final ArrayList<NodeInfo> greetingNodes;

    public NetworkHandler(NodeInfo nodeInfo) {
        nodeState = NodeState.STARTING;
        node = nodeInfo;
        nodes = new LinkedList<>();
        greetingNodes = new ArrayList<>();
        networkTargetLock = new Object();
        tokenLock = new Object();
    }

    // If this is not the only node, start a token transmitter thread (gRPC client)
    public void init(LinkedList<NodeInfo> nodeList, Transmitter transmitter, Receiver receiver) {
        this.nodes.addAll(nodeList);

        this.receiver = receiver;
        this.transmitter = transmitter;

        if (nodes.size() > 1) {
            target = getNextNodeInNetwork();
            transmitter.greeting(node);
            nodeState = NodeState.RUNNING;
        } else {
            target = null;
            token = null;
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    this.wait();
                }

                ArrayList<NodeInfo> greetingsQueue = receiver.getGreetingsQueue();
                greetingsQueue.forEach(this::insertNodeToNetwork);

                Token receivedToken = receiver.getToken();
                if (receivedToken != null) {
                    computeToken(receivedToken);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void computeToken(Token receivedToken) {
        //TODO handle token measurements
        ArrayList<NodeInfo> tokenNodesToAdd = new ArrayList<>();
        ArrayList<NodeInfo> tokenNodesToRemove = new ArrayList<>();

        // Add the nodes to the network list and retransmit them to the next node
        if (receivedToken.getToAdd().size() > 0) {
            receivedToken.getToAdd().forEach(nodeInfo -> {
                // If the node to add is the same that is the current one that means that the token made
                // a full loop.
                // The addition of the node to the network is complete.
                if (nodeInfo.getId() != node.getId()) {
                    sortedAdd(nodes, nodeInfo);
                    tokenNodesToAdd.add(nodeInfo);
                }
            });
            //update gRPC references
            updateNetworkTarget();
        }

        // The target node is the one that is currently the successor in the network
        NodeInfo targetNode = target;

        // Remove the nodes to the network list and retransmit them to the next node
        if (receivedToken.getToRemove().size() > 0) {
            receivedToken.getToRemove().forEach(nodeInfo -> {
                if (nodeInfo.getId() != node.getId()) {
                    nodes.forEach(n -> {
                        if (n.getId() == nodeInfo.getId()) {
                            nodes.remove(n);
                        }
                    });
                    tokenNodesToRemove.add(nodeInfo);
                } else {
                    // If the node to remove is the current one that means that the token made
                    // a full loop through the network.
                    // The removal of the node from the network is complete.
                    quit();
                }
            });

            updateNetworkTarget();
        }

        tokenNodesToAdd.addAll(greetingNodes);
        greetingNodes.clear();
        if (nodeState == NodeState.QUITTING) tokenNodesToRemove.add(node);

        token = new Token(tokenNodesToAdd, tokenNodesToRemove, node, targetNode);
        tokenReady();
    }

    // The entry token has been handled and is ready to be sent to the next node
    private void tokenReady() {
        synchronized (transmitter) {
            transmitter.notify();
        }
    }

    // Updates the node reference inside the network. This means set the new target node that will receive the token.
    private void updateNetworkTarget() {
        synchronized (networkTargetLock) {
            NodeInfo newNext = getNextNodeInNetwork();
            if (target != null && newNext.getId() != target.getId()) {
                target = newNext;
            } else if (target == null) {
                // If next is null this means this was the first node entering the network and now is updating its references
                // due to an other node greeting to it. Now the network contains two nodes and the token loop can start.
                target = newNext;
                nodeState = NodeState.RUNNING;
                token = new Token(new ArrayList<>(),
                        new ArrayList<>(),
                        node, target);
                tokenReady();
            }
        }
    }

    private NodeInfo getNextNodeInNetwork() {
        NodeInfo next = nodes.get((getIndex(nodes, node) + 1) % nodes.size());
        return new NodeInfo(next.getId(),next.getIp(), next.getPort());
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

    private void sortedAdd(List<NodeInfo> list, NodeInfo element) {
        int index = Collections.binarySearch(list, element, Comparator.comparing(NodeInfo::getId));
        if (index < 0) {
            index = -index - 1;
        }
        list.add(index, element);
        System.out.println(nodes);
    }

    private void insertNodeToNetwork(NodeInfo nodeInfo) {
        sortedAdd(nodes, nodeInfo);
        System.out.println(nodes);
        greetingNodes.add(nodeInfo);
        updateNetworkTarget();
    }

    private void quit() {
        System.out.println("Node successfully removed");
    }

    public NodeInfo getTarget() {
        synchronized (networkTargetLock) {
            return new NodeInfo(target.getId(), target.getIp(), target.getPort());
        }
    }

    public Token getToken() {
        synchronized (tokenLock) {
            return token;
        }
    }

    public NodeInfo getNode() {
        return node;
    }

    public NodeState getNodeState() {
        synchronized (nodeState) {
            return nodeState;
        }
    }

    public void removeNodeFromNetwork() {
        nodeState = NodeState.QUITTING;
    }

    public enum NodeState {
        STARTING,
        RUNNING,
        QUITTING
    }
 }


