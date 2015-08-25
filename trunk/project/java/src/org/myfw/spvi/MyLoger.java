/**
 * 
 */
package org.myfw.spvi;

import java.io.FileOutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.myfw.spvi.util.FileUtil;

/**
 * @author zhouhui
 * 
 */
public class MyLoger {
	private MyLoger(){}
	private static Level loglevel = Level.OFF;//默认的关闭日志
	private static String logdest = "console";//默认输出到控制台
	static {
		Properties prop = FileUtil.getProperties("config.properties");
		String tloglevel = prop.getProperty("loglevel");
		if (tloglevel != null)
			loglevel = Level.parse(tloglevel.toUpperCase());
		String tlogdest = prop.getProperty("logdest");
		if (tlogdest != null)
			logdest = tlogdest;
	}

	public static Logger getLogger() {
		return Logger.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
	}

	public static void log(String msg,Level level) {
		if (loglevel.intValue()<=level.intValue()) {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			String currentMethodName = stackTrace[2].getMethodName();
			Logger l = Logger.getLogger(stackTrace[2].getClassName());
			if(!logdest.equals("console")){
				try {
					l.addHandler(new StreamHandler(new FileOutputStream(logdest,true),new SimpleFormatter()));
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e.getMessage(),e);
				}
			}
			l.log(level, "[" + currentMethodName + "]:" + msg);
		}
	}
}
