package gateway.singleton;

import java.util.ArrayList;
import java.util.List;

public class DataHandler {
    private static DataHandler instance = null;
    private List<String> data;

    private DataHandler() {
        data = new ArrayList<String>();
    }

    public static synchronized DataHandler getInstance()
    {
        if (instance == null) {
            instance = new DataHandler();
            instance.data.add("Hello, world!");
        }

        return instance;
    }

    public synchronized void addData(String data) {
        this.data.add(data);
    }

    //a shallow copy is returned instead of the reference to be thread safe
    public synchronized List<String> getAllData() {
        return new ArrayList<String>(this.data);
    }

    public synchronized String getLastData() {
        return new String(this.data.get(this.data.size()));
    }

}
