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
import java.text.NumberFormat;
import java.util.Calendar;

public class UtilFn
{
	private static Long freeDiskSpace;
	private static int speed = 0;
	private static int rawCount;

	public static void printTime()
	{
		System.out.println(Calendar.getInstance().getTime());
	}

	public static int findLargest(SettingManager[] setting)
	{
		int max = 0;
		int maxVal = 1;
		int now;
		for (int i = 0; i < setting.length; i++)
		{
			if (setting[i].getPixel().equals("0"))
			{
				return i;
			} else
			{
				now = Integer.parseInt(setting[i].getPixel());
				if (now > maxVal)
				{
					max = i;
					maxVal = now;
				}
			}
		}
		return max;
	}

	public static String removeJpgExtension(String inputS) {
		String chkS = inputS.substring(inputS.lastIndexOf("."), inputS.length());
		if (chkS.equalsIgnoreCase(".jpg")) {
			inputS = inputS.substring(0, inputS.lastIndexOf("."));
		}
		return inputS;
	}
	
	public static String removeExtension(String inputS) {
		inputS = inputS.substring(0, inputS.lastIndexOf("."));
		return inputS;
	}

	public static String getExtension(String inputS)  {
		inputS = inputS.substring(inputS.lastIndexOf("."), inputS.length());
		return inputS;
	}

	public static String formatMemorySize(Long afreeDiskSpace)
	{
		freeDiskSpace = afreeDiskSpace;
		if (afreeDiskSpace < 1024)
		{
			return "Free Disk Space: " + afreeDiskSpace + "B";
		} else if (afreeDiskSpace < 1048576)
		{
			return "Free Disk Space: " + afreeDiskSpace / 1024 + "KB";
		} else if (afreeDiskSpace < 1073741824)
		{
			return "Free Disk Space: " + afreeDiskSpace / 1048576 + "MB";
		} else
		{
			return "Free Disk Space: " + afreeDiskSpace / 1073741824 + "GB";
		}
	}

	public static Long getFreeDiskSpace()
	{
		return freeDiskSpace;
	}

	public static int calUsageSpace(int num, String resTxt)
	{
		int result = 0;
		if (!resTxt.equals(""))
		{
			int res = Integer.parseInt(resTxt);
			int size = (int) (0.16 * res * res + 35 * res);
			result = size * num;
		}
		return result;
	}

	public static String usageSpaceDisplay(long result)
	{
		if (result < 1024)
		{
			return "Estimate Output Size:" + (result) + "B";
		} else if (result < 1048576)
		{
			return "Estimate Output Size:" + (int) (result / 1024) + "KB";
		} else
		{
			return "Estimate Output Size:" + (int) (result / 1048576) + " MB";
		}
	}

	public static int calSpeed()
	{
		int ans = 3;
		Long start = Calendar.getInstance().getTimeInMillis();
		for (int i = 0; i < 2000000; i++)
		{
			ans = ans * ans / ans * ans / ans * ans / ans * ans / ans * ans
					/ ans * ans / ans * ans / ans * ans / ans * ans / ans * ans
					/ ans;
		}
		Long end = Calendar.getInstance().getTimeInMillis();
		speed = (int) (end - start);
		System.out.println("Speed=" + speed);
		return speed;
	}

	public static int getSpeed()
	{
		if (speed == 0)
		{
			calSpeed();
		}
		return speed;
	}

	public static int calTime(int totalPhotos, int enableCount)
	{
		int rawTime = speed * 40;
		int normPhoto = totalPhotos - rawCount;
		int secNeed = rawCount * rawTime + normPhoto * speed * 2;
		if (enableCount > 0) {
			secNeed += enableCount * speed * 2;
		}

		secNeed = (int) secNeed / 1000;
		return secNeed;
	}

	public static int getRawCount()
	{
		return rawCount;
	}

	public static void resetRawCount()
	{
		rawCount = 0;
	}

	public static void addRaw()
	{
		rawCount++;
	}

	protected static void log(String prefix, String message, Object... params) {
		if (MainThread.DEBUG)
			System.out.printf(prefix + message + '\n', params);
	}

	public static String getDirStructure(String inLoc, String file_loc) {

		if (!inLoc.endsWith(File.separator)) {
			inLoc = inLoc + File.separator;
		}

		if (!file_loc.endsWith(File.separator)) {
			file_loc = file_loc + File.separator;
		}

		file_loc = file_loc.replace(inLoc, "");

		return file_loc;
	}

	public static void createDir(String path) {
		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdirs();
		}
	}

	public static String getFilename(String path) {
		int pos = path.lastIndexOf(File.separator);
		return path.substring(pos + 1, path.length());
	}

	/**
	 * Returns the file size in MB, KB or B rounded to two decimal places.
	 * @param path	path to the file
	 * @return
	 */
	public static String getFilesize(String path) {
		File file = new File(path);
		long fileSize = file.length();
		float kfileSize = fileSize / 1024;
		float mfileSize = kfileSize / 1024;

		String size;
		if (fileSize > 1048576)
		{
			size = round(mfileSize, 2) + "MB";
		} else if (fileSize > 1024)
		{
			size = round(kfileSize, 2) + "KB";
		} else
		{
			size = Float.toString(fileSize) + "B";
		}
		return size;
	}

	/**
	 * Rounds the Rval to the number of places in Rpl
	 * @param Rval value to round
	 * @param Rpl  number of places to round to
	 * @return
	 */
	public static String round(float Rval, int Rpl) {
		NumberFormat fmt = NumberFormat.getInstance();
        fmt.setMaximumFractionDigits(Rpl);
        return fmt.format(Rval);        
	}

	public static boolean isJpeg(String chkName) {
		String[] jpg = {".jpg", ".jpeg"};
		for (String extn : jpg) {
			if (chkName.endsWith(extn)) {
				return true;
			}
		}
		return false;
	}

	public static String Foldername(String path) {
		int pos = path.lastIndexOf(File.separator);
		path = path.substring(0, pos);
		pos = path.lastIndexOf(File.separator);
		return path.substring(pos + 1, path.length());
	}

	public static String divide(String type) {
		try {
			String[] operands = type.split("/");
			double operand1 = Double.parseDouble(operands[0]);
			double operand2 = Double.parseDouble(operands[1]);
			double ans = operand1 / operand2;
			return Double.toString(ans);
		} catch (NumberFormatException e) {
			return type;
		}
	}
}
