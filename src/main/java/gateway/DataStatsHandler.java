package gateway;

import java.util.ArrayList;
import java.util.List;

public class DataStatsHandler {
    private static DataStatsHandler instance = null;
    private List<String> datas;

    private DataStatsHandler() {
        datas = new ArrayList<String>();
    }

    public static synchronized DataStatsHandler getInstance()
    {
        if (instance == null)
            instance = new DataStatsHandler();

        return instance;
    }

    public synchronized void addData(String data) {
        this.datas.add(data);
    }

    //a shallow copy is returned instead of the reference to be thread safe
    public synchronized List<String> getAllData() {
        return new ArrayList<String>(this.datas);
    }

    public synchronized String getLastData() {
        return new String(this.datas.get(this.datas.size()));
    }

}
