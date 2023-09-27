package edu.gzhu.jsj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import javax.net.ssl.HttpsURLConnection;

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
                if (md.getColumnTypeName(i).equals("INT")) {
                    jo.put(md.getColumnName(i), rs.getInt(md.getColumnName(i)));
                } else {
                    jo.put(md.getColumnName(i), rs.getString(md.getColumnName(i)));
                }
            }
            ja.put(jo.toMap());
        }

        rs.close();
        stmt.close();

        return ja.toString();
    }

    private void updateDB(String sql) throws Exception {

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);

        stmt.close();
    }

    private String codeToSession(String js_code) throws Exception {

        byte[] buf = new byte[256];

        // 微信API
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=wxc83c7be6c7ffc4f4&secret=75a4d76f4c85eae257735e341ea2f30c&js_code="
                + js_code + "&grant_type=authorization_code";

        URL wx_login = new URI(url).toURL();
        HttpsURLConnection httpsConn = (HttpsURLConnection) wx_login.openConnection();

        httpsConn.setRequestMethod("GET");

        // 成功获取openid
        String openid = "";
        if (httpsConn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
            InputStream wx_is = httpsConn.getInputStream();

            int bufLen = wx_is.read(buf);
            wx_is.close();

            openid = new JSONObject(new String(buf, 0, bufLen)).getString("openid");

            // if (queryDB("SELECT openid FROM gzhu_cs_speech_db.students WHERE openid = '"
            // + openid + "'").equals("[]")) {
            // return "none";
            // } else {
            // return openid;
            // }
        }

        return openid;
    }

    private class getSpeechListsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            try {
                byte[] content = queryDB("SELECT * FROM gzhu_cs_speech_db.speechlists;").getBytes("utf8");

                exchange.sendResponseHeaders(200, content.length);

                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    private class loginHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            try {
                byte[] buf = new byte[256];

                // 接收用户的code，并返回HTTP200
                InputStream is = exchange.getRequestBody();
                OutputStream os = exchange.getResponseBody();
                int bufLen = is.read(buf);
                is.close();
                exchange.sendResponseHeaders(200, 0);

                String js_code = new String(buf, 0, bufLen);

                // 获取用户的openid
                String openid = codeToSession(js_code);

                // openid获取成功
                if (!openid.isEmpty()) {
                    if (queryDB(
                            String.format("SELECT openid FROM gzhu_cs_speech_db.students WHERE openid = '%s';", openid))
                            .equals("[]")) {
                        os.write("{\"reg\":false}".getBytes());
                    } else {
                        os.write(new String("{\"reg\":true,\"openid\":\"" + openid + "\"}").getBytes());
                    }
                }

                os.close();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    private class registerHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            try {
                byte[] buf = new byte[256];

                // 接收用户的code，并返回HTTP200
                InputStream is = exchange.getRequestBody();
                OutputStream os = exchange.getResponseBody();
                int bufLen = is.read(buf);
                is.close();
                exchange.sendResponseHeaders(200, 0);

                JSONObject jo = new JSONObject(new String(buf, 0, bufLen, "utf8"));

                // 获取用户的openid
                String openid = codeToSession(jo.getString("js_code"));

                // 更新数据库
                if (queryDB(String.format("SELECT openid FROM gzhu_cs_speech_db.students WHERE openid = '%s';", openid))
                        .equals("[]")) {
                    updateDB(String.format("CALL studentRegistration('%s', '%s', '%s');",
                            jo.getString("studentName"),
                            jo.getString("studentId"),
                            openid));
                } else {
                    updateDB(String.format("CALL studentUpdate('%s', '%s');",
                            jo.getString("studentName"),
                            jo.getString("studentId")));
                }

                os.write(new String("{\"reg\":true,\"openid\":\"" + openid + "\"}").getBytes());
                os.close();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    private class submitHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            try {
                byte[] buf = new byte[256];

                InputStream is = exchange.getRequestBody();
                OutputStream os = exchange.getResponseBody();
                int bufLen = is.read(buf);
                is.close();
                exchange.sendResponseHeaders(200, 0);

                JSONObject jo = new JSONObject(new String(buf, 0, bufLen));

                // 更新
                if (jo.getInt("flag") == 1) {
                    updateDB(String.format("CALL addSubmit(%d, '%s');",
                            jo.getInt("id"),
                            jo.getString("openid")));
                    os.write(0);
                } else if (jo.getInt("flag") == -1) {
                    updateDB(String.format("CALL cancelSubmit(%d, '%s');",
                            jo.getInt("id"),
                            jo.getString("openid")));
                    os.write(0);
                }

                os.close();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    private class selectedHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            try {
                byte[] buf = new byte[256];

                InputStream is = exchange.getRequestBody();
                OutputStream os = exchange.getResponseBody();
                int bufLen = is.read(buf);
                is.close();
                exchange.sendResponseHeaders(200, 0);

                JSONObject jo = new JSONObject(new String(buf, 0, bufLen));

                byte[] content = queryDB(String.format(
                        "SELECT selected FROM gzhu_cs_speech_db.students WHERE openid = '%s';", jo.getString("openid")))
                        .getBytes("utf8");

                os.write(content);
                os.close();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    JDBCServer() throws Exception {

        connectDB();

        s = HttpServer.create(new InetSocketAddress(__GLOBAL__.PORT), 0);
        s.createContext("/speechlists", new getSpeechListsHandler());
        s.createContext("/login", new loginHandler());
        s.createContext("/register", new registerHandler());
        s.createContext("/submit", new submitHandler());
        s.createContext("/selected", new selectedHandler());
    }

    public void start() throws Exception {

        s.start();
    }

    public void close() throws Exception {

        s.stop(0);
        conn.close();
    }
}