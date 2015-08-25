/**
 * 
 */
package org.myfw.spvi.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouhui
 * 
 */
@SuppressWarnings("rawtypes")
public class ClassUtil {
	private ClassUtil(){}
	/*
	 * 取一个包内的所有类
	 */
	public static List getClassInPacks(String packs) {
		String[] pks = packs.split(",");
		List list = new ArrayList();
		for (int i = 0, n = pks.length; i < n; i++) {
			File pk = new File(FileUtil.loadFileInClassPath(pks[i].replaceAll("\\.", "/")).getFile());
			getClasss(pks[i],list, pk);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private static void getClasss(String pk,List list, File file) {
		File[] fs = file.listFiles();
		for (int i = 0, n = fs.length; i < n; i++) {
			if (fs[i].isFile()){
				String cn=pk+"."+fs[i].getName().replaceAll("\\.class$", "");
				try {
					list.add(Class.forName(cn));
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e.getMessage());
				}
			}else
				getClasss(pk+"."+fs[i].getName(),list, fs[i]);
		}
	}
}
