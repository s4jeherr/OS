import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileTransaction {
    private final List<FileOperation> operations = new ArrayList<>();
    private final ZFSSnapshotManager snapshotManager;
    private final String snapshotName;
    private boolean committed = false;

    public FileTransaction(ZFSSnapshotManager snapshotManager, String snapshotName) {
        this.snapshotManager = snapshotManager;
        this.snapshotName = snapshotName;
    }

    public void addOperation(FileOperation operation) {
        operations.add(operation);
    }

    public void commit() throws IOException, InterruptedException {
        try {
            snapshotManager.createSnapshot(snapshotName); // 🔥 Take snapshot before applying
            for (FileOperation op : operations) {
                op.apply();
            }
            committed = true;
        } catch (IOException | InterruptedException e) {
            rollback();
            throw e;
        }
    }

    public void rollback() {
        if (!committed) {
            try {
                snapshotManager.rollbackSnapshot(snapshotName); // 🔄 Revert to snapshot
            } catch (IOException | InterruptedException e) {
                System.err.println("Snapshot rollback failed: " + e.getMessage());
            }
        }
    }
}
