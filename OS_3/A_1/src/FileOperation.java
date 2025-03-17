import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface FileOperation {
    void apply() throws IOException, NoSuchAlgorithmException;   // Execute the operation
}