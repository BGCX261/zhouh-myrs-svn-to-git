/**
 * 
 */
package org.myfw.spvi.classloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.objectweb.asm.ClassWriter;

/**
 * @author zhouhui
 *
 */
public class MyClassLoader extends ClassLoader{
	private ClassWriter cw;
	private String className;
	public MyClassLoader(ClassWriter cw){
		this.cw = cw;
	}
	public MyClassLoader(){};
	public MyClassLoader(String className,ClassWriter cw){
		this.cw = cw;
		this.className = className;
	}
	public Class<?> defineClass(){
		return defineClass(className, cw.toByteArray(),0,cw.toByteArray().length);
	}
	public Class<?> defineClass(byte[] bytes){
		return defineClass(null, bytes,0,bytes.length);
	}
	public void saveToDisk(String path){
		int idx = path.lastIndexOf("/");
		File dir = new File(path.substring(0, idx));
		if(!dir.exists())dir.mkdirs();
		OutputStream os = null;
		try {
			os = new FileOutputStream(new File(dir,path.substring(idx+1)));
			os.write(cw.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			new RuntimeException(e.getMessage());
		}finally{
			try {
				if(os!=null)
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
				new RuntimeException(e.getMessage());
			}
		}
	}
	/**
	 * 把类文件保存到磁盘
	 * @param path	保存位置
	 * @param bytes 类文件字节数组
	 */
	public static void saveToDisk(String path,byte[] bytes){
		int idx = path.lastIndexOf("/");
		File dir = new File(path.substring(0, idx));
		if(!dir.exists())dir.mkdirs();
		OutputStream os = null;
		try {
			os = new FileOutputStream(new File(dir,path.substring(idx+1)));
			os.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
			new RuntimeException(e.getMessage());
		}finally{
			try {
				if(os!=null)
					os.close();
			} catch (IOException e) {
				e.printStackTrace();
				new RuntimeException(e.getMessage());
			}
		}
	}
}
