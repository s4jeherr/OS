import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public class FileReadOperation implements FileOperation {
    private Path filePath;
    private String snapshotName;
    private String fileContent;

    public FileReadOperation(Path filePath, String snapshotName) {
        this.filePath = filePath;
        this.snapshotName = snapshotName;
    }

    @Override
    public void apply() throws IOException {
        // Ensure you read from the correct version (snapshot version)
        Path snapshotFilePath = Path.of(filePath.toString() + "@snapshot_" + snapshotName);
        if (Files.exists(snapshotFilePath)) {
            String fileContent = Files.readString(snapshotFilePath);
            System.out.println("Read file content: " + fileContent);
        } else {
            throw new IOException("Snapshot file does not exist!");
        }
    }
}
