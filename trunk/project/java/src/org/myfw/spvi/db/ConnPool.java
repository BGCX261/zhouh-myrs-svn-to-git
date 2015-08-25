/**
 * 
 */
package org.myfw.spvi.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;

import org.myfw.spvi.MyLoger;
import org.myfw.spvi.util.FileUtil;
import org.myfw.spvi.util.StringUtil;

/**
 * @author zhouhui
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ConnPool {
	private static LinkedList conns = new LinkedList();
	private static String driver;
	private static String url;
	private static String username;
	private static String password;
	private static int initialPoolSize = 10;
	private static int increment = 5;
	private static int maxActivie = 100;
//	private static int maxIdle = 300;// 以秒为单位
	static {
		// 读配置文件初始化连接池
		Properties prop = FileUtil.getProperties("config.properties");
		driver = prop.getProperty("driver");
		url = prop.getProperty("url");
		username = prop.getProperty("username");
		password = prop.getProperty("password");
		Integer tmaxActivie = StringUtil.parseInt(prop.getProperty("maxActivie"));
//		Integer tmaxIdle = StringUtil.parseInt(prop.getProperty("maxIdle"));
		Integer tinitialPoolSize = StringUtil.parseInt(prop.getProperty("initialPoolSize"));
		if (tmaxActivie != null)
			maxActivie = tmaxActivie;
//		if (tmaxIdle != null)
//			maxIdle = tmaxIdle;
		if (tinitialPoolSize != null)
			initialPoolSize = tinitialPoolSize;
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			MyLoger.log(e.getMessage(), Level.SEVERE);
			throw new RuntimeException(e.getMessage());
		}
		for (int i = 0; i < initialPoolSize; i++) {
			conns.push(createConn());
		}

	}

	private ConnPool() {
	}// 私有构造器

	public static String getDriver() {
		return driver;
	}

	public static void setDriver(String driver) {
		ConnPool.driver = driver;
	}

	public static String getUrl() {
		return url;
	}

	public static void setUrl(String url) {
		ConnPool.url = url;
	}

	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		ConnPool.username = username;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		ConnPool.password = password;
	}

	public static Connection getConnection() {
		synchronized (conns) {
			if (conns.size() == 0) {
				for (int i = 0; i < increment; i++) {
					conns.push(createConn());
				}
			}
		}
		return (Connection) conns.poll();
	}

	public static void push(Connection conn) {
		synchronized (conns) {
			if (conns.size() < maxActivie) {
				try {
					if (conn != null && !conn.isClosed())
						conns.push(conn);
				} catch (SQLException e) {
					MyLoger.log(e.getMessage(), Level.SEVERE);
					throw new RuntimeException(e.getMessage());
				}
			}
		}
	}

	public static void close(Connection conn) {
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
				MyLoger.log(e.getMessage(), Level.SEVERE);
				throw new RuntimeException(e.getMessage());
			}
	}

	private static Connection createConn() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			MyLoger.log(e.getMessage(), Level.SEVERE);
			throw new RuntimeException(e.getMessage());
		}
		conn = new MyConn(conn);
		return conn;
	}
}
