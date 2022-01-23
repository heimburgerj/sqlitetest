package de.heimburgerj.sqlitetest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

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
                Person person = new Person(rs.getInt("id"), rs.getString("name"));
                System.out.println("name = " + person.name());
                System.out.println("id = " + person.id());
                File file = new File("src/test/resources/temp/out.yaml");
                Files.createDirectories(file.getParentFile().toPath());
                if (!file.exists())
                    file.createNewFile();
                ObjectMapper om = new ObjectMapper(new YAMLFactory());
                om.writeValue(file, person);
                System.out.println("Exported person " + person + " to file " + file + ".");
            }
        } catch (SQLException | IOException e) {
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
            System.out.println("Loaded driver successfully : " + driver.getClass() + " " + driver.getMajorVersion()
                    + "." + driver.getMinorVersion());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            System.err.println(
                    "Failed to load driver " + driverClassName + " : " + e.getClass() + " (" + e.getMessage() + ")");
        }
    }
}