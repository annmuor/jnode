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

package jnode.core;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public final class FileUtils {
	private static final int BLOCK_SIZE = 4096;

    /**
     * Читает весь файл в строку
     * @param path путь к файлу
     * @return строка с содержимым файла
     * @throws IOException
     */
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

    /**
     * zip file
     * @param inPath source
     * @param outPath destination
     * @param nameInsideZip zip entry name
     * @throws IOException
     */
    public static void zipFile(String inPath, String outPath, String nameInsideZip) throws IOException {
        try(FileInputStream in = new FileInputStream(inPath)){
            try(ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outPath))){
                out.putNextEntry(new ZipEntry(nameInsideZip));

                byte[] buffer = new byte[BLOCK_SIZE];
                int len;

                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }

                out.closeEntry();
            }
        }
    }

}
