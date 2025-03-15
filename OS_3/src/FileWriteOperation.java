import java.io.*;
import java.nio.file.*;

public class FileWriteOperation implements FileOperation {
    private final Path filePath;
    private final String newContent;
    private String oldContent; // Store original content for rollback
    private boolean fileExisted; // Track if the file existed before

    public FileWriteOperation(Path filePath, String newContent) {
        this.filePath = filePath;
        this.newContent = newContent;
    }

    @Override
    public void apply() throws IOException {
        fileExisted = Files.exists(filePath);

        if (fileExisted) {
            oldContent = Files.readString(filePath); // Save old content
        }

        Files.writeString(filePath, newContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void rollback() throws IOException {
        if (fileExisted) {
            Files.writeString(filePath, oldContent, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            Files.deleteIfExists(filePath); // If file didn't exist, remove it
        }
    }
}
