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
		seenby = new ArrayList<FtnAddress>();
		StringBuilder _path = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is,
				"CP866"));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.toLowerCase().startsWith("file ")) {
				file = line.replaceFirst("[Ff][Ii][Ll][Ee] ", "");
			} else if (line.toLowerCase().startsWith("area ")) {
				area = line.replaceFirst("[Aa][Rr][Ee][Aa] ", "");
			} else if (line.toLowerCase().startsWith("desc ")) {
				desc = line.replaceFirst("[Dd][Ee][Ss][Cc] ", "");
			} else if (line.toLowerCase().startsWith("areadesc ")) {
				areaDesc = line.replaceFirst(
						"[Aa][Rr][Ee][Aa][Dd][Ee][Ss][Cc] ", "");
			} else if (line.toLowerCase().startsWith("pw ")) {
				password = line.replaceFirst("[Pp][Ww] ", "");
			} else if (line.toLowerCase().startsWith("from ")) {
				from = new FtnAddress(line.replaceFirst("[Ff][Rr][Oo][Mm] ", ""));
			} else if (line.toLowerCase().startsWith("to ")) {
				to = new FtnAddress(line.replaceFirst("[Tt][Oo] ", ""));
			} else if (line.toLowerCase().startsWith("origin ")) {
				origin = new FtnAddress(line.replaceFirst(
						"[Oo][Rr][Ii][Gg][Ii][Nn] ", ""));
			} else if (line.toLowerCase().startsWith("size ")) {
				try {
					size = Long.valueOf(line.replaceFirst("[Ss][Ii][Zz][Ee] ",
							""));
				} catch (NumberFormatException e) {
					size = 0L;
				}
			} else if (line.toLowerCase().startsWith("path ")) {
				_path.append(line);
				_path.append("\r\n");
			} else if (line.toLowerCase().startsWith("seenby ")) {
				seenby.add(new FtnAddress(line.replaceFirst(
						"[Ss][Ee][Ee][Nn][Bb][Yy] ", "")));
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