package cz.mzk.tiledimageview.cache;

import java.io.File;

import cz.mzk.tiledimageview.Logger;

/**
 * @author Martin Řehánek
 */
public class DiskUtils {

    private static final Logger LOGGER = new Logger(DiskUtils.class);

    public static boolean deleteDirContent(File file) {
        if (file == null) {
            throw new NullPointerException("file is null");
        }
        if (!file.exists()) {
            LOGGER.w("file doesn't exist: " + file.getAbsolutePath());
            return false;
        }
        if (!file.isDirectory()) {
            LOGGER.w("not directory: " + file.getAbsolutePath());
            return false;
        } else {
            File[] filesInDir = file.listFiles();
            if (filesInDir != null && filesInDir.length > 0) {
                for (File fileInDir : filesInDir) {
                    if (!deleteWithContent(fileInDir)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public static boolean deleteWithContent(File file) {
        if (file == null) {
            throw new NullPointerException("file is null");
        }
        if (!file.exists()) {
            LOGGER.w("doesn't exist: " + file.getAbsolutePath());
            return false;
        }
        if (file.isFile()) {
            boolean deleted = file.delete();
            if (!deleted) {
                LOGGER.w("failed to delete file " + file.getAbsolutePath());
            }
            return deleted;
        } else if (!file.isDirectory()) {
            LOGGER.w("not file nor directory: " + file.getAbsolutePath());
            return false;
        } else { // dir
            File[] filesInDir = file.listFiles();
            if (filesInDir != null && filesInDir.length > 0) {
                for (File fileInDir : filesInDir) {
                    if (!deleteWithContent(fileInDir)) {
                        return false;
                    }
                }
            }
            boolean deleted = file.delete();
            if (!deleted) {
                LOGGER.w("failed to delete directory " + file.getAbsolutePath());
            }
            return deleted;
        }
    }

}
