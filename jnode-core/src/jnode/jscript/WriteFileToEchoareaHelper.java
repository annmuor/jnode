package jnode.jscript;

import java.io.IOException;
import java.text.MessageFormat;

import jnode.core.FileUtils;
import jnode.dto.Echoarea;
import jnode.ftn.FtnTools;
import jnode.logger.Logger;

public class WriteFileToEchoareaHelper extends IJscriptHelper {
	
	private static final Logger logger = Logger
			.getLogger(WriteFileToEchoareaHelper.class);

	public void writeFileToEchoarea(String echoArea, String subject,
			String filename) {
		Echoarea area = FtnTools.getAreaByName(echoArea, null);
		String content;
		try {
			content = FileUtils.readFile(filename);
		} catch (IOException e) {
			logger.l2(MessageFormat.format("fail read file {0}", filename), e);
			return;
		}

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
				return 1;
			}
		};
	}
}
