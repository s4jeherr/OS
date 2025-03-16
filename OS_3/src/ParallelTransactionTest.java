import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelTransactionTest {
    public static void main(String[] args) {
        ZFSSnapshotManager snapshotManager = new ZFSSnapshotManager("new-pool");
        ExecutorService executor = Executors.newFixedThreadPool(3);

        Runnable transactionTask = () -> {
            try {
                FileTransaction transaction = new FileTransaction(snapshotManager, Thread.currentThread().getName());
                Path file = Path.of("/new-pool/optimistic.txt");

                transaction.addOperation(new FileWriteOperation(file, "New content by " + Thread.currentThread().getName()));

                transaction.commit();
            } catch (Exception e) {
                System.err.println("Transaction failed: " + e.getMessage());
            }
        };

        executor.execute(transactionTask);
        executor.execute(transactionTask);
        executor.execute(transactionTask);

        executor.shutdown();
    }
}
