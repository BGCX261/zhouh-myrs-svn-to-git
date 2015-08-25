/**
 * 
 */
package org.myfw.spvi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.myfw.spvi.persistent.Session;

/**
 * @author zhouhui
 * 
 */
public abstract class AbstractPersist {
	protected Session session;

	/**
	 * 把查询结果封装到对象中
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	abstract protected Object getObject(ResultSet rs) throws SQLException;

	/**
	 * 
	 * @return 获取所有的列(第一个是表名)
	 */
	abstract protected String[] getColumns();

	/**
	 * 回填主键值
	 * 
	 * @param o
	 * @param id
	 */
	abstract protected void setId(Object o, String id);
	/**
	 * 
	 * @return ID生成方式
	 */
	abstract protected String getIdPolicy();
	/**
	 * ID列的索引
	 * @return
	 */
	abstract protected int getIdIndex();

	/**
	 * 获取参数值数组
	 * 
	 * @param o
	 * @return 参数值数组
	 */
	abstract protected Object[] getParameters(Object o);

	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * 填充参数
	 * 
	 * @param o
	 * @param ps
	 * @return
	 * @throws SQLException
	 */
	private int fillParameters(Object o, PreparedStatement ps) throws SQLException {
		Object[] params = getParameters(o);
		StringBuilder pms = new StringBuilder();
		int idx = 1;
		for (int i = 0, n = params.length; i < n; i++) {
			if (params[i] != null) {
				if (idx > 1)
					pms.append(",");
				pms.append(params[i]);
				ps.setObject(idx++, params[i]);
			}
		}
		MyLoger.log(pms.toString(), Level.INFO);
		return idx;
	}

	private final static int SAVE = 1;
	private final static int UPDATE = 2;
	private final static int DELETE = 3;
	private final static int QUERY = 4;

	/**
	 * 拼接sql
	 * 
	 * @param type
	 *            crud操作类型
	 * @param o
	 * @return
	 */
	private String getSql(int type, Object o) {
		String[] cols = getColumns();
		StringBuilder sql = new StringBuilder();
		StringBuilder wcol = new StringBuilder();
		StringBuilder ucol = new StringBuilder();
		StringBuilder icol = new StringBuilder();
		StringBuilder ph = new StringBuilder();
		StringBuilder scol = new StringBuilder();
		Object[] params = getParameters(o);
		int idx = 1;
		for (int i = 0, n = params.length; i < n; i++) {
			Object val = params[i];
			if (val != null) {
				if (idx > 1) {
					icol.append(",");
					ph.append(",");
					ucol.append(",");
				}
				wcol.append(" and ");
				ucol.append(cols[i + 1]).append("=?");
				wcol.append(cols[i + 1]).append("=?");
				icol.append(cols[i + 1]);
				ph.append("?");
				idx++;
			}
			if (i > 0)
				scol.append(",");
			scol.append(cols[i + 1]);
		}
		String idCol = cols[getIdIndex() + 1];
		switch (type) {
		case SAVE:
			sql.append("insert into ").append(cols[0]).append("(").append(icol).append(")values(").append(ph).append(")");
			break;
		case UPDATE:
			sql.append("update ").append(cols[0]).append(" set ").append(ucol).append(" where ").append(idCol).append("=?");
			break;
		case DELETE:
			sql.append("delete from ").append(cols[0]).append(" where ").append(idCol).append("=?");
			break;
		case QUERY:
			sql.append("select ").append(scol).append(" from ").append(cols[0]).append(" where 1=1").append(wcol);
			break;
		}
		return sql.toString();
	}

	/**
	 * 保存
	 * 
	 * @param o
	 * @throws Exception
	 */
	public void save(Object o) throws Exception {
		Connection conn = session.getConn();
		String sql = getSql(SAVE, o);
		PreparedStatement ps = conn.prepareStatement(sql);
		MyLoger.log(sql, Level.INFO);
		fillParameters(o, ps);
		if (ps.execute()) {
			if ("IDENTITY".equals(getIdPolicy())) {
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next())
					setId(o, rs.getObject(1).toString());
			}
		}

	}

	/**
	 * 更新
	 * 
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public int update(Object o) throws Exception {
		Connection conn = session.getConn();
		String sql = getSql(UPDATE, o);
		PreparedStatement ps = conn.prepareStatement(sql);
		MyLoger.log(sql, Level.INFO);
		int n = fillParameters(o, ps);
		ps.setObject(n, getParameters(o)[getIdIndex()]);
		return ps.executeUpdate();
	}

	/**
	 * 删除
	 * 
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public int delete(Object o) throws Exception {
		Connection conn = session.getConn();
		String sql = getSql(DELETE, o);
		PreparedStatement ps = conn.prepareStatement(sql);
		MyLoger.log(sql, Level.INFO);
		fillParameters(o, ps);
		return ps.executeUpdate();
	}

	/**
	 * 查询对象
	 * 
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public Object queryForObject(Object o) throws Exception {
		Connection conn = session.getConn();
		String sql = getSql(QUERY, o);
		PreparedStatement ps = conn.prepareStatement(sql);
		MyLoger.log(sql, Level.INFO);
		fillParameters(o, ps);
		ResultSet rs = ps.executeQuery();
		if (rs.next())
			return getObject(rs);
		return null;
	}

	/**
	 * 查询列表
	 * 
	 * @param o
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List queryForList(Object o) throws Exception {
		Connection conn = session.getConn();
		String sql = getSql(QUERY, o);
		PreparedStatement ps = conn.prepareStatement(sql);
		MyLoger.log(sql, Level.INFO);
		fillParameters(o, ps);
		ResultSet rs = ps.executeQuery();
		List list = new ArrayList();
		while (rs.next()) {
			list.add(getObject(rs));
		}
		return list;
	}

}
