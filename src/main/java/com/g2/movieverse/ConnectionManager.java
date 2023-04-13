package com.g2.movieverse;

import com.jcraft.jsch.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ConnectionManager {
    private static Session session = null;
    private static Connection conn = null;

    public static void openConnection()
    {
        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;
        String[] accountInfo = PasswordLoader.getCSAccount();
        String user = accountInfo[0];
        String password = accountInfo[1];
        String databaseName = "p320_02";

        String driverName = "org.postgresql.Driver";
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(user, rhost, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.setConfig("PreferredAuthentications","publickey,keyboard-interactive,password");
            session.connect();
            System.out.println("Connected");
            int assigned_port = session.setPortForwardingL(lport, "localhost", rport);
            System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://localhost:"+ assigned_port + "/" + databaseName;

            System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection established");

            // Do something with the database....

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection()
    {
        try {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Closing Database Connection");
                conn.close();
            }
            if (session != null && session.isConnected()) {
                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            System.out.println("Connection closed successfully");
        }
    }

    public static ResultSet sendQuery(String query)
    {
        ResultSet rs = null;

        try {
            Statement statement = conn.createStatement();
            rs = statement.executeQuery(query);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println("Executing query: [" + query + "] failed.");
        }

        return rs;
    }

    public static int sendUpdate(String query)
    {
        int ret = 0;

        try {
            Statement statement = conn.createStatement();
             ret = statement.executeUpdate(query);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println("Executing update: [" + query + "] failed.");
        }

        return ret;
    }
}
