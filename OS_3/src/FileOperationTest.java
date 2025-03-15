import java.io.*;
import java.nio.file.*;

public class FileOperationTest {
    public static void main(String[] args) {
        try {
            Path testFile = Path.of("data/test.txt");

            // Test Write Operation
            FileOperation writeOp = new FileWriteOperation(testFile, "Hello, Transaction!");
            writeOp.apply();
            System.out.println("Write operation applied.");

            // Test Rollback
            writeOp.rollback();
            System.out.println("Write operation rolled back.");

            // Test Rename Operation
            Path renamedFile = Path.of("renamed.txt");
            FileOperation renameOp = new FileRenameOperation(testFile, renamedFile);
            renameOp.apply();
            System.out.println("Rename operation applied.");

            renameOp.rollback();
            System.out.println("Rename operation rolled back.");

            // Test Delete Operation
            FileOperation deleteOp = new FileDeleteOperation(testFile);
            deleteOp.apply();
            System.out.println("Delete operation applied.");

            deleteOp.rollback();
            System.out.println("Delete operation rolled back.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
