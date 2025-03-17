import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class FileMetadata {
    private Path filePath;
    private String initialHash;
    private long initialMTime;

    public FileMetadata(Path filePath) throws IOException, NoSuchAlgorithmException {
        this.filePath = filePath;
        this.initialHash = hashFileContent(filePath);
        this.initialMTime = getFileMTime(filePath);
    }

    public boolean hasChanged() throws IOException, NoSuchAlgorithmException {
        String currentHash = hashFileContent(filePath);
        long currentMTime = getFileMTime(filePath);
        return !initialHash.equals(currentHash) || initialMTime != currentMTime;
    }

    private static String hashFileContent(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(path);
        byte[] hashBytes = digest.digest(fileBytes);
        return HexFormat.of().formatHex(hashBytes);
    }

    private static long getFileMTime(Path path) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        return attrs.lastModifiedTime().toMillis();
    }
}
