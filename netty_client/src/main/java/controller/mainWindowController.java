package controller;

import enums.Commands;
import exceptions.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.AbstractMessage;
import model.CommandMessageWithInfo;
import model.FileMessage;
import net.NettyNet;
import net.OnMessageReceived;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;

import static enums.Commands.*;

public class mainWindowController implements Initializable, OnMessageReceived {
    public VBox mainPanel;
    public VBox loginPanel;
    public TextField loginField;
    public PasswordField passwordField;
    public Label pathLabel;
    public ObservableList<String> observableFiles = FXCollections.observableArrayList("dummy file"); //иначе не инициализировалось
    public ListView<String> fileList;
    ;
    public VBox giveNameToFolderWindow;
    public TextField nameInputField;
    public Button goBackButton;
    public VBox registerPanel;
    public TextField registerLoginField;
    public PasswordField registerPasswordField;
    public PasswordField confirmPasswordField;
    public TextField tokenField;
    public VBox changeNickPanel;
    public TextField newNickField;
    private String userID;

    private String rootDirectoryForUser; //у каждого юзера будет root = serverFolder/id/nick
    private String currentPath;
    private NettyNet net;

    private static final Logger LOGGER = LogManager.getLogger(mainWindowController.class);

    private final int MINLENGTH = 2;
    private final int MAXLENGTH = 12;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        net = new NettyNet(this);
        initClickListener();
    }

    public void mockAction(ActionEvent actionEvent) {

    }

    public void hideNewFolderPanel(ActionEvent actionEvent) {
        giveNameToFolderWindow.setVisible(false);
        mainPanel.setVisible(true);
    }

    public void login(ActionEvent actionEvent) {
        net.sendMessage(new CommandMessageWithInfo(AUTH, new String[]{loginField.getText(), passwordField.getText()}));
    }


    public void closeApplication(ActionEvent actionEvent) {
        //todo logout?
        net.sendMessage(new CommandMessageWithInfo(DISCONNECT));
        Platform.exit();
        System.exit(0);
    }


    public void sendChangePass(ActionEvent actionEvent) {

    }


    private void requestFiles(String id) {
        CommandMessageWithInfo initPathAndFiles = new CommandMessageWithInfo(INIT, new String[]{userID});
        net.sendMessage(initPathAndFiles);
    }

    private void showUi() {
        loginPanel.setVisible(false);
        registerPanel.setVisible(false);
        mainPanel.setVisible(true);
    }


    private void initializeListView(String[] filesArray) throws IOException {
        Platform.runLater(() -> {
            observableFiles.clear();
            fileList.getItems().clear();
            observableFiles = FXCollections.observableArrayList(filesArray);
            fileList.getItems().addAll(observableFiles);
        });
    }

    private void setPathLabel(String text) {
        String toTrim = "netty_server/serverFolder/" + userID + "/";
        int trim = toTrim.length();
        String trimmedPath = text.substring(trim);
        Platform.runLater(() -> pathLabel.setText(trimmedPath));
    }

    public void onReceive(AbstractMessage msg) throws IOException {
        LOGGER.info("client received " + msg.toString());
        if (msg instanceof CommandMessageWithInfo responseFromServer) {
            Commands command = responseFromServer.getCommand();
            String[] content = responseFromServer.getContent();
            switch (command) {
                case AUTH_OK -> {
                    userID = content[0];
                    requestFiles(userID);
                    showUi();
                }
                case ERROR -> showError(content[0]);
                case PATHANDFILES -> {
                    initializePath(responseFromServer.getPath());
                    initializeListView(responseFromServer.getFilesArray());
                    goBackButton.setDisable(currentPath.equals(rootDirectoryForUser));
                }
            }
        }
        if (msg instanceof FileMessage fileMessage) {
            openSaveFileWindow(fileMessage);
        }
    }

    private void openSaveFileWindow(FileMessage fileToSave) {
        Platform.runLater(
                () -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Select destination");
                    fileChooser.setInitialFileName(fileToSave.getFilename());
                    Stage stage = new Stage();

                    File file = fileChooser.showSaveDialog(stage);
                    if (file != null) {
                        try {
                            saveFile(file, fileToSave.getBytes());
                        } catch (IOException | FileExistsException e) {
                            showError(e.getMessage());
                        }
                    }
                }
        );
    }

    private void saveFile(File file, byte[] data) throws IOException, FileExistsException {
        Path destination = Path.of(file.getPath());
        if (!Files.exists(destination)) {
            Path f = Files.createFile(destination);
            Files.write(f, data);
        } else {
            throw new FileExistsException();
        }
    }

    private void initializePath(String path) {
        setPathLabel(path);
        currentPath = path;
        if (rootDirectoryForUser == null) {
            rootDirectoryForUser = currentPath;
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    message,
                    ButtonType.CLOSE
            );
            alert.showAndWait();
        });
        LOGGER.info("Error shown to user: " + message);
    }

    public void newFolderButtonPushed(ActionEvent actionEvent) {
        giveNameToFolderWindow.setVisible(true);
        mainPanel.setVisible(false);
    }

    private String validateName() throws IncorrectFolderNameException {
        String folderName = nameInputField.getText();
        if (folderName == null) {
            throw new IncorrectFolderNameException();
        }
        checkFolderNameIsValid(folderName);
        return folderName;
    }

    public void createFolder(ActionEvent actionEvent) {
        String folderName;
        try {
            folderName = validateName();
            CommandMessageWithInfo msg = new CommandMessageWithInfo(MKDIR);
            msg.setPath(currentPath + "/" + folderName);
            net.sendMessage(msg);
            hideNewFolderPanel(actionEvent);
        } catch (IncorrectFolderNameException e) {
            showError(e.getMessage());
        }
    }

    private void checkFolderNameIsValid(String name) throws IncorrectFolderNameException {
        if (name.equals("") || name.startsWith(".") || name.startsWith("/") || name.endsWith(".") || name.endsWith("/")) {
            throw new IncorrectFolderNameException();
        }
    }

    private void initClickListener() {
        fileList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String itemClickedOn = fileList.getSelectionModel().getSelectedItem();
                changeDirectory(itemClickedOn);
            }
        });
    }

    public void changeDirectory(String destination) {
        CommandMessageWithInfo msg = new CommandMessageWithInfo(CD);
        msg.setPath(currentPath + "/" + destination);
        net.sendMessage(msg);
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String name = fileList.getSelectionModel().getSelectedItem();
        if (name != null) {
            net.sendMessage(new CommandMessageWithInfo(DOWNLOAD, currentPath + "/" + name));
        }
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        Stage stage = new Stage();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null && file.exists()) {
            FileMessage fileToSend = new FileMessage(currentPath, file.getName());
            fileToSend.setBytes(Files.readAllBytes(file.toPath()));
            net.sendMessage(fileToSend);
        }
    }

    public void goBack(ActionEvent actionEvent) {
        if (!currentPath.equals(rootDirectoryForUser)) {
            changeDirectory("..");
        }
    }

    public void register(ActionEvent actionEvent) {
        String login = registerLoginField.getText();
        String pass = registerPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();
        String token = tokenField.getText();
        try {
            validateFields(login, pass);
            checkPassMatches(pass, confirmPass);
            CommandMessageWithInfo registerMessage = new CommandMessageWithInfo(REGISTER);
            registerMessage.setContent(new String[]{token, login, pass});
            net.sendMessage(registerMessage);

        } catch (PasswordMismatchException | IncorrectCharactersException | NullPointerException |
                 LoginSizeNotInLimitsException | PasswordSizeNotInLimitsException e) {
            showError(e.getMessage());
        }

    }

    private void validateFields(String login, String pass) throws IncorrectCharactersException, LoginSizeNotInLimitsException, PasswordSizeNotInLimitsException {
        hasForbiddenCharacters(login);
        validateLoginLength(login);
        validatePassLength(pass);
    }

    private void validatePassLength(String pass) throws PasswordSizeNotInLimitsException {
        if (sizeNotWithinLimits(pass)) {
            throw new PasswordSizeNotInLimitsException();
        }
    }

    private void validateLoginLength(String login) throws LoginSizeNotInLimitsException {
        if (sizeNotWithinLimits(login)) {
            throw new LoginSizeNotInLimitsException();
        }
    }

    private void validateNickNameSize(String nick) throws IncorrectNicknameSizeException {
        if (sizeNotWithinLimits(nick)) {
            throw new IncorrectNicknameSizeException();
        }
    }

    private boolean sizeNotWithinLimits(String input) {
        return input.length() < MINLENGTH || input.length() > MAXLENGTH;
    }

    private void hasForbiddenCharacters(String input) throws IncorrectCharactersException {
        for (int i = 0; i < input.length(); i++) {
            if (!Character.isDigit(input.charAt(i))
                    && !Character.isLetter(input.charAt(i))) {
                throw new IncorrectCharactersException();
            }
        }
    }

    private void checkPassMatches(String pass, String confirmPass) throws PasswordMismatchException {
        if (!pass.equals(confirmPass)) {
            throw new PasswordMismatchException();
        }
    }

    public void viewRegisterWindow(ActionEvent actionEvent) {
        loginPanel.setVisible(false);
        registerPanel.setVisible(true);
    }

    public void viewLoginWindow(ActionEvent actionEvent) {
        registerPanel.setVisible(false);
        loginPanel.setVisible(true);
    }

    public void delete(ActionEvent actionEvent) {
        String name = fileList.getSelectionModel().getSelectedItem();
        if (name != null) {
            Platform.runLater(() -> {
                Alert alert = new Alert(
                        Alert.AlertType.WARNING,
                        "Delete " + name + " from server?",
                        ButtonType.OK,
                        ButtonType.CLOSE
                );
                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                    String itemToDelete = currentPath + "/" + name;
                    net.sendMessage(new CommandMessageWithInfo(DEL, itemToDelete));
                }
            });
        }
    }


    public void changePass(ActionEvent actionEvent) {

    }

    public void showChangeNickPanel(ActionEvent actionEvent) {
        changeNickPanel.setVisible(true);
        mainPanel.setVisible(false);
    }

    private void sendValidNick(String newNick) {
        net.sendMessage(new CommandMessageWithInfo(CHANGENICK, new String[]{userID, newNick}));
    }

    public void renameUser(ActionEvent actionEvent) {
        try {
            String validNick = validateNewNick();
            rootDirectoryForUser = null;
            sendValidNick(validNick);
            hideChangeNickPanel(actionEvent);
        } catch (IncorrectCharactersException | IncorrectNicknameSizeException e) {
            showError(e.getMessage());
        }
    }

    private String validateNewNick() throws IncorrectCharactersException, IncorrectNicknameSizeException {
        String newNick = newNickField.getText();
        hasForbiddenCharacters(newNick);
        validateNickNameSize(newNick);
        return newNick;
    }

    public void hideChangeNickPanel(ActionEvent actionEvent) {
        changeNickPanel.setVisible(false);
        mainPanel.setVisible(true);
    }
}

