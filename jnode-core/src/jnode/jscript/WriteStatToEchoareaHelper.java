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

package jnode.jscript;

import jnode.dto.Echoarea;
import jnode.ftn.FtnTools;
import jnode.logger.Logger;
import jnode.stat.ConnectionStat;

public class WriteStatToEchoareaHelper extends IJscriptHelper {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(WriteStatToEchoareaHelper.class);

	public void writeStatToEchoarea(String echoArea, String subject,
			String statfilename, boolean reset) {
		Echoarea area = FtnTools.getAreaByName(echoArea, null);
        String content = ConnectionStat.getText(statfilename, reset);

		FtnTools.writeEchomail(area, subject, content);

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
				return 0;
			}
		};
	}
}
