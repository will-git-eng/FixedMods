package ru.will.git.clientwg.asm;

import com.google.common.base.Preconditions;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public enum ASMMethods
{
	Minecraft_func_147115_a("net/minecraft/client/Minecraft", null, "func_147115_a", "(Z)V"),
	Minecraft_func_147116_af("net/minecraft/client/Minecraft", null, "func_147116_af", "()V"),
	PlayerControllerMP_interactWithEntitySendPacket("net/minecraft/client/multiplayer/PlayerControllerMP", "interactWithEntitySendPacket", "func_78768_b", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)Z"),
	PlayerControllerMP_attackEntity("net/minecraft/client/multiplayer/PlayerControllerMP", "attackEntity", "func_78764_a", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V");

	private final String owner;
	private final String name;
	private final String srgName;
	private final String desc;

	ASMMethods(String owner, String name, String srgName, String desc)
	{
		this.owner = Preconditions.checkNotNull(owner);
		this.srgName = Preconditions.checkNotNull(srgName);
		this.name = name == null ? srgName : name;
		this.desc = Preconditions.checkNotNull(desc);
	}

	public boolean isMatch(MethodNode methodNode)
	{
		return methodNode.name.equals(this.getRuntimeName()) && methodNode.desc.equals(this.desc);
	}

	public boolean isMatch(AbstractInsnNode insnNode)
	{
		return insnNode instanceof MethodInsnNode && this.isMatch((MethodInsnNode) insnNode);
	}

	public boolean isMatch(MethodInsnNode insnNode)
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
