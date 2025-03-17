import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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

}
