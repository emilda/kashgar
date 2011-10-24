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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class INIscanner
{
	public static void main(String args[]) {
		INIscanner.readINIfile("/Users/emilda/Pictures/kashgar/photos/test-raw/kslide-0015.tif");
	}
	
	public static List<MonashTag> readINIfile(String path)
	{
		List<MonashTag> iniTags = new ArrayList<MonashTag>();
		
		int pos = path.lastIndexOf(File.separator);
		String filename = path.substring(pos+1);
		String folder = path.substring(0, pos+1);
		
		File file = new File(folder + ".picasa.ini");
		try
		{
			Scanner fileScanner = new Scanner(file);
			fileScanner.useDelimiter("\\[");
			while (fileScanner.hasNext())
			{
				String data = fileScanner.next();

				Scanner filenameScanner = new Scanner(data);
				String line = filenameScanner.nextLine();
				line = line.substring(0, line.length() - 1);
				
				if (line.equals(filename)) {
					String value = scanData("caption=", data);
					if (null != value) {
						MonashTag tag = new MonashTag("Caption", value);	
						iniTags.add(tag);
					}
					value = scanData("keywords=", data);
					if (null != value) {
						MonashTag tag = new MonashTag("Keywords", value);	
						iniTags.add(tag);
					}
					value = scanData("geotag=", data);
					if (null != value) {
						MonashTag tag = new MonashTag("GPS", value);	
						iniTags.add(tag);
					}
				} else {
					continue;
				}
			}
			return iniTags;
		} catch (FileNotFoundException e) {
			return iniTags;
		}
	}

	private static String scanData(String pattern, String data) {
		Scanner patternScan = new Scanner(data);
		patternScan.findWithinHorizon(pattern, 0);
		if (patternScan.hasNext()) {
			String match = patternScan.nextLine();
			//System.out.println("match: " + match);
			if (match.endsWith("]")) {
				return null;
			}
			return match;
		}
		return null;
	}
}
