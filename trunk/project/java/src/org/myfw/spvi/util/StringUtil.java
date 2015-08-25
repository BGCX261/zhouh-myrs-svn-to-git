/**
 * 
 */
package org.myfw.spvi.util;

/**
 * @author zhouhui
 *
 */
public class StringUtil {
	private StringUtil(){}
	/**
	 * 把指定的位置的字符变大写
	 * @param str 	要改变的字符串
	 * @param start 起始位置
	 * @param end	结束位置
	 * @return
	 */
	public static String toUpper(String str,int start,int end){
		char[] chars = str.toCharArray();
		int len = chars.length;
		if(end>len)end=len;
		for(int i=start;i<end;i++){
			if(Character.isLowerCase(chars[i]))
			chars[i]=Character.toUpperCase(chars[i]);
		}
		return new String(chars);
	}
	public static Integer parseInt(String sInt){
		return ("".equals(sInt)||sInt==null)?null:Integer.parseInt(sInt);
	}
}
