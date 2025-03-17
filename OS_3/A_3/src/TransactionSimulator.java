import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TransactionSimulator {
    private static final Path IDEAS_DIR = Path.of("ideas");  // Adjusted for file system access
    private static final ZFSSnapshotManager snapshotManager = new ZFSSnapshotManager("new-pool");
    private static final int NUM_TRANSACTIONS_PER_FILE = 100; // Increases transaction volume
    private static final int MAX_CONCURRENT_THREADS = 20; // Increases parallel execution

    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_THREADS);
        List<Path> ideaFiles = getIdeaFiles();

        if (ideaFiles.isEmpty()) {
            System.out.println("No idea files found. Add some ideas before running the simulation.");
            return;
        }

        System.out.println("Starting conflict validation simulation...");

        List<Future<TransactionResult>> futures = new ArrayList<>();
        for (Path file : ideaFiles) {
            List<Callable<TransactionResult>> transactions = generateTransactions(file, NUM_TRANSACTIONS_PER_FILE);
            for (Callable<TransactionResult> transaction : transactions) {
                futures.add(executor.submit(transaction));
            }
        }

        analyzeResults(futures);
        executor.shutdown();
    }

    private static List<Path> getIdeaFiles() throws IOException {
        // Now it checks the file system for the ideas directory
        return Files.list(IDEAS_DIR)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());
    }

    private static List<Callable<TransactionResult>> generateTransactions(Path file, int count) {
        List<Callable<TransactionResult>> transactions = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            String comment = "Random Comment " + random.nextInt(10000);
            transactions.add(() -> simulateTransaction(file, comment));
        }

        return transactions;
    }

    private static TransactionResult simulateTransaction(Path filePath, String newContent) {
        long startTime = System.nanoTime();
        boolean success = false;
        boolean conflict = false;

        try {
            String transactionId = "txn-" + Thread.currentThread().getId();
            FileTransaction transaction = new FileTransaction(snapshotManager, transactionId);

            // Read the current content to preserve it
            String existingContent = Files.exists(filePath) ? Files.readString(filePath) : "";
            String updatedContent = existingContent + "\n" + newContent;

            transaction.addOperation(new FileWriteOperation(filePath, updatedContent));

            // Simulating random processing delay
            Thread.sleep(new Random().nextInt(5000));

            transaction.commit();
            success = true;
        } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
            conflict = true;
        }

        long endTime = System.nanoTime();
        return new TransactionResult(success, conflict, (endTime - startTime) / 1_000_000);
    }

    private static void analyzeResults(List<Future<TransactionResult>> futures) {
        int successCount = 0, conflictCount = 0;
        long totalRollbackTime = 0;
        int rollbackCount = 0;

        for (Future<TransactionResult> future : futures) {
            try {
                TransactionResult result = future.get();
                if (result.success) {
                    successCount++;
                } else if (result.conflict) {
                    conflictCount++;
                    rollbackCount++;
                    totalRollbackTime += result.executionTimeMs;
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error processing transaction: " + e.getMessage());
            }
        }

        System.out.println("\n=== Simulation Results ===");
        System.out.println("Total Transactions: " + futures.size());
        System.out.println("Successful Commits: " + successCount);
        System.out.println("Conflicts Detected: " + conflictCount);
        System.out.println("Conflict Rate: " + (conflictCount * 100.0 / futures.size()) + "%");
        if (rollbackCount > 0) {
            System.out.println("Average Rollback Time: " + (totalRollbackTime / rollbackCount) + " ms");
        }
    }
}

class TransactionResult {
    final boolean success;
    final boolean conflict;
    final long executionTimeMs;

    TransactionResult(boolean success, boolean conflict, long executionTimeMs) {
        this.success = success;
        this.conflict = conflict;
        this.executionTimeMs = executionTimeMs;
    }
}
