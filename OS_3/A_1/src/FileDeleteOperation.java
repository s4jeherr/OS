import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public class FileDeleteOperation implements FileOperation {
    private Path filePath;
    private String snapshotName;

    public FileDeleteOperation(Path filePath, String snapshotName) {
        this.filePath = filePath;
        this.snapshotName = snapshotName;
    }

    @Override
    public void apply() throws IOException, NoSuchAlgorithmException {
        // Before deleting, you might want to check if the file has changed externally
        FileMetadata metadata = new FileMetadata(filePath);
        if (metadata.hasChanged()) {
            throw new IOException("Conflict detected! File was modified externally. Aborting deletion.");
        }

        // Delete the file if no conflict
        Path snapshotFilePath = Path.of(filePath.toString() + "@snapshot_" + snapshotName);
        if (Files.exists(snapshotFilePath)) {
            Files.delete(snapshotFilePath);
            System.out.println("File deleted: " + filePath);
        } else {
            throw new IOException("Snapshot file does not exist!");
        }
    }
}
