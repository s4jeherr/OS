import java.io.*;
import java.nio.file.*;

public class FileRenameOperation implements FileOperation {
    private final Path oldPath;
    private final Path newPath;

    public FileRenameOperation(Path oldPath, Path newPath) {
        this.oldPath = oldPath;
        this.newPath = newPath;
    }

    @Override
    public void apply() throws IOException {
        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void rollback() throws IOException {
        Files.move(newPath, oldPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
