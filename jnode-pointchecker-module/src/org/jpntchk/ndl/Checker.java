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

package org.jpntchk.ndl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnode.ftn.FtnTools;
import jnode.ftn.types.FtnAddress;
import jnode.ndl.FtnNdlAddress;
import jnode.ndl.NodelistScanner;

/**
 * 
 * @author kreon
 * 
 */
public enum Checker {
	INSTANCE;
	private StringBuffer errors = new StringBuffer();
	private List<FtnNdlAddress> bosses = new ArrayList<>();
	private String currentFileName = "";

	public String getCurrentFileName() {
		return currentFileName;
	}

	public void setCurrentFileName(String currentFileName) {
		this.currentFileName = currentFileName;
	}

	public String getErrors() {
		return errors.toString();
	}

	public void clean() {
		errors.delete(0, errors.length());
	}

	public void addError(int linenum, String msg) {
		String error = "Line: " + linenum + " error : " + msg + "\n";
		errors.append(error);
	}

	public boolean check(byte[] data, boolean multi, String name) {
		bosses.clear();
		List<Long> points = new ArrayList<>();
		Pattern pBoss = Pattern.compile("^Boss,([0-9]:\\d{1,5}/\\d{1,5})$");
		Pattern pPoint = Pattern
				.compile("^Point,(\\d+),(\\S+),(\\S+),(\\S+),(\\S+),(\\d+),(\\S*)$");
		String[] lines = new String(data).replaceAll("\n", "").split("\r");
		int linenum = 0;
		int _points = 0;
		boolean bossnotfound = false;
		for (String line : lines) {
			linenum++;
			if (line.startsWith(";")) {
				if (multi || bosses.isEmpty()) {
					continue;
				} else {
					addError(linenum,
							"No multi pointlist, comment after boss string");
				}
				continue;
			}
			Matcher m = pBoss.matcher(line);
			if (m.matches()) {
				FtnNdlAddress boss = NodelistScanner.getInstance().isExists(
						new FtnAddress(m.group(1)));
				if (boss == null) {
					addError(linenum, line + " not found in nodelist\n");
					bossnotfound = true;
				} else {
					if (multi || bosses.isEmpty()) {
						if (bosses.contains(m.group(1))) {
							addError(linenum, line
									+ " already exists in pointlist");
							bossnotfound = true;
						} else {
							bosses.add(boss);
							points.clear();
							bossnotfound = false;
						}
					} else {
						addError(linenum,
								"Not multi pointlist, next boss found\n");
					}
					continue;
				}
				m = pPoint.matcher(line);
				if (m.matches()) {
					if (bosses.isEmpty()) {
						addError(linenum,
								"Point string present, but no boss present before");
					} else {
						Long point = Long.valueOf(m.group(1));
						if (points.contains(point)) {
							if (bossnotfound) {
								addError(linenum,
										"Point for boss, thats not found in nodelist");
							} else {
								addError(linenum,
										"Point " + point
												+ " already exists for "
												+ bosses.get(bosses.size() - 1));
							}
						} else {
							String flags = m.group(7);
							if (flags != null && checkflags(flags, linenum)) {
								points.add(point);
								_points++;
							}
						}
					}
					continue;
				}
				addError(linenum, "Unknown line: " + line);
			}
		}
		boolean isReg = false;
		boolean isNet = false;
		if (multi && bosses.size() > 1) {
			// TODO
		}
		// create netmail :-)
		boolean success = (errors.length() == 0);
		String subject = (success) ? "Segment checked : OK"
				: "Segment checked: Errors";
		String text = "File: " + currentFileName + "\nDate: "
				+ new Date().toString() + "\n" + "Lines: " + linenum + "\n"
				+ "Flags: "
				+ ((isReg) ? "regional" : (isNet) ? "net" : "local") + "\n"
				+ "Boss lines: " + bosses.size() + "\n" + "Point lines: "
				+ _points + "\n";
		if (!success)
			text += errors.toString();
		for (FtnNdlAddress boss : bosses) {
			FtnTools.writeNetmail(FtnTools.getPrimaryFtnAddress(), boss, name,
					boss.getLine().split(",")[4], subject, text);
		}
		return success;
	}

	public boolean checkflags(String flagline, int linenum) {
		if (flagline.length() == 0)
			return true;
		String regex = "^(CM|MO|LO|V21|V22|V29|V32|V32B|V32T|V33|V34|HST|"
				+ "H14|H16|H96|MAX|PEP|CSP|ZYX|VFC|Z19|V90C|V90S|X2C|X2S|MNP|V42|"
				+ "MN|V42B|XA|XB|XC|XP|XR|XW|XX|V110L|V110H|V120L|V120H|X75|ISDN|"
				+ "IBN|IFC|ITN|IVM|IFT|ITX|IUC|IMI|ISE|IP|IEM|#\\d{2}|T[a-zA-Z]{2}|"
				+ "I(EM|NA|MI|MA|TN|FT):([-a-zA-Z0-9\\.@]+))$";
		boolean uflag = false;
		boolean status = true;
		Pattern p = Pattern.compile(regex);

		for (String flag : flagline.split(",")) {
			if (p.matcher(flag).matches()) {
				continue;
			}
			if (flag.equals("U")) {
				uflag = true;
				continue;
			}
			if (uflag) {
				continue;
			}
			addError(linenum, "unknown flag: " + flag);
			status = false;

		}
		return status;

	}
}
