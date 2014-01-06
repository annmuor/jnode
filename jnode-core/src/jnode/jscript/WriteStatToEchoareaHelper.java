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
