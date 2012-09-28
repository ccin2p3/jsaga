package org.glite.ce.creamapi.jobmanagement.db;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CLI {
    public static void main(String[] argv) {
        System.out.println("Checking if Driver is registered with DriverManager.");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Couldn't find the driver!");
            System.out.println("Let's print a stack trace, and exit.");
            cnfe.printStackTrace();
            System.exit(1);
        }

        System.out.println("Registered the driver ok, so let's make a connection.");

        Connection c = null;

        try {
            // The second and third arguments are the username and password,
            // respectively. They should be whatever is necessary to connect
            // to the database.
            c = DriverManager.getConnection("jdbc:postgresql://localhost/creamdb", "cream", "cmsgrid");
            Statement stmt = c.createStatement();

            stmt.executeUpdate("DROP TABLE JOB");
            
            //tbd
            stmt.executeUpdate("CREATE TABLE JOB (" +
                    "id           VARCHAR (15) UNIQUE, " +
                    "iceId        VARCHAR (40), " +
                    "gridJobId    VARCHAR (40), " +
                    "lrmsId       VARCHAR (10)," +
                    "lrmsAbsId    VARCHAR, " +
                    "delegationId VARCHAR, " +
                    "userId       VARCHAR, " +
                    "type         VARCHAR (10) NOT NULL," +
                    "PRIMARY KEY( id )"                  +
                                                       ")" );
            
            stmt.executeUpdate("INSERT INTO JOB VALUES ('CREAM1234568', 'ice1', '', '1234', 'blah1234', 'deleg1', 'zangrand', 'normal')");
         
            ResultSet result = stmt.executeQuery(
                    "SELECT id, userId, type " +
                    "FROM JOB " +
                    "ORDER BY userId DESC");
            result.next();
            if(result != null) {
                System.out.println(result.getString("id"));
                System.out.println(result.getString("userId"));
                System.out.println(result.getString("type"));
            }
        } catch (SQLException se) {
            System.out.println("Couldn't connect: print out a stack trace and exit.");
            se.printStackTrace();
            System.exit(1);
        }

        if (c != null) {
            System.out.println("We connected to the database!");
            try {
                c.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else
            System.out.println("We should never get here.");
    }
}
