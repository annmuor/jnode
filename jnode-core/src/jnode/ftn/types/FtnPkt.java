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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import jnode.core.ConcurrentDateFormatAccess;
import jnode.ftn.FtnTools;
import jnode.ftn.exception.LastMessageException;
import jnode.install.DefaultVersion;
import jnode.logger.Logger;

/**
 * 
 * @author kreon
 * 
 */
public class FtnPkt {
	private static final Logger logger = Logger.getLogger(FtnMessage.class);
	private FtnAddress fromAddr;
	private FtnAddress toAddr;
	private String password;
	private Date date;
	private InputStream is;
	private boolean close;
	private static final ConcurrentDateFormatAccess FORMAT = new ConcurrentDateFormatAccess(
			"yyyy MM dd HH mm ss", Locale.US);

	public FtnAddress getFromAddr() {
		return fromAddr;
	}

	public FtnAddress getToAddr() {
		return toAddr;
	}

	public String getPassword() {
		return password;
	}

	public Date getDate() {
		return date;
	}

	public FtnPkt() {
	}

	public FtnPkt(FtnAddress fromAddr, FtnAddress toAddr, String password,
			Date date) {
		super();
		this.fromAddr = fromAddr;
		this.toAddr = toAddr;
		this.password = password;
		this.date = date;
	}

	public byte[] pack() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		write(bos);
		try {
			bos.close();
		} catch (IOException e) {
		}
		return bos.toByteArray();
	}

	public void write(OutputStream fos) {
		DataOutputStream os = new DataOutputStream(fos);
		try {
			os.writeShort(FtnTools.revShort(fromAddr.getNode()));
			os.writeShort(FtnTools.revShort(toAddr.getNode()));
			String date = FORMAT.format(this.date); // here
			{
				int n = 0;
				for (String d : date.split(" ")) {
					short s = new Short(d);
					if (n == 1) {
						s--;
					}
					os.writeShort(FtnTools.revShort(s));
					n++;
				}
			}
			os.write(new byte[] { 0, 0, 2, 0 });
			os.writeShort(FtnTools.revShort(fromAddr.getNet()));
			os.writeShort(FtnTools.revShort(toAddr.getNet()));
			os.write(new byte[] { (byte) 255,
					DefaultVersion.getSelf().getMajorVersion().byteValue() }); // prodcode
																				// 19FF
																				// ver
																				// 1.0
			os.write(FtnTools.substr(password, 8));
			for (int i = password.length(); i < 8; i++) {
				os.write(0);
			}
			os.writeShort(FtnTools.revShort(fromAddr.getZone()));
			os.writeShort(FtnTools.revShort(toAddr.getZone()));
			os.write(new byte[] { 0, 0, 0, 1, 19,
					DefaultVersion.getSelf().getMinorVersion().byteValue(), 1,
					0 });// prodcode 19FF
			// ver 1.5
			os.writeShort(FtnTools.revShort(fromAddr.getZone()));
			os.writeShort(FtnTools.revShort(toAddr.getZone()));
			os.writeShort(FtnTools.revShort(fromAddr.getPoint()));
			os.writeShort(FtnTools.revShort(toAddr.getPoint()));
			os.write(new byte[] { 0, 0, 0, 0 });
		} catch (IOException e) {
			//
		}
	}

	public void finalz(OutputStream fos) {
		try {
			fos.write(new byte[] { 0, 0 });
			fos.close();
		} catch (IOException e) {
			logger.l2("fail finalz", e);
		}
	}

	public byte[] finalz() {
		return new byte[] { 0, 0 };
	}

	public void unpack(InputStream iz) throws IOException {
		unpack(iz, true);
	}

	public void unpack(InputStream iz, boolean close) throws IOException {
		this.is = iz;
		this.close = close;
		DataInputStream is = new DataInputStream(iz);
		fromAddr = new FtnAddress();
		toAddr = new FtnAddress();
		fromAddr.setNode(FtnTools.revShort(is.readShort()));
		toAddr.setNode(FtnTools.revShort(is.readShort()));
		{
			short date[] = new short[6];
			for (int i = 0; i < 6; i++) {
				date[i] = FtnTools.revShort(is.readShort());
			}
			try {
				Calendar calendar = Calendar.getInstance();
				calendar.set(date[0], date[1], date[2], date[3], date[4],
						date[5]);
				this.date = calendar.getTime();
			} catch (Exception e) {
				this.date = new Date(0);
			}
		}
		is.skip(4);
		fromAddr.setNet(FtnTools.revShort(is.readShort()));
		toAddr.setNet(FtnTools.revShort(is.readShort()));
		is.skip(2);
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			for (int i = 0; i < 8; i++) {
				int c = is.read();
				if (c != 0) {
					bos.write(c);
				}
			}
			bos.close();
			password = new String(bos.toByteArray());
		}
		is.skip(12);
		fromAddr.setZone(FtnTools.revShort(is.readShort()));
		toAddr.setZone(FtnTools.revShort(is.readShort()));
		fromAddr.setPoint(FtnTools.revShort(is.readShort()));
		toAddr.setPoint(FtnTools.revShort(is.readShort()));
		is.skip(4);
	}

	public FtnMessage getNextMessage() {
		try {
			FtnMessage mess = new FtnMessage();
			mess.unpack(is);
			mess.pkt = this;
			return mess;
		} catch (LastMessageException e) {
			if (close) {
				try {
					is.close();
				} catch (IOException ignore) {
				}
			}
			return null;
		}
	}

	@Override
	public String toString() {
		return String.format(
				"PKT From: %s\nPKT To: %s\nPKT Date: %s\nPKT Password: %s\n",
				fromAddr, toAddr, date, password);
	}
}
