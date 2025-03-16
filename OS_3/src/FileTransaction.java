import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class FileTransaction {
    private final List<FileWriteOperation> operations = new ArrayList<>();
    private final ZFSSnapshotManager snapshotManager;
    private final String snapshotName;
    private boolean committed = false;

    public FileTransaction(ZFSSnapshotManager snapshotManager, String transactionId) throws IOException, InterruptedException {
        this.snapshotManager = snapshotManager;
        this.snapshotName = "txn_snapshot_" + transactionId;
        snapshotManager.createSnapshot(snapshotName);
    }

    public void addOperation(FileWriteOperation operation) {
        operations.add(operation);
    }

    public void commit() throws IOException, NoSuchAlgorithmException {
        try {
            for (FileWriteOperation op : operations) {
                op.apply(); // Apply changes only if no conflicts
            }
            snapshotManager.deleteSnapshot(snapshotName);
            committed = true;
            System.out.println("Transaction committed successfully.");
        } catch (IOException | InterruptedException e) {
            System.err.println("Transaction aborted due to conflict: " + e.getMessage());
            rollback(); // Restore from snapshot
        }
    }

    public void rollback() {
        if (!committed) {
            try {
                snapshotManager.rollbackSnapshot(snapshotName);
                System.out.println("Transaction rolled back to snapshot: " + snapshotName);
            } catch (IOException | InterruptedException e) {
                System.err.println("Snapshot rollback failed: " + e.getMessage());
            }
        }
    }
}
