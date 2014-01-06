package jnode.jscript;

import jnode.dto.Echoarea;
import jnode.ftn.FtnTools;
import jnode.logger.Logger;
import jnode.stat.ConnectionStat;
import jnode.stat.threads.StatPoster;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.MessageFormat;

public class WriteStatToEchoareaHelper extends IJscriptHelper {
	
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
