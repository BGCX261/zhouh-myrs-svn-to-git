package org.myfw.spvi.util;


import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author zhouhui
 *
 */
public class AnnoUtil {
	private AnnoUtil() {
	}
	/**
	 * 取一个类所有字段上的annotation
	 * @param clazz
	 * @return map的key是字段名称;value是也是一个map,key是annotation名称，value是annotation的值
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static Map getFieldAnnotations(Class clazz) throws Exception {
		final int v = Opcodes.V1_6;
		Map map = new HashMap();
		ClassWriter cw = new ClassWriter(v);
		MyClassVisitor cp = new MyClassVisitor(v, cw,map);
		ClassReader cr = new ClassReader(clazz.getName());
		cr.accept(cp,ClassReader.SKIP_DEBUG);
		return map;
	}
}
@SuppressWarnings("rawtypes")
class MyClassVisitor extends ClassVisitor {
	private Map map;

	/**
	 * @param api
	 * @param cv
	 */
	public MyClassVisitor(int api, ClassVisitor cv,Map map) {
		super(api, cv);
		this.map = map;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		return new MyFieldVisitor(api, cv.visitField(access, name, desc, signature, value), name, map);
	}
}

@SuppressWarnings("rawtypes")
class MyFieldVisitor extends FieldVisitor {
	private Map map;
	private String fieldName;

	/**
	 * @param api
	 * @param fv
	 */
	public MyFieldVisitor(int api, FieldVisitor fv, String fieldName, Map map) {
		super(api, fv);
		this.map = map;
		this.fieldName = fieldName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		Map o = (Map) map.get(fieldName);
		String annoName = desc.substring(desc.lastIndexOf("/")+1,desc.length()-1);
		if(o==null){
			o = new HashMap();
		}
		o.put(annoName, null);
		map.put(fieldName, o);
		return new MyAnnotationVisitor(api, fv.visitAnnotation(desc, visible),annoName,o);
	}

}

@SuppressWarnings("rawtypes")
class MyAnnotationVisitor extends AnnotationVisitor {
	private Map map;
	private String annoName;

	/**
	 * @param api
	 * @param av
	 */
	public MyAnnotationVisitor(int api, AnnotationVisitor av,String annoName,Map map) {
		super(api, av);
		this.map = map;
		this.annoName = annoName;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String name, String desc) {
		return new MyAnnotationVisitor(api,av.visitArray(name),annoName,map);
	}

	@Override
	public void visit(String name, Object value) {
		fillMap(value);
		av.visit(name, value);
	}

	@Override
	public void visitEnum(String name, String desc, String value) {
		fillMap(value);
		av.visitEnum(name, desc, value);
	}

	@Override
	public AnnotationVisitor visitArray(String name) {
		return new MyAnnotationVisitor(api,av.visitArray(name),annoName,map);
	}

	@Override
	public void visitEnd() {
		av.visitEnd();
	}
	@SuppressWarnings("unchecked")
	private void fillMap(Object value){
		String[] o = (String[]) map.get(annoName);
		if(o!=null){
			String[] n = new String[o.length+1];
			for(int i=0,c=o.length;i<c;i++){
				n[i]=o[i];
			}
			n[o.length]= (String)value;
			map.put(annoName, n);
		}else
			map.put(annoName, new String[]{(String)value});
	}
}