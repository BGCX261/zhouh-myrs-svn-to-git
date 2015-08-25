/**
 * 
 */
package org.myfw.spvi.factory;

import java.sql.Connection;

import org.myfw.spvi.db.ConnPool;
import org.myfw.spvi.persistent.Session;

/**
 * @author zhouhui
 *
 */
public class SessionFactory {
	@SuppressWarnings("rawtypes")
	private static ThreadLocal currentSession = new ThreadLocal();
	@SuppressWarnings("unchecked")
	public Session openSession(){
		Session session = (Session) currentSession.get();
		Connection connection = ConnPool.getConnection();//在连接池中取
		if(session==null) {
			session = new Session(connection);
			currentSession.set(session);
		}
		return session;
	}
}
