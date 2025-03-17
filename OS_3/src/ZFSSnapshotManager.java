import java.io.IOException;

public class ZFSSnapshotManager {
    private final String dataset;

    public ZFSSnapshotManager(String dataset) {
        this.dataset = dataset;
    }

    public void createSnapshot(String snapshotName) throws IOException, InterruptedException {
        try {
            executeCommand("zfs snapshot " + dataset + "@" + snapshotName);
            System.out.println("Created snapshot: " + dataset + "@" + snapshotName);
        } catch (IOException e) {
            throw new IOException("Failed to create snapshot: " + snapshotName, e); // 🔴 Propagate failure
        }
    }

    public void rollbackSnapshot(String snapshotName) throws IOException, InterruptedException {
        try {
            executeCommand("zfs rollback " + dataset + "@" + snapshotName);
            System.out.println("Rolled back to snapshot: " + dataset + "@" + snapshotName);
        } catch (IOException e) {
            throw new IOException("Failed to rollback snapshot: " + snapshotName, e); // 🔴 Stop execution
        }
    }

    public void deleteSnapshot(String snapshotName) throws IOException, InterruptedException {
        try {
            executeCommand("zfs destroy " + dataset + "@" + snapshotName);
            System.out.println("Deleted snapshot: " + dataset + "@" + snapshotName);
        } catch (IOException e) {
            throw new IOException("Failed to delete snapshot: " + snapshotName, e); // 🔴 Ensure errors propagate
        }
    }

    private void executeCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", "sudo " + command});
        process.waitFor();
        if (process.exitValue() != 0) {
            throw new IOException("ZFS command failed: " + command);
        }
    }
}
