package server;

import database.DataBaseService;
import enums.Commands;
import exceptions.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import static enums.Commands.*;

public class MessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {


    DataBaseService service = NettyServer.getDataBaseService();
    private static final Logger LOGGER = LogManager.getLogger(MessageHandler.class);
    private CommandMessageWithInfo response;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage abstractMessage) throws Exception {

        LOGGER.info("server received " + abstractMessage);
        response = new CommandMessageWithInfo(PATHANDFILES);


        if (abstractMessage instanceof CommandMessageWithInfo incomingMSG) {
            Commands command = incomingMSG.getCommand();
            String[] content = incomingMSG.getContent();
            String path = incomingMSG.getPath();

            switch (command) {
                case AUTH -> {
                    try {
                        response.setContent(new String[]{String.valueOf(service.authenticate(content[0], content[1]))});
                        response.setCommand(AUTH_OK);
                    } catch (WrongLoginOrPassException e) {
                        errorMessage(e.getMessage());
                    }
                }
                case INIT -> {
                    setPathAndFilesResponse(FileHandler.getPathByID(content[0]));
                }
                case CHANGENICK -> {
                    String newNick = content[1];
                    String userId = content[0];
                    String oldNick = service.getNick(userId);
                    try {
                        service.changeNick(userId, newNick);
                        FileHandler.renameFolder("netty_server/serverFolder/" + userId + "/" + oldNick,
                                "netty_server/serverFolder/" + userId + "/" + newNick);
                    } catch (SQLException e) {
                        errorMessage("Database error");
                        e.printStackTrace();
                    }
                    setPathAndFilesResponse("netty_server/serverFolder/" + userId + "/" + newNick);
                }
                case CHANGEPASS -> {
                    try {
                        service.changePass(content[0], content[1], content[2]);
                        LOGGER.info("Password for user " + content[0] + " was changed");
                        setPathAndFilesResponse(FileHandler.getPathByID(content[0]));
                    } catch (WrongPasswordException e) {
                        errorMessage(e.getMessage());
                    }
                }
                case REGISTER -> {
                    try {
                        service.registerUser(content[0], content[1], content[2]);
                        int id = service.authenticate(content[1], content[2]);
                        FileHandler.makeFolder("netty_server/serverFolder/" + id);
                        String pathForNewUser = "netty_server/serverFolder/" + id + "/" + content[1];
                        FileHandler.makeFolder(pathForNewUser);
                        FileHandler.createReadme(pathForNewUser);

                        response.setContent(new String[]{String.valueOf(id)});
                        response.setCommand(AUTH_OK);

                    } catch (TokenIsAlreadyTakenException | InvalidTokenException | LoginAlreadyExistsInDbException e) {
                        errorMessage(e.getMessage());
                    }
                }
                case MKDIR -> {
                    try {
                        FileHandler.makeFolder(path);
                        setPathAndFilesResponse(FileHandler.levelUp(path));
                    } catch (FolderExistsException e) {
                        errorMessage(e.getMessage());
                    }
                }
                case CD -> {
                    if (FileHandler.checkIsDirectory(path)) { // пока двойной клик по файлу ничего не предусматривает
                        if (!path.endsWith("/..")) {
                            setPathAndFilesResponse(path);
                        } else {
                            setPathAndFilesResponse(FileHandler.levelUp(path.substring(0, path.length() - 3)));
                        }
                    } else {
                        LOGGER.info("user double clicked on a " + path);
                        return;
                    }
                }
                case DOWNLOAD -> {
                    if (FileHandler.checkIsDirectory(path)) {
                        errorMessage(new ChooseAfileNotFolderException().getMessage());
                    } else {
                        FileMessage fileToSend = new FileMessage(path);
                        fileToSend.setBytes(Files.readAllBytes(Path.of(path)));
                        fileToSend.setFilename(new File(Path.of(path).toUri()).getName());
                        ctx.writeAndFlush(fileToSend);
                        return;
                    }
                }
                case DEL -> {
                    FileHandler.delete(path);
                    setPathAndFilesResponse(FileHandler.levelUp(path));
                }
                case DISCONNECT -> {
                    ctx.disconnect();
                    LOGGER.info("CLIENT DISCONNECTED");
                }
            }
        }
        if (abstractMessage instanceof FileMessage fileMessage) {
            LOGGER.info("server received" + fileMessage);
            try {
                FileHandler.saveFileToServer(fileMessage.getCurrentPath(), fileMessage.getBytes(), fileMessage.getFilename());
                setPathAndFilesResponse(fileMessage.getCurrentPath());
            } catch (FileAlreadyExistsException e) {
                errorMessage(e.getMessage());
            }
        }
        LOGGER.info("RESPONSE IS " + response);
        ctx.writeAndFlush(response);
    }

    private void errorMessage(String info) {
        response.setCommand(ERROR);
        response.setContent(new String[]{info});
    }

    private void setPathAndFilesResponse(String path) {
        response.setCommand(PATHANDFILES);
        response.setPath(path);
        response.setFilesArray(FileHandler.getFilesByPath(path));
    }

}
