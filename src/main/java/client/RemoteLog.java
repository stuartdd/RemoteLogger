package client;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;


public class RemoteLog {
    private static final String NL = System.getProperty("line.separator");
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String HOST = "http://localhost:1999/log";
    private static final ConcurrentLinkedQueue<String> QUEUE  =new ConcurrentLinkedQueue<>();
    private static final long LATENCY_SLEEP  = 1000;
    private static final long LATENCY_PAUSE  = 10;
    private static LogThread THREAD;
    private static boolean broken = false;

    static {
        THREAD = new LogThread();
        THREAD.start();
    }

    public static void log(String s) {
        if (broken) {
            QUEUE.clear();
            return;
        }
        QUEUE.add(DateTime.now().toString("yyyy-MM-dd HH-mm-ss.SSS': '")+s);
    }

    public static class LogThread extends Thread {
        private boolean canRun;

        public LogThread() {
            this.canRun = true;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            while (canRun && !broken) {
                if (QUEUE.isEmpty()) {
                    if (sb.length() > 0) {
                        logImediate("POST", sb.toString());
                        sb.setLength(0);
                    }
                    try {
                        sleep(LATENCY_SLEEP);
                    } catch (InterruptedException e) {
                    }
                } else {
                    sb.append(QUEUE.poll()).append(NL);
                 }
            }
        }
        public void close() {
            this.canRun = false;
        }
    }

    private static String logImediate(String method, String s) {
        URL obj;
        DataOutputStream wr = null;
        BufferedReader in = null;
        HttpURLConnection con;
        try {
            obj = new URL(HOST);
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setDoOutput(true);

            wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(s);
            wr.flush();
            wr.close();
            wr = null;

            int responseCode = con.getResponseCode();
            if (responseCode >= 300) {
                return "Failed:" + responseCode;
            }
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            in = null;
            return response.toString();
        } catch (Throwable e) {
            silent(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (wr != null) {
                    wr.close();
                }
            } catch (Throwable e) {
                silent(e);
            }
        }
        return "Failed:Off the end!";
    }

    private static void silent(Throwable t) {
        broken = true;
        System.out.println(t.getMessage());
    }

}
