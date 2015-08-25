/**
 * 
 */
package org.myfw.spvi.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;

import org.myfw.spvi.MyLoger;

/**
 * @author zhouhui
 *
 */
public class FileUtil {
	private FileUtil(){}
	public static Properties getProperties(String file){
		Properties prop = new Properties();
		InputStream is = null;
		try {
			is = loadFileInClassPath(file).openStream();
			prop.load(is);
		} catch (IOException e) {
			MyLoger.log(e.getMessage(), Level.SEVERE);
			throw new RuntimeException(e.getMessage());
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					MyLoger.log(e.getMessage(), Level.SEVERE);
					throw new RuntimeException(e.getMessage());
				}
		}
		return prop;
	}
	public static URL loadFileInClassPath(String path){
		URL url = null;

		// First, try to locate this resource through the current
		// context classloader.
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		if (contextClassLoader!=null) {
			url = contextClassLoader.getResource(path);
		}
		if (url != null)
			return url;

		// Next, try to locate this resource through this class's classloader
		url = FileUtil.class.getClassLoader().getResource(path);
		if (url != null)
			return url;

		// Next, try to locate this resource through the system classloader
		url = ClassLoader.getSystemClassLoader().getResource(path);

		// Anywhere else we should look?
		return url;
	}
}
