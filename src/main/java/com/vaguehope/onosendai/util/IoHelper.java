package com.vaguehope.onosendai.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public final class IoHelper {

	private static final int COPY_BUFFER_SIZE = 1024 * 4;

	private IoHelper () {
		throw new AssertionError();
	}

	public static long copy (final InputStream source, final OutputStream sink) throws IOException {
		byte[] buffer = new byte[COPY_BUFFER_SIZE];
		long bytesReadTotal = 0L;
		int bytesRead;
		while ((bytesRead = source.read(buffer)) != -1) {
			sink.write(buffer, 0, bytesRead);
			bytesReadTotal += bytesRead;
		}
		return bytesReadTotal;
	}

	/**
	 * Returns null if file does not exist.
	 */
	public static String fileToString (final File file) throws IOException {
		try {
			final FileInputStream stream = new FileInputStream(file);
			try {
				final FileChannel fc = stream.getChannel();
				final MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
				/* Instead of using default, pass in a decoder. */
				return Charset.defaultCharset().decode(bb).toString();
			}
			finally {
				stream.close();
			}
		}
		catch (final FileNotFoundException e) {
			return null;
		}
	}

	public static void resourceToFile(final String res, final File f) throws IOException {
		final byte[] arr = new byte[COPY_BUFFER_SIZE];
		final InputStream is = IoHelper.class.getResourceAsStream(res);
		try {
			final OutputStream os = new FileOutputStream(f);
			try {
				int count;
				while ((count = is.read(arr)) >= 0) {
					os.write(arr, 0, count);
				}
			}
			finally {
				os.close();
			}
		}
		finally {
			is.close();
		}
	}

}
