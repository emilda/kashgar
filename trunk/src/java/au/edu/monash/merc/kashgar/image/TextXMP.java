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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPIterator;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.XMPSchemaRegistry;
import com.adobe.xmp.options.PropertyOptions;
import com.adobe.xmp.properties.XMPPropertyInfo;

public class TextXMP {

	private static final String LOG_PREFIX = "[XMP File] ";
	private static final String KASGAR_NS = "http://monash.edu.au/merc/kashgar/2011/monashkashgar";
	private static final String EXIF_NS = "http://ns.adobe.com/exif/1.0/";
	private static final String TIFF_NS = "http://ns.adobe.com/tiff/1.0/";
	private static final String PDF_NS = "http://ns.adobe.com/pdf/1.3/";
	private static final String PURL_NS = "http://purl.org/dc/elements/1.1/";
	
	public static void main(String args[]) throws FileNotFoundException, XMPException {
		XMPMeta xmpMeta = XMPMetaFactory.create();
		XMPIterator iterator = xmpMeta.iterator();
		iterator.hasNext();

		XMPSchemaRegistry registry = XMPMetaFactory.getSchemaRegistry();
		//meta.setProperty(registry.getNamespaceURI("exif"), "ExposureIndex", "testVal");
		registry.registerNamespace(KASGAR_NS, "monashkashgar");
		String namespaceURI = registry.getNamespaceURI("monashkashgar");
		
		xmpMeta.setProperty(namespaceURI, "Caption", "Testing");
		
		PropertyOptions options = new PropertyOptions(PropertyOptions.ARRAY);
		PropertyOptions no_options = new PropertyOptions(PropertyOptions.NO_OPTIONS);
		
		String keywords = "dim,ems";
		String[] keyword = keywords.split(",");
		for(int i=0; i < keyword.length; i++) {
			if (i == 0) {
				xmpMeta.appendArrayItem(namespaceURI, "Keywords", options, keyword[i], no_options);
			} else {
				xmpMeta.appendArrayItem(namespaceURI, "Keywords", keyword[i]);
			}
		}
		
		String[] gps = getGPSCoordinates("-34.237782,140.556992");
		xmpMeta.setProperty(namespaceURI, "GPSLatitude", gps[0]);
		xmpMeta.setProperty(namespaceURI, "GPSLongitude", gps[1]);
		
		System.out.println(xmpMeta.dumpObject());
		//System.out.println(registry.getNamespaces());

		File file = new File("testXML.xmp");
		FileOutputStream out = new FileOutputStream(file);
		XMPMetaFactory.serialize(xmpMeta, out);
	}

	/**
	 * This method is used to create monashkashgar elements. This element gives descriptive 
	 * information for standard tags like Exif (which is given as values). It also creates 
	 * elements which are required for the creation of MODS or DC metadata for inclusion in 
	 * the repository that is not included in the XMP standard. 
	 * 
	 * @param abs_path_name	path to XMP file previously created with metadata from the image
	 * @param monash_tags	List of elements to be added ad monashkashgar element
	 */
	public static void writeMonashTags(String abs_path_name, List<MonashTag> monash_tags) throws XMPException, IOException {
		UtilFn.log(LOG_PREFIX, "Add monashkashgar tags to %s", abs_path_name);
		
		FileInputStream fis = new FileInputStream(abs_path_name);
		XMPMeta xmpMeta = XMPMetaFactory.parse(fis);
		fis.close();
		
		XMPSchemaRegistry registry = XMPMetaFactory.getSchemaRegistry();
		registry.registerNamespace(KASGAR_NS, "monashkashgar");
		String namespaceURI = registry.getNamespaceURI("monashkashgar");
		
		String type = xmpMeta.getPropertyString(EXIF_NS, "FileSource");
		MonashTag tag = MetadataManager.getFileSourceTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		// Add the unit of ExposureTime
		type = xmpMeta.getPropertyString(EXIF_NS, "ExposureTime");
		if (null != type) {
			type += " sec";
			tag = new MonashTag("ExposureTime", type);
			monash_tags.add(tag);
		}
				
		// Convert rational number to decimal and add unit to it
		type = xmpMeta.getPropertyString(EXIF_NS, "FocalLength");
		if (null != type) {
			type = UtilFn.divide(type) + " mm";
			tag = new MonashTag("FocalLength", type);
			monash_tags.add(tag);
		}

		type = xmpMeta.getPropertyString(EXIF_NS, "FNumber");
		if (null != type) {
			type = "F" + UtilFn.divide(type);
			tag = new MonashTag("FNumber", type);
			monash_tags.add(tag);
		}
		type = xmpMeta.getPropertyString(EXIF_NS, "ApertureValue");
		if (null != type) {
			type = "F" + UtilFn.divide(type);
			tag = new MonashTag("ApertureValue", type);
			monash_tags.add(tag);
		}
		
		// Convert rational number to decimal and add unit to it
		type = xmpMeta.getPropertyString(EXIF_NS, "ExposureBiasValue");
		if (null != type) {
			type = UtilFn.divide(type) + " EV";
			tag = new MonashTag("ExposureBiasValue", type);
			monash_tags.add(tag);
		}
				
		type = xmpMeta.getPropertyString(EXIF_NS, "SceneType");
		tag = MetadataManager.getSceneTypeTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		String xDimn = xmpMeta.getPropertyString(TIFF_NS, "ImageWidth");
		String yDimn = xmpMeta.getPropertyString(TIFF_NS, "ImageLength");
		tag = MetadataManager.getPixelDimension(xDimn, yDimn);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(EXIF_NS, "ExposureProgram");
		tag = MetadataManager.getExposureProgramTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(EXIF_NS, "MeteringMode");
		tag = MetadataManager.getMeteringModeTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(TIFF_NS, "Compression");
		tag = MetadataManager.getCompressionTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(EXIF_NS, "ExposureMode");
		tag = MetadataManager.getExposureModeTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(EXIF_NS, "CustomRendered");
		tag = MetadataManager.getCustomRenderedTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(EXIF_NS, "Contrast");
		tag = MetadataManager.getContrastTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(TIFF_NS, "PhotometricInterpretation");
		tag = MetadataManager.getPhotometricInterpretationTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(TIFF_NS, "PlanarConfiguration");
		tag = MetadataManager.getPlanarConfigurationTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(TIFF_NS, "ResolutionUnit");
		tag = MetadataManager.getResolutionUnitTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(EXIF_NS, "SensingMethod");
		tag = MetadataManager.getSensingMethodTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(EXIF_NS, "SceneCaptureType");
		tag = MetadataManager.getSceneCaptureTypeTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(EXIF_NS, "GainControl");
		tag = MetadataManager.getGainControlTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(EXIF_NS, "Saturation");
		tag = MetadataManager.getSaturationTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(EXIF_NS, "Sharpness");
		tag = MetadataManager.getSharpnessTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		type = xmpMeta.getPropertyString(EXIF_NS, "SubjectDistanceRangeTxt");
		tag = MetadataManager.getSubjectDistanceRangeTxt(type);
		if (null != tag) monash_tags.add(tag);
		
		boolean keyword = false;
		boolean caption = false;
		
		PropertyOptions options = new PropertyOptions(PropertyOptions.ARRAY);
		PropertyOptions no_options = new PropertyOptions(PropertyOptions.NO_OPTIONS);
		
		for (MonashTag mtag : monash_tags) {
			UtilFn.log(LOG_PREFIX, "%s = %s", mtag.getName(), mtag.getDescription());
			String name = mtag.getName();
			if (name.equals("Keywords")) {
				keyword = true;
				
				String[] token = mtag.getDescription().split(",");
				for(int i=0; i < token.length; i++) {
					if (i == 0) {
						xmpMeta.appendArrayItem(namespaceURI, "Keywords", options, token[i], no_options);
					} else {
						xmpMeta.appendArrayItem(namespaceURI, "Keywords", token[i]);
					}
				}
			} else if (name.equals("Caption")) {
				caption = true;
				xmpMeta.setProperty(namespaceURI, name, mtag.getDescription());
			} else if (name.equals("GPS")) {
				String[] gps = getGPSCoordinates(mtag.getDescription());
				xmpMeta.setProperty(namespaceURI, "GPSLatitude", gps[0]);
				xmpMeta.setProperty(namespaceURI, "GPSLongitude", gps[1]);
			} else {
				xmpMeta.setProperty(namespaceURI, name, mtag.getDescription());
			}
		}
		if (!keyword) {
			String keywords = xmpMeta.getPropertyString(PDF_NS, "Keywords");
			
			if (null != keywords) {
				String[] token = keywords.split(",");
				for(int i=0; i < token.length; i++) {
					if (i == 0) {
						xmpMeta.appendArrayItem(namespaceURI, "Keywords", options, token[i], no_options);
					} else {
						xmpMeta.appendArrayItem(namespaceURI, "Keywords", token[i]);
					}
				}
			}
		}
		if (!caption) {
			String Caption = xmpMeta.getPropertyString(PURL_NS, "description[1]");
			if (null != Caption) {
				xmpMeta.setProperty(namespaceURI, "Caption", Caption);
			}
		}
		
		OutputStream out = new FileOutputStream(abs_path_name);
		XMPMetaFactory.serialize(xmpMeta, out);
		out.close();
	}

	private static String[] getGPSCoordinates(String coordinates) {
		String[] gps = coordinates.split(",");
		String latitude;
		String longitude;
		if (gps[0].startsWith("-")) {
			latitude = gps[0].substring(1) + "S";
		} else {
			latitude = gps[0] + "N";
		}
		if (gps[1].startsWith("-")) {
			longitude = gps[1].substring(1) + "W";
		} else {
			longitude = gps[1] + "E";
		}
		return new String[] {latitude, longitude};
	}

	private static void printXMPMeta(XMPMeta xmpMeta) throws XMPException {
		
		XMPIterator itr = xmpMeta.iterator();
		
		while (itr.hasNext()) {
			XMPPropertyInfo item = (XMPPropertyInfo) itr.next();	
			UtilFn.log(LOG_PREFIX, "%s, %s = %s", item.getNamespace(), item.getPath(), item.getValue(), item.getOptions().getOptionsString());
		}
		
		//System.out.println(xmpMeta.dumpObject());
		//property = meta.getProperty(NS1, "Bag");   // Seq, Alt, Struct
	}
	
}
