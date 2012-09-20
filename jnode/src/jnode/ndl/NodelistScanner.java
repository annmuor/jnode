package jnode.ndl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jnode.ftn.FtnAddress;
import jnode.logger.Logger;
import jnode.main.Main;
import jnode.ndl.FtnNdlAddress.Status;

/**
 * Singleton
 * 
 * @author kreon
 * 
 */
public class NodelistScanner {
	private static final NodelistScanner self = new NodelistScanner();
	private static final Logger logger = Logger
			.getLogger(NodelistScanner.class);

	public static NodelistScanner getInstance() {
		return self;
	}

	private boolean createNdlIndexFile() {
		boolean ok = false;
		File ndl = new File(Main.getNodelistPath());
		File idx = new File(Main.getNodelistIdx());
		List<FtnNdlAddress> address = new ArrayList<FtnNdlAddress>();
		try {
			FileInputStream fis = new FileInputStream(ndl);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line;
			String zone = "";
			String net = "";
			String region = "";
			Pattern pZone = Pattern.compile("^Zone,(\\d+),.*");
			Pattern pNet = Pattern.compile("^Host,(\\d+),.*");
			Pattern pReg = Pattern.compile("^Region,(\\d+),.*");
			Pattern pNode = Pattern
					.compile("^(Hub|Hold|Pvt|Down|)?,(\\d+),[^,]+,[^,]+,([^,]+),.*");
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
					ftn = zone + ":0/0";
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
						address.add(node);
					} catch (NumberFormatException e) {
						//
					}
				}
			}
			br.close();
			NodelistIndex index = new NodelistIndex(
					address.toArray(new FtnNdlAddress[0]), ndl.lastModified());
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(idx));
			oos.writeObject(index);
			logger.info("Создан индекс нодлиста: " + address.size()
					+ " адресов");
			oos.close();
			ok = true;
		} catch (IOException e) {
			logger.error("Ошибка при создании индекса нодлиста "
					+ ndl.getAbsolutePath() + " -> " + idx.getAbsolutePath()
					+ " : " + e.getMessage());
		}
		return ok;
	}

	private NodelistIndex createNdlIndex() {
		NodelistIndex index = null;
		File idx = new File(Main.getNodelistIdx());
		long timestamp = 0L;
		{
			File ndl = new File(Main.getNodelistPath());
			if (ndl.exists()) {
				timestamp = ndl.lastModified();
			}
		}
		if (idx.exists()) {
			try {
				ObjectInputStream os = new ObjectInputStream(
						new FileInputStream(idx));
				index = (NodelistIndex) os.readObject();
				os.close();
				if (timestamp > index.getTimestamp()) {
					if (createNdlIndexFile()) {
						return createNdlIndex();
					} else {
						return index;
					}
				}
			} catch (IOException e) {
				logger.error("Не могу прочитать nodelist.index");
			} catch (ClassNotFoundException e) {
				logger.error("Не могу найти nodelist.index");
			} catch (ClassCastException e) {
				logger.error("В файле nodelist.index содержится не nodelist.index");
				if (createNdlIndexFile()) {
					return createNdlIndex();
				}
			}
		} else {
			if (createNdlIndexFile()) {
				return createNdlIndex();
			}
		}
		return index;
	}

	public FtnNdlAddress isExists(FtnAddress address) {
		NodelistIndex index = createNdlIndex();
		if (index == null) {
			logger.warn("Нодлист не найден, разрешаем считаем адрес " + address
					+ " существующим");
			return new FtnNdlAddress(address);
		} else {
			return index.exists(address);
		}
	}
}
