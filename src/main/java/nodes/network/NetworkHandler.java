package nodes.network;

import jBeans.NodeInfo;
import nodes.network.messages.GreetingMessage;
import nodes.network.messages.MessageType;
import nodes.network.messages.NetworkMessage;
import nodes.network.messages.Token;

import java.io.IOException;
import java.util.*;

public class NetworkHandler implements Runnable{
    private NodeInfo node;
    private LinkedList<NodeInfo> nodes;
    private Transmitter transmitter;
    private Receiver receiver;

    private NodeInfo next;
    private Token token;

    private final LinkedList<NodeInfo> greetingNodes;

    public NetworkHandler(NodeInfo nodeInfo) {
        node = nodeInfo;
        greetingNodes = new LinkedList<>();
    }

    // If this is not the only node, start a token transmitter thread (gRPC client)
    public void init(LinkedList<NodeInfo> nodes) throws IOException {
        this.nodes = nodes;

        transmitter = new Transmitter(this, node);
        receiver = new Receiver(this, node);

        Thread transmitterThread = new Thread(transmitter);

        if (nodes.size() > 1) {
            next = getNextNodeInNetwork(nodes, node);
            transmitter.greeting();
        } else {
            next = null;
            token = new Token(
                MessageType.TOKEN ,
                new LinkedList<>(),
                new LinkedList<>(),
                node);
        }

        transmitterThread.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    this.wait();
                }
                ArrayList<NetworkMessage> queue = receiver.getMessagesQueue();
                queue.forEach(msg -> {
                    switch (msg.getType()) {
                        case TOKEN:
                            computeToken((Token) msg);
                            break;
                        case GREETING:
                            insertGreetingNodeToNetwork((GreetingMessage) msg);
                            break;
                        case EXIT:
                            break;
                    }
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void computeToken(Token receivedToken) {
        System.out.println("Token Received from " + receivedToken.getNode().getId() + " " + System.currentTimeMillis());
        //TODO handle token measurements

        List<NodeInfo> tokenNodesToAdd = new LinkedList<>();
        List<NodeInfo> tokenNodesToRemove = new LinkedList<>();

        //If there are nodes to add, make a call to the network handler to add the new nodes
        if (receivedToken.getToAdd().size() > 0) {
            receivedToken.getToAdd().forEach(nodeInfo -> {
                // If the node to add its the same that is updating its list that means that the token made
                // a full loop. The node addition is complete.
                if (nodeInfo.getId() != node.getId()) {
                    System.out.println("Adding " + nodeInfo.getId());
                    sortedAdd(nodes, nodeInfo);
                    tokenNodesToAdd.add(nodeInfo);
                }
            });
            //update gRPC references
            updateNetworkReference();
        }

        tokenNodesToAdd.addAll(greetingNodes);
        greetingNodes.clear();
        token = new Token(MessageType.TOKEN, tokenNodesToAdd, tokenNodesToRemove, node);


        /*try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        //send the token
        tokenReady();

        //If there are nodes to remove, make a call to the network handler to remove the new nodes
        /*
        if (toRemove.size() > 0) {
            removeNodesFromList(toRemove);
        }*/
    }

    private void tokenReady() {
        synchronized (transmitter) {
            transmitter.notify();
        }
    }

    public synchronized void insertGreetingNodeToNetwork(GreetingMessage message) {
        NodeInfo nodeInfo = message.getNodeInfo();
        System.out.println("Greeting received from " + nodeInfo.getId());
        sortedAdd(nodes, nodeInfo);
        greetingNodes.add(nodeInfo);
        updateNetworkReference();
    }

    public synchronized NodeInfo getTarget() {
        return next;
    }

    public synchronized Token getToken() {
        return token;
    }

    // Updates the node reference inside the network. This means set the new target node that will receive the token.
    private void updateNetworkReference() {

        NodeInfo newNext = getNextNodeInNetwork(nodes, this.node);
        if (next != null && newNext.getId() != next.getId()) {
            next = newNext;
            System.out.println("Updating target node to " + next.getId());
        } else if (next == null) {
            // If next is null this means this was the first node entering the network and now is updating its references
            // due to an other node greeting to it. Now the network contains two nodes and the token loop can start.
            System.out.println("Starting token loop!");
            next = newNext;
            tokenReady();
        }
    }

    private static NodeInfo getNextNodeInNetwork(LinkedList<NodeInfo> nodes, NodeInfo node) {
        return nodes.get((getIndex(nodes, node) + 1) % nodes.size());
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

    public NodeInfo getNode() {
        return node;
    }

    private void sortedAdd(List<NodeInfo> list, NodeInfo element) {
        int index = Collections.binarySearch(list, element, Comparator.comparing(NodeInfo::getId));
        if (index < 0) {
            index = -index - 1;
        }
        list.add(index, element);
    }
 }
