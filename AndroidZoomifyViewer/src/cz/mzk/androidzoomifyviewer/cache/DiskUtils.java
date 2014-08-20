package cz.mzk.androidzoomifyviewer.cache;

import java.io.File;

/**
 * @author Martin Řehánek
 * 
 */
public class DiskUtils {

	public static boolean deleteDirContent(File file) {
		if (!file.isDirectory()) {
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
			return file.delete();
		}
	}

	public static boolean deleteWithContent(File file) {
		if (file == null) {
			return false;
		} else if (file.isFile()) {
			return file.delete();
		} else if (!file.isDirectory()) {
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
			return file.delete();
		}
	}

}
