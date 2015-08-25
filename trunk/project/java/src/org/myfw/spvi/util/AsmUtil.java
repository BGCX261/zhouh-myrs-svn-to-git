/**
 * 
 */
package org.myfw.spvi.util;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author zhouhui
 * 
 */
public class AsmUtil implements Opcodes {
	private AsmUtil(){}
	/**
	 * 无参构造函数
	 * 
	 * @param cw
	 * @param innerClassName
	 *            超类名称
	 */
	public static void init(ClassWriter cw, String internalName) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, internalName, "<init>", "()V");
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
	/**
	 * 包装基本类型
	 * @param type
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Class getPackedType(Class type) {
		if (type.isPrimitive()) {
			if (type.equals(boolean.class)) {
				type = Boolean.class;
			} else if (type.equals(byte.class)) {
				type = Byte.class;
			} else if (type.equals(short.class)) {
				type = Short.class;
			} else if (type.equals(char.class)) {
				type = Character.class;
			} else if (type.equals(int.class)) {
				type = Integer.class;
			} else if (type.equals(float.class)) {
				type = Float.class;
			} else if (type.equals(double.class)) {
				type = Double.class;
			} else if (type.equals(long.class)) {
				type = Long.class;
			}
		}
		return type;
	}

	/**
	 * 创建一个对象
	 * 
	 * @param mv
	 * @param internalName
	 *            对象名称
	 */
	public static void newObject(MethodVisitor mv, String internalName) {
		mv.visitTypeInsn(NEW, internalName);
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, internalName, "<init>", "()V");
	}
	/**
	 * 加载方法参数
	 * @param mv
	 * @param acc 	访问标识
	 * @param args 	方法参数类型数组
	 */
	public static void loadArgs(MethodVisitor mv, int acc, Type[] args) {
		int start = acc != ACC_STATIC ? 1 : 0;
		for (int i = 0, n = args.length; i < n; i++) {
			mv.visitVarInsn(getLoad(args[i]), i + start);
		}
	}
	public static int getReturn(Type type){
		int ret;
		switch (type.getSort()) {
		case Type.VOID:
			ret = RETURN;
			break;
		case Type.BOOLEAN:
		case Type.CHAR:
		case Type.BYTE:
		case Type.SHORT:
		case Type.INT:
			ret = IRETURN;
			break;
		case Type.FLOAT:
			ret = FRETURN;
			break;
		case Type.LONG:
			ret = LRETURN;
			break;
		case Type.DOUBLE:
			ret = DRETURN;
			break;
		default:
			ret = ARETURN;
			break;
		}
		return ret;
	}
	public static int getLoad(Type type){
		int ret;
		switch (type.getSort()) {
		case Type.BOOLEAN:
		case Type.CHAR:
		case Type.BYTE:
		case Type.SHORT:
		case Type.INT:
			ret = ILOAD;
			break;
		case Type.FLOAT:
			ret = FLOAD;
			break;
		case Type.LONG:
			ret = LLOAD;
			break;
		case Type.DOUBLE:
			ret = DLOAD;
			break;
		default:
			ret = ALOAD;
			break;
		}
		return ret;
	}
}
