/*
 * Copyright (c) 2010-2011, Monash e-Research Centre
 * (Monash University, Australia)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright
 * 	  notice, this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright
 * 	  notice, this list of conditions and the following disclaimer in the
 * 	  documentation and/or other materials provided with the distribution.
 * 	* Neither the name of the Monash University nor the names of its
 * 	  contributors may be used to endorse or promote products derived from
 * 	  this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package au.edu.monash.merc.kashgar.image;
/**
 * Utility stuff related to reading file from the input folder mentioned.
 */
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
/**
 * @author Sindhu Emilda
 * @version v2.0
 */
public class FilesReader
{
	static String[] supportedExtn = {".dcr", ".nef", ".jpg", ".jpeg", ".tif", ".tiff", ".bmp", ".gif",
						".png", ".tga", ".psd", ".dng", ".crw", ".cr2", ".raf", ".x3f", ".mrw",
						".3fr", ".arw", ".dib", ".btf", ".ciff", ".cs1", 
						".erf", ".fpx", ".kdc", ".mef", ".nrw", ".orf", 
						".pef", ".raw", ".rw2", ".sr2", ".srf", ".xcf"};
	private static List<ReportLog> rLogs = new ArrayList<ReportLog>();
	static List<File> processPhotos = new ArrayList<File>();
	static List<Integer> noInFolder = new ArrayList<Integer>();
	static List<Integer> totalInFolder = new ArrayList<Integer>();
	static List<String> nameOfFolder = new ArrayList<String>();

	private static int noOfFile = 0;
	
	public static List<Integer> getNoInFolder()
	{
		return noInFolder;
	}

	public static List<Integer> getTotalInFolder()
	{
		return totalInFolder;
	}

	public static List<String> getNameOfFolder()
	{
		return nameOfFolder;
	}

	public static List<File> getPhotoLoc()
	{
		return processPhotos;
	}
	
	/**
	 * Load the file details from the input folder and sub directories underneath 
	 * into the various data structures.
	 * @param curFolder
	 * @return
	 */
	public static List<ReportLog> loadFiles(File curFolder)
	{
		noOfFile = 0;
		processPhotos.clear();
		noInFolder.clear();
		totalInFolder.clear();
		nameOfFolder.clear();
		Folder f = findFiles(curFolder);
		//printFolders(f);
		return rLogs;
	}

	/**
	 * Find the supported file in the current folder. Supported files
	 * types are mentioned in FileReader.supportedExtn. Files that needs to be processed
	 * are stored into FilesReader.processPhotos.
	 * 
	 * File name filter and photo filter are defined to find the supported files.
	 * ReportLog is populated with unsupported files (if any).
	 * @param curFolder
	 * @return
	 */
	public static Folder findFiles(File curFolder)
	{
		Folder topFolder = new Folder();
		topFolder.setFolderName(curFolder.getName());
		
		FileFilter folderFilter = new FileFilter()
		{
			public boolean accept(File pathname)
			{
				return (pathname.isDirectory() && !pathname.isHidden());
			}
		};
		FilenameFilter photoFilter = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				String fname = name;
				if (name.startsWith(".")) {
					return false;
				}
				if (name.equals("thumbs.db")) {
					return false;
				}
				name = name.toLowerCase();
				boolean isAccepted = false;
				for (String extn : supportedExtn) {
					if (name.endsWith(extn)) {
						isAccepted = true;
						break;
					}
				}
				if (!isAccepted) {
 					File file = new File(dir + File.separator + fname);
 					if (!file.isDirectory()) {
 						ReportLog rLog = new ReportLog();
 						rLog.setFilename(name);
 						rLog.setMessage("Unsupported file format");
 						rLog.setStatus(false);
 						rLog.setPhoto(false);
 						rLogs.add(rLog);
 					}
				}
				return isAccepted;
			}
		};
		
		File[] listOfFolder = curFolder.listFiles(folderFilter);
		for (int i = 0; i < listOfFolder.length; i++)
		{
			Folder subFolder = new Folder();
			subFolder = findFiles(listOfFolder[i]);
			subFolder.setFolderName(listOfFolder[i].getName());
			topFolder.setSubFolder(subFolder);
		}
		
		File[] listOfFile = curFolder.listFiles(photoFilter);
		for (int i = 0; i < listOfFile.length; i++)
		{
			topFolder.setFile(listOfFile[i]);
			processPhotos.add(listOfFile[i]);
			noInFolder.add(i + 1);
			totalInFolder.add(listOfFile.length);
			nameOfFolder.add(curFolder.getName());
			noOfFile++;
		}
		return topFolder;
	}

	public static void printFolders(Folder fd)
	{
		if (fd.hasChildren())
		{
			List<Folder> fds = fd.getSubFolders();
			for (Folder sfd : fds)
			{
				printFolders(sfd);
			}
		}
	}

	public static int getnoOfFile()
	{
		return noOfFile;
	}
}
