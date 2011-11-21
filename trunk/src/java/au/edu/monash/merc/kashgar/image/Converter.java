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
 * Converts the given image file into Jpeg with the options specified.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;
/**
 * @author Sindhu Emilda
 * @version v2.0
 */
public class Converter {

	private static Logger logger = Logger.getLogger(Converter.class.getName());
	
	/**
	 * Set the path to exe and library for Mac or Windows. Uses Runtime to execute the convert command.
	 * 
	 * Convert is an executable from ImageMagick. ImageMagick® is a software suite to create, edit, 
	 * compose, or convert images. It can read and write images in a variety of formats.
	 * 
	 * convert program is used to convert to JPEG image formats as well as resize if specified in options.
	 */
	public static String convert(String fromFile, String toFile, String options) {
		String output = null;

		try {
			String envCommands = null;
			String shellCommand = null;
			String finalCommand = null;
			logger.info(System.getProperty("os.name"));
			if (System.getProperty("os.name").equals("Mac OS X")) {
				shellCommand = "/bin/bash";
				String IMpath = System.getProperty("user.dir") + "/../MacIM";
				envCommands = "BASEDIR=" + IMpath;
				envCommands += "\nexport MAGICK_HOME=\"$BASEDIR/ImageMagick-6.6.5\"\n";
				envCommands += "export PATH=\"$MAGICK_HOME/bin:$PATH\"\n";
				envCommands += "export DYLD_LIBRARY_PATH=\"$MAGICK_HOME/lib\"\n";
				finalCommand = "exit\n";
				logger.info(envCommands);
			} else {
				String CONVERT_PATH = "convert";
				String converterLoc = new java.io.File(".." + File.separator + CONVERT_PATH).getCanonicalPath();
				shellCommand = "cmd";
				envCommands = "cd " + converterLoc + "\n";
				finalCommand = "exit\n";
			}

			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(shellCommand);
			BufferedReader processOutput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));

			String commandToSend = envCommands;
			commandToSend += "convert " + options + " \"" + fromFile + "\" \"" + toFile + "\" 2>&1\n";
			commandToSend += finalCommand;
			logger.info(commandToSend);
			
			processInput.write(commandToSend);
			processInput.flush();
			
			int lineCounter = 0;
			while (true) {
				String line = processOutput.readLine();
				if (line == null) {
					break;
				}
				output += ++lineCounter + ": " + line;
			}
			logger.info(output);
			
			processInput.close();
			processOutput.close();
			pr.waitFor();
			
		} catch (Exception x) {
			x.printStackTrace();
		}
		return output;
	}

}
