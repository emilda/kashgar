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
package au.edu.monash.merc.kashgar.installer;
/**
 * Installer for Windows operating system.
 */
import java.awt.Desktop;
import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
/**
 * @author Sindhu Emilda
 * @version v2.0
 */
public class Installer
{
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * Launches Picasa application and invokes the Kashgar application
	 * Button/ Plugin installation file.
	 */
	public void installKashgar()
	{
		System.out.println("Starting to install the Kashgar application ... \n waiting ...");

		Desktop desktop = Desktop.getDesktop();
		if (desktop.isSupported(Desktop.Action.BROWSE))
		{
			URI uri = null;
			try
			{
				String currentDir = System.getProperty("user.dir");
				System.out.println(System.getProperty("os.name"));
				String regpath = "HKEY_LOCAL_MACHINE\\SOFTWARE\\KashgarPicasa";
				String valueName = "installPath";
				String type = "REG_SZ";
				String data = currentDir+"\\win";
				System.out.println("Exec:" + "reg add \"" + regpath + "\" /v \"" + valueName + "\" /t " + type + " /d \"" + data + "\" /f");
				Runtime.getRuntime().exec("reg add \"" + regpath + "\" /v \"" + valueName + "\" /t " + type + " /d \"" + data + "\" /f");
				
				char searchChar = '\\';
				char replaceChar = '/';

				String path = StringUtils.replaceChars(currentDir, searchChar, replaceChar);
				logger.info("Path: " + path);
				path = path.replaceAll(" ", "%20");
				String picInstallFile = "picasa://importbutton/?url=file:///" + path + "/picasa/kashgar.pbz";
				System.out.println("Picasa Button Installation File - " + picInstallFile);
				uri = new URI(picInstallFile);
				desktop.browse(uri);
			} catch (Exception e)
			{
				logger.error("Failed to launch the Picasa installation page. " + e.getMessage());
			}
		}

		logger.info("Installation is finished.");
	}

	/**
	 * This class is invoked from Installar.exe for installing Kashgar application
	 * in Windows operating system.
	 */
	public static void main(String[] args)
	{
		String javaHome = System.getProperty("java.home");

		if (javaHome == null || javaHome.trim() == "")
		{
			System.out.println("Please set the java home in environment.");
			System.exit(0);
		}
		Installer install = new Installer();
		install.installKashgar();
	}
}
