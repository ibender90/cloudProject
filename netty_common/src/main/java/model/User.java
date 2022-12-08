package model;

import java.nio.file.Path;
import java.util.Objects;

public class User {
    private String login;
    private String password;
    private String nick;

    private String pathToFiles;

    private Integer id; //to be immutable in db



    public User(String login, String password, String nick, Integer id) {
        this.login = login;
        this.password = password;
        this.nick = nick;
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(login, user.login) && Objects.equals(password, user.password) && Objects.equals(nick, user.nick);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password, nick);
    }

    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", nick='" + nick + '\'' +
                '}';
    }

    public String getPathToFiles() {
        return pathToFiles;
    }

    public void setPathToFiles(String pathToFiles) {
        this.pathToFiles = pathToFiles;
    }

    public Integer getId(){
        return id;
    }

}
