package ru.will.git.clientwg.asm;

import com.google.common.base.Preconditions;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;

public enum ASMFields
{
	MovingObjectPosition_blockZ("net/minecraft/util/MovingObjectPosition", "blockZ", "field_72309_d", "I");

	private final String owner;
	private final String name;
	private final String srgName;
	private final String desc;

	ASMFields(String owner, String name, String srgName, String desc)
	{
		this.owner = Preconditions.checkNotNull(owner);
		this.srgName = Preconditions.checkNotNull(srgName);
		this.name = name == null ? srgName : name;
		this.desc = Preconditions.checkNotNull(desc);
	}

	public boolean isMatch(FieldNode fieldNode)
	{
		return fieldNode.name.equals(this.getRuntimeName()) && fieldNode.desc.equals(this.desc);
	}

	public boolean isMatch(AbstractInsnNode insnNode)
	{
		return insnNode instanceof FieldInsnNode && this.isMatch((FieldInsnNode) insnNode);
	}

	public boolean isMatch(FieldInsnNode insnNode)
	{
		return insnNode.owner.equals(this.owner) && insnNode.name.equals(this.getRuntimeName()) && insnNode.desc.equals(this.desc);
	}

	public String getOwner()
	{
		return this.owner;
	}

	public String getName()
	{
		return this.name;
	}

	public String getSrgName()
	{
		return this.srgName;
	}

	public String getRuntimeName()
	{
		return CoreMod.isObfuscated() ? this.srgName : this.name;
	}

	public String getDesc()
	{
		return this.desc;
	}
}
