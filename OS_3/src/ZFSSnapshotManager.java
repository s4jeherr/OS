import java.io.IOException;

public class ZFSSnapshotManager {

    private final String datasetName; // ZFS dataset name (e.g., "poolname/yourdataset")

    public ZFSSnapshotManager(String datasetName) {
        this.datasetName = datasetName;
    }

    public void createSnapshot(String snapshotName) throws IOException, InterruptedException {
        String command = "zfs snapshot " + datasetName + "@" + snapshotName;
        executeCommand(command);
    }

    public void rollbackSnapshot(String snapshotName) throws IOException, InterruptedException {
        String command = "zfs rollback " + datasetName + "@" + snapshotName;
        executeCommand(command);
    }

    public void deleteSnapshot(String snapshotName) throws IOException, InterruptedException {
        String command = "zfs destroy " + datasetName + "@" + snapshotName;
        executeCommand(command);
    }

    private void executeCommand(String command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("bash", "-c", command).start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Command failed: " + command);
        }
    }
}