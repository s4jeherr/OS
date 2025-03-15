import java.io.IOException;

public interface FileOperation {
    void apply() throws IOException;   // Execute the operation
    void rollback() throws IOException; // Undo the operation
}