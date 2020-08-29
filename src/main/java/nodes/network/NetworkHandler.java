package nodes.network;

import jBeans.NodeInfo;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private long i = 0;

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
        while (nodeState != NodeState.DONE) {
            try {
                synchronized (this) {
                    this.wait();
                }

                ArrayList<NodeInfo> greetingsQueue = receiver.getGreetingsQueue();
                greetingsQueue.forEach(nodeInfo -> {
                    sortedAdd(nodes, nodeInfo);
                    greetingNodes.add(nodeInfo);
                    updateNetworkTarget();
                });

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
        i++;
        if (i % 1000 == 0) {
            System.out.println("Token received: " + i);
        }

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
            System.out.println("Add");
            updateNetworkTarget();
        }

        // The target node is the one that is currently the successor in the network
        NodeInfo targetNode = target;

        // Remove the nodes to the network list and retransmit them to the next node
        if (receivedToken.getToRemove().size() > 0) {
            receivedToken.getToRemove().forEach(nodeInfo -> {
                if (nodeInfo.getId() != node.getId()) {
                    for (int i = 0; i < nodes.size(); i++) {
                        if (nodes.get(i).getId() == nodeInfo.getId()) {
                            nodes.remove(nodes.get(i));
                            i = nodes.size();
                        }
                    }
                    tokenNodesToRemove.add(nodeInfo);
                } else {
                    // If the node to remove is the current one that means that the token made
                    // a full loop through the network.
                    // The removal of the node from the network is complete.
                    quit();
                }
            });
            System.out.println("Remove");
            updateNetworkTarget();
        }

        tokenNodesToAdd.addAll(greetingNodes);
        greetingNodes.clear();
        if (nodeState == NodeState.QUITTING) tokenNodesToRemove.add(node);

        if (targetNode.getId() == node.getId()) {
            System.out.println("Last node");
            target = null;
            token = null;
            nodeState = NodeState.STARTING;
        } else {
            token = new Token(tokenNodesToAdd, tokenNodesToRemove, node, targetNode);
            tokenReady();
        }
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

        System.out.println(nodes);
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
    }

    private void quit() {
        nodeState = NodeState.DONE;
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
        QUITTING,
        DONE
    }
 }


