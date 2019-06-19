package ru.will.git.clientwg.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

import static org.objectweb.asm.Opcodes.*;

public final class ASMTransformer implements IClassTransformer
{
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (transformedName.equals("net.minecraft.client.multiplayer.PlayerControllerMP"))
			return transformPlayerControllerMP(basicClass);
		if (transformedName.equals("net.minecraft.client.Minecraft"))
			return transformMinecraft(basicClass);

		return basicClass;
	}

	public static byte[] transformPlayerControllerMP(byte[] basicClass)
	{
		ClassNode cNode = new ClassNode();
		new ClassReader(basicClass).accept(cNode, 0);

		for (MethodNode methodNode : cNode.methods)
		{
			if (ASMMethods.PlayerControllerMP_attackEntity.isMatch(methodNode))
			{
				insertEntityInteractCheckInStart(methodNode, returnVoid());
			}
			else if (ASMMethods.PlayerControllerMP_interactWithEntitySendPacket.isMatch(methodNode))
			{
				insertEntityInteractCheckInStart(methodNode, returnFalse());
			}
		}

		ClassWriter cWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cNode.accept(cWriter);
		return cWriter.toByteArray();
	}

	private static byte[] transformMinecraft(byte[] basicClass)
	{
		ClassNode cNode = new ClassNode();
		new ClassReader(basicClass).accept(cNode, 0);

		for (MethodNode methodNode : cNode.methods)
		{
			if (ASMMethods.Minecraft_func_147115_a.isMatch(methodNode))
			{
				insertBlockInteractCheckAfterMopBlockZ(methodNode, returnVoid());
			}
			else if (ASMMethods.Minecraft_func_147116_af.isMatch(methodNode))
			{
				insertBlockInteractCheckAfterMopBlockZ(methodNode, returnVoid());
			}
		}

		ClassWriter cWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cNode.accept(cWriter);
		return cWriter.toByteArray();
	}

	private static void insertBlockInteractCheckAfterMopBlockZ(MethodNode methodNode, InsnList returnInsn)
	{
		FieldInsnNode findNode = null;

		for (Iterator<AbstractInsnNode> iter = methodNode.instructions.iterator(); iter.hasNext(); )
		{
			AbstractInsnNode insn = iter.next();
			if (ASMFields.MovingObjectPosition_blockZ.isMatch(insn))
			{
				FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
				if (fieldInsnNode.getOpcode() == GETFIELD)
				{
					findNode = fieldInsnNode;
					break;
				}
			}
		}

		if (findNode != null)
		{
			AbstractInsnNode istoreNode = findNode.getNext();
			if (istoreNode != null && istoreNode.getOpcode() == ISTORE)
			{
				LabelNode continueExecutionLabel = new LabelNode(new Label());
				MethodInsnNode hookInsn = new MethodInsnNode(INVOKESTATIC, MethodHooks.OWNER, MethodHooks.NAME_BLOCK_INTERACT, MethodHooks.DESC_BLOCK_INTERACT, false);
				JumpInsnNode jumpInsn = new JumpInsnNode(IFEQ, continueExecutionLabel);

				InsnList list = new InsnList();
				list.add(hookInsn);
				list.add(jumpInsn);
				list.add(returnInsn);
				list.add(continueExecutionLabel);
				methodNode.instructions.insert(istoreNode, list);
			}
		}
	}

	private static void insertEntityInteractCheckInStart(MethodNode methodNode, InsnList returnInsn)
	{
		LabelNode continueExecutionLabel = new LabelNode(new Label());
		MethodInsnNode hookInsn = new MethodInsnNode(INVOKESTATIC, MethodHooks.OWNER, MethodHooks.NAME_ENTITY_INTERACT, MethodHooks.DESC_ENTITY_INTERACT, false);
		JumpInsnNode jumpInsn = new JumpInsnNode(IFEQ, continueExecutionLabel);

		InsnList list = new InsnList();
		list.add(hookInsn);
		list.add(jumpInsn);
		list.add(returnInsn);
		list.add(continueExecutionLabel);

		methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), list);
	}

	private static InsnList returnVoid()
	{
		InsnList list = new InsnList();
		list.add(new InsnNode(RETURN));
		return list;
	}

	private static InsnList returnFalse()
	{
		InsnList list = new InsnList();
		list.add(new InsnNode(ICONST_0));
		list.add(new InsnNode(IRETURN));
		return list;
	}
}
