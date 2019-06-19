package ru.will.git.thaumcraft.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import thaumcraft.codechicken.core.launch.DepLoader;

public final class ThaumCraftClassTransformer implements IClassTransformer
{
	private static final boolean DEBUG = false;
	private static final Logger LOGGER = LogManager.getLogger("ThaumCraft");

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (basicClass == null || basicClass.length == 0)
			return basicClass;

		ClassNode classNode = new ClassNode();
		new ClassReader(basicClass).accept(classNode, 0);

		boolean transformed = transformedName.equals("net.minecraft.item.Item") ? transformItem(classNode) : transformAnyClass(classNode);
		if (transformed)
		{
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(classWriter);
			byte[] bytes = classWriter.toByteArray();
			if (DEBUG)
				LOGGER.info("Class {} transformed", classNode.name);
			return bytes;
		}

		return basicClass;
	}

	private static boolean transformItem(ClassNode classNode)
	{
		if (classNode.interfaces.add(IItemHook.INTERNAL_CLASS_NAME))
		{
			if (DEBUG)
				LOGGER.info("Transforming class {}...", classNode.name);

			MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC, IItemHook.METHOD_NAME, IItemHook.METHOD_DESC, null, null);
			mv.visitCode();
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitInsn(Opcodes.IRETURN);
			mv.visitEnd();

			if (DEBUG)
				LOGGER.info("Interface {} implemented in class {}", IItemHook.INTERNAL_CLASS_NAME, classNode.name);

			return true;
		}

		return false;
	}

	private static boolean transformAnyClass(ClassNode classNode)
	{
		String methodName = DepLoader.isObfuscated() ? "func_77659_a" : "onItemRightClick";
		String methodDesc = "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;";

		if (findMethod(classNode, methodName, methodDesc) != null && findMethod(classNode, IItemHook.METHOD_NAME, IItemHook.METHOD_DESC) == null)
		{
			if (DEBUG)
				LOGGER.info("Transforming class {}...", classNode.name);

			MethodVisitor mv = classNode.visitMethod(Opcodes.ACC_PUBLIC, IItemHook.METHOD_NAME, IItemHook.METHOD_DESC, null, null);
			mv.visitCode();
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitInsn(Opcodes.IRETURN);
			mv.visitEnd();

			return true;
		}

		return false;
	}

	private static MethodNode findMethod(ClassNode classNode, String name, String desc)
	{
		for (MethodNode methodNode : classNode.methods)
		{
			if (methodNode.name.equals(name) && methodNode.desc.equals(desc))
				return methodNode;
		}
		return null;
	}
}
