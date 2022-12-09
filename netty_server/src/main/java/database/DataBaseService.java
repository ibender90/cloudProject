package database;

import exceptions.InvalidTokenException;
import exceptions.LoginAlreadyExistsInDbException;
import exceptions.TokenIsAlreadyTakenException;
import exceptions.WrongLoginOrPassException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.FileHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseService {
    public static Connection connection;
    public static Statement statement;

    private static final Logger LOGGER = LogManager.getLogger(DataBaseService.class);


    public void registerUser(String token, String login, String pass) throws SQLException, LoginAlreadyExistsInDbException, InvalidTokenException, TokenIsAlreadyTakenException {
        checkLoginExists(login);
        checkTokenIsCorrect(token);
        checkTokenNotUsed(token);
        addUser(login, pass, login);
        assignToken(token, login);
        LOGGER.info("USER REGISTERED SUCCESSFULLY " + login);
    }

    private void assignToken(String token, String login) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE tokens SET user_id = ?" +
                "WHERE token = ?");
        preparedStatement.setString(1, getID(login));
        preparedStatement.setString(2, token);
        preparedStatement.execute();
        LOGGER.info("TOKEN " + token + "ASSIGNED TO USER " + login);
    }

    private void freeToken(String token) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE tokens SET user_id = ''" +
                "WHERE token = ?");
        preparedStatement.setString(1, token);
        preparedStatement.execute();
    }

    private void checkTokenNotUsed(String token) throws TokenIsAlreadyTakenException {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT user_id FROM tokens WHERE token = ?");
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.getInt(1) > 0) {
                throw new TokenIsAlreadyTakenException();
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void checkTokenIsCorrect(String token) throws InvalidTokenException {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT id FROM tokens WHERE token = ?");
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.isClosed() || rs.getInt(1) == 0) {
                throw new InvalidTokenException();
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }


    private static void createUsersTableIfAbsent() throws SQLException {
        ResultSet exists = statement.executeQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='users';");
        if (exists.isClosed() || exists.getInt(1) == 0) {
            statement.execute("CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE," +
                    "login TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "nick TEXT NOT NULL)");

            LOGGER.info("USERS TABLE CREATED");
        }
    }

    private static void createTokensTableIfAbsent() throws SQLException {
        ResultSet exists = statement.executeQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='tokens';");
        if (exists.isClosed() || exists.getInt(1) == 0) {
            statement.execute("CREATE TABLE tokens (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE," +
                    "token TEXT UNIQUE NOT NULL," +
                    "user_id INTEGER UNIQUE)");

            LOGGER.info("TOKENS TABLE CREATED");
            addTokensFromList();
        }
    }


    private static void addTokensFromList() { //temporary hardcode
        List<String> tokens = new ArrayList<>(List.of(new String[]{
                "1234&^$%",
                "123$#%&&",
                "1234#@!#",
                "1234!@$#",
                "1234^%^^",
                "1234$$$$",
                "1234^T$#",
                "1234%%$$"
        }));
        for (String token : tokens) {
            PreparedStatement ps = null;
            try {
                ps = connection.prepareStatement(
                        "INSERT INTO tokens (token) VALUES (?)"
                );
                ps.setString(1, token);
                ps.execute();
            } catch (SQLException e) {
                LOGGER.error("SQL exception, tokens were not added");
                e.printStackTrace();
            }
        }
    }

    public String getNick(String id) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT nick FROM users WHERE id =?");
            ps.setInt(1, Integer.parseInt(id));
            ResultSet resultSet = ps.executeQuery();
            return resultSet.getString("nick");
            //выбросить ли ошибку если такого ид нет в базе?
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    public int authenticate(String login, String password) throws SQLException, WrongLoginOrPassException {

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, login, password FROM users WHERE login = ? AND password = ?");
        preparedStatement.setString(1, login);
        preparedStatement.setString(2, password);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.isClosed()) {
            throw new WrongLoginOrPassException();
        }
        return resultSet.getInt("id");
    }

    public String addUser(String login, String pass, String nick) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (login, password, nick) VALUES (?, ?, ?)"
        );
        ps.setString(1, login);
        ps.setString(2, pass);
        ps.setString(3, nick);
        ps.execute();
        LOGGER.info("USER " + login + " WAS ADDED TO DATABASE");
        return getID(login);
    }

    private String getID(String login) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT id FROM users WHERE login = ?"
        );
        ps.setString(1, login);
        ResultSet rs = ps.executeQuery();
        return rs.getString(1);
    }

    private void checkLoginExists(String login) throws SQLException, LoginAlreadyExistsInDbException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT id FROM users WHERE login = ?"
        );
        ps.setString(1, login);
        ResultSet resultSet = ps.executeQuery();
        if (resultSet.next()) { //result set not empty
            throw new LoginAlreadyExistsInDbException();
        }
    }


    private static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:users.db");
        statement = connection.createStatement();
        LOGGER.info("CONNECTION WITH DATABASE ESTABLISHED");
    }

    private static void disconnect() throws SQLException {
        connection.close();
        LOGGER.info("DISCONNECTED FROM DATABASE");
    }

    public void start() {
        try {
            connect();
            createUsersTableIfAbsent();
            createTokensTableIfAbsent();
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void stop() {
        try {
            disconnect();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void changeNick(String userId, String newNick) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET nick = ?" +
                "WHERE id = ?");
        preparedStatement.setString(1, newNick);
        preparedStatement.setString(2, userId);
        preparedStatement.execute();
    }
}
//    public static void main(String[] args) throws SQLException, ClassNotFoundException, WrongLoginOrPassException {
//
//        DataBaseService service = new DataBaseService();
//
//        try {
//            service.start();
//            service.addTokensFromFile("netty_server/src/main/resources/Tokens.txt");
//
//            //addTestUser();
//            //addTestToken();
////            ResultSet rs = statement.executeQuery("SELECT * FROM users where id = 1");
////            String login = rs.getString("login");
////            String pass = rs.getString("password");
////            String nick = rs.getString("nick");
////            System.out.println(login + " " + pass + " " + nick);
////
////            ResultSet rs1 = statement.executeQuery("SELECT * FROM tokens WHERE id = 1");
////            String token = rs1.getString("token");
////            System.out.println(token);
////            System.out.println(testToken.equals(token));
//
////            String nick1 = service.getNick("1");
////            System.out.println(nick1.equals("nick1"));
////
////            int id = service.authenticate("log1", "pass1");
////            System.out.println(id == 1);
////
////            String newID = service.addUser("login4", "pass4", "nick4");
////            PreparedStatement ps1 = connection.prepareStatement("SELECT * FROM users where id = ?");
////            ps1.setString(1, newID);
//
////            ResultSet rs3 = ps1.executeQuery();
////            String newLogin = rs3.getString("login");
////            String newPass = rs3.getString("password");
////            String newNick = rs3.getString("nick");
////
////            System.out.println(newLogin + " " + newPass + " " + newNick + " " + newID);
////
////            PreparedStatement ps2 = connection.prepareStatement("DELETE FROM users where id = ?");
////            ps2.setString(1, newID);
////            ps2.execute();
//
//            //service.assignToken(testToken, "log1");
//
////            service.freeToken(testToken);
//
////            PreparedStatement ps3 = connection.prepareStatement("SELECT * FROM tokens where token = ?");
////            ps3.setString(1, testToken);
////            ResultSet rs4 = ps3.executeQuery();
////            String assignedID = rs4.getString("user_id");
////            System.out.println(assignedID);
//
//            //service.registerUser("%^&)(*!@!#", "logggin", "passwd");
//
//
////        } catch (InvalidTokenException e) {
////            throw new RuntimeException(e);
////        } catch (LoginAlreadyExistsInDbException e) {
////            throw new RuntimeException(e);
////        } catch (TokenIsAlreadyTakenException e) {
////            throw new RuntimeException(e);
//        } finally {
//            service.stop();
//        }
//
//
//    }
//}
