package c.m.gindexer;

import c.m.GctsClientTool;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.apache.http.HttpHeaders.USER_AGENT;

public class IndexerMain {

    private IncomingJobUpdates get(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Gson g = new Gson();
            IncomingJobUpdates jresp = g.fromJson(response.toString(), IncomingJobUpdates.class);
            System.out.println(jresp);
            return jresp;

        } else {
            System.out.println("GET request not worked");
        }

        return null;
    }

    private static void doWork() throws Exception {

        System.out.println("starting the indexer tp GCTS");
        // Prod English and NL jobs
        String url = "http://10.167.5.191:8080/cupd/v2/updatelog?from=";
        url += startBatch;

        didWork = false;
        IndexerMain im = new IndexerMain();
        IncomingJobUpdates resp = im.get(url);
        if (resp != null) {
            MBatch[] batches = resp.batches;
            for (int i = 0; i < batches.length; i++) {

                System.out.println("batch " + batches[i].batchNumber + " has " + batches[i].documents.length);
                for (int j = 0; j < batches[i].documents.length; j++) {
                    didWork = true;
                    MDocument msg = batches[i].documents[j];
                    if ( msg.action.equals("I")) {
                        // update/insert
                        GctsClientTool.update(msg.postingId, msg.originalDoc);
                    } else {
                        // delete
                        GctsClientTool.delete(msg.postingId);
                    }
                }
                startBatch = batches[i].batchNumber;
            }
        }

    }

    private static int startBatch = 0;
    private static boolean didWork = true;

    public static void main(String[] a) throws Exception {

        while (didWork) {
            doWork();
            System.out.println("didWork " + didWork + " startBatch " + startBatch);
            Thread.sleep(10000);
        }

    }
}
