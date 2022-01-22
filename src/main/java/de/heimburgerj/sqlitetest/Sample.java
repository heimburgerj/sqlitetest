package de.heimburgerj.sqlitetest;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.postgresql.Driver;

public class Sample {
    public static void main(String[] args) {
        Connection connection = null;
        List<String> drivers = Arrays.asList("org.postgresql.Driver", "oracle.jdbc.driver.OracleDriver");
        drivers.forEach(Sample::testDriver);
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists person");
            statement.executeUpdate("create table person (id integer, name string)");
            statement.executeUpdate("insert into person values(1, 'leo')");
            statement.executeUpdate("insert into person values(2, 'yui')");
            ResultSet rs = statement.executeQuery("select * from person");
            while (rs.next()) {
                // read the result set
                System.out.println("name = " + rs.getString("name"));
                System.out.println("id = " + rs.getInt("id"));
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }

    private static void testDriver(String driverClassName) {
        try {

            Class<?> driverType = Class.forName(driverClassName);
            java.sql.Driver driver = (java.sql.Driver) driverType.getConstructor().newInstance();
            System.out.println("Loaded driver successfully : " + driver.getClass() + " "
                    + driver.getMajorVersion() + "." + driver.getMinorVersion());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            System.err.println(
                    "Failed to load driver " + driverClassName + " : " + e.getClass() + " (" + e.getMessage() + ")");
        }
    }
}