import java.nio.file.Path;

public class FileTransactionTest {

    public static void main(String[] args) throws Exception {
        ZFSSnapshotManager snapshotManager = new ZFSSnapshotManager("new-pool");
        FileTransaction transaction = new FileTransaction(snapshotManager, Thread.currentThread().getName());
        Path file = Path.of("/new-pool/optimistic.txt");

        transaction.addOperation(new FileWriteOperation(file, "Optimistic Write!"));

        transaction.commit();
    }
}