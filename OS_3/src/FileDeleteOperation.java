import java.io.*;
import java.nio.file.*;

public class FileDeleteOperation implements FileOperation {
    private final Path filePath;
    private byte[] backupData; // Store file contents for rollback
    private boolean fileExisted;

    public FileDeleteOperation(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public void apply() throws IOException {
        fileExisted = Files.exists(filePath);
        if (fileExisted) {
            backupData = Files.readAllBytes(filePath);
            Files.delete(filePath);
        }
    }

    @Override
    public void rollback() throws IOException {
        if (fileExisted) {
            Files.write(filePath, backupData, StandardOpenOption.CREATE);
        }
    }
}
