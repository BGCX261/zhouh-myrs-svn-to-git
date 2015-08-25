/**
 * 
 */
package org.myfw.spvi;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * 线程共享变量
 * @author zhouhui
 * 
 */
@SuppressWarnings("rawtypes")
public class ThreadVar {
	private Map map = new Hashtable();

	public Object get(Object key) {
		Map vars = (Map) map.get(Thread.currentThread().getId());
		if (vars != null)
			return vars.get(key);
		return null;
	}

	/**
	 * 
	 * @param key
	 *            若为引用类型要实现hashCode和equals方法
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void set(Object key, Object value) {
		long tid = Thread.currentThread().getId();
		Map vars = (Map) map.get(tid);
		if (vars == null) {
			vars = new HashMap();
			map.put(tid, vars);
		}
		vars.put(key, value);
	}

	/**
	 * 
	 * @param key
	 *            若为引用类型要实现hashCode和equals方法
	 */
	public void remove(Object key) {
		Map vars = (Map) map.get(Thread.currentThread().getId());
		if (vars == null) {
			map.remove(key);
		}
	}
}
