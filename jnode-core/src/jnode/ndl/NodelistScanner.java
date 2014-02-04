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

package jnode.ndl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnode.ftn.types.FtnAddress;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.ndl.FtnNdlAddress.Status;

/**
 * Singleton
 * 
 * @author kreon
 * 
 */
public class NodelistScanner {
	private static final String NODELIST_PATH = "nodelist.path";
	private static final String NODELIST_INDEX = "nodelist.index";
	private static final NodelistScanner self = new NodelistScanner();
	private static final Logger logger = Logger
			.getLogger(NodelistScanner.class);

	public static NodelistScanner getInstance() {
		return self;
	}

	public NodelistIndex createIndex(InputStream stream, long lastModified) {
		NodelistIndex index = null;
		List<FtnNdlAddress> address = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(stream));
			String line;
			String zone = "";
			String net = "";
			String region = "";
			Pattern pZone = Pattern.compile("^Zone,(\\d+),.*");
			Pattern pNet = Pattern.compile("^Host,(\\d+),.*");
			Pattern pReg = Pattern.compile("^Region,(\\d+),.*");
			Pattern pNode = Pattern
					.compile("^(Hub|Hold|Pvt|Down|)?,(\\d+),[^,]+,[^,]+,[^,]+,.*");
			while ((line = br.readLine()) != null) {
				Matcher mZone = pZone.matcher(line);
				Matcher mNet = pNet.matcher(line);
				Matcher mReg = pReg.matcher(line);
				Matcher mNode = pNode.matcher(line);
				String ftn = "";
				Status status = Status.NORMAL;
				if (mZone.matches()) {
					zone = mZone.group(1);
					net = zone;
					ftn = zone + ":" + net + "/0";
				} else if (mReg.matches()) {
					region = mReg.group(1);
					ftn = zone + ":" + region + "/0";
					net = region;
				} else if (mNet.matches()) {
					net = mNet.group(1);
					ftn = zone + ":" + net + "/0";
					status = Status.HOST;
				} else if (mNode.matches()) {
					ftn = zone + ":" + net + "/" + mNode.group(2);
					if (mNode.group(1).equalsIgnoreCase("hold")) {
						status = Status.HOLD;
					} else if (mNode.group(1).equalsIgnoreCase("down")) {
						status = Status.DOWN;
					} else if (mNode.group(1).equalsIgnoreCase("pvt")) {
						status = Status.PVT;
					} else if (mNode.group(1).equalsIgnoreCase("hub")) {
						status = Status.HUB;
					}
				}
				if (ftn.length() > 0) {
					try {
						FtnNdlAddress node = new FtnNdlAddress(ftn, status);
						node.setLine(line);
						address.add(node);
					} catch (NumberFormatException e) {
						//
					}
				}
			}
			br.close();
			index = new NodelistIndex(address.toArray(new FtnNdlAddress[0]),
					lastModified);
			logger.l4("Nodelist index was created. It containts: "
					+ address.size() + " adresses");
		} catch (IOException e) {
			logger.l2("Nodelist creation error ");
		}
		return index;
	}

	private NodelistIndex createNdlIndexFile(File ndl) {
		File idx = new File(MainHandler.getCurrentInstance().getProperty(
				NODELIST_INDEX, "NODELIST.idx"));

		try {
			NodelistIndex index = createIndex(new FileInputStream(ndl),
					ndl.lastModified());
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(idx));
			oos.writeObject(index);
			oos.close();
			return index;
		} catch (IOException e) {
			logger.l2("Nodelist writing error " + ndl.getAbsolutePath()
					+ " -> " + idx.getAbsolutePath() + " : " + e.getMessage());
		}
		return null;
	}

	private NodelistIndex createNdlIndex() {
		NodelistIndex index = null;
		File idx = new File(MainHandler.getCurrentInstance().getProperty(
				NODELIST_INDEX, "NODELIST.idx"));

		File ndl = getNodelistFile();
		if (idx.exists()) {
			try {
				ObjectInputStream os = new ObjectInputStream(
						new FileInputStream(idx));
				index = (NodelistIndex) os.readObject();
				os.close();
				long timestamp = 0L;
				if (ndl != null) {
					timestamp = ndl.lastModified();
					if (timestamp > index.getTimestamp()) {
						NodelistIndex newIndex = createNdlIndexFile(ndl);
						if (newIndex != null) {
							index = null;
							index = newIndex;
							newIndex = null;
						}
					}
				}
				return index;
			} catch (IOException e) {
				logger.l2("nodelist.index : ioexception", e);
			} catch (ClassNotFoundException e) {
				logger.l2("nodelist.index : classnotfound", e);
			} catch (ClassCastException e) {
				logger.l2("nodeist.index : classcast", e);
			}
		}
		if (ndl != null) {
			index = createNdlIndexFile(ndl);
		}
		return index;
	}

	private File getNodelistFile() {
		String nodelist = MainHandler.getCurrentInstance().getProperty(
				NODELIST_PATH, "NODELIST.*");
		File ndl = null;
		if (nodelist.endsWith("*")) {
			final String nodelist_w = nodelist.substring(0,
					nodelist.length() - 1);
			int lidx = nodelist.lastIndexOf(File.separator);
			String dir = ".";
			if (lidx >= 0) {
				dir = nodelist.substring(0, lidx);
			}
			File ndlDir = new File(dir);
			long unixtime = 0;
			if (ndlDir.isDirectory()) {
				for (File file : ndlDir.listFiles()) {
					if (file.getAbsolutePath().toLowerCase()
							.startsWith(nodelist_w.toLowerCase())) {
						if (file.lastModified() > unixtime) {
							ndl = file;
							unixtime = file.lastModified();
						}
					}
				}
			}
		} else {
			ndl = new File(nodelist);
		}
		if (ndl != null) {
			logger.l4("Using nodelist file " + ndl.getName());
		}

		return ndl;
	}

	public FtnNdlAddress isExists(FtnAddress address) {
		NodelistIndex index = createNdlIndex();
		if (index == null) {
			logger.l3("Nodelist not found; We trust that " + address
					+ " exists");
			return new FtnNdlAddress(address);
		} else {
			return index.exists(address);
		}
	}
}
