import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class BrainstormingTool {
    private static final Path IDEAS_DIR = Path.of("ideas");
    private static final ZFSSnapshotManager snapshotManager = new ZFSSnapshotManager("new-pool");

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
        Files.createDirectories(IDEAS_DIR);  // Ensure the ideas directory exists
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nBrainstorming Tool");
            System.out.println("1. Add a new idea");
            System.out.println("2. Read an idea");
            System.out.println("3. Add a comment to an idea");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addNewIdea(scanner);
                    break;
                case "2":
                    readIdea(scanner);
                    break;
                case "3":
                    addComment(scanner);
                    break;
                case "4":
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void addNewIdea(Scanner scanner) throws IOException, NoSuchAlgorithmException, InterruptedException {
        System.out.print("Enter idea title: ");
        String title = scanner.nextLine();
        Path ideaPath = IDEAS_DIR.resolve(title + ".txt");

        // Ensure the directory exists
        Files.createDirectories(IDEAS_DIR);

        // Create a new, empty file (overwrite if it already exists) with initial content
        Files.writeString(ideaPath, "Initializing file...\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Now create the transaction
        System.out.println("Enter idea content:");
        String content = scanner.nextLine();

        // Create the transaction
        FileTransaction transaction = new FileTransaction(snapshotManager, title);
        FileWriteOperation writeOperation = new FileWriteOperation(ideaPath, content);

        // Add the operation to the transaction
        transaction.addOperation(writeOperation);

        // Commit the transaction
        System.out.println("Attempting to commit transaction for: " + title);
        transaction.commit();
        System.out.println("Transaction committed successfully.");
    }



    private static void readIdea(Scanner scanner) throws IOException {
        listIdeas();
        System.out.print("Enter idea title to read: ");
        String title = scanner.nextLine();
        Path filePath = IDEAS_DIR.resolve(title + ".txt");

        if (Files.exists(filePath)) {
            String content = Files.readString(filePath);
            System.out.println("\n--- " + title + " ---\n" + content);
        } else {
            System.out.println("Idea not found.");
        }
    }

    private static void addComment(Scanner scanner) throws IOException, NoSuchAlgorithmException, InterruptedException {
        listIdeas();
        System.out.print("Enter idea title to comment on: ");
        String title = scanner.nextLine();
        Path filePath = IDEAS_DIR.resolve(title + ".txt");

        if (!Files.exists(filePath)) {
            System.out.println("Idea not found.");
            return;
        }

        System.out.println("Enter your comment:");
        String comment = scanner.nextLine();

        // Format the comment with a timestamp
        String formattedComment = "\n[Comment - " + java.time.LocalDateTime.now() + "]: " + comment;

        FileTransaction transaction = new FileTransaction(snapshotManager, "comment-" + title);

        // Ensure we are appending and detecting conflicts
        String existingContent = Files.readString(filePath);
        FileWriteOperation writeOperation = new FileWriteOperation(filePath, existingContent + formattedComment);

        transaction.addOperation(writeOperation);
        transaction.commit();

        System.out.println("Comment added successfully!");
    }


    private static void listIdeas() throws IOException {
        System.out.println("\nAvailable Ideas:");
        Files.list(IDEAS_DIR)
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .forEach(System.out::println);
    }

    private static boolean launchEditor(Path filePath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("nano", filePath.toString()); // Change "nano" to preferred editor
        pb.inheritIO();
        Process process = pb.start();
        return process.waitFor() == 0;  // Returns true if the editor exited successfully
    }
}
