/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2015 Joern Huxhorn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2007-2015 Joern Huxhorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.huxhorn.sulky.version;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClassFileScanner
{
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ClassFileScanner.class);

	private static final String CLASS_EXTENSION = ".class";
	private static final int MAGIC_NUMBER_CLASS = 0xCAFEBABE;

	private final List<ClassStatisticMapper> classStatisticMappers =new ArrayList<ClassStatisticMapper>();

	public List<ClassStatisticMapper> getClassStatisticMappers()
	{
		return classStatisticMappers;
	}

	public void reset()
	{
		for (ClassStatisticMapper classStatisticMapper : classStatisticMappers)
		{
			classStatisticMapper.reset();
		}
	}

	public void scanDirectory(File directory)
			throws IOException
	{
		if(!directory.isDirectory())
		{
			throw new IllegalArgumentException("'directory' must be a directory!");
		}
		String source = directory.getName();
		scanDirectory(directory, source);
	}

	public void scanDirectory(File directory, String source)
			throws IOException
	{
		if(!directory.isDirectory())
		{
			throw new IllegalArgumentException("'directory' must be a directory!");
		}
		if(source == null)
		{
			throw new NullPointerException("'source' must not be null!");
		}

		String absoluteBasePath = directory.getAbsolutePath();
		absoluteBasePath = absoluteBasePath.replace('\\', '/');
		absoluteBasePath = absoluteBasePath+"/";

		File[] files = directory.listFiles();
		if(files == null)
		{
			return;
		}
		for (File current : files)
		{
			processFile(absoluteBasePath, current, source);
		}

	}

	private void processFile(String absoluteBasePath, File file, String source)
			throws IOException
	{
		if(file.isDirectory())
		{
			File[] files = file.listFiles();
			if(files == null)
			{
				return;
			}
			for (File current : files)
			{
				processFile(absoluteBasePath, current, source);
			}
		}
		if(file.isFile())
		{
			String absoluteFilePath = file.getAbsolutePath();
			absoluteFilePath = absoluteFilePath.replace('\\', '/');
			if (!absoluteFilePath.endsWith(CLASS_EXTENSION))
			{
				return;
			}
			String classFileName = absoluteFilePath.substring(absoluteBasePath.length());
			FileInputStream input = null;
			try
			{
				input = new FileInputStream(file);
				scanClass(input, classFileName, source);
			}
			finally
			{
				if(input != null)
				{
					try
					{
						input.close();
					}
					catch (IOException ex)
					{
						// ignore
					}
				}
			}

		}

	}

	public void scanJar(File jarFile)
			throws IOException
	{
		String sourceName = jarFile.getName();
		ZipFile zipFile = null;
		try
		{
			zipFile = new ZipFile(jarFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements())
			{
				ZipEntry current = entries.nextElement();
				if (current.isDirectory())
				{
					continue;
				}
				String currentName = current.getName();
				if (!currentName.endsWith(CLASS_EXTENSION))
				{
					continue;
				}

				InputStream classStream = zipFile.getInputStream(current);
				if (classStream == null)
				{
					continue;
				}

				try
				{
					scanClass(classStream, currentName, sourceName);
				}
				catch (IOException ex)
				{
					// ignore
				}
				finally
				{
					try
					{
						classStream.close();
					}
					catch (IOException ex)
					{
						// ignore
					}
				}
			}
		}
		finally
		{
			if (zipFile != null)
			{
				try
				{
					zipFile.close();
				}
				catch (IOException ex)
				{
					// ignore
				}
			}
		}
	}

	private void scanClass(InputStream is, String classFileName, String sourceName)
			throws IOException
	{
		if (!classFileName.endsWith(CLASS_EXTENSION))
		{
			return;
		}

		DataInputStream dis = new DataInputStream(is);
		int magic = dis.readInt();
		if(MAGIC_NUMBER_CLASS != magic)
		{
			if(logger.isDebugEnabled()) logger.debug("Wrong magic number for class in {}. Ignoring.", classFileName);
			return;
		}
		/*char minor = */dis.readChar();
		char currentMajor = dis.readChar();
		int slashIndex = classFileName.lastIndexOf('/');
		String packageString="";
		String className = classFileName;
		if(slashIndex >= 0)
		{
			packageString = classFileName.substring(0, slashIndex);
			packageString = packageString.replace('/','.');
			className = classFileName.substring(slashIndex+1);
		}
		className = className.substring(0, className.length()-CLASS_EXTENSION.length());

		for (ClassStatisticMapper current : classStatisticMappers)
		{
			current.evaluate(sourceName, packageString, className, currentMajor);
		}
	}
}
