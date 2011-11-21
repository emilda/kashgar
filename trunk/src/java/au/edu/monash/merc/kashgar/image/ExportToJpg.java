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
 * This thread does the ground work before the conversion or resizing of the images.
 * Two of this thread are created in the MainThread.java.
 */
import java.io.*;

import org.apache.log4j.Logger;

import au.edu.monash.merc.kashgar.exception.KashgarException;
/**
 * @author Sindhu Emilda
 * @version v2.0
 */
public class ExportToJpg implements ImageConvertor
{
	private static final String LOG_PREFIX = "[Export] ";
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private Thread exportThread;
	private SettingManager[] convertPhoto;
	private File file;
	private Boolean isRaw;
	private String outLoc;
	private String partInLoc;
	private int enableCount;
	private int largestSettingLine;

	public Thread.State getState() {
		if (exportThread == null) {
			exportThread = new Thread(this);
		}
		return exportThread.getState();
	}

	/**
	 * Call converter Thread and store parameter
	 * 
	 * @param convertPhoto = Array of 5 settings
	 * @param file = file to convert
	 * @param isRaw = raw have a different contrast setting
	 * @param outLoc = output location
	 * @param partInLoc = getting the folder name
	 * @param enableCount
	 * @param largestSettingLine
	 */
	public void startWork(SettingManager[] convertPhoto, File file, Boolean isRaw, String outLoc, String partInLoc,
								int enableCount, int largestSettingLine) {
		this.convertPhoto = convertPhoto;
		this.file = file;
		this.isRaw = isRaw;
		this.outLoc = outLoc;
		this.partInLoc = partInLoc;
		this.enableCount = enableCount;
		this.largestSettingLine = largestSettingLine;
		if (exportThread == null) {
			exportThread = new Thread(this);
		}
		if (exportThread.getState() == Thread.State.NEW) {
			this.exportThread.start();
		}
	}

	/**
	 * Convert photo without resize
	 * 
	 * @param file
	 * @param name
	 * @param convertTo
	 * @param isRaw
	 * @return
	 */
	private File exportOriSize(File file, String name, String convertTo, Boolean isRaw) {
		String convertFrom = file.getParent();
		String fileName = file.getName();
		String fromFile = convertFrom + File.separator + fileName;
		logger.info(LOG_PREFIX + "exportOriSize " + fromFile);
		String toFile = convertTo + name + ".jpg";

		String options = "";
		if (isRaw) {
			//options += " -modulate 105 -auto-level ";
			options += " -quality 100 ";
		}

		String output = Converter.convert(fromFile, toFile, options);
		logger.info(output);
		return new File(toFile);
	}

	/**
	 * Convert photo with resize
	 * 
	 * @param file
	 * @param name
	 * @param convertTo
	 * @param size
	 * @param isRaw
	 * @return
	 */
	private File exportFile(File file, String name, String convertTo, String size, Boolean isRaw) {
		String convertFrom = file.getParent();
		String fileName = file.getName();
		String fromFile = convertFrom + File.separator + fileName;

		logger.info(LOG_PREFIX + "exportFile " + fromFile);
		
		String toFile = convertTo + name + ".jpg";
		String options = " -resize " + size + "x" + size;

		if (size.equals("0")) {
			options = "";
		}

		if (isRaw) {
			//options += " -modulate 105 -auto-level ";
			options += " -quality 100 ";
		}

		String output = Converter.convert(fromFile, toFile, options);
		logger.info(output);
		return new File(toFile);
	}

	/**
	 * Resize after JPG has generated
	 * 
	 * @param file
	 * @param name
	 * @param convertTo
	 * @param size
	 */
	private void resize(File file, String name, String convertTo, String size) {
		String convertFrom = file.getParent();
		String fileName = file.getName();
		String fromFile = convertFrom + File.separator + fileName;

		String toFile = convertTo + name + ".jpg";
		logger.info(LOG_PREFIX + "resize " + toFile);
		String options = " -resize " + size + "x" + size;

		if (size.equals("0")) {
			options = "";
		}

		String output = null;
		try {
			output = Converter.convert(fromFile, toFile, options);
		} catch (Exception x) {
			x.printStackTrace();
			throw new KashgarException(x);
		}
		logger.info(output);
	}

	/**
	 * The thread send command to convert.exe
	 */
	public void run() {
		Thread runThread = Thread.currentThread();
		if ((exportThread != null) && (exportThread == runThread)) {
			File largestJPG = null;
			if (enableCount == 0) {
				return;
			}

			if (convertPhoto[largestSettingLine].getPixel().equals("0")) {
				largestJPG = exportOriSize(file, UtilFn.removeJpgExtension(file.getName())
						+ convertPhoto[largestSettingLine].getFileName(), outLoc + partInLoc, isRaw);
			} else if (convertPhoto[largestSettingLine].getPixel().equals("-1")) {
				if (convertPhoto[largestSettingLine].getFileName().equals(""))
				{
					copyFile(file, new File(outLoc + partInLoc + file.getName()));
				} else
				{
					copyFile(file, new File(outLoc + partInLoc + UtilFn.removeJpgExtension(file.getName())
							+ convertPhoto[largestSettingLine].getFileName() + UtilFn.getExtension(file.getName())));
				}
			} else {
				largestJPG = exportFile(file, UtilFn.removeJpgExtension(file.getName())
						+ convertPhoto[largestSettingLine].getFileName(), outLoc + partInLoc,
						convertPhoto[largestSettingLine].getPixel(), isRaw);
			}

			for (int k = 0; k < enableCount; k++)
			{
				if (k != largestSettingLine)
				{
					if (convertPhoto[k].isConvert())
					{

						resize(largestJPG, UtilFn.removeJpgExtension(file.getName()) + convertPhoto[k].getFileName(),
								outLoc + partInLoc, convertPhoto[k].getPixel());
					} else
					{
						if (convertPhoto[k].getFileName().equals(""))
						{
							copyFile(file, new File(outLoc + partInLoc + file.getName()));
						} else
						{
							copyFile(file, new File(outLoc + partInLoc + UtilFn.removeJpgExtension(file.getName())
									+ convertPhoto[k].getFileName() + UtilFn.getExtension(file.getName())));
						}
					}
				}
			}
		}
	}

	/**
	 * Copy file, copy Bytes
	 * 
	 * @param f1 input file
	 * @param f2 output file
	 */
	private void copyFile(File f1, File f2) {
		logger.info(LOG_PREFIX + "copyFile " + f1.getName() + " to " + f2.getName());
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(f1);
			out = new FileOutputStream(f2);
			byte[] buf = new byte[4096];
			
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			 
		} catch (FileNotFoundException ex) {
			logger.error(LOG_PREFIX + ex.getMessage() + " in the specified directory.");
		} catch (IOException e) {
			logger.error(LOG_PREFIX + e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore whatever
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore whatever
				}
			}
		}
	}
}
