package server;

import database.DataBaseService;
import exceptions.FolderExistsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class FileHandler {
    private static DataBaseService service = NettyServer.getDataBaseService();
    private static final Logger LOGGER = LogManager.getLogger(FileHandler.class);

    private static final File readmeFile = new File("ReadmeFile.txt");

    private static final String readmeContent = "Welcome to cloud service app";

    public static String getPathByID(String id) {
        LOGGER.info(service.getNick(id));
        return "netty_server/serverFolder/" + id + "/" + service.getNick(id);

    }

    public static String levelUp(String stringPath) {
        return Path.of(stringPath).getParent().toString();
    }


    public static String[] getFilesByPath(String stringPath) {
        return Path.of(stringPath).toFile().list();
    }

    public static void makeFolder(String path) throws FolderExistsException {
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        } else throw new FolderExistsException();
    }

    public static void makeServerFolderIfAbsent() {
        try {
            makeFolder("netty_server/serverFolder");
            LOGGER.info("ROOT DIRECTORY WAS CREATED");
        } catch (FolderExistsException e) {
            LOGGER.info("ROOT DIRECTORY EXISTS - OK");
        }
    }

    public static void saveFileToServer(String destinationPath, byte[] data, String name) throws IOException {
        Path destination = Path.of(destinationPath + "/" + name);
        if (!Files.exists(destination)) {
            Path f = Files.createFile(destination);
            Files.write(f, data);
            LOGGER.info("NEW FILE WRITTEN TO SERVER");
        } else throw new FileAlreadyExistsException("File already exists");
    }

    public static boolean checkIsDirectory(String path) {
        return new File(path).isDirectory();
    }

    public static void addReadme(String path) {
        try {
            Path newReadme = Files.createFile(Path.of(path + "/" + readmeFile.getName()));
            Files.copy(readmeFile.toPath(), newReadme, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createReadme(String path) {
        try {
            Path newReadme = Files.createFile(Path.of(path + "/" + "readme.txt"));
            Files.writeString(newReadme, readmeContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> readTokensFromFile(String path) {

        Path filePath = Paths.get(path);
        Charset charset = StandardCharsets.UTF_8;
        List<String> lines = null;
        try {
            lines = Files.readAllLines(filePath, charset);
        } catch (IOException e) {
            //todo log exception
            e.printStackTrace();
        }
        return lines;
    }

    public static void delete(String path) {

        File toDelete = new File(path);
        File[] files = toDelete.listFiles();
        if (files != null) {
            for (File file : files) {
                // recursive call if the subdirectory is non-empty
                delete(file.getPath());
            }
        }
        toDelete.delete();
    }

    public static void renameFolder(String oldPath, String newPath) {
        File dir = new File(oldPath);
        dir.renameTo(new File(newPath));
    }
//    public static void main(String[] args) throws IOException {
//
//        renameFolder("netty_server/serverFolder/1/lender" ,
//                "netty_server/serverFolder/1/fender");
//    }
//        System.out.println(levelUp("serverFolder/1/nick1/folder"));
//}
//        String path = "netty_server/serverFolder/1/nick1";
//        String name = "folder";
//            String pathWithName = path + "/" + name;
//            new File(pathWithName).mkdirs();
//    }
//
//        service = new InMemoryUserService();
//        service.start();
//        Set<String> f =  Stream.of(new File("/serverFolder/1/nick1").listFiles())
//                .map(File::getName)
//                .collect(Collectors.toSet());
//        System.out.println(f);
    //System.out.println(FileHandler.getFiles("1"));
    //System.out.println(Arrays.toString(FileHandler.getFiles("1")));

//        Path path = Path.of(getPath("1"));
//        String[] files = path.toFile().list();
//        System.out.println(Arrays.toString(files));
//        List<String> list = Files.list(path).
//                map(p -> p.getFileName().toString()).toList(); //вытаскиваю имена файлов и привожу к виду списка строк
//        System.out.println(list);

//        String path = FileHandler.getPath("1");
//        String[] files = FileHandler.getFiles("1");
//        String[] copy = new String[files.length+1];
//        System.arraycopy(files,0,copy,1, files.length);
//        copy[0] = path;
//        System.out.println(Arrays.toString(copy));
//    }
}
