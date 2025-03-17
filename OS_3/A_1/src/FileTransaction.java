import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class FileTransaction {
    private final List<FileOperation> operations = new ArrayList<>();
    private final ZFSSnapshotManager snapshotManager;
    private final String snapshotName;
    private boolean committed = false;

    public FileTransaction(ZFSSnapshotManager snapshotManager, String transactionId) throws IOException, InterruptedException {
        this.snapshotManager = snapshotManager;
        // Replace spaces in transaction ID to avoid issues with ZFS snapshot names
        this.snapshotName = "txn_snapshot_" + transactionId.replaceAll("\\s+", "_");

        try {
            snapshotManager.createSnapshot(snapshotName);
        } catch (IOException e) {
            System.err.println("Warning: Failed to create snapshot. Proceeding without snapshot.");
            // If snapshot creation fails, do not assign a value to snapshotName
        }
    }

    public void addOperation(FileOperation operation) {
        operations.add(operation);
    }

    public void commit() throws IOException, NoSuchAlgorithmException {
        try {
            for (FileOperation op : operations) {
                op.apply(); // Apply changes only if no conflicts
            }
            snapshotManager.deleteSnapshot(snapshotName); // Delete snapshot after commit
            committed = true;
        } catch (IOException | InterruptedException e) {
            System.err.println("Transaction aborted due to conflict: " + e.getMessage());
            rollback(); // Restore from snapshot
            throw new IOException("Conflict detected! Aborting transaction.", e); // 🔴 Now throws exception
        }
    }

    public void rollback() {
        if (!committed && snapshotName != null) {
            try {
                snapshotManager.rollbackSnapshot(snapshotName);
                System.out.println("Transaction rolled back.");
            } catch (IOException | InterruptedException e) {
                System.err.println("Snapshot rollback failed: " + e.getMessage());
            }
        }
    }
}

