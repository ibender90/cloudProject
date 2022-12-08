package database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.*;


class DataBaseServiceTest {

    private static Connection connection;
    private static Statement statement;


    @BeforeAll
    public static void setUp() throws ClassNotFoundException, SQLException {

    }

    @AfterAll
    public static void finish() {
        //service.stop();
    }


    @Test
    void registerUser() throws SQLException {


    }

    @Test
    void getNick() {
    }

    @Test
    void authenticate() {
    }

    @Test
    void addUser() throws SQLException {

        //addUser("log5", "pass5", "nick5");
        ResultSet correctResultSet = connection.createStatement().executeQuery("SELECT * " +
                "FROM users WHERE login = log5");
        Assertions.assertEquals("log5", correctResultSet.getString("login"));
        Assertions.assertEquals("pass5", correctResultSet.getString("password"));
        Assertions.assertEquals("nick5", correctResultSet.getString("nick5"));
    }

    @Test
    void start() {
    }

    @Test
    void stop() {
    }
}