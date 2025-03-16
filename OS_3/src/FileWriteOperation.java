import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileWriteOperation implements FileOperation {
    private final Path filePath;
    private final String newContent;
    private final String oldContentHash;

    public FileWriteOperation(Path filePath, String newContent) throws IOException, NoSuchAlgorithmException {
        this.filePath = filePath;
        this.newContent = newContent;
        this.oldContentHash = hashFileContent(filePath);
    }

    private String hashFileContent(Path path) throws IOException, NoSuchAlgorithmException {
        byte[] content = Files.readAllBytes(path);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return bytesToHex(digest.digest(content));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    @Override
    public void apply() throws IOException, NoSuchAlgorithmException {
        FileMetadata metadata = new FileMetadata(filePath);

        // Before writing, check if the file has changed
        if (metadata.hasChanged()) {
            throw new IOException("Conflict detected! File was modified externally. Aborting transaction.");
        }

        // Proceed with writing changes
        Files.writeString(filePath, newContent);
    }

}
