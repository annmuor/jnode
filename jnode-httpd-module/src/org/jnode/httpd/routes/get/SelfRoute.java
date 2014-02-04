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

package org.jnode.httpd.routes.get;

import java.util.List;

import jnode.ftn.types.FtnAddress;
import jnode.main.MainHandler;
import jnode.main.SystemInfo;

import org.jnode.httpd.util.HTML;

import spark.Request;
import spark.Response;
import spark.Route;

public class SelfRoute extends Route {
	private final String FORMAT_TABLE = "<table class=\"info\">%s</table>";
	private final String FORMAT_TR = "<tr><th>%s</th><td>%s</td></tr>";

	public SelfRoute() {
		super("/index.html");
	}

	public SelfRoute(String path) {
		super(path);
	}

	@Override
	public Object handle(Request req, Response resp) {
		String index = HTML.getContents("index.html");
		if (index.length() > 0) {
			return index;
		}
		SystemInfo info = MainHandler.getCurrentInstance().getInfo();

		String text = String.format(
				FORMAT_TABLE,
				String.format(FORMAT_TR, "Имя узла", info.getStationName())
						+ String.format(FORMAT_TR, "Расположение узла",
								info.getLocation())
						+ String.format(FORMAT_TR, "Сисоп", info.getSysop())
						+ String.format(FORMAT_TR, "FTN-адрес(а)",
								getAddrList(info.getAddressList()))
						+ String.format(FORMAT_TR, "Версия софта",
								MainHandler.getVersion())
						+ String.format(FORMAT_TR, "ОС", getOS()));
		return HTML.start(false).append(text).footer().get();
	}

	private String getOS() {
		return System.getProperty("os.name") + " "
				+ System.getProperty("os.version") + " ("
				+ System.getProperty("os.arch") + ")";
	}

	private String getAddrList(List<FtnAddress> list) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		for (FtnAddress address : list) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(address.toString());
		}
		return sb.toString();
	}
}
