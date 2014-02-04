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

package jnode.ftn.types;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class FtnTIC {
	private FtnAddress from;
	private FtnAddress to;
	private FtnAddress origin;
	private String area;
	private String areaDesc;
	private String file;
	private String desc;
	private Long size;
	private List<FtnAddress> seenby;
	private String path;
	private String password;
	private String realpath;
	private Long crc32;

	public void unpack(InputStream is) throws IOException {
		seenby = new ArrayList<>();
		StringBuilder _path = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is,
				"CP866"));
		String line;
		desc = "-- Description Missing --";
		areaDesc = "-- Description Missing --";
		size = 0L;
		while ((line = reader.readLine()) != null) {
			if (line.matches("^[Ff][Ii][Ll][Ee]:? .*$")) {
				file = line.replaceFirst("[Ff][Ii][Ll][Ee]:? ", "");
			} else if (line.matches("^[Aa][Rr][Ee][Aa]:? .*$")) {
				area = line.replaceFirst("[Aa][Rr][Ee][Aa]:? ", "");
			} else if (line.matches("^[Dd][Ee][Ss][Cc]:? .*$")) {
				desc = line.replaceFirst("[Dd][Ee][Ss][Cc]:? ", "");
			} else if (line.matches("^[Aa][Rr][Ee][Aa][Dd][Ee][Ss][Cc]:? .*$")) {
				areaDesc = line.replaceFirst(
						"[Aa][Rr][Ee][Aa][Dd][Ee][Ss][Cc]:? ", "");
			} else if (line.matches("^[Pp][Ww]:? .*$")) {
				password = line.replaceFirst("[Pp][Ww]:? ", "");
			} else if (line.matches("^[Ff][Rr][Oo][Mm]:? .*$")) {
				from = new FtnAddress(line.replaceFirst("[Ff][Rr][Oo][Mm]:? ",
						""));
			} else if (line.matches("^[Tt][Oo]:? .*$")) {
				to = new FtnAddress(line.replaceFirst("[Tt][Oo]:? ", ""));
			} else if (line.matches("^[Oo][Rr][Ii][Gg][Ii][Nn]:? .*$")) {
				origin = new FtnAddress(line.replaceFirst(
						"[Oo][Rr][Ii][Gg][Ii][Nn]:? ", ""));
			} else if (line.matches("^[Ss][Ii][Zz][Ee]:? .*$")) {
				try {
					size = Long.valueOf(line.replaceFirst(
							"[Ss][Ii][Zz][Ee]:? ", ""));
				} catch (NumberFormatException e) {
					size = 0L;
				}
			} else if (line.matches("^[Pp][Aa][Tt][Hh]:? .*$")) {
				_path.append(line);
				_path.append("\r\n");
			} else if (line.matches("^[Ss][Ee][Ee][Nn][Bb][Yy]:? .*$")) {
				seenby.add(new FtnAddress(line.replaceFirst(
						"[Ss][Ee][Ee][Nn][Bb][Yy]:? ", "")));
			}
		}
		path = _path.toString();
		reader.close();
	}

	public byte[] pack() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(bos,
				"CP866"));
		writer.write("Created by jNode, written by Ivan Agarkov\r\n");
		writer.write("File " + file + "\r\n");
		writer.write("Area " + area + "\r\n");
		if (areaDesc != null) {
			writer.write("Areadesc " + areaDesc + "\r\n");
		}
		if (desc != null) {
			writer.write("Desc " + desc + "\r\n");
		}
		writer.write("From " + from + "\r\n");
		writer.write("To " + to + "\r\n");
		writer.write("Origin " + origin + "\r\n");
		writer.write("Size " + size + "\r\n");
		writer.write(String.format("CRC %08x\r\n", crc32).toUpperCase());
		writer.write(path);
		for (FtnAddress addr : seenby) {
			writer.write("Seenby " + addr.toString() + "\r\n");
		}
		writer.write("Pw " + password + "\r\n");
		writer.close();
		return bos.toByteArray();
	}

	public FtnAddress getFrom() {
		return from;
	}

	public void setFrom(FtnAddress from) {
		this.from = from;
	}

	public FtnAddress getTo() {
		return to;
	}

	public void setTo(FtnAddress to) {
		this.to = to;
	}

	public FtnAddress getOrigin() {
		return origin;
	}

	public void setOrigin(FtnAddress origin) {
		this.origin = origin;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getAreaDesc() {
		return areaDesc;
	}

	public void setAreaDesc(String areaDesc) {
		this.areaDesc = areaDesc;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public List<FtnAddress> getSeenby() {
		return seenby;
	}

	public void setSeenby(List<FtnAddress> seenby) {
		this.seenby = seenby;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRealpath() {
		return realpath;
	}

	public void setRealpath(String realpath) {
		this.realpath = realpath;
	}

	public Long getCrc32() {
		return crc32;
	}

	public void setCrc32(Long crc32) {
		this.crc32 = crc32;
	}

	@Override
	public String toString() {
		return "FtnTIC [from=" + from + ", to=" + to + ", origin=" + origin
				+ ", area=" + area + ", areaDesc=" + areaDesc + ", file="
				+ file + ", desc=" + desc + ", size=" + size + ", seenby="
				+ seenby + ", path=" + path + ", password=" + password + "]";
	}

}
