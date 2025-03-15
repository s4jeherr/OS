import java.nio.file.Path;

public class ZFSTransactionTest {
    public static void main(String[] args) {
        String dataset = "data/yourdataset";
        String snapshotName = "txn_snapshot";

        ZFSSnapshotManager snapshotManager = new ZFSSnapshotManager(dataset);
        FileTransaction transaction = new FileTransaction(snapshotManager, snapshotName);

        Path filePath = Path.of("/your/zfs/directory/testfile.txt");
        transaction.addOperation(new FileWriteOperation(filePath, "Testing ZFS snapshots!"));

        try {
            transaction.commit();
            System.out.println("Transaction committed successfully.");
        } catch (Exception e) {
            System.err.println("Transaction failed, rolled back to snapshot.");
        }
    }
}
