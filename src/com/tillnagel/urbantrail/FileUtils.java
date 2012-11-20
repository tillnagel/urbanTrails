package com.tillnagel.urbantrail;

import java.io.File;
import java.io.FilenameFilter;

public class FileUtils {

	/**
	 * Gets a list of file names in a directory with a specific file extension (e.g. .gif or .json)
	 * 
	 * @param directoryName
	 *            The name of the directory
	 * @param ext
	 *            The file extension.
	 * @return A list of names of matching files.
	 */
	public static String[] listFile(String directoryName, String ext) {
		
		File dir = new File(directoryName);
		if (dir.isDirectory() == false) {
			System.out.println("Directory does not exists : " + directoryName);
			return null;
		}

		// Gets a list of all files and filters by extension
		GenericExtFilter filter = new GenericExtFilter(ext);
		String[] fileNamesList = dir.list(filter);
		for (int i = 0; i < fileNamesList.length; i++) {
			fileNamesList[i] = directoryName + File.separator + fileNamesList[i];
		}

		return fileNamesList;
	}

	// inner class, generic extension filter
	public static class GenericExtFilter implements FilenameFilter {

		private String ext;

		public GenericExtFilter(String ext) {
			this.ext = ext;
		}

		public boolean accept(File dir, String name) {
			return (name.endsWith(ext));
		}
	}

}
