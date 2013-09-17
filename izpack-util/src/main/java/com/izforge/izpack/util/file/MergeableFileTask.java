package com.izforge.izpack.util.file;

/**
 * Marker interface indicating that implementors can merge multiple files
 * into a single target file.
 * 
 * @author daniela
 *
 */
public interface MergeableFileTask {
	
	/**
	 * Set true to suggest that the task merges available source files to the
	 * target. This is only a hint to the task, and does not guarantee that 
	 * merging will be used.
	 * 
	 * @param useMerge true to suggest that the task merges available source 
	 * files to the target
	 */
	public abstract void useMerge(boolean useMerge);
}
