package cz.mzk.androidzoomifyviewer.cache;

import java.io.File;

import android.util.Log;

/**
 * @author Martin Řehánek
 * 
 */
public class DiskUtils {

	private static final String TAG = DiskUtils.class.getSimpleName();

	public static boolean deleteDirContent(File file) {
		if (file == null) {
			throw new NullPointerException("file is null");
		}
		if (!file.exists()) {
			Log.w(TAG, "file doesn't exist: " + file.getAbsolutePath());
			return false;
		}
		if (!file.isDirectory()) {
			Log.w(TAG, "not directory: " + file.getAbsolutePath());
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
			Log.w(TAG, "doesn't exist: " + file.getAbsolutePath());
			return false;
		}
		if (file.isFile()) {
			boolean deleted = file.delete();
			if (!deleted) {
				Log.w(TAG, "failed to delete file " + file.getAbsolutePath());
			}
			return deleted;
		} else if (!file.isDirectory()) {
			Log.w(TAG, "not file nor directory: " + file.getAbsolutePath());
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
				Log.w(TAG, "failed to delete directory " + file.getAbsolutePath());
			}
			return deleted;
		}
	}

}
