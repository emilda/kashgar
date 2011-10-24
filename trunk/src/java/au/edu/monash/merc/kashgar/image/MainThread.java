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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import com.adobe.xmp.XMPException;

public class MainThread implements Runnable {
	
	private static final String LOG_PREFIX = "[kashgar] ";
	private Logger logger = Logger.getLogger(this.getClass().getName());
	public static final Boolean DEBUG = false; // Boolean.getBoolean("kashgar.debug");
	
	private Thread imgProcessThread;
	private KashgarGUI mainGUI;
	private String inLoc;
	private String outLoc;
	private String xsltFile;
	private SettingManager[] setting;
	ImageConvertor exportToJpgOne;
	ImageConvertor exportToJpgTwo;
	private int enableCount = 0;
	private List<ReportLog> rLogs = new ArrayList<ReportLog>();

	/**
	 * Set parameter before starting the thread
	 * 
	 * @param mainGUI 	= MainGUI class
	 * @param inLoc 	= input folder location
	 * @param outLoc 	= output folder location
	 * @param setting 	= Array of setting Manager, five lines of settings
	 */
	public void startWork(KashgarGUI mainGUI, String inLoc, String outLoc,
			String xslt, SettingManager[] setting) {
		
		this.mainGUI = mainGUI;
		this.inLoc = inLoc;
		this.outLoc = outLoc;
		this.xsltFile = xslt.trim();
		this.setting = setting;
		if (imgProcessThread == null) {
			imgProcessThread = new Thread(this);
		}

		exportToJpgOne = new ExportToJpg();
		exportToJpgTwo = new ExportToJpg();

		this.imgProcessThread.start();

	}

	/**
	 * Main Logic
	 */
	public void run() {
		File selectedFolder = new File(inLoc);
		Thread runThread = Thread.currentThread();
		
		if ((imgProcessThread != null) && (imgProcessThread == runThread) && selectedFolder.exists()) {

			UtilFn.resetRawCount();
			List<ReportLog> unSupportedFiles = FilesReader.loadFiles(selectedFolder);
			for (ReportLog log : unSupportedFiles) {
				rLogs.add(log);
			}
			
			List<File> photoLoc = new ArrayList<File>(FilesReader.getPhotoLoc());
			List<Integer> noInFolder = new ArrayList<Integer>(FilesReader.getNoInFolder());
			List<Integer> totalInFolder = new ArrayList<Integer>(FilesReader.getTotalInFolder());
			List<String> nameOfFolder = new ArrayList<String>(FilesReader.getNameOfFolder());
			
			for (int i = 0; i < photoLoc.size(); i++) {
				String chkName = photoLoc.get(i).getName().toLowerCase();
				boolean isRaw = UtilFn.isJpeg(chkName);
				if (isRaw) {
					UtilFn.addRaw();
				}
			}

			File saveFolder = new File(outLoc);
			if (!saveFolder.exists()) {
				saveFolder.mkdirs();
			}
			Long freeSpace = saveFolder.getFreeSpace();
			mainGUI.showFreeSpace(UtilFn.formatMemorySize(freeSpace));

			if (freeSpace < getUsageSpace(photoLoc.size())) {
				mainGUI.noSpaceWarn();
				return;
			}

			mainGUI.setEstimateTime(UtilFn.calTime(photoLoc.size(), enableCount ));
			mainGUI.showProgressBar(photoLoc.size());
			UtilFn.printTime();
			
			SettingManager[] convertPhoto = new SettingManager[enableCount];
			
			int j = 0;
			for (int i = 0; i < 5; i++) {
				if (setting[i].isEnable()) {
					convertPhoto[j] = setting[i];
					j++;
				}
			}
			int largestSettingLine = UtilFn.findLargest(convertPhoto);
			
			for (int i = 0; i < photoLoc.size(); i++) { // process each photo
				String img_filename = photoLoc.get(i).getAbsolutePath();
				
				ReportLog rLog = new ReportLog();
				rLog.setFilename(img_filename);
				
				if (mainGUI.isStop()) {
					if (convertPhoto.length == 0) {
						mainGUI.finishStopping();
					}
					break;
				}
				
				String partInLoc = UtilFn.getDirStructure(inLoc, photoLoc.get(i).getParent());
				UtilFn.createDir(outLoc + partInLoc);
				String out_filename = outLoc + partInLoc + UtilFn.removeExtension(photoLoc.get(i).getName());
				String xmp_filename = out_filename + ".xmp";
				String txt_filename = out_filename + ".txt";
				UtilFn.log(LOG_PREFIX, "xmp_filename: %s", xmp_filename);

				try {
					System.setProperty("exiftool.debug", "true");
					Exiftool tool = new Exiftool();	// Feature.STAY_OPEN
					tool.writeXMPFile(img_filename, xmp_filename);
					tool.writeTxtFiles(img_filename, txt_filename);
					
					List<MonashTag> monash_tags = new ArrayList<MonashTag>();
					MonashTag tag = MetadataManager.getCustomMetadataTag(mainGUI.getmetadataFmt(), 
										noInFolder.get(i), totalInFolder.get(i), nameOfFolder.get(i));
					monash_tags.add(tag);
					
					if (!UtilFn.isJpeg(img_filename.toLowerCase())) {
						List<MonashTag> iniTags = INIscanner.readINIfile(img_filename);
						for (MonashTag iniTag : iniTags) {
							monash_tags.add(iniTag);
						}
					}
					
					List<MonashTag> fileTags = MetadataManager.getFileTags(img_filename);
					for (MonashTag fileTag : fileTags) {
						monash_tags.add(fileTag);
					}
					TextXMP.writeMonashTags(xmp_filename, monash_tags);
					
					if (null != xsltFile && !"".equals(xsltFile)) 
					{
						String xml_filename = out_filename + ".xml";
						UtilFn.log(LOG_PREFIX, "XML file name: %s", xml_filename);
						XMPTransform.transform(new StreamSource(xmp_filename), new StreamSource(xsltFile), new File(xml_filename));
					}					
					rLog.setStatus(true);
					rLog.setMessage("Output files sucessfully generated for " + img_filename);
				} catch (IOException e) {
					UtilFn.log(LOG_PREFIX, "ERROR: Write .xmp/ .txt were not successfull.");
					logger.error(LOG_PREFIX + "ERROR: Write .xmp/ .txt were not successfull.");
					rLog.setMessage(e.getMessage());
					rLog.setStatus(false);
				} catch (XMPException e) {
					UtilFn.log(LOG_PREFIX, "ERROR: Write monashkashgar tags were not successfull.");
					logger.error(LOG_PREFIX + "ERROR: Write monashkashgar tags were not successfull.");
					rLog.setMessage(e.getMessage());
					rLog.setStatus(false);
				} catch (TransformerException e) {
					UtilFn.log(LOG_PREFIX, "ERROR: XSLT transformation exception.");
					logger.error(LOG_PREFIX + "ERROR: XSLT transformation exception.");
					rLog.setMessage(e.getMessage());
					rLog.setStatus(false);
				}
				
				Boolean accepted = false;
				while (!accepted) {
					if (enableCount == 0 || mainGUI.isStop()) {
						break;
					}
					if (Thread.State.NEW == exportToJpgOne.getState() || Thread.State.TERMINATED == exportToJpgOne.getState()) {
						if (Thread.State.TERMINATED == exportToJpgOne.getState()) {
							mainGUI.setProgressBar(i);
							exportToJpgOne = new ExportToJpg();
						}
						exportToJpgOne.startWork(convertPhoto, photoLoc.get(i),
								!UtilFn.isJpeg(img_filename.toLowerCase()), outLoc, partInLoc, enableCount, largestSettingLine);
						accepted = true;
					} else if (Thread.State.NEW == exportToJpgTwo.getState() || Thread.State.TERMINATED == exportToJpgTwo.getState()) {
						if (Thread.State.TERMINATED == exportToJpgTwo.getState()) {
							mainGUI.setProgressBar(i);
							exportToJpgTwo = new ExportToJpg();
						}
						exportToJpgTwo.startWork(convertPhoto, photoLoc.get(i), !UtilFn.isJpeg(img_filename.toLowerCase()), outLoc, partInLoc,
								enableCount, largestSettingLine);
						accepted = true;
					} else {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							logger.error(LOG_PREFIX + "Thread interrupted.\n" + e.getMessage());
						}
					}
				}
				rLogs.add(rLog);
			}  // Processed each photo
			
			while ( (Thread.State.NEW != exportToJpgOne.getState() && Thread.State.TERMINATED != exportToJpgOne.getState()) ||
					(Thread.State.NEW != exportToJpgTwo.getState() && Thread.State.TERMINATED != exportToJpgTwo.getState()) ) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			}
			UtilFn.printTime();
			if (!mainGUI.isStop()) {
				while ( (Thread.State.NEW != exportToJpgOne.getState() && Thread.State.TERMINATED != exportToJpgOne.getState()) ||
						(Thread.State.NEW != exportToJpgTwo.getState() && Thread.State.TERMINATED != exportToJpgTwo.getState()) ) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {}

				}
				mainGUI.setProgressBar(photoLoc.size());
				if (Report.genReport(rLogs)) {
					mainGUI.reportBtnShow();
				}
				mainGUI.setFinishBtn();
			} else {
				while ( (Thread.State.NEW != exportToJpgOne.getState() && Thread.State.TERMINATED != exportToJpgOne.getState()) ||
						(Thread.State.NEW != exportToJpgTwo.getState() && Thread.State.TERMINATED != exportToJpgTwo.getState()) ) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {}

				}
				mainGUI.finishStopping();
			}
		} else { // End of thread.
			ReportLog rLog = new ReportLog();
			rLog.setFilename(inLoc);
			rLog.setMessage("Input directory does not exist!");
			rLog.setStatus(false);
			rLog.setPhoto(false);
			rLogs.add(rLog);
			if (Report.genReport(rLogs)) {
				mainGUI.reportBtnShow();
			}
			mainGUI.setFinishBtn();
		}
	}

	private Long getUsageSpace(int photoLoc_size) {
		Long sum = (long) 0;
		for (int i = 0; i < 5; i++) {
			if (setting[i].isEnable()) {
				enableCount++;
				if (setting[i].isConvert()) {
					if (setting[i].isResize()) {
						sum = sum + UtilFn.calUsageSpace(photoLoc_size, setting[i].getPixel());
					} else {
						sum = sum + 3 * 1024 * 1024 * photoLoc_size;
					}

				} else {
					sum = sum + 18 * 1024 * 1024 * photoLoc_size;
				}
			}
		}
		mainGUI.showUsageSpace(UtilFn.usageSpaceDisplay(sum));
		return sum;
	}
}
