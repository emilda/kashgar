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
 * Generates the required descriptive values for monashkashgar metadata for Arrow 
 * as the metadata extracted by exiftool is values only.
 */
import it.tidalwave.imageio.dcr.DCRImageReaderSpi;
import it.tidalwave.imageio.io.FileImageInputStream2;
import it.tidalwave.imageio.nef.NEFImageReaderSpi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifThumbnailDirectory;
/**
 * @author Sindhu Emilda
 * @version v2.0
 */
public class MetadataManager
{
	private Logger logger = Logger.getLogger(this.getClass().getName());
	public boolean isSuccess;
	public String errorMsg;

	MetadataManager() {}

	/**
	 * Checks if the image is raw.
	 * Not used at the moment.
	 * @param fileLoc
	 * @return
	 */
	public static boolean isRaw(String fileLoc) {
		File file = new File(fileLoc);
		try {
			FileImageInputStream2 img = new FileImageInputStream2(file);
			DCRImageReaderSpi dcrimgreaderspi = new DCRImageReaderSpi();
			Boolean isDCR = dcrimgreaderspi.canDecodeInput(img);

			NEFImageReaderSpi nefimgreaderspi = new NEFImageReaderSpi();
			Boolean isNEF = nefimgreaderspi.canDecodeInput(img);

			return (isDCR || isNEF);

		} catch (FileNotFoundException e) {
			// Do nothing
		} catch (IOException e) {
			// Do nothing
		}

		return false;
	}

	/**
	 * This method extracts metadata from the image file using the Java metadata
	 * extractor by drewnoakes. 
	 * Not used at the moment as this library fails to read certain files and the 
	 * metadata returned is not very comprehensive.
	 * @param fileLoc
	 * @return
	 */
	public ArrayList<Tag> getKnownMetadata(String fileLoc) {
		File file = new File(fileLoc);

		Metadata metadata = null;
		ArrayList<Tag> tags = null;
		try {
			metadata = ImageMetadataReader.readMetadata(file);

			tags = new ArrayList<Tag>();
			for (Directory directory : metadata.getDirectories()) {
				for (Tag tag : directory.getTags()) {

					//String directoryName = directory.getName();
					String format = "[%s] %s = %s%n";
					System.out.printf(format, tag.getDirectoryName(), tag.getTagName(), tag.getDescription());
					tags.add(tag);
				}

				// print out any errors
				for (String error : directory.getErrors())
					System.err.println("ERROR: " + error);
			}
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace(System.err);
			logger.info(e);
			errorMsg = e.getMessage();
			isSuccess = false;
		}
		return tags;
	}

	/**
	 * Generates thumbnail using the library from drewnoakes.
	 * Not used at the moment.
	 * @param fileLoc
	 * @throws MetadataException
	 * @throws IOException
	 */
	public static void genThumb(String fileLoc) throws MetadataException, IOException {

		Metadata metadata = null;
		try {
			metadata = ImageMetadataReader.readMetadata(new File(fileLoc));
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		ExifThumbnailDirectory directory = metadata.getDirectory(ExifThumbnailDirectory.class);
		//ExifDirectory directory = (ExifDirectory) metadata.getDirectory(ExifDirectory.class);
		if (directory.hasThumbnailData()) {
			directory.writeThumbnail(fileLoc.trim() + ".thumb.jpg");
		} else {
			System.out.println("No thumbnail data exists in this image");
		}
	}

	/**
	 * Generates the custom metadata tag based on  user input in the interface.
	 * @param format
	 * @param num
	 * @param total
	 * @param name
	 * @return
	 */
	public static MonashTag getCustomMetadataTag(String format, int num, int total, String name)
	{
		String customTag = format;
		customTag = customTag.replace("<x>", Integer.toString(num));
		customTag = customTag.replace("<y>", Integer.toString(total));
		customTag = customTag.replace("<dir>", name);
		return new MonashTag("ImageNumber", customTag);
	}
	
	/**
	 * Get the file related tags as required by Arrow.
	 * @param img_filename
	 * @return
	 */
	public static List<MonashTag> getFileTags(String img_filename) {
		List<MonashTag> fileTags = new ArrayList<MonashTag>();
		
		MonashTag tag = new MonashTag("FileName", UtilFn.getFilename(img_filename));	
		fileTags.add(tag);
		
		tag = new MonashTag("FolderName", UtilFn.Foldername(img_filename));	
		fileTags.add(tag);
		
		tag = new MonashTag("FileSize", UtilFn.getFilesize(img_filename));
		fileTags.add(tag);
		
		return fileTags;
	}

	/**
	 * Get the descriptive test for FileSource required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getFileSourceTxt(String fileSource) {
		try {
			int fsource = Integer.parseInt(fileSource);
			String fileSourceTxt;
			switch (fsource) {
				case 1:  fileSourceTxt = "Film Scanner"; break;
				case 2:  fileSourceTxt = "Reflection Print Scanner"; break;
				case 3:  fileSourceTxt = "Digital Camera"; break;
				default: fileSourceTxt = "NONE"; break;
			}
			return new MonashTag("FileSourceTxt", fileSourceTxt);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for SceneType required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getSceneTypeTxt(String sceneType) {
		try {
			int sType = Integer.parseInt(sceneType);
			String sTypeTxt;
			switch (sType) {
				case 1:  sTypeTxt = "Directly photographed"; break;
				default: sTypeTxt = "NONE"; break;
			}
			return new MonashTag("SceneTypeTxt", sTypeTxt);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for Dimensions required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getPixelDimension(String xDimn, String yDimn) {
		try {
			int x = Integer.parseInt(xDimn);
			int y = Integer.parseInt(yDimn);
			String dimn = x + " x " + y + " pixels";
			return new MonashTag("Dimensions", dimn);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for ExposureProgram required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getExposureProgramTxt(String expPgm) {
		try {
			int ep = Integer.parseInt(expPgm);
			String epTxt;
			switch (ep) {
				case 1:  epTxt = "Manual"; break;
				case 2:  epTxt = "Program AE"; break;
				case 3:  epTxt = "Aperture-priority AE"; break;
				case 4:  epTxt = "Shutter speed priority AE"; break;
				case 5:  epTxt = "Creative (Slow speed)"; break;
				case 6:  epTxt = "Action (High speed)"; break;
				case 7:  epTxt = "Portrait"; break;
				case 8:  epTxt = "Landscape"; break;
				default: epTxt = "Not Defined"; break;
			}
			return new MonashTag("ExposureProgramTxt", epTxt);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for MeteringMode required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getMeteringModeTxt(String mMode) {
		try {
			int mm = Integer.parseInt(mMode);
			String mmTxt;
			switch (mm) {
				case 1:  mmTxt = "Average"; break;
				case 2:  mmTxt = "Center-weighted average"; break;
				case 3:  mmTxt = "Spot"; break;
				case 4:  mmTxt = "Multi-spot"; break;
				case 5:  mmTxt = "Multi-segment"; break;
				case 6:  mmTxt = "Partial"; break;
				case 255:mmTxt = "Other"; break;
				default: mmTxt = "Unknown"; break;
			}
			return new MonashTag("MeteringModeTxt", mmTxt);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for Compression required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getCompressionTxt(String comprsn) {
		try {
			int com = Integer.parseInt(comprsn);
			String comTxt;
			switch (com) {
				case 1:  comTxt = "Uncompressed"; break;
				case 2:  comTxt = "CCITT 1D"; break;
				case 3:  comTxt = "T4/Group 3 Fax"; break;
				case 4:  comTxt = "T6/Group 4 Fax"; break;
				case 5:  comTxt = "LZW"; break;
				case 6:  comTxt = "JPEG (old-style)"; break;
				case 7:  comTxt = "JPEG"; break;
				case 8:  comTxt = "Adobe Deflate"; break;
				case 9:  comTxt = "JBIG B&W"; break;
				case 10: comTxt = "JBIG Color"; break;
				case 99: comTxt = "JPEG"; break;
				case 262:comTxt = "Kodak 262"; break;
				case 32766:  comTxt = "Next"; break;
				case 32767:  comTxt = "Sony ARW Compressed"; break;
				case 32769:  comTxt = "Packed RAW"; break;
				case 32770:  comTxt = "Samsung SRW Compressed"; break;
				case 32771:  comTxt = "CCIRLEW"; break;
				case 32773:  comTxt = "PackBits"; break;
				case 32809:  comTxt = "Thunderscan"; break;
				case 32867:  comTxt = "Kodak KDC Compressed"; break;
				case 32895:  comTxt = "IT8CTPAD"; break;
				case 32896:  comTxt = "IT8LW"; break;
				case 32897:  comTxt = "IT8MP"; break;
				case 32898:  comTxt = "IT8BL"; break;
				case 32908:  comTxt = "PixarFilm"; break;
				case 32909:  comTxt = "PixarLog"; break;
				case 32946:  comTxt = "Deflate"; break;
				case 32947:  comTxt = "DCS"; break;
				case 34661:  comTxt = "JBIG"; break;
				case 34676:  comTxt = "SGILog"; break;
				case 34677:  comTxt = "SGILog24"; break;
				case 34712:  comTxt = "JPEG 2000"; break;
				case 34713:  comTxt = "Nikon NEF Compressed"; break;
				case 34715:  comTxt = "JBIG2 TIFF FX"; break;
				case 34718:  comTxt = "Microsoft Document Imaging (MDI) Binary Level Codec"; break;
				case 34719:  comTxt = "Microsoft Document Imaging (MDI) Progressive Transform Codec"; break;
				case 34720:  comTxt = "Microsoft Document Imaging (MDI) Vector"; break;
				case 65000:  comTxt = "Kodak DCR Compressed"; break;
				case 65535:  comTxt = "Pentax PEF Compressed"; break;
				default:  comTxt = "Unknown"; break;
			}
			return new MonashTag("CompressionTxt", comTxt);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for ExposureMode required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getExposureModeTxt(String emode) {
		try {
			int em = Integer.parseInt(emode);
			String emTxt;
			switch (em) {
				case 0:  emTxt = "Auto"; break;
				case 1:  emTxt = "Manual"; break;
				case 2:  emTxt = "Auto bracket"; break;
				default: emTxt = "Not Defined"; break;
			}
			return new MonashTag("ExposureModeTxt", emTxt);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for CustomRendered required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getCustomRenderedTxt(String type) {
		try {
			int cr = Integer.parseInt(type);
			String crText;
			switch (cr) {
				case 0:  crText = "Normal"; break;
				case 1:  crText = "Custom"; break;
				default: crText = "Not Defined"; break;
			}
			return new MonashTag("CustomRenderedTxt", crText);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for Contrast required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getContrastTxt(String type) {
		try {
			int cr = Integer.parseInt(type);
			String crText;
			switch (cr) {
				case 0:  crText = "Normal"; break;
				case 1:  crText = "Low"; break;
				case 2:  crText = "High"; break;
				default: crText = "Not Defined"; break;
			}
			return new MonashTag("ContrastTxt", crText);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for PhotometricInterpretation required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getPhotometricInterpretationTxt(String type) {
		try {
			int pi = Integer.parseInt(type);
			String piTxt;
			switch (pi) {
				case 0:  piTxt = "WhiteIsZero"; break;
				case 1:  piTxt = " BlackIsZero"; break;
				case 2:  piTxt = "RGB"; break;
				case 3:  piTxt = "RGB Palette "; break;
				case 4:  piTxt = "Transparency Mask"; break;
				case 5:  piTxt = "CMYK"; break;
				case 6:  piTxt = "YCbCr"; break;
				case 8:  piTxt = "CIELab"; break;
				case 9:  piTxt = "ICCLab"; break;
				case 10:  piTxt = "ITULab"; break;
				case 32803:  piTxt = "Color Filter Array"; break;
				case 32844:  piTxt = "Pixar LogL"; break;
				case 32845:  piTxt = "Pixar LogLuv"; break;
				case 34892:  piTxt = "Linear Raw"; break;
				default: piTxt = "Unknown"; break;
			}
			return new MonashTag("PhotometricInterpretationTxt", piTxt);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for PlanarConfiguration required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getPlanarConfigurationTxt(String type) {
		try {
			int pc = Integer.parseInt(type);
			String pcText;
			switch (pc) {
				case 1:  pcText = "Chunky"; break;
				case 2:  pcText = "Planar"; break;
				default: pcText = "Not Defined"; break;
			}
			return new MonashTag("PlanarConfigurationTxt", pcText);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for ResolutionUnit required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getResolutionUnitTxt(String type) {
		try {
			int pc = Integer.parseInt(type);
			String pcText;
			switch (pc) {
				case 2:  pcText = "inches"; break;
				case 3:  pcText = "cm"; break;
				default: pcText = "None"; break;
			}
			return new MonashTag("ResolutionUnitTxt", pcText);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for SensingMethod required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getSensingMethodTxt(String type) {
		try {
			int sm = Integer.parseInt(type);
			String smTxt;
			switch (sm) {
				case 1:  smTxt = "Monochrome area"; break;
				case 2:  smTxt = "One-chip color area"; break;
				case 3:  smTxt = "Two-chip color area"; break;
				case 4:  smTxt = "Three-chip color area"; break;
				case 5:  smTxt = "Color sequential area"; break;
				case 6:  smTxt = "Monochrome linear"; break;
				case 7:  smTxt = "Trilinear"; break;
				case 8:  smTxt = "Color sequential linear"; break;
				default: smTxt = "Unknown"; break;
			}
			return new MonashTag("SensingMethodTxt", smTxt);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for SceneCaptureType required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getSceneCaptureTypeTxt(String type) {
		try {
			int sc = Integer.parseInt(type);
			String scTxt;
			switch (sc) {
				case 0:  scTxt = "Standard"; break;
				case 1:  scTxt = "Landscape"; break;
				case 2:  scTxt = "Portrait"; break;
				case 3:  scTxt = "Night"; break;
				default: scTxt = "Unknown"; break;
			}
			return new MonashTag("SceneCaptureTypeTxt", scTxt);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for GainControl required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getGainControlTxt(String type) {
		try {
			int gc = Integer.parseInt(type);
			String gcTxt;
			switch (gc) {
				case 1:  gcTxt = "Low gain up"; break;
				case 2:  gcTxt = "High gain up"; break;
				case 3:  gcTxt = "Low gain down"; break;
				case 4:  gcTxt = "High gain down"; break;
				default: gcTxt = "None"; break;
			}
			return new MonashTag("GainControlTxt", gcTxt);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for Saturation required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getSaturationTxt(String type) {
		try {
			int s = Integer.parseInt(type);
			String sText;
			switch (s) {
			case 0:  sText = "Normal"; break;
			case 1:  sText = "Low"; break;
			case 2:  sText = "High"; break;
			default: sText = "Not Defined"; break;
			}
			return new MonashTag("SaturationTxt", sText);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for Sharpness required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getSharpnessTxt(String type) {
		try {
			int s = Integer.parseInt(type);
			String sText;
			switch (s) {
			case 0:  sText = "Normal"; break;
			case 1:  sText = "Soft"; break;
			case 2:  sText = "Hard"; break;
			default: sText = "Not Defined"; break;
			}
			return new MonashTag("SharpnessTxt", sText);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the descriptive test for SubjectDistanceRange required by Arrow.
	 * @param type
	 * @return
	 */
	public static MonashTag getSubjectDistanceRangeTxt(String type) {
		try {
			int sdr = Integer.parseInt(type);
			String sdrText;
			switch (sdr) {
			case 0:  sdrText = "Unknown"; break;
			case 1:  sdrText = "Macro"; break;
			case 2:  sdrText = "Close"; break;
			case 3:  sdrText = "Distant"; break;
			default: sdrText = "Not Defined"; break;
			}
			return new MonashTag("SubjectDistanceRangeTxt", sdrText);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}