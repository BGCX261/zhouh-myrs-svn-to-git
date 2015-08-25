/**
 * 
 */
package org.myfw.spvi.persistent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import org.myfw.spvi.AbstractPersist;
import org.myfw.spvi.MyLoger;
import org.myfw.spvi.factory.PersistFactory;

/**
 * @author zhouhui
 * 
 */
public class Session {
	private Connection conn;
	private boolean commit = false;
	private boolean beginTrans = false;
	@SuppressWarnings("rawtypes")
	private ThreadLocal persists = new ThreadLocal();

	/*
	 * private static Map entityBeans; static{ entityBeans = new HashMap(); //TODO扫描指定的包 并把column与实体bean的对应关系保存起来 String packs = null; // List list = ClassUtil.getClassInPacks(packs); }
	 */
	/**
	 * @param conn
	 */
	public Session(Connection conn) {
		super();
		this.conn = conn;
	}

	public Connection getConn() {
		return conn;
	}

	public boolean isCommit() {
		return commit;
	}

	public void setCommit(boolean commit) {
		this.commit = commit;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public boolean isBeginTrans() {
		return beginTrans;
	}

	public void setBeginTrans(boolean beginTrans) {
		this.beginTrans = beginTrans;
	}

	/**
	 * 开始事务
	 */
	public void beginTransaction() {
		beginTrans = true;
	}

	public void close() {
		try {
			this.conn.close();
			this.conn = null;
		} catch (SQLException e) {
			MyLoger.log(e.getMessage(), Level.SEVERE);
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * 提交事务
	 */
	public void commit() {
		try {
			if (conn != null)
				conn.commit();
		} catch (SQLException e) {
			MyLoger.log(e.getMessage(), Level.SEVERE);
			throw new RuntimeException(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private AbstractPersist getPersist(Object entity) {
		AbstractPersist p = null;
		try {
			Object o = persists.get();
			if (o == null) {
				p = PersistFactory.get(entity, this);
				persists.set(o);
			} else
				p = (AbstractPersist) o;
		} catch (Exception e) {
			MyLoger.log(e.getMessage(), Level.SEVERE);
			throw new RuntimeException(e.getMessage());
		}
		if (this.conn == null) {
			throw new RuntimeException("连接已关闭！");
		}
		return p;
	}

	/**
	 * 保存对象
	 * 
	 * @param entity
	 *            要保存的对象
	 */
	public void save(Object entity) {
		try {
			AbstractPersist sp = getPersist(entity);
			sp.save(entity);
		} catch (Exception e) {
			MyLoger.log(e.getMessage(), Level.SEVERE);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 更新对象
	 * 
	 * @param entity
	 *            要保存的对象
	 * @return
	 */
	public int update(Object entity) {
		try {
			AbstractPersist sp = getPersist(entity);
			return sp.update(entity);
		} catch (Exception e) {
			MyLoger.log(e.getMessage(), Level.SEVERE);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 删除对象
	 * 
	 * @param entity
	 *            要删除的对象
	 * @return
	 */
	public int delete(Object entity) {
		try {
			AbstractPersist sp = getPersist(entity);
			return sp.delete(entity);
		} catch (Exception e) {
			MyLoger.log(e.getMessage(), Level.SEVERE);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 查询一个对象
	 * 
	 * @param entity
	 *            含查询条件的对象
	 * @return
	 */
	public Object queryObject(Object entity) {
		try {
			AbstractPersist sp = getPersist(entity);
			return sp.queryForObject(entity);
		} catch (Exception e) {
			MyLoger.log(e.getMessage(), Level.SEVERE);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 查询对象列表
	 * 
	 * @param entity
	 *            含查询条件的对象
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List queryList(Object entity) {
		try {
			AbstractPersist sp = getPersist(entity);
			return sp.queryForList(entity);
		} catch (Exception e) {
			MyLoger.log(e.getMessage(), Level.SEVERE);
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
