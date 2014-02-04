/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jnode.pointchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jnode.dto.Filearea;
import jnode.ftn.FtnTools;
import jnode.jscript.IJscriptHelper;

public class PointListHelper extends IJscriptHelper {

	private File createPointList(String seg, String dir, String header,
			String footer) {
		File fdir = new File(dir);
		if (!(fdir.isDirectory() && fdir.canRead())) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("; Maked by jnode-pointchecker-module-1.0\r\n");
		sb.append("; (C) Ivan Agarkov, 2014\r\n");
		if (header != null && header.length() > 0) {
			sb.append(getFileContents(new File(header)));
		}

		for (File file : fdir.listFiles()) {
			if (file.isFile()) {
				sb.append("; file " + file.getName() + "\r\n");
				sb.append(getFileContents(file));
				sb.append("\r\n");
			}
		}

		if (footer != null && footer.length() > 0) {
			sb.append(getFileContents(new File(footer)));
		}

		try {
			String str = sb.toString().replaceAll("\r\n", "\n")
					.replaceAll("\r", "").replaceAll("\n{1,}", "\n")
					.replaceAll("\n", "\r\n");
			File ret = File.createTempFile("zip", "pnt");
			FileOutputStream fos = new FileOutputStream(ret);
			ZipOutputStream zos = new ZipOutputStream(fos);
			zos.putNextEntry(new ZipEntry(seg));
			zos.write(str.toString().getBytes("CP866"));
			zos.closeEntry();
			zos.close();
			fos.close();
			return ret;
		} catch (IOException e) {
		}
		return null;
	}

	private String getFileContents(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] buf = new byte[(int) file.length()];
			fis.read(buf);
			fis.close();
			return new String(buf, "CP866");
		} catch (IOException e) {
		}
		return "";
	}

	/**
	 * Отправить файл в арию
	 * 
	 * @param areaName
	 * @param zip
	 * @param seg
	 * @param dir
	 * @param header
	 * @param footer
	 */
	public void hatchToArea(String areaName, String zip, String seg,
			String dir, String header, String footer) {
		Filearea area = FtnTools.getFileareaByName(areaName, null);
		File attachment = createPointList(getFilename(seg), dir, header, footer);
		FtnTools.hatchFile(
				area,
				attachment,
				getFilename(zip),
				"Pointlist, day "
						+ Calendar.getInstance().get(Calendar.DAY_OF_YEAR));

	}

	@Override
	public Version getVersion() {
		return new Version() {

			@Override
			public int getMinor() {
				return 1;
			}

			@Override
			public int getMajor() {
				return 2;
			}
		};
	}

	/**
	 * zxx xxx итд
	 * 
	 * @param template
	 * @return
	 */
	private String getFilename(String template) {
		Calendar cal = Calendar.getInstance();
		int dayXXX = cal.get(Calendar.DAY_OF_YEAR);
		int dayXX = dayXXX % 100;
		int dayX = dayXX % 10;
		String ret = template;
		if (template.endsWith("xxx")) {
			ret = template.replaceAll("xxx$", String.format("%03d", dayXXX));
		} else if (template.endsWith("xx")) {
			ret = template.replaceAll("xx$", String.format("%02d", dayXX));
		} else if (template.endsWith("x")) {
			ret = template.replaceAll("x$", String.format("%01d", dayX));
		}
		return ret;
	}

}
