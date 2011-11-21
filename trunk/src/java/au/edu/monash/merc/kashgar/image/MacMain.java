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
 * Main class invoked in the Mac OS. Name of this class is given as
 * the MainClass entry in Info.plist file contained under the
 * mac_kashgar_dist directory.
 */
import javax.swing.JFrame;

import com.apple.eawt.Application;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.AppEvent;
import java.util.List;
import java.io.*;
/**
 * @author Sindhu Emilda
 * @version v2.0
 */
public class MacMain extends JFrame {

	private static final long serialVersionUID = 1L;
	static File topFolder;

	public static void main(String[] args) {

		Application.getApplication().setOpenFileHandler(new OpenFilesHandler() {
			public void openFiles(AppEvent.OpenFilesEvent e) {
				UtilFn.calSpeed();
				List<File> filelist = e.getFiles();
				String path = null;
				for (File myfile : filelist) {
					path = myfile.getAbsolutePath();
					break;
				}
				File firstFile = new File(path);
				String FileLoc = firstFile.getParent();
				if (FileLoc == null) {
					MacMain.topFolder = firstFile;
				} else {
					MacMain.topFolder = new File(FileLoc);
				}
				KashgarGUI mainGUI = new KashgarGUI();
				JFrame frame = new JFrame("Kashgar - Metadata Extractor");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(mainGUI.mainLayer(MacMain.topFolder));
				frame.pack();
				frame.setResizable(false);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}
}
