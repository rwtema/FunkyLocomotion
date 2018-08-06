package com.rwtema.funkylocomotion.asm;

import com.google.common.collect.Lists;
import com.rwtema.funkylocomotion.helper.ItemHelper;
import com.rwtema.funkylocomotion.items.ItemWrench;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import static org.objectweb.asm.Opcodes.*;

public class WrenchFactory {
	private static LaunchClassLoader loader = (LaunchClassLoader) ItemWrench.class.getClassLoader();

	public static ItemWrench makeMeAWrench() {
		ArrayList<ClassNode> nodes = new ArrayList<>(ItemHelper.wrenchClassNames.length);
		ArrayList<String> ifaceList = new ArrayList<>(ItemHelper.wrenchClassNames.length);
		LinkedList<String> toCheck = Lists.newLinkedList();
		Collections.addAll(toCheck, ItemHelper.wrenchClassNames);
		while (!toCheck.isEmpty()) {
			try {
				String wrenchClassName = toCheck.poll();
				byte[] classBytes = loader.getClassBytes(wrenchClassName);
				if (classBytes != null) {
					ClassNode node = new ClassNode(ASM5);
					ClassReader reader = new ClassReader(classBytes);
					reader.accept(node, ClassReader.EXPAND_FRAMES);
					for (String anInterface : node.interfaces) {
						toCheck.add(anInterface.replace('/', '.'));
					}
					nodes.add(node);
					ifaceList.add(wrenchClassName.replace('.', '/'));
				}
			} catch (IOException ignore) {

			}
		}

		if (nodes.isEmpty()) return new ItemWrench();

		HashSet<String> methods = new HashSet<>();

		try {
			byte[] classBytes = loader.getClassBytes(ItemWrench.class.getName());

			ClassNode node = new ClassNode(ASM5);
			ClassReader reader = new ClassReader(classBytes);
			reader.accept(node, ClassReader.EXPAND_FRAMES);

			for (MethodNode method : node.methods) {
				methods.add(getMethodDesc(method));
			}
		} catch (IOException ignore) {

		}

		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;

		String name = "FLM_ItemWrench";
		String superName = Type.getInternalName(ItemWrench.class);

		String[] ifaces = ifaceList.toArray(new String[ifaceList.size()]);

		cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER, name, null, superName, ifaces);

		cw.visitSource(".dynamic", null);

		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}

		for (ClassNode node : nodes) {
			for (MethodNode method : node.methods) {
				String mn = getMethodDesc(method);
				if (methods.contains(mn))
					continue;
				methods.add(mn);

				Type returnType = Type.getReturnType(method.desc);
				int returnOpCode = returnType.getOpcode(IRETURN);

				mv = cw.visitMethod(ACC_PUBLIC, method.name, method.desc, null, null);
				mv.visitCode();

				switch (returnOpCode) {
					case RETURN:
						break;
					case IRETURN:
						mv.visitInsn(returnType == Type.BOOLEAN_TYPE ? ICONST_1 : ICONST_0);
						break;
					case LRETURN:
						mv.visitInsn(LCONST_0);
						break;
					case FRETURN:
						mv.visitInsn(FCONST_0);
						break;
					case DRETURN:
						mv.visitInsn(DCONST_0);
						break;
					case ARETURN:
						mv.visitInsn(ACONST_NULL);
						break;
				}
				mv.visitInsn(returnOpCode);
				mv.visitInsn(RETURN);

				mv.visitMaxs(returnOpCode != RETURN ? 1 : 0, 1 + Type.getArgumentTypes(method.desc).length);
				mv.visitEnd();
			}
		}

		cw.visitEnd();

		Class<?> ret = (new ASMClassLoader()).define(name, cw.toByteArray());

		try {
			return (ItemWrench) ret.newInstance();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static String getMethodDesc(MethodNode method) {
		return method.name + "_" + method.desc;
	}

	private static class ASMClassLoader extends ClassLoader {
		private ASMClassLoader() {
			super(ASMClassLoader.class.getClassLoader());
		}

		public Class<?> define(String name, byte[] data) {
			return defineClass(name, data, 0, data.length);
		}
	}

}
