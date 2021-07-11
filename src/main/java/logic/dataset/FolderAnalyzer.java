package logic.dataset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import excption.FolderAnalizerExcepType;
import excption.FolderAnalizerException;
import logic.model.AnalyzedClass;



public class FolderAnalyzer {
	
	private List<AnalyzedClass> classes;
	private File directoryRoot;
	
	
	public FolderAnalyzer (File root) {
		classes = new ArrayList<>();
		directoryRoot = root;
		
	}
	
	/*
	 * If filter it's true return only the java classes
	 */
	public List<AnalyzedClass> getFileList(boolean filter) throws FolderAnalizerException {
		
		if (filter) 
			getClassesFromDirectoryFiltered(directoryRoot);
		
		else 
			getClassesFromDirectory(directoryRoot);
		
		return this.classes;
	}
	
	private void getClassesFromDirectory(File directory) throws FolderAnalizerException {
		
		File[] directoryFiles = directory.listFiles();
		AnalyzedClass analyzedClass;
		//read files in the directory (recursively explore directory)
		for (File file : directoryFiles) {
			if (file.isDirectory()) {
				getClassesFromDirectory(file);
			}
			else {
				//add relative path to classes list
				analyzedClass = new AnalyzedClass(getRelativePath(file.toPath().toString()));
				classes.add(analyzedClass);
			}
			
		}
		
	}
	
	
	private void getClassesFromDirectoryFiltered(File directory) throws FolderAnalizerException {
		
		File[] directoryFiles = directory.listFiles();
		AnalyzedClass analyzedClass;
		//read files in the directory (recursively explore directory)
		for (File file : directoryFiles) {
			String fileName = file.getName();
			
			if (file.isDirectory()) {
				//ignore test folder
				if (!containsIgnoreCase(fileName, "test")) {
					getClassesFromDirectoryFiltered(file);
				}
				
			}
			else if (fileName.endsWith(".java")) {
				//add relative path to classes list
				analyzedClass = new AnalyzedClass(getRelativePath(file.getPath()));
				classes.add(analyzedClass);	
			}
		}
	}
	
	private String getRelativePath(String absolutePath) throws FolderAnalizerException {
		String relativePath;
		final int lenghtRoot = directoryRoot.getPath().length();
		final int lenght = absolutePath.length();
		//consistency check
		if (lenght < lenghtRoot) {
			throw new FolderAnalizerException(FolderAnalizerExcepType.REPOMSG,"directory lenght < root lenght");
		}
		//remove root path + \
		relativePath = absolutePath.substring(lenghtRoot+1);
		
		return relativePath;
	}
	
	
	/*
	 * This function check if String src contains String what, ignoring the upper or lower case
	 * */
	private static boolean containsIgnoreCase(String src, String what) {
	    final int length = what.length();
	    if (length == 0)
	        return true; // Empty string is contained

	    final char firstLo = Character.toLowerCase(what.charAt(0));
	    final char firstUp = Character.toUpperCase(what.charAt(0));

	    for (int i = src.length() - length; i >= 0; i--) {
	        // Quick check before calling the more expensive regionMatches() method:
	        final char ch = src.charAt(i);
	        if (ch != firstLo && ch != firstUp)
	            continue;

	        if (src.regionMatches(true, i, what, 0, length))
	            return true;
	    }

	    return false;
	}

}
