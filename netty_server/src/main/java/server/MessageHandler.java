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
                case REGISTER -> {
                    try {
                        //token , login , pass
                        service.registerUser(content[0], content[1], content[2]);
                        int id = service.authenticate(content[1], content[2]);
                        FileHandler.makeFolder("netty_server/serverFolder/" + id);
                        String pathForNewUser = "netty_server/serverFolder/" + id + "/" + content[1];
                        FileHandler.makeFolder(pathForNewUser);
                        //FileHandler.addReadme(pathForNewUser);
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
                    if (FileHandler.checkIsDirectory(path)) {  // пока двойной клик по файлу ничего не предусматривает
                        if (!path.endsWith("/..")) {
                            setPathAndFilesResponse(path);
                        } else {
                            setPathAndFilesResponse(FileHandler.levelUp(path.substring(0, path.length() - 3)));
                        }
                    } else {
                        return;
                    }
                }
                case DOWNLOAD -> {
                    if (!FileHandler.checkIsDirectory(path)) {
                        FileMessage fileToSend = new FileMessage(path);
                        fileToSend.setBytes(Files.readAllBytes(Path.of(path)));
                        fileToSend.setFilename(new File(Path.of(path).toUri()).getName());

                        ctx.writeAndFlush(fileToSend);
                        return;
                    } else {
                        //todo new error
                        errorMessage("Choose a file, not folder");
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
            System.out.println("server received" + fileMessage);
            try {
                FileHandler.saveFileToServer(fileMessage.getCurrentPath(), fileMessage.getBytes(), fileMessage.getFilename());
                setPathAndFilesResponse(fileMessage.getCurrentPath());
            } catch (FileAlreadyExistsException e) {
                errorMessage(e.getMessage());
            }
        }
        LOGGER.info("RESPONSE IS "+ response);
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
