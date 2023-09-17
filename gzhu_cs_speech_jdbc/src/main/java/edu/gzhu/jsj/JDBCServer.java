package edu.gzhu.jsj;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class JDBCServer {

    private Connection conn;
    private HttpServer s;

    private void connectDB() throws Exception {

        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection(__GLOBAL__.DB_URL, __GLOBAL__.USER, __GLOBAL__.PASSWORD);
    }

    private String queryDB(String sql) throws Exception {

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        ResultSetMetaData md = rs.getMetaData();
        JSONArray ja = new JSONArray();
        int count = md.getColumnCount();
        while (rs.next()) {
            JSONObject jo = new JSONObject();
            for (int i = 1; i <= count; ++i) {
                jo.put(md.getColumnName(i), rs.getString(md.getColumnName(i)));
            }
            ja.put(jo.toMap());
        }
        return ja.toString();
    }

    private class jsonHandle implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            try {
                byte[] content = queryDB("SELECT * FROM gzhu_cs_speech_db.speechlists").getBytes("utf16");

                exchange.sendResponseHeaders(200, content.length);

                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            } catch (Exception e) {
                System.err.println(e);
                System.exit(1);
            }
        }
    }

    JDBCServer() throws Exception {

        connectDB();

        s = HttpServer.create(new InetSocketAddress(__GLOBAL__.PORT), 0);
        s.createContext("/speechlists", new jsonHandle());
    }

    public void start() throws Exception {

        s.start();
    }

    public void close() throws Exception {

        s.stop(0);
        conn.close();
    }
}