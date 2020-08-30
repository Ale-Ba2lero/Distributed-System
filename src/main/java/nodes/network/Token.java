package nodes.network;

import com.networking.node.NetworkServiceOuterClass.*;
import jBeans.NodeInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import nodes.sensor.Measurement;

public class Token{
    private final NodeInfo from;
    private final NodeInfo to;
    private final LinkedList<NodeInfo> toAdd;
    private final LinkedList<NodeInfo> toRemove;
    private final ArrayList<Measurement> measurements;

    public Token(
            List<NodeInfo> toAdd,
            List<NodeInfo> toRemove,
            NodeInfo from,
            NodeInfo to,
            ArrayList<Measurement> measurements) {
        this.from = from;
        this.to = to;
        this.toAdd = new LinkedList<>(toAdd);
        this.toRemove = new LinkedList<>(toRemove);
        this.measurements = measurements;
    }

    public ArrayList<Measurement> getMeasurements() {
        return measurements;
    }

    public LinkedList<NodeInfo> getToAdd() {
        return toAdd;
    }

    public LinkedList<NodeInfo> getToRemove() {
        return toRemove;
    }

    public ProtoToken tokenBuild() {
        ProtoToken.Builder protoToken = ProtoToken.newBuilder();

        ArrayList<ProtoNodeInfo> protoToAdd = new ArrayList<>();
        toAdd.forEach((nodeInfo -> protoToAdd.add(ProtoNodeInfo
                .newBuilder()
                .setId(nodeInfo.getId())
                .setIp(nodeInfo.getIp())
                .setPort(nodeInfo.getPort())
                .build())));
        protoToken.addAllToAdd(protoToAdd);

        ArrayList<ProtoNodeInfo> protoToRemove = new ArrayList<>();
        toRemove.forEach((nodeInfo -> protoToRemove.add(ProtoNodeInfo
            .newBuilder()
            .setId(nodeInfo.getId())
            .setIp(nodeInfo.getIp())
            .setPort(nodeInfo.getPort())
            .build())));
        protoToken.addAllToRemove(protoToRemove);

        ArrayList<ProtoMeasurement> protoMeasurements = new ArrayList<>();
        measurements.forEach(m -> protoMeasurements.add(ProtoMeasurement
            .newBuilder()
            .setId(m.getId())
            .setType(m.getType())
            .setValue(m.getValue())
            .setTimestamp(m.getTimestamp()).build()));
        protoToken.addAllMeasurements(protoMeasurements);

        protoToken.setFrom(ProtoNodeInfo
            .newBuilder()
            .setId(from.getId())
            .setIp(from.getIp())
            .setPort(from.getPort())
            .build());

        protoToken.setTo(ProtoNodeInfo
            .newBuilder()
            .setId(to.getId())
            .setIp(to.getIp())
            .setPort(to.getPort())
            .build());

        return protoToken.build();
    }

    public NodeInfo getFrom() {
        return from;
    }

    public NodeInfo getTo() {return to;}

    public static LinkedList<NodeInfo> fromProtoToNode (List<ProtoNodeInfo> protoNodeInfo) {
        LinkedList<NodeInfo> nodeList = new LinkedList<>();

        protoNodeInfo.forEach(pni -> nodeList.add(new NodeInfo(pni.getId(), pni.getIp(), pni.getPort())));

        return nodeList;
    }
}
