import java.io.IOException;

public class ZFSSnapshotManager {
    private final String dataset;

    public ZFSSnapshotManager(String dataset) {
        this.dataset = dataset;
    }

    public void createSnapshot(String snapshotName) throws IOException, InterruptedException {
        executeCommand("zfs snapshot " + dataset + "@" + snapshotName);
        System.out.println("Created snapshot: " + dataset + "@" + snapshotName);
    }

    public void rollbackSnapshot(String snapshotName) throws IOException, InterruptedException {
        executeCommand("zfs rollback " + dataset + "@" + snapshotName);
        System.out.println("Rolled back to snapshot: " + dataset + "@" + snapshotName);
    }

    public void deleteSnapshot(String snapshotName) throws IOException, InterruptedException {
        executeCommand("zfs destroy " + dataset + "@" + snapshotName);
        System.out.println("Deleted snapshot: " + dataset + "@" + snapshotName);
    }

    private void executeCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", "sudo " + command});
        process.waitFor();
        if (process.exitValue() != 0) {
            throw new IOException("ZFS command failed: " + command);
        }
    }
}
