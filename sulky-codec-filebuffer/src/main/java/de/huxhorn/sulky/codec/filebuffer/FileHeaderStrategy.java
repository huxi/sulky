package de.huxhorn.sulky.codec.filebuffer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface FileHeaderStrategy
{
	/**
	 *
	 * @param dataFile the file to read the magic value from.
	 * @return the magic value of the file if it is compatible, null otherwise.
	 * @throws java.io.IOException in case of IOException :p
	 */
	Integer readMagicValue(File dataFile)
		throws IOException;

	/**
	 *
	 * @param dataFile the file to write the header to. Must be empty or nonexistent.
	 * @param magicValue the magic value of the file.
	 * @param metaData the meta data of the file, can be null.
	 * @return the written FileHeader
	 * @throws IllegalArgumentException if file exists and isn't empty.
	 * @throws java.io.IOException in case of IOException :p
	 */
	FileHeader writeFileHeader(File dataFile, int magicValue, Map<String, String> metaData)
		throws IOException;

	/**
	 *
	 * @param dataFile the file to read the header from.
	 * @return the FileHeader read from the file.
	 * @throws java.io.IOException in case of IOException :p
	 */
	FileHeader readFileHeader(File dataFile)
		throws IOException;

	int getMinimalSize();
}
