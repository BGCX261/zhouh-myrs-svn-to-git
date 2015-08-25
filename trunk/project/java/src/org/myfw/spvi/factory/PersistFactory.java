package org.myfw.spvi.factory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.myfw.spvi.AbstractPersist;
import org.myfw.spvi.annotation.Column;
import org.myfw.spvi.annotation.Id;
import org.myfw.spvi.annotation.Table;
import org.myfw.spvi.annotation.Transient;
import org.myfw.spvi.classloader.MyClassLoader;
import org.myfw.spvi.persistent.Session;
import org.myfw.spvi.util.AsmUtil;
import org.myfw.spvi.util.StringUtil;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

@SuppressWarnings("rawtypes")
public class PersistFactory implements Opcodes {
	private static Map beans = new HashMap();

	private PersistFactory() {
	}

	@SuppressWarnings("unchecked")
	public static AbstractPersist get(Object obj, Session session) throws Exception {
		String beanName = obj.getClass().getName();
		Object o = beans.get(beanName);
		Class klazz = null;
		if (o == null) {
			Class clazz = obj.getClass();
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);// 自动计算操作数栈、变量栈和帧
			MethodVisitor mv;
			String classInName = Type.getInternalName(obj.getClass());
			cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, classInName + "Persist", null, "org/myfw/spvi/AbstractPersist", null);
			// 生成构造函数
			{
				mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lorg/myfw/spvi/persistent/Session;)V", null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKESPECIAL, "org/myfw/spvi/AbstractPersist", "<init>", "()V");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitFieldInsn(PUTFIELD, classInName + "Persist", "session", "Lorg/myfw/spvi/persistent/Session;");
				mv.visitInsn(RETURN);
				mv.visitMaxs(2, 2);
				mv.visitEnd();
			}
			// getParameters方法
			List fs = getFields(clazz);
			{
				mv = cw.visitMethod(ACC_PROTECTED, "getParameters", "(Ljava/lang/Object;)[Ljava/lang/Object;", null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 1);
				mv.visitTypeInsn(CHECKCAST, classInName);
				mv.visitVarInsn(ASTORE, 2);
				mv.visitIntInsn(BIPUSH,fs.size());
				mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
				mv.visitVarInsn(ASTORE, 3);

				for (int i = 0, n = fs.size(); i < n; i++) {
					mv.visitVarInsn(ALOAD, 3);
					mv.visitIntInsn(BIPUSH, i);
					mv.visitVarInsn(ALOAD, 2);
					Field f = (Field) fs.get(i);
					Class type = f.getType();
					if (type.isPrimitive()) {
						mv.visitMethodInsn(INVOKEVIRTUAL, classInName, "get" + StringUtil.toUpper(f.getName(), 0, 1), "()" + Type.getDescriptor(type));
						mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(AsmUtil.getPackedType(type)), "valueOf", "(" + Type.getDescriptor(type) + ")" + Type.getDescriptor(AsmUtil.getPackedType(type)));
					} else {
						mv.visitMethodInsn(INVOKEVIRTUAL, classInName, "get" + StringUtil.toUpper(f.getName(), 0, 1), "()" + Type.getDescriptor(type));
					}
					mv.visitInsn(AASTORE);
				}

				mv.visitVarInsn(ALOAD, 3);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(1, 1);
				mv.visitEnd();
			}
			// 生成getObject方法
			{
				mv = cw.visitMethod(ACC_PROTECTED, "getObject", "(Ljava/sql/ResultSet;)Ljava/lang/Object;", null, new String[] { "java/sql/SQLException" });
				mv.visitCode();
				AsmUtil.newObject(mv, classInName);
				mv.visitVarInsn(ASTORE, 2);

				for (int i = 0, n = fs.size(); i < n; i++) {
					Field f = (Field) fs.get(i);
					Class type = f.getType();
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitIntInsn(BIPUSH, i + 1);
					mv.visitMethodInsn(INVOKEINTERFACE, "java/sql/ResultSet", "get" + StringUtil.toUpper(type.getSimpleName(), 0, 1), "(I)" + Type.getDescriptor(type));
					mv.visitMethodInsn(INVOKEVIRTUAL, classInName, "set" + StringUtil.toUpper(f.getName(), 0, 1), "(" + Type.getDescriptor(type) + ")V");
				}
				mv.visitVarInsn(ALOAD, 2);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(1, 1);
				mv.visitEnd();
			}
			// 获取表名
			String tableName = clazz.getSimpleName();
			Annotation[] tas = clazz.getAnnotations();
			if (tas != null && tas.length > 0) {
				Table t = (Table) tas[0];
				if (t.value() != null && !"".equals(t.value()))
					tableName = t.value();
			}
			// 获取列名
			String[] cols = new String[fs.size()];
			Field idField = null;
			int idIndex = -1;
			String idPolicy =null;
			for (int i = 0, n = fs.size(); i < n; i++) {
				Field f = (Field) fs.get(i);
				Annotation[] cas = f.getAnnotations();
				String colName = f.getName();
				for (int j = 0, c = cas.length; j < c; j++) {
					if (cas[j] instanceof Column) {
						Column column = (Column) cas[j];
						if (column.value() != null && !"".equals(column.value()))
							colName = column.value();
					}
					if (cas[j] instanceof Id) {
						Id id = (Id) cas[j];//id生成策略
						idPolicy = id.value().toString();
						idField = f;
						idIndex=i;
					}
				}
				cols[i]=colName;
			}
			// 生成getColumns的方法
			int len = cols.length;
			{
				mv = cw.visitMethod(ACC_PROTECTED, "getColumns", "()[Ljava/lang/String;", null, null);
				mv.visitCode();
				mv.visitIntInsn(BIPUSH,len+1);
				mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
				mv.visitVarInsn(ASTORE, 1);
				
				mv.visitVarInsn(ALOAD, 1);
				mv.visitIntInsn(BIPUSH,0);
				mv.visitLdcInsn(tableName);
				mv.visitInsn(AASTORE);
				
				for(int i=1;i<=len;i++){
					mv.visitVarInsn(ALOAD, 1);
					mv.visitIntInsn(BIPUSH,i);
					mv.visitLdcInsn(cols[i-1]);
					mv.visitInsn(AASTORE);
				}
				mv.visitVarInsn(ALOAD, 1);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(1, 1);
				mv.visitEnd();
			}
			//getIdIndex
			{
				mv = cw.visitMethod(ACC_PROTECTED, "getIdIndex", "()I", null, null);
				mv.visitCode();
				mv.visitIntInsn(BIPUSH,idIndex);
				mv.visitInsn(IRETURN);
				mv.visitMaxs(1, 1);
				mv.visitEnd();
			}
			//getIdPolicy
			{
				mv = cw.visitMethod(ACC_PROTECTED, "getIdPolicy", "()Ljava/lang/String;", null, null);
				mv.visitCode();
				mv.visitLdcInsn(idPolicy);
				mv.visitInsn(ARETURN);
				mv.visitMaxs(1, 1);
				mv.visitEnd();
			}
			// 生成设置主键的方法
			{
				mv = cw.visitMethod(ACC_PROTECTED, "setId", "(Ljava/lang/Object;Ljava/lang/String;)V", null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 1);
				mv.visitTypeInsn(CHECKCAST, classInName);
				mv.visitVarInsn(ASTORE, 3);
				mv.visitVarInsn(ALOAD, 3);
				mv.visitVarInsn(ALOAD, 2);
				Class idType = AsmUtil.getPackedType(idField.getType());
				mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(idType), "valueOf", "(Ljava/lang/String;)Ljava/lang/Long;");
				mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(idType), "longValue", "()J");
				mv.visitMethodInsn(INVOKEVIRTUAL, classInName, "set" + StringUtil.toUpper(idField.getName(), 0, 1), "(J)V");
				mv.visitInsn(RETURN);
				mv.visitMaxs(1, 1);
				mv.visitEnd();
			}
			cw.visitEnd();
			MyClassLoader mc = new MyClassLoader(cw);
			mc.saveToDisk("h:/test/" + classInName + "Persist.class");// 测试用的
			klazz = mc.defineClass();
			beans.put(beanName, klazz);
		} else {
			klazz = (Class) o;
		}
		AbstractPersist ap = (AbstractPersist) klazz.getConstructor(Session.class).newInstance(session);
		return ap;
	}

	@SuppressWarnings("unchecked")
	private static List getFields(Class clazz) {
		Field[] fs = clazz.getDeclaredFields();
		List fList = new ArrayList();
		aa: for (int i = 0, n = fs.length; i < n; i++) {
			Annotation[] cas = fs[i].getAnnotations();
			for (int j = 0, c = cas.length; j < c; j++) {
				if (cas[j] instanceof Transient)
					continue aa;
			}
			fList.add(fs[i]);
		}
		return fList;
	}
}
