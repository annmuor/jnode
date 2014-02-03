package jnode.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public final class FileUtils {
	private static final int BLOCK_SIZE = 4096;

	public static String readFile(String path) throws IOException {
		try (FileInputStream stream = new FileInputStream(new File(path))) {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			return Charset.forName("UTF8").decode(bb).toString();
		}
	}

	/**
	 * Замена File.renameTo
	 * 
	 * @param source
	 * @param dest
	 * @param override
	 * @return
	 */
	public static boolean move(File source, File dest, boolean override) {
		if (!source.exists()) {
			return false;
		}
		if (dest.exists() && !override) {
			return false;
		}

		try {
			FileInputStream fis = new FileInputStream(source);
			FileOutputStream fos = new FileOutputStream(dest);
			int len = 0;
			byte[] block = new byte[BLOCK_SIZE];
			do {
				len = fis.read(block);
				if (len > 0) {
					fos.write(block, 0, len);
				}
			} while (len > 0);
			fos.close();
			fis.close();
			if (source.length() == dest.length()) {
				source.delete();
				return true;
			}
		} catch (IOException e) {
		} catch (RuntimeException e) {
		}
		return false;
	}

}
