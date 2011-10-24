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

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class KashgarGUI extends JPanel implements ActionListener {
	private SettingManager[] setting = new SettingManager[5];

	private JLayeredPane layeredPane;

	private TextField inLocTxt = new TextField();
	private Button chInLocBtn;

	private TextField outLocTxt = new TextField();
	private Button chOutLocBtn;

	private TextField metadataFmtTxt = new TextField("Image <x> of <y>");

	private TextField xsltLocTxt = new TextField();
	private Button chXsltLocBtn;
	
	private Checkbox line1Chk = new Checkbox();
	private TextField name1Txt = new TextField("_LARGE");
	private Checkbox convert1Chk = new Checkbox("Cnvrt to JPG");
	private Checkbox resize1Chk = new Checkbox("Resize");
	private JSlider resize1Sld = new JSlider();
	private TextField pixel1Txt = new TextField("1600");

	private Checkbox line2Chk = new Checkbox();
	private TextField name2Txt = new TextField("_thumb");
	private Checkbox convert2Chk = new Checkbox("Cnvrt to JPG");
	private Checkbox resize2Chk = new Checkbox("Resize");
	private JSlider resize2Sld = new JSlider();
	private TextField pixel2Txt = new TextField("150");

	private Checkbox line3Chk = new Checkbox();
	private TextField name3Txt = new TextField("_MED");
	private Checkbox convert3Chk = new Checkbox("Cnvrt to JPG");
	private Checkbox resize3Chk = new Checkbox("Resize");
	private JSlider resize3Sld = new JSlider();
	private TextField pixel3Txt = new TextField("750");

	private Checkbox line4Chk = new Checkbox();
	private TextField name4Txt = new TextField("_SMALL");
	private Checkbox convert4Chk = new Checkbox("Cnvrt to JPG");
	private Checkbox resize4Chk = new Checkbox("Resize");
	private JSlider resize4Sld = new JSlider();
	private TextField pixel4Txt = new TextField("640");

	private Checkbox line5Chk = new Checkbox();
	private TextField name5Txt = new TextField("_ORI");
	private Checkbox convert5Chk = new Checkbox("Cnvrt to JPG");
	private Checkbox resize5Chk = new Checkbox("Resize");
	private JSlider resize5Sld = new JSlider();
	private TextField pixel5Txt = new TextField("1600");

	private Label estimateSizeLabel;
	private Label freeDiskSpaceLabel;
	private Label estTime;
	private Label progressLabel;
	private JProgressBar progressBar;

	private Button nextButton;
	private Button reportButton;

	private boolean isStart = false;
	private boolean isStop = false;
	private boolean isFinish = false;

	private static File topFolder;
	private static JFileChooser chooser;

	/**
	 * This is Entry point Default accepting from Picasa However, Only the first
	 * args would be useful As we only need to know the folder location and
	 * Picasa will not send photo from different folders.
	 * 
	 * @param args = Array of String of photo location.
	 */
	public static void main(String[] args) {

		UtilFn.calSpeed();
		KashgarGUI mainGUI = new KashgarGUI();
		String firstArgs;
		if (args.length == 0) {
			System.out.println("Start info not found!");
			firstArgs = System.getProperty("user.dir");
		} else {
			firstArgs = args[0];
		}
		File firstFile = new File(firstArgs);
		String FileLoc = firstFile.getParent();
		if (FileLoc == null) {
			topFolder = firstFile;
		} else {
			topFolder = new File(FileLoc);
		}
		JFrame frame = new JFrame("Kashgar - Metadata Extractor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(mainGUI.mainLayer(topFolder));
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	/**
	 * This is a private component for GUI define the property of each component
	 * and accepting the folder location
	 * 
	 * @param topLv = folder location
	 * @return
	 */
	Component mainLayer(File topLv) {
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension(800, 600));

		Label inputLocationLabel = new Label("Input Location");
		inputLocationLabel.setBounds(26, 28, 88, 20);
		layeredPane.add(inputLocationLabel);

		inLocTxt.setText(topLv.getAbsolutePath());
		inLocTxt.setBounds(133, 30, 532, 20);
		layeredPane.add(inLocTxt);

		chInLocBtn = new Button("Change");
		chInLocBtn.addActionListener(this);
		chInLocBtn.setBounds(697, 29, 80, 25);
		chInLocBtn.setActionCommand("input");
		layeredPane.add(chInLocBtn);

		Label outputLocationLabel = new Label("Output Location");
		outputLocationLabel.setBounds(19, 70, 96, 20);
		layeredPane.add(outputLocationLabel);

		// Mac have user.dir as application resource dir, need to fix
		if (System.getProperty("os.name").equals("Mac OS X")) {
			outLocTxt.setText(new File(System.getProperty("user.home"))
					+ File.separator + "ImagesForARROW");
		} else {
			outLocTxt.setText(new File(System.getProperty("user.dir")).getParent()
					+ File.separator + "output");
		}
		outLocTxt.setBounds(133, 70, 532, 20);
		layeredPane.add(outLocTxt);

		chOutLocBtn = new Button("Change");
		chOutLocBtn.addActionListener(this);
		chOutLocBtn.setBounds(698, 65, 80, 25);
		chOutLocBtn.setActionCommand("output");
		layeredPane.add(chOutLocBtn);

		Label metadataFmtLabel = new Label("Metadata Format");
		metadataFmtLabel.setBounds(14, 113, 107, 17);
		layeredPane.add(metadataFmtLabel);

		metadataFmtTxt.setBounds(133, 108, 533, 20);
		layeredPane.add(metadataFmtTxt);

		line1Chk.setBounds(31, 173, 21, 13);
		layeredPane.add(line1Chk);

		name1Txt.setBounds(65, 169, 152, 20);
		layeredPane.add(name1Txt);

		Label xsltFileLabel = new Label("Optional, XSLT file");
		xsltFileLabel.setBounds(12, 140, 88, 20);
		layeredPane.add(xsltFileLabel);

		xsltLocTxt.setText("");
		xsltLocTxt.setBounds(133, 138, 532, 20);
		layeredPane.add(xsltLocTxt);

		chXsltLocBtn = new Button("Change");
		chXsltLocBtn.addActionListener(this);
		chXsltLocBtn.setBounds(697, 138, 80, 25);
		chXsltLocBtn.setActionCommand("inputFile");
		layeredPane.add(chXsltLocBtn);
		
		convert1Chk.setBounds(225, 173, 105, 17);
		convert1Chk.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				resize1Chk.setEnabled(convert1Chk.getState());
				if (!convert1Chk.getState()) {
					resize1Chk.setState(convert1Chk.getState());
					resize1Sld.setEnabled(resize1Chk.getState());
					pixel1Txt.setEnabled(resize1Chk.getState());
				}
			}
		});
		layeredPane.add(convert1Chk);

		resize1Chk.setBounds(331, 172, 63, 17);
		resize1Chk.setEnabled(false);
		resize1Chk.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				resize1Sld.setEnabled(resize1Chk.getState());
				pixel1Txt.setEnabled(resize1Chk.getState());
			}

		});
		layeredPane.add(resize1Chk);

		resize1Sld = new JSlider(JSlider.HORIZONTAL, 0, 6, 6);
		resize1Sld.setMajorTickSpacing(1);
		resize1Sld.setPaintTicks(true);
		resize1Sld.setPaintTrack(true);
		resize1Sld.setSnapToTicks(true);
		resize1Sld.setEnabled(false);
		resize1Sld.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int pixels = (int) source.getValue();
					switch (pixels) {
					case 0:
						pixel1Txt.setText("150");
						break;
					case 1:
						pixel1Txt.setText("480");
						break;
					case 2:
						pixel1Txt.setText("640");
						break;
					case 3:
						pixel1Txt.setText("750");
						break;
					case 4:
						pixel1Txt.setText("1024");
						break;
					case 5:
						pixel1Txt.setText("1200");
						break;
					case 6:
						pixel1Txt.setText("1600");
						break;
					default:
						break;
					}
				}
			}
		});
		resize1Sld.setBounds(418, 167, 236, 30);
		layeredPane.add(resize1Sld);

		pixel1Txt.setBounds(668, 167, 51, 25);
		pixel1Txt.setEnabled(false);
		layeredPane.add(pixel1Txt);

		Label pixel1Label = new Label("pixels");
		pixel1Label.setBounds(726, 173, 35, 17);
		layeredPane.add(pixel1Label);

		line2Chk.setBounds(31, 203, 21, 13);
		layeredPane.add(line2Chk);

		name2Txt.setBounds(65, 199, 152, 20);
		layeredPane.add(name2Txt);

		convert2Chk.setBounds(225, 203, 105, 17);
		convert2Chk.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				resize2Chk.setEnabled(convert2Chk.getState());
				if (!convert2Chk.getState()) {
					resize2Chk.setState(convert2Chk.getState());
					resize2Sld.setEnabled(resize2Chk.getState());
					pixel2Txt.setEnabled(resize2Chk.getState());
				}
			}
		});
		layeredPane.add(convert2Chk);

		resize2Chk.setBounds(331, 202, 63, 17);
		resize2Chk.setEnabled(false);
		resize2Chk.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				resize2Sld.setEnabled(resize2Chk.getState());
				pixel2Txt.setEnabled(resize2Chk.getState());
			}

		});
		layeredPane.add(resize2Chk);

		resize2Sld = new JSlider(JSlider.HORIZONTAL, 0, 6, 0);
		resize2Sld.setMajorTickSpacing(1);
		resize2Sld.setPaintTicks(true);
		resize2Sld.setPaintTrack(true);
		resize2Sld.setSnapToTicks(true);
		resize2Sld.setEnabled(false);
		resize2Sld.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int pixels = (int) source.getValue();
					switch (pixels) {
					case 0:
						pixel2Txt.setText("150");
						break;
					case 1:
						pixel2Txt.setText("480");
						break;
					case 2:
						pixel2Txt.setText("640");
						break;
					case 3:
						pixel2Txt.setText("750");
						break;
					case 4:
						pixel2Txt.setText("1024");
						break;
					case 5:
						pixel2Txt.setText("1200");
						break;
					case 6:
						pixel2Txt.setText("1600");
						break;
					default:
						break;
					}
				}
			}
		});
		resize2Sld.setBounds(418, 197, 236, 30);
		layeredPane.add(resize2Sld);

		pixel2Txt.setBounds(668, 197, 51, 25);
		pixel2Txt.setEnabled(false);
		layeredPane.add(pixel2Txt);

		Label pixel2Label = new Label("pixels");
		pixel2Label.setBounds(726, 203, 35, 17);
		layeredPane.add(pixel2Label);

		line3Chk.setBounds(31, 233, 21, 13);
		layeredPane.add(line3Chk);

		name3Txt.setBounds(65, 229, 152, 20);
		layeredPane.add(name3Txt);

		convert3Chk.setBounds(225, 233, 105, 17);
		convert3Chk.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				resize3Chk.setEnabled(convert3Chk.getState());
				if (!convert3Chk.getState()) {
					resize3Chk.setState(convert3Chk.getState());
					resize3Sld.setEnabled(resize3Chk.getState());
					pixel3Txt.setEnabled(resize3Chk.getState());
				}
			}
		});
		layeredPane.add(convert3Chk);

		resize3Chk.setBounds(331, 232, 63, 17);
		resize3Chk.setEnabled(false);
		resize3Chk.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {

				resize3Sld.setEnabled(resize3Chk.getState());
				pixel3Txt.setEnabled(resize3Chk.getState());
			}

		});
		layeredPane.add(resize3Chk);

		resize3Sld = new JSlider(JSlider.HORIZONTAL, 0, 6, 3);
		resize3Sld.setMajorTickSpacing(1);
		resize3Sld.setPaintTicks(true);
		resize3Sld.setPaintTrack(true);
		resize3Sld.setSnapToTicks(true);
		resize3Sld.setEnabled(false);
		resize3Sld.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int pixels = (int) source.getValue();
					switch (pixels) {
					case 0:
						pixel3Txt.setText("150");
						break;
					case 1:
						pixel3Txt.setText("480");
						break;
					case 2:
						pixel3Txt.setText("640");
						break;
					case 3:
						pixel3Txt.setText("750");
						break;
					case 4:
						pixel3Txt.setText("1024");
						break;
					case 5:
						pixel3Txt.setText("1200");
						break;
					case 6:
						pixel3Txt.setText("1600");
						break;
					default:
						break;
					}
				}
			}
		});
		resize3Sld.setBounds(418, 227, 236, 30);
		layeredPane.add(resize3Sld);

		pixel3Txt.setBounds(668, 227, 51, 25);
		pixel3Txt.setEnabled(false);
		layeredPane.add(pixel3Txt);

		Label pixel3Label = new Label("pixels");
		pixel3Label.setBounds(726, 233, 35, 17);
		layeredPane.add(pixel3Label);

		line4Chk.setBounds(31, 263, 21, 13);
		layeredPane.add(line4Chk);

		name4Txt.setBounds(65, 259, 152, 20);
		layeredPane.add(name4Txt);

		convert4Chk.setBounds(225, 263, 105, 17);
		convert4Chk.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				resize4Chk.setEnabled(convert4Chk.getState());
				if (!convert4Chk.getState()) {
					resize4Chk.setState(convert4Chk.getState());
					resize4Sld.setEnabled(resize4Chk.getState());
					pixel4Txt.setEnabled(resize4Chk.getState());
				}
			}
		});
		layeredPane.add(convert4Chk);

		resize4Chk.setBounds(331, 262, 63, 17);
		resize4Chk.setEnabled(false);
		resize4Chk.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				resize4Sld.setEnabled(resize4Chk.getState());
				pixel4Txt.setEnabled(resize4Chk.getState());
			}

		});
		layeredPane.add(resize4Chk);

		resize4Sld = new JSlider(JSlider.HORIZONTAL, 0, 6, 2);
		resize4Sld.setMajorTickSpacing(1);
		resize4Sld.setPaintTicks(true);
		resize4Sld.setPaintTrack(true);
		resize4Sld.setSnapToTicks(true);
		resize4Sld.setEnabled(false);
		resize4Sld.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int pixels = (int) source.getValue();
					switch (pixels) {
					case 0:
						pixel4Txt.setText("150");
						break;
					case 1:
						pixel4Txt.setText("480");
						break;
					case 2:
						pixel4Txt.setText("640");
						break;
					case 3:
						pixel4Txt.setText("750");
						break;
					case 4:
						pixel4Txt.setText("1024");
						break;
					case 5:
						pixel4Txt.setText("1200");
						break;
					case 6:
						pixel4Txt.setText("1600");
						break;
					default:
						break;
					}
				}
			}
		});
		resize4Sld.setBounds(418, 257, 236, 30);
		layeredPane.add(resize4Sld);

		pixel4Txt.setBounds(668, 257, 51, 25);
		pixel4Txt.setEnabled(false);
		layeredPane.add(pixel4Txt);

		Label pixel4Label = new Label("pixels");
		pixel4Label.setBounds(726, 263, 35, 17);
		layeredPane.add(pixel4Label);

		line5Chk.setBounds(31, 293, 21, 13);
		layeredPane.add(line5Chk);

		name5Txt.setBounds(65, 289, 152, 20);
		layeredPane.add(name5Txt);

		convert5Chk.setBounds(225, 293, 105, 17);
		convert5Chk.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				resize5Chk.setEnabled(convert5Chk.getState());
				if (!convert5Chk.getState()) {
					resize5Chk.setState(convert5Chk.getState());
					resize5Sld.setEnabled(resize5Chk.getState());
					pixel5Txt.setEnabled(resize5Chk.getState());
				}
			}
		});
		layeredPane.add(convert5Chk);

		resize5Chk.setBounds(331, 292, 63, 17);
		resize5Chk.setEnabled(false);
		resize5Chk.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				resize5Sld.setEnabled(resize5Chk.getState());
				pixel5Txt.setEnabled(resize5Chk.getState());
			}

		});
		layeredPane.add(resize5Chk);

		resize5Sld = new JSlider(JSlider.HORIZONTAL, 0, 6, 6);
		resize5Sld.setMajorTickSpacing(1);
		resize5Sld.setPaintTicks(true);
		resize5Sld.setPaintTrack(true);
		resize5Sld.setSnapToTicks(true);
		resize5Sld.setEnabled(false);
		resize5Sld.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int pixels = (int) source.getValue();
					switch (pixels) {
					case 0:
						pixel5Txt.setText("150");
						break;
					case 1:
						pixel5Txt.setText("480");
						break;
					case 2:
						pixel5Txt.setText("640");
						break;
					case 3:
						pixel5Txt.setText("750");
						break;
					case 4:
						pixel5Txt.setText("1024");
						break;
					case 5:
						pixel5Txt.setText("1200");
						break;
					case 6:
						pixel5Txt.setText("1600");
						break;
					default:
						break;
					}
				}
			}
		});
		resize5Sld.setBounds(418, 287, 236, 30);
		layeredPane.add(resize5Sld);

		pixel5Txt.setBounds(668, 287, 51, 25);
		pixel5Txt.setEnabled(false);
		layeredPane.add(pixel5Txt);

		Label pixel5Label = new Label("pixels");
		pixel5Label.setBounds(726, 293, 35, 17);
		layeredPane.add(pixel5Label);

		estimateSizeLabel = new Label("Estimated Output Size:");
		estimateSizeLabel.setBounds(150, 360, 300, 20);
		layeredPane.add(estimateSizeLabel);

		freeDiskSpaceLabel = new Label("Free Disk Space:");
		freeDiskSpaceLabel.setBounds(150, 380, 300, 20);
		layeredPane.add(freeDiskSpaceLabel);

		estTime = new Label("Estimated Time: ");
		estTime.setBounds(150, 400, 300, 20);
		layeredPane.add(estTime);

		progressLabel = new Label();
		progressLabel.setBounds(275, 420, 50, 20);
		layeredPane.add(progressLabel);

		nextButton = new Button("start");
		nextButton.setActionCommand("start");
		nextButton.addActionListener(this);
		nextButton.setBounds(170, 500, 80, 25);
		layeredPane.add(nextButton);

		reportButton = new Button("View Report");
		reportButton.setActionCommand("report");
		reportButton.addActionListener(this);
		reportButton.setBounds(255, 500, 100, 25);
		reportButton.setVisible(false);
		layeredPane.add(reportButton);

		return layeredPane;
	}

	/**
	 * This function is called after user press start Progress bar accept the
	 * total number of photos and display on the panel
	 * 
	 * @param num
	 *            = total number of photos
	 */
	public void showProgressBar(int num) {
		progressBar = new JProgressBar(0, num);
		progressBar.setBounds(202, 440, 195, 16);
		progressLabel.setText("0%");
		layeredPane.add(progressBar);
	}

	/**
	 * This function is called after finish processing one photo and accept
	 * count of the current photo and display the percentage of the whole
	 * process
	 * 
	 * @param num
	 *            = count of the processed photos
	 */
	public void setProgressBar(int num) {
		progressBar.setValue(num);
		progressLabel.setText((int) (progressBar.getPercentComplete() * 100)
				+ "%");
	}

	/**
	 * This function display estimate time
	 * 
	 * @param time
	 *            = number of second
	 */
	public void setEstimateTime(int time) {
		if (time < 60) {
			estTime.setText("Estimate Time: " + time + " sec");
		} else {
			estTime.setText("Estimate Time: " + (int) (time / 60) + " min");
		}
	}

	/**
	 * This function change the label text
	 * 
	 * @param display
	 *            = String for free space
	 */
	public void showFreeSpace(String display) {
		freeDiskSpaceLabel.setText(display);
	}

	/**
	 * This function change the label text
	 * 
	 * @param display
	 *            = String for usage space
	 */
	public void showUsageSpace(String display) {
		estimateSizeLabel.setText(display);
	}

	/**
	 * actionPerformed for this class listen from all buttons in this class and
	 * performed action according to action event
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("input")) {
			chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File(inLocTxt.getText()));
			chooser.setDialogTitle("Input Top Folder");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);

			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				inLocTxt.setText(chooser.getSelectedFile().toString());
			} else {
				System.out.println("No Selection ");
			}
		} else if (e.getActionCommand().equals("inputFile")) {
			chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File(xsltLocTxt.getText()));
			chooser.setDialogTitle("Select XSLT File");
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);

			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				xsltLocTxt.setText(chooser.getSelectedFile().toString());
			} else {
				System.out.println("No Selection ");
			}
		} else if (e.getActionCommand().equals("output")) {
			chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File(outLocTxt.getText()));
			chooser.setDialogTitle("Save Location");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);

			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				outLocTxt.setText(chooser.getSelectedFile().toString());
			} else {
				System.out.println("No Selection ");
			}
		} else if (e.getActionCommand().equals("start")) {
			setAllBtn(false);
			isStart = true;
			nextButton.setLabel("STOP");
			nextButton.setActionCommand("stop");
			pressStartBtn();

		} else if (e.getActionCommand().equals("stop")) {
			setAllBtn(true);
			isStop = true;
			nextButton.setLabel("stopping");
			nextButton.setActionCommand("stopping");
			nextButton.setEnabled(false);
		} else if (e.getActionCommand().equals("restart")) {
			setAllBtn(false);
			isStart = true;
			isStop = false;
			nextButton.setLabel("STOP");
			nextButton.setActionCommand("stop");
			pressStartBtn();
		} else if (e.getActionCommand().equals("finish")) {
			System.exit(0);
		} else if (e.getActionCommand().endsWith("report")) {
			Desktop desktop = Desktop.getDesktop();
			try {
				File reportLoc = new File(new File(System.getProperty("user.dir")).getParent() + File.separator + "report.txt");
				desktop.open(reportLoc); // open was browse
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
	}

	/**
	 * This function is called when both thread is stopped Enable disabled
	 * buttons
	 */
	public void finishStopping() {
		nextButton.setLabel("Restart");
		nextButton.setActionCommand("restart");
		nextButton.setEnabled(true);
	}

	/**
	 * This function is called in actionPerformed when user press start or
	 * restart and load all settings into five setting manager classes
	 */
	private void pressStartBtn() {
		MainThread mainFn = new MainThread();
		setting[0] = new SettingManager(1, getLine1(), getName1(), getConvert1(), getResize1(), getPixel1());
		setting[1] = new SettingManager(2, getLine2(), getName2(), getConvert2(), getResize2(), getPixel2());
		setting[2] = new SettingManager(3, getLine3(), getName3(), getConvert3(), getResize3(), getPixel3());
		setting[3] = new SettingManager(4, getLine4(), getName4(), getConvert4(), getResize4(), getPixel4());
		setting[4] = new SettingManager(5, getLine5(), getName5(), getConvert5(), getResize5(), getPixel5());
		mainFn.startWork(this, getInLoc(), getOutLoc(), getXsltFile(), setting);
	}

	/**
	 * This function group four components and allow disable or enable group of
	 * buttons and input box
	 * 
	 * @param a
	 *            = boolean for disable or enable
	 */
	private void setAllBtn(Boolean a) {
		outLocTxt.setEnabled(a);
		inLocTxt.setEnabled(a);
		chOutLocBtn.setEnabled(a);
		chInLocBtn.setEnabled(a);
	}

	/**
	 * 
	 * @return the text in input location text box
	 */
	public String getInLoc() {
		if (!inLocTxt.getText().endsWith(File.separator)) {
			return inLocTxt.getText() + File.separator;
		}
		return inLocTxt.getText();
	}

	/**
	 * 
	 * @return the text in output location text box
	 */
	public String getOutLoc() {
		if (!outLocTxt.getText().endsWith(File.separator)) {
			return outLocTxt.getText() + File.separator;
		}
		return outLocTxt.getText();
	}

	/**
	 * 
	 * @return the text in XSLT file selection text box
	 */
	public String getXsltFile() {
		return xsltLocTxt.getText();
	}

	/**
	 * Show report button when both threads are competed without user interrupt
	 */
	public void reportBtnShow() {
		reportButton.setVisible(true);
	}

	private String getName1() {
		if (line1Chk.getState()) {
			return name1Txt.getText();
		} else {
			return null;
		}

	}

	private String getName2() {
		if (line2Chk.getState()) {
			return name2Txt.getText();
		} else {
			return null;
		}
	}

	private String getName3() {
		if (line3Chk.getState()) {
			return name3Txt.getText();
		} else {
			return null;
		}
	}

	private String getName4() {
		if (line4Chk.getState()) {
			return name4Txt.getText();
		} else {
			return null;
		}
	}

	private String getName5() {
		if (line5Chk.getState()) {
			return name5Txt.getText();
		} else {
			return null;
		}

	}

	private String getPixel1() {
		if (convert1Chk.getState()) {
			if (resize1Chk.getState()) {
				return pixel1Txt.getText();
			} else {
				return "0";
			}
		} else {
			return "-1";
		}
	}

	private String getPixel2() {
		if (convert2Chk.getState()) {
			if (resize2Chk.getState()) {
				return pixel2Txt.getText();
			} else {
				return "0";
			}
		} else {
			return "-1";
		}
	}

	private String getPixel3() {
		if (convert3Chk.getState()) {
			if (resize3Chk.getState()) {
				return pixel3Txt.getText();
			} else {
				return "0";
			}
		} else {
			return "-1";
		}
	}

	private String getPixel4() {
		if (convert4Chk.getState()) {
			if (resize4Chk.getState()) {
				return pixel4Txt.getText();
			} else {
				return "0";
			}
		} else {
			return "-1";
		}
	}

	private String getPixel5() {
		if (convert5Chk.getState()) {
			if (resize5Chk.getState()) {
				return pixel5Txt.getText();
			} else {
				return "0";
			}
		} else {
			return "-1";
		}
	}

	private boolean getLine1() {
		return line1Chk.getState();
	}

	private boolean getLine2() {
		return line2Chk.getState();
	}

	private boolean getLine3() {
		return line3Chk.getState();
	}

	private boolean getLine4() {
		return line4Chk.getState();
	}

	private boolean getLine5() {
		return line5Chk.getState();
	}

	private boolean getConvert1() {
		return convert1Chk.getState();
	}

	private boolean getConvert2() {
		return convert2Chk.getState();
	}

	private boolean getConvert3() {
		return convert3Chk.getState();
	}

	private boolean getConvert4() {
		return convert4Chk.getState();
	}

	private boolean getConvert5() {
		return convert5Chk.getState();
	}

	private boolean getResize1() {
		return resize1Chk.getState();
	}

	private boolean getResize2() {
		return resize2Chk.getState();
	}

	private boolean getResize3() {
		return resize3Chk.getState();
	}

	private boolean getResize4() {
		return resize4Chk.getState();
	}

	private boolean getResize5() {
		return resize5Chk.getState();
	}

	public boolean isStart() {
		return isStart;
	}

	public boolean isStop() {
		return isStop;
	}

	public String getmetadataFmt() {
		return metadataFmtTxt.getText();
	}

	public void setFinishBtn() {
		nextButton.setLabel("Finish");
		nextButton.setActionCommand("finish");
	}

	public void noSpaceWarn() {
		isStop = true;
		nextButton.setLabel("Reset");
		JOptionPane.showConfirmDialog(this, "insufficient Disk Space", "Error",
				JOptionPane.CLOSED_OPTION, JOptionPane.ERROR_MESSAGE);
	}

	public boolean isFinish() {
		return isFinish;
	}

}
