package wasa_ele.ghostlite;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**

 */
public class WorkerRunnable implements Runnable{

    private static String requestPath;

    private static Map<String, String> CONTENT_TYPES = new HashMap<>();

    static {
        CONTENT_TYPES.put("html", "text/html");
        CONTENT_TYPES.put("htm", "text/html");
        CONTENT_TYPES.put("css", "text/css");
        CONTENT_TYPES.put("js", "text/javascript");
        CONTENT_TYPES.put("jpg", "image/jpeg");
        CONTENT_TYPES.put("jpeg", "image/jpeg");
        CONTENT_TYPES.put("png", "image/png");
        CONTENT_TYPES.put("gif", "image/gif");
        CONTENT_TYPES.put("pdf", "application/pdf");
        CONTENT_TYPES.put("txt", "text/plain");
        CONTENT_TYPES.put("xml", "text/xml");
        CONTENT_TYPES.put("zip", "application/zip");
        CONTENT_TYPES.put("exe", "application/octet-stream");
        CONTENT_TYPES.put("json", "text/plain");
    }

    private Socket clientSocket = null;
    private String documentRoot = null;

    public WorkerRunnable(Socket clientSocket, String serverText, String documentRoot) {
        this.clientSocket = clientSocket;
        this.documentRoot = documentRoot;
    }

    public void run() {
        // System.out.println("START worker Thread: " + Thread.currentThread().getId());
        try {
            outputRequest(this.clientSocket);
            outputResponse(this.clientSocket, this.documentRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println("END worker Thread: " + Thread.currentThread().getId());
    }

    private static void outputRequest(Socket client) throws IOException {
        //System.out.println("//------------------------------------------");

        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

        try {
            // 1行目からリクエストパスを取得しておく（/でアクセスされた場合はindex.htmlを表示）
            String inline = br.readLine();
            requestPath = inline.split(" ")[1];
            while (br.ready() && inline != null) {
                // System.out.println(Thread.currentThread().getId() + ": " + inline);
                inline = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void outputResponse(Socket client, String documentRoot) throws IOException {
        PrintStream ps = new PrintStream(client.getOutputStream());
        try {
            String[] strArray = requestPath.split("/");
            String db_name = strArray[1] + ".mbtiles";
            String z = strArray[2];
            String x = strArray[3];
            String y = strArray[4].substring(0, strArray[4].length() - 4);

            int intY = Integer.valueOf(y);
            int intZ = Integer.valueOf(z);

            intY = (1 << intZ) - 1 - intY;  // y軸が南北で反転されているっぽいのでy軸を反転する。
            y = Integer.toString(intY);     // y軸が南北で反転されているっぽいのでy軸を反転する。

            // System.out.println(db_name + "/" + z + "/" + x + "/" + y);

            final File sdCardDirectory = new File(documentRoot);
            final File sqliteFile = new File(sdCardDirectory, db_name);
            MapDBQuery sqlClient = new MapDBQuery(sqliteFile);
            try {
                sqlClient.open();
                ps.println("HTTP/1.1 200 OK");
                ps.println("Content-Type: application/x-protobuf");
                ps.println("Content-Encoding: gzip");
                ps.println("Access-Control-Allow-Origin: *");
                ps.println("Access-Control-Allow-Headers: Origin, " +
                        "X-Requested-With, Content-Type, Accept");
                ps.println("Cache-Control: public, max-age=604800");
                sqlClient.printByteToPrintStream(ps, x, y, z);
            } finally {
                sqlClient.close();
            }

        } catch (Exception e) {
            ps.println("HTTP/1.1 404 Not Found");
            ps.println("Content-Type: text/plain");
            ps.println("Access-Control-Allow-Origin: *");
            ps.println("Access-Control-Allow-Headers: Origin, " +
                    "X-Requested-With, Content-Type, Accept");
            ps.println("");
            ps.println("ERROR: File Not Found");
            e.printStackTrace();
        } finally {
            ps.close();
        }
    }
}