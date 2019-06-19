package drzhark.mocreatures.entity.passive;

import java.util.List;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import drzhark.mocreatures.MoCTools;
import drzhark.mocreatures.MoCreatures;
import drzhark.mocreatures.entity.MoCEntityTameableAnimal;
import drzhark.mocreatures.inventory.MoCAnimalChest;
import drzhark.mocreatures.network.MoCMessageHandler;
import drzhark.mocreatures.network.message.MoCMessageAnimation;
import drzhark.mocreatures.network.message.MoCMessageHeart;
import drzhark.mocreatures.network.message.MoCMessageShuffle;
import drzhark.mocreatures.network.message.MoCMessageVanish;
import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockJukebox.TileEntityJukebox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class MoCEntityHorse extends MoCEntityTameableAnimal
{
	private int gestationtime;
	private int countEating;
	private int textCounter;
	private int fCounter;
	public int shuffleCounter;
	public int wingFlapCounter;
	private float transFloat = 0.2F;
	public MoCAnimalChest localhorsechest;
	public boolean eatenpumpkin;
	private boolean hasReproduced;
	private int nightmareInt;
	public ItemStack localstack;
	public int mouthCounter;
	public int standCounter;
	public int tailCounter;
	public int vanishCounter;
	public int sprintCounter;
	public int transformType;
	public int transformCounter;

	public MoCEntityHorse(World world)
	{
		super(world);
		this.setSize(1.4F, 1.6F);
		this.gestationtime = 0;
		this.eatenpumpkin = false;
		this.nightmareInt = 0;
		super.isImmuneToFire = false;
		this.setEdad(50);
		this.setChestedHorse(false);
		super.roper = null;
		super.stepHeight = 1.0F;
		if (MoCreatures.isServer())
			if (super.rand.nextInt(5) == 0)
				this.setAdult(false);
			else
				this.setAdult(true);

	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(this.calculateMaxHealth());
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		super.dataWatcher.addObject(22, Byte.valueOf((byte) 0));
		super.dataWatcher.addObject(23, Byte.valueOf((byte) 0));
		super.dataWatcher.addObject(24, Byte.valueOf((byte) 0));
		super.dataWatcher.addObject(25, Integer.valueOf(0));
		super.dataWatcher.addObject(26, Byte.valueOf((byte) 0));
	}

	@Override
	public boolean attackEntityFrom(DamageSource damagesource, float i)
	{
		Entity entity = damagesource.getEntity();
		if (super.riddenByEntity != null && entity == super.riddenByEntity)
			return false;
		else if (entity instanceof EntityWolf)
		{
			EntityCreature entitycreature = (EntityCreature) entity;
			entitycreature.setAttackTarget((EntityLivingBase) null);
			return false;
		}
		else
		{
			i = i - (this.getArmorType() + 2);
			if (i < 0.0F)
				i = 0.0F;

			return super.attackEntityFrom(damagesource, i);
		}
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return super.riddenByEntity == null;
	}

	@Override
	public boolean checkSpawningBiome()
	{
		int i = MathHelper.floor_double(super.posX);
		int j = MathHelper.floor_double(super.boundingBox.minY);
		int k = MathHelper.floor_double(super.posZ);
		BiomeGenBase currentbiome = MoCTools.Biomekind(super.worldObj, i, j, k);
		String s = MoCTools.BiomeName(super.worldObj, i, j, k);
		if (BiomeDictionary.isBiomeOfType(currentbiome, Type.PLAINS) && super.rand.nextInt(3) == 0)
			this.setType(60);

		if (BiomeDictionary.isBiomeOfType(currentbiome, Type.DESERT))
			this.setType(60);

		if (s.toLowerCase().contains("prairie"))
			this.setType(super.rand.nextInt(5) + 1);

		return true;
	}

	public float colorFX(int sColor, int typeInt)
	{
		return typeInt == 48 ? sColor == 1 ? 0.69921875F : sColor == 2 ? 0.625F : 0.0859375F : typeInt == 49 ? sColor == 1 ? 0.57421875F : sColor == 2 ? 0.3515625F : 0.76171875F : typeInt == 51 ? sColor == 1 ? 0.1171875F : sColor == 2 ? 0.5625F : 0.99609375F : typeInt == 52 ? sColor == 1 ? 0.99609375F : sColor == 2 ? 0.41015625F : 0.703125F : typeInt == 53 ? sColor == 1 ? 0.734375F : sColor == 2 ? 0.9296875F : 0.40625F : typeInt == 54 ? sColor == 1 ? 0.4296875F : sColor == 2 ? 0.48046875F : 0.54296875F : typeInt == 55 ? sColor == 1 ? 0.7578125F : sColor == 2 ? 0.11328125F : 0.1328125F : typeInt == 56 ? sColor == 1 ? 0.24609375F : sColor == 2 ? 0.17578125F : 0.99609375F : typeInt == 57 ? sColor == 1 ? 0.26953125F : sColor == 2 ? 0.5703125F : 0.56640625F : typeInt == 58 ? sColor == 1 ? 0.3515625F : sColor == 2 ? 0.53125F : 0.16796875F : typeInt == 59 ? sColor == 1 ? 0.8515625F : sColor == 2 ? 0.15625F : 0.0F : typeInt > 22 && typeInt < 26 ? sColor == 1 ? 0.234375F : sColor == 2 ? 0.69921875F : 0.4375F : typeInt == 40 ? sColor == 1 ? 0.54296875F : sColor == 2 ? 0.0F : 0.0F : sColor == 1 ? 0.99609375F : sColor == 2 ? 0.921875F : 0.54296875F;
	}

	public void dissapearHorse()
	{
		super.isDead = true;
	}

	private void drinkingHorse()
	{
		this.openMouth();
		MoCTools.playCustomSound(this, "drinking", super.worldObj);
	}

	@Override
	public void dropArmor()
	{
		if (MoCreatures.isServer())
		{
			int i = this.getArmorType();
			if (i != 0)
				MoCTools.playCustomSound(this, "armoroff", super.worldObj);

			if (i == 1)
			{
				EntityItem entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(Items.iron_horse_armor, 1));
				entityitem.delayBeforeCanPickup = 10;
				super.worldObj.spawnEntityInWorld(entityitem);
			}

			if (i == 2)
			{
				EntityItem entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(Items.golden_horse_armor, 1));
				entityitem.delayBeforeCanPickup = 10;
				super.worldObj.spawnEntityInWorld(entityitem);
			}

			if (i == 3)
			{
				EntityItem entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(Items.diamond_horse_armor, 1));
				entityitem.delayBeforeCanPickup = 10;
				super.worldObj.spawnEntityInWorld(entityitem);
			}

			if (i == 4)
			{
				EntityItem entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(MoCreatures.horsearmorcrystal, 1));
				entityitem.delayBeforeCanPickup = 10;
				super.worldObj.spawnEntityInWorld(entityitem);
			}

			this.setArmorType((byte) 0);
		}

	}

	public void dropBags()
	{
		if (this.isBagger() && this.getChestedHorse() && MoCreatures.isServer())
		{
			EntityItem entityitem = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(Blocks.chest, 1));
			float f3 = 0.05F;
			entityitem.motionX = (float) super.worldObj.rand.nextGaussian() * f3;
			entityitem.motionY = (float) super.worldObj.rand.nextGaussian() * f3 + 0.2F;
			entityitem.motionZ = (float) super.worldObj.rand.nextGaussian() * f3;
			super.worldObj.spawnEntityInWorld(entityitem);
			this.setChestedHorse(false);
		}
	}

	private void eatingHorse()
	{
		this.openMouth();
		MoCTools.playCustomSound(this, "eating", super.worldObj);
	}

	@Override
	protected void fall(float f)
	{
		if (!this.isFlyer() && !this.isFloater())
		{
			float i = (float) (Math.ceil(f - 3.0F) / 2.0D);
			if (MoCreatures.isServer() && i > 0.0F)
			{
				if (this.getType() >= 10)
					i /= 2.0F;

				if (i > 1.0F)
					this.attackEntityFrom(DamageSource.fall, i);

				if (super.riddenByEntity != null && i > 1.0F)
					super.riddenByEntity.attackEntityFrom(DamageSource.fall, i);

				Block block = super.worldObj.getBlock(MathHelper.floor_double(super.posX), MathHelper.floor_double(super.posY - 0.2000000029802322D - super.prevRotationPitch), MathHelper.floor_double(super.posZ));
				if (block != Blocks.air)
				{
					SoundType stepsound = block.stepSound;
					super.worldObj.playSoundAtEntity(this, stepsound.getStepResourcePath(), stepsound.getVolume() * 0.5F, stepsound.getPitch() * 0.75F);
				}
			}

		}
	}

	@Override
	public byte getArmorType()
	{
		return (byte) super.dataWatcher.getWatchableObjectInt(25);
	}

	public int getInventorySize()
	{
		return this.getType() == 40 ? 18 : this.getType() > 64 ? 27 : 9;
	}

	public boolean getChestedHorse()
	{
		return super.dataWatcher.getWatchableObjectByte(23) == 1;
	}

	protected MoCEntityHorse getClosestMommy(Entity entity, double d)
	{
		double d1 = -1.0D;
		MoCEntityHorse entityliving = null;
		List list = super.worldObj.getEntitiesWithinAABBExcludingEntity(entity, entity.boundingBox.expand(d, d, d));

		for (int i = 0; i < list.size(); ++i)
		{
			Entity entity1 = (Entity) list.get(i);
			if (entity1 instanceof MoCEntityHorse && (!(entity1 instanceof MoCEntityHorse) || ((MoCEntityHorse) entity1).getHasBred()))
			{
				double d2 = entity1.getDistanceSq(entity.posX, entity.posY, entity.posZ);
				if ((d < 0.0D || d2 < d * d) && (d1 == -1.0D || d2 < d1))
				{
					d1 = d2;
					entityliving = (MoCEntityHorse) entity1;
				}
			}
		}

		return entityliving;
	}

	@Override
	public double getCustomJump()
	{
		double HorseJump = 0.4D;
		if (this.getType() < 6)
			HorseJump = 0.4D;
		else if (this.getType() > 5 && this.getType() < 11)
			HorseJump = 0.45D;
		else if (this.getType() > 10 && this.getType() < 16)
			HorseJump = 0.5D;
		else if (this.getType() > 15 && this.getType() < 21)
			HorseJump = 0.55D;
		else if (this.getType() > 20 && this.getType() < 26)
			HorseJump = 0.45D;
		else if (this.getType() > 25 && this.getType() < 30)
			HorseJump = 0.5D;
		else if (this.getType() >= 30 && this.getType() < 40)
			HorseJump = 0.55D;
		else if (this.getType() >= 40 && this.getType() < 60)
			HorseJump = 0.6D;
		else if (this.getType() >= 60)
			HorseJump = 0.45D;

		return HorseJump;
	}

	@Override
	public double getCustomSpeed()
	{
		double HorseSpeed = 0.8D;
		if (this.getType() < 6)
			HorseSpeed = 0.9D;
		else if (this.getType() > 5 && this.getType() < 11)
			HorseSpeed = 1.0D;
		else if (this.getType() > 10 && this.getType() < 16)
			HorseSpeed = 1.1D;
		else if (this.getType() > 15 && this.getType() < 21)
			HorseSpeed = 1.2D;
		else if (this.getType() > 20 && this.getType() < 26)
			HorseSpeed = 0.8D;
		else if (this.getType() > 25 && this.getType() < 30)
			HorseSpeed = 1.0D;
		else if (this.getType() > 30 && this.getType() < 40)
			HorseSpeed = 1.2D;
		else if (this.getType() > 40 && this.getType() < 60)
			HorseSpeed = 1.3D;
		else if (this.getType() != 60 && this.getType() != 61)
		{
			if (this.getType() == 65)
				HorseSpeed = 0.7D;
			else if (this.getType() > 65)
				HorseSpeed = 0.9D;
		}
		else
			HorseSpeed = 1.1D;

		if (this.sprintCounter > 0 && this.sprintCounter < 150)
			HorseSpeed *= 1.5D;

		if (this.sprintCounter > 150)
			HorseSpeed *= 0.5D;

		return HorseSpeed;
	}

	@Override
	protected String getDeathSound()
	{
		this.openMouth();
		return this.isUndead() ? "horsedyingundead" : this.isGhost() ? "horsedyingghost" : this.getType() != 60 && this.getType() != 61 ? this.getType() >= 65 && this.getType() <= 67 ? "donkeydying" : "horsedying" : "zebrahurt";
	}

	@Override
	public boolean getDisplayName()
	{
		return this.isGhost() && this.getEdad() < 10 ? false : this.getName() != null && !this.getName().equals("");
	}

	@Override
	protected Item getDropItem()
	{
		boolean flag = super.rand.nextInt(100) < MoCreatures.proxy.rareItemDropChance;
		return !flag || this.getType() != 36 && (this.getType() < 50 || this.getType() >= 60) ? this.getType() == 39 ? Items.feather : this.getType() == 40 ? Items.feather : this.getType() == 38 && flag && super.worldObj.provider.isHellWorld ? MoCreatures.heartfire : this.getType() == 32 && flag ? MoCreatures.heartdarkness : this.getType() == 26 ? Items.bone : this.getType() != 23 && this.getType() != 24 && this.getType() != 25 ? this.getType() != 21 && this.getType() != 22 ? Items.leather : Items.ghast_tear : flag ? MoCreatures.heartundead : Items.rotten_flesh : MoCreatures.unicornhorn;
	}

	public boolean getEating()
	{
		return super.dataWatcher.getWatchableObjectByte(24) == 1;
	}

	public boolean getHasBred()
	{
		return super.dataWatcher.getWatchableObjectByte(26) == 1;
	}

	public boolean getHasReproduced()
	{
		return this.hasReproduced;
	}

	@Override
	protected String getHurtSound()
	{
		this.openMouth();
		if (this.isFlyer() && super.riddenByEntity == null)
			this.wingFlap();
		else if (super.rand.nextInt(3) == 0)
			this.stand();

		return this.isUndead() ? "mocreatures:horsehurtundead" : this.isGhost() ? "mocreatures:horsehurtghost" : this.getType() != 60 && this.getType() != 61 ? this.getType() >= 65 && this.getType() <= 67 ? "mocreatures:donkeyhurt" : "mocreatures:horsehurt" : "mocreatures:zebrahurt";
	}

	@Override
	public boolean getIsRideable()
	{
		return super.dataWatcher.getWatchableObjectByte(22) == 1;
	}

	@Override
	protected String getLivingSound()
	{
		this.openMouth();
		if (super.rand.nextInt(10) == 0 && !this.isMovementCeased())
			this.stand();

		return this.isUndead() ? "mocreatures:horsegruntundead" : this.isGhost() ? "mocreatures:horsegruntghost" : this.getType() != 60 && this.getType() != 61 ? this.getType() >= 65 && this.getType() <= 67 ? "mocreatures:donkeygrunt" : "mocreatures:horsegrunt" : "mocreatures:zebragrunt";
	}

	@Override
	protected String getMadSound()
	{
		this.openMouth();
		this.stand();
		return this.isUndead() ? "horsemadundead" : this.isGhost() ? "horsemadghost" : this.getType() != 60 && this.getType() != 61 ? this.getType() >= 65 && this.getType() <= 67 ? "donkeyhurt" : "horsemad" : "zebrahurt";
	}

	public float calculateMaxHealth()
	{
		int maximumHealth = 10;
		if (this.getType() < 6)
			maximumHealth = 15;
		else if (this.getType() > 5 && this.getType() < 11)
			maximumHealth = 20;
		else if (this.getType() > 10 && this.getType() < 16)
			maximumHealth = 25;
		else if (this.getType() > 15 && this.getType() < 21)
			maximumHealth = 25;
		else if (this.getType() > 20 && this.getType() < 26)
			maximumHealth = 25;
		else if (this.getType() > 25 && this.getType() < 30)
			maximumHealth = 15;
		else if (this.getType() >= 30 && this.getType() < 40)
			maximumHealth = 30;
		else if (this.getType() == 40)
			maximumHealth = 40;
		else if (this.getType() > 40 && this.getType() < 60)
			maximumHealth = 20;
		else if (this.getType() >= 60)
			maximumHealth = 20;

		return maximumHealth;
	}

	@Override
	public int getMaxTemper()
	{
		return this.getType() == 60 ? 200 : 100;
	}

	public int getNightmareInt()
	{
		return this.nightmareInt;
	}

	@Override
	protected float getSoundVolume()
	{
		return 0.8F;
	}

	@Override
	public int getTalkInterval()
	{
		return 400;
	}

	@Override
	public ResourceLocation getTexture()
	{
		String tempTexture;
		switch (this.getType())
		{
			case 1:
				tempTexture = "horsewhite.png";
				break;
			case 2:
				tempTexture = "horsecreamy.png";
				break;
			case 3:
				tempTexture = "horsebrown.png";
				break;
			case 4:
				tempTexture = "horsedarkbrown.png";
				break;
			case 5:
				tempTexture = "horseblack.png";
				break;
			case 6:
				tempTexture = "horsebrightcreamy.png";
				break;
			case 7:
				tempTexture = "horsespeckled.png";
				break;
			case 8:
				tempTexture = "horsepalebrown.png";
				break;
			case 9:
				tempTexture = "horsegrey.png";
				break;
			case 10:
			case 14:
			case 15:
			case 18:
			case 19:
			case 20:
			case 29:
			case 31:
			case 33:
			case 34:
			case 35:
			case 37:
			case 41:
			case 42:
			case 43:
			case 44:
			case 45:
			case 46:
			case 47:
			case 62:
			case 63:
			case 64:
			default:
				tempTexture = "horsebug.png";
				break;
			case 11:
				tempTexture = "horsepinto.png";
				break;
			case 12:
				tempTexture = "horsebrightpinto.png";
				break;
			case 13:
				tempTexture = "horsepalespeckles.png";
				break;
			case 16:
				tempTexture = "horsespotted.png";
				break;
			case 17:
				tempTexture = "horsecow.png";
				break;
			case 21:
				tempTexture = "horseghost.png";
				break;
			case 22:
				tempTexture = "horseghostb.png";
				break;
			case 23:
				tempTexture = "horseundead.png";
				break;
			case 24:
				tempTexture = "horseundeadunicorn.png";
				break;
			case 25:
				tempTexture = "horseundeadpegasus.png";
				break;
			case 26:
				tempTexture = "horseskeleton.png";
				break;
			case 27:
				tempTexture = "horseunicornskeleton.png";
				break;
			case 28:
				tempTexture = "horsepegasusskeleton.png";
				break;
			case 30:
				tempTexture = "horsebug.png";
				break;
			case 32:
				tempTexture = "horsebat.png";
				break;
			case 36:
				tempTexture = "horseunicorn.png";
				break;
			case 38:
				super.isImmuneToFire = true;
				tempTexture = "horsenightmare.png";
				break;
			case 39:
				tempTexture = "horsepegasus.png";
				break;
			case 40:
				super.isImmuneToFire = true;
				tempTexture = "horsedarkpegasus.png";
				break;
			case 48:
				tempTexture = "horsefairyyellow.png";
				break;
			case 49:
				tempTexture = "horsefairypurple.png";
				break;
			case 50:
				tempTexture = "horsefairywhite.png";
				break;
			case 51:
				tempTexture = "horsefairyblue.png";
				break;
			case 52:
				tempTexture = "horsefairypink.png";
				break;
			case 53:
				tempTexture = "horsefairylightgreen.png";
				break;
			case 54:
				tempTexture = "horsefairyblack.png";
				break;
			case 55:
				tempTexture = "horsefairyred.png";
				break;
			case 56:
				tempTexture = "horsefairydarkblue.png";
				break;
			case 57:
				tempTexture = "horsefairycyan.png";
				break;
			case 58:
				tempTexture = "horsefairygreen.png";
				break;
			case 59:
				tempTexture = "horsefairyorange.png";
				break;
			case 60:
				tempTexture = "horsezebra.png";
				break;
			case 61:
				tempTexture = "horsezorse.png";
				break;
			case 65:
				tempTexture = "horsedonkey.png";
				break;
			case 66:
				tempTexture = "horsemule.png";
				break;
			case 67:
				tempTexture = "horsezonky.png";
		}

		if ((this.isArmored() || this.isMagicHorse()) && this.getArmorType() > 0)
		{
			String armorTex = "";
			if (this.getArmorType() == 1)
				armorTex = "metal.png";

			if (this.getArmorType() == 2)
				armorTex = "gold.png";

			if (this.getArmorType() == 3)
				armorTex = "diamond.png";

			if (this.getArmorType() == 4)
				armorTex = "crystaline.png";

			return MoCreatures.proxy.getTexture(tempTexture.replace(".png", armorTex));
		}
		else if (this.isUndead() && this.getType() < 26)
		{
			String baseTex = "horseundead";
			int max = 79;
			if (this.getType() == 25)
				baseTex = "horseundeadpegasus";

			if (this.getType() == 24)
			{
				baseTex = "horseundeadunicorn";
				max = 69;
			}

			String iteratorTex = "1";
			if (MoCreatures.proxy.getAnimateTextures())
			{
				if (super.rand.nextInt(3) == 0)
					++this.textCounter;

				if (this.textCounter < 10)
					this.textCounter = 10;

				if (this.textCounter > max)
					this.textCounter = 10;

				iteratorTex = "" + this.textCounter;
				iteratorTex = iteratorTex.substring(0, 1);
			}

			String decayTex = "" + this.getEdad() / 100;
			decayTex = decayTex.substring(0, 1);
			return MoCreatures.proxy.getTexture(baseTex + decayTex + iteratorTex + ".png");
		}
		else if (!MoCreatures.proxy.getAnimateTextures())
			return MoCreatures.proxy.getTexture(tempTexture);
		else if (this.isNightmare())
		{
			if (super.rand.nextInt(1) == 0)
				++this.textCounter;

			if (this.textCounter < 10)
				this.textCounter = 10;

			if (this.textCounter > 59)
				this.textCounter = 10;

			String NTA = "horsenightmare";
			String NTB = "" + this.textCounter;
			NTB = NTB.substring(0, 1);
			String NTC = ".png";
			return MoCreatures.proxy.getTexture(NTA + NTB + NTC);
		}
		else
		{
			if (this.transformCounter != 0 && this.transformType != 0)
			{
				String newText = "horseundead.png";
				if (this.transformType == 23)
					newText = "horseundead.png";

				if (this.transformType == 24)
					newText = "horseundeadunicorn.png";

				if (this.transformType == 25)
					newText = "horseundeadpegasus.png";

				if (this.transformType == 36)
					newText = "horseunicorn.png";

				if (this.transformType == 39)
					newText = "horsepegasus.png";

				if (this.transformType == 40)
					newText = "horseblackpegasus.png";

				if (this.transformType == 48)
					newText = "horsefairyyellow.png";

				if (this.transformType == 49)
					newText = "horsefairypurple.png";

				if (this.transformType == 50)
					newText = "horsefairywhite.png";

				if (this.transformType == 51)
					newText = "horsefairyblue.png";

				if (this.transformType == 52)
					newText = "horsefairypink.png";

				if (this.transformType == 53)
					newText = "horsefairylightgreen.png";

				if (this.transformType == 54)
					newText = "horsefairyblack.png";

				if (this.transformType == 55)
					newText = "horsefairyred.png";

				if (this.transformType == 56)
					newText = "horsefairydarkblue.png";

				if (this.transformType == 57)
					newText = "horsefairycyan.png";

				if (this.transformType == 58)
					newText = "horsefairygreen.png";

				if (this.transformType == 59)
					newText = "horsefairyorange.png";

				if (this.transformType == 32)
					newText = "horsebat.png";

				if (this.transformType == 38)
					newText = "horsenightmare1.png";

				if (this.transformCounter % 5 == 0)
					return MoCreatures.proxy.getTexture(newText);

				if (this.transformCounter > 50 && this.transformCounter % 3 == 0)
					return MoCreatures.proxy.getTexture(newText);

				if (this.transformCounter > 75 && this.transformCounter % 4 == 0)
					return MoCreatures.proxy.getTexture(newText);
			}

			return MoCreatures.proxy.getTexture(tempTexture);
		}
	}

	public byte getVanishC()
	{
		return (byte) this.vanishCounter;
	}

	private int HorseGenetics(int typeA, int typeB)
	{
		boolean flag = MoCreatures.proxy.easyBreeding;
		if (typeA == typeB)
			return typeA;
		else if ((typeA != 60 || typeB >= 21) && (typeB != 60 || typeA >= 21))
		{
			if ((typeA != 65 || typeB >= 21) && (typeB != 65 || typeA >= 21))
			{
				if ((typeA != 60 || typeB != 65) && (typeB != 60 || typeA != 65))
				{
					if ((typeA <= 20 || typeB >= 21) && (typeB <= 20 || typeA >= 21))
					{
						if ((typeA != 36 || typeB != 39) && (typeB != 36 || typeA != 39))
						{
							if (typeA > 20 && typeB > 20 && typeA != typeB)
								return super.rand.nextInt(5) + 1;
							else
							{
								int chanceInt = super.rand.nextInt(4) + 1;
								if (!flag)
								{
									if (chanceInt == 1)
										return typeA;

									if (chanceInt == 2)
										return typeB;
								}

								return (typeA != 1 || typeB != 2) && (typeA != 2 || typeB != 1) ? (typeA != 1 || typeB != 3) && (typeA != 3 || typeB != 1) ? (typeA != 1 || typeB != 4) && (typeA != 4 || typeB != 1) ? (typeA != 1 || typeB != 5) && (typeA != 5 || typeB != 1) ? (typeA != 1 || typeB != 7) && (typeA != 7 || typeB != 1) ? (typeA != 1 || typeB != 8) && (typeA != 8 || typeB != 1) ? (typeA != 1 || typeB != 9) && (typeA != 9 || typeB != 1) ? (typeA != 1 || typeB != 11) && (typeA != 11 || typeB != 1) ? (typeA != 1 || typeB != 12) && (typeA != 12 || typeB != 1) ? (typeA != 1 || typeB != 17) && (typeA != 17 || typeB != 1) ? (typeA != 2 || typeB != 4) && (typeA != 4 || typeB != 2) ? (typeA != 2 || typeB != 5) && (typeA != 5 || typeB != 2) ? (typeA != 2 || typeB != 7) && (typeA != 7 || typeB != 2) ? (typeA != 2 || typeB != 8) && (typeA != 8 || typeB != 2) ? (typeA != 2 || typeB != 12) && (typeA != 12 || typeB != 2) ? (typeA != 2 || typeB != 16) && (typeA != 16 || typeB != 2) ? (typeA != 2 || typeB != 17) && (typeA != 17 || typeB != 2) ? (typeA != 3 || typeB != 4) && (typeA != 4 || typeB != 3) ? (typeA != 3 || typeB != 5) && (typeA != 5 || typeB != 3) ? (typeA != 3 || typeB != 6) && (typeA != 6 || typeB != 3) ? (typeA != 3 || typeB != 7) && (typeA != 7 || typeB != 3) ? (typeA != 3 || typeB != 9) && (typeA != 9 || typeB != 3) ? (typeA != 3 || typeB != 12) && (typeA != 12 || typeB != 3) ? (typeA != 3 || typeB != 16) && (typeA != 16 || typeB != 3) ? (typeA != 3 || typeB != 17) && (typeA != 17 || typeB != 3) ? (typeA != 4 || typeB != 6) && (typeA != 6 || typeB != 4) ? (typeA != 4 || typeB != 7) && (typeA != 7 || typeB != 4) ? (typeA != 4 || typeB != 9) && (typeA != 9 || typeB != 4) ? (typeA != 4 || typeB != 11) && (typeA != 11 || typeB != 4) ? (typeA != 4 || typeB != 12) && (typeA != 12 || typeB != 4) ? (typeA != 4 || typeB != 13) && (typeA != 13 || typeB != 4) ? (typeA != 4 || typeB != 16) && (typeA != 16 || typeB != 4) ? (typeA != 4 || typeB != 17) && (typeA != 17 || typeB != 4) ? (typeA != 5 || typeB != 6) && (typeA != 6 || typeB != 5) ? (typeA != 5 || typeB != 7) && (typeA != 7 || typeB != 5) ? (typeA != 5 || typeB != 8) && (typeA != 8 || typeB != 5) ? (typeA != 5 || typeB != 11) && (typeA != 11 || typeB != 5) ? (typeA != 5 || typeB != 12) && (typeA != 12 || typeB != 5) ? (typeA != 5 || typeB != 13) && (typeA != 13 || typeB != 5) ? (typeA != 5 || typeB != 16) && (typeA != 16 || typeB != 5) ? (typeA != 6 || typeB != 8) && (typeA != 8 || typeB != 6) ? (typeA != 6 || typeB != 17) && (typeA != 17 || typeB != 6) ? (typeA != 7 || typeB != 16) && (typeA != 16 || typeB != 7) ? (typeA != 8 || typeB != 11) && (typeA != 11 || typeB != 8) ? (typeA != 8 || typeB != 12) && (typeA != 12 || typeB != 8) ? (typeA != 8 || typeB != 13) && (typeA != 13 || typeB != 8) ? (typeA != 8 || typeB != 16) && (typeA != 16 || typeB != 8) ? (typeA != 8 || typeB != 17) && (typeA != 17 || typeB != 8) ? (typeA != 9 || typeB != 16) && (typeA != 16 || typeB != 9) ? (typeA != 11 || typeB != 16) && (typeA != 16 || typeB != 11) ? (typeA != 11 || typeB != 17) && (typeA != 17 || typeB != 11) ? (typeA != 12 || typeB != 16) && (typeA != 16 || typeB != 12) ? (typeA != 13 || typeB != 17) && (typeA != 17 || typeB != 13) ? typeA : 9 : 13 : 7 : 13 : 13 : 7 : 7 : 7 : 7 : 7 : 13 : 7 : 2 : 17 : 16 : 13 : 17 : 4 : 4 : 4 : 5 : 13 : 7 : 7 : 7 : 7 : 8 : 3 : 11 : 11 : 11 : 8 : 11 : 2 : 8 : 8 : 12 : 13 : 6 : 3 : 8 : 4 : 3 : 16 : 13 : 12 : 13 : 7 : 12 : 9 : 7 : 2 : 6;
							}
						}
						else
							return 50;
					}
					else
						return typeA < typeB ? typeA : typeB;
				}
				else
					return 67;
			}
			else
				return 66;
		}
		else
			return 61;
	}

	@Override
	public boolean interact(EntityPlayer entityplayer)
	{
		if (super.interact(entityplayer))
			return false;
		else if (this.getType() == 60 && !this.getIsTamed() && this.isZebraRunning())
			return false;
		else
		{
			ItemStack itemstack = entityplayer.inventory.getCurrentItem();
			if (itemstack != null && !this.getIsRideable() && itemstack.getItem() == Items.saddle)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				this.setRideable(true);
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && itemstack.getItem() == Items.iron_horse_armor && this.isArmored())
			{
				if (this.getArmorType() == 0)
					MoCTools.playCustomSound(this, "armorput", super.worldObj);

				this.dropArmor();
				this.setArmorType((byte) 1);
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				return true;
			}
			else if (itemstack != null && this.getIsTamed() && itemstack.getItem() == Items.golden_horse_armor && this.isArmored())
			{
				if (this.getArmorType() == 0)
					MoCTools.playCustomSound(this, "armorput", super.worldObj);

				this.dropArmor();
				this.setArmorType((byte) 2);
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				return true;
			}
			else if (itemstack != null && this.getIsTamed() && itemstack.getItem() == Items.diamond_horse_armor && this.isArmored())
			{
				if (this.getArmorType() == 0)
					MoCTools.playCustomSound(this, "armorput", super.worldObj);

				this.dropArmor();
				this.setArmorType((byte) 3);
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				return true;
			}
			else if (itemstack != null && this.getIsTamed() && itemstack.getItem() == MoCreatures.horsearmorcrystal && this.isMagicHorse())
			{
				if (this.getArmorType() == 0)
					MoCTools.playCustomSound(this, "armorput", super.worldObj);

				this.dropArmor();
				this.setArmorType((byte) 4);
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

				return true;
			}
			else if (itemstack != null && this.getIsTamed() && itemstack.getItem() == MoCreatures.essenceundead)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, new ItemStack(Items.glass_bottle));
				else
					entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));

				if (this.isUndead() || this.isGhost())
					this.setHealth(this.getMaxHealth());

				if (this.getType() != 39 && this.getType() != 32 && this.getType() != 40)
				{
					if (this.getType() != 36 && (this.getType() <= 47 || this.getType() >= 60))
					{
						if (this.getType() < 21 || this.getType() == 60 || this.getType() == 61)
							this.transform(23);
					}
					else
						this.transform(24);
				}
				else
					this.transform(25);

				this.drinkingHorse();
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && itemstack.getItem() == MoCreatures.essencefire)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, new ItemStack(Items.glass_bottle));
				else
					entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));

				if (this.isNightmare())
				{
					if (this.getIsAdult() && this.getHealth() == this.getMaxHealth())
						this.eatenpumpkin = true;

					this.setHealth(this.getMaxHealth());
				}

				if (this.getType() == 61)
					this.transform(38);

				this.drinkingHorse();
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && itemstack.getItem() == MoCreatures.essencedarkness)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, new ItemStack(Items.glass_bottle));
				else
					entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));

				if (this.getType() == 32)
				{
					if (this.getIsAdult() && this.getHealth() == this.getMaxHealth())
						this.eatenpumpkin = true;

					this.setHealth(this.getMaxHealth());
				}

				if (this.getType() == 61)
					this.transform(32);

				if (this.getType() == 39)
					this.transform(40);

				this.drinkingHorse();
				return true;
			}
			else if (itemstack != null && this.getIsTamed() && itemstack.getItem() == MoCreatures.essencelight)
			{
				if (--itemstack.stackSize == 0)
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, new ItemStack(Items.glass_bottle));
				else
					entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));

				if (this.isMagicHorse())
				{
					if (this.getIsAdult() && this.getHealth() == this.getMaxHealth())
						this.eatenpumpkin = true;

					this.setHealth(this.getMaxHealth());
				}

				if (this.isNightmare())
					this.transform(36);

				if (this.getType() == 32 && super.posY > 128.0D)
					this.transform(39);

				if (this.isUndead() && this.getIsAdult() && MoCreatures.isServer())
				{
					this.setEdad(10);
					if (this.getType() > 26)
						this.setType(this.getType() - 3);
				}

				this.drinkingHorse();
				return true;
			}
			else
			{
				if (itemstack != null && this.isAmuletHorse() && this.getIsTamed())
				{
					if ((this.getType() == 26 || this.getType() == 27 || this.getType() == 28) && itemstack.getItem() == MoCreatures.amuletbone)
					{
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);
						this.vanishHorse();
						return true;
					}

					if (this.getType() > 47 && this.getType() < 60 && itemstack.getItem() == MoCreatures.amuletfairy)
					{
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);
						this.vanishHorse();
						return true;
					}

					if ((this.getType() == 39 || this.getType() == 40) && itemstack.getItem() == MoCreatures.amuletpegasus)
					{
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);
						this.vanishHorse();
						return true;
					}

					if ((this.getType() == 21 || this.getType() == 22) && itemstack.getItem() == MoCreatures.amuletghost)
					{
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);
						this.vanishHorse();
						return true;
					}
				}

				if (itemstack != null && itemstack.getItem() == Items.dye && this.getType() == 50)
				{
					int colorInt = BlockColored.func_150031_c(itemstack.getItemDamage());
					switch (colorInt)
					{
						case 1:
							this.transform(59);
						case 2:
						case 7:
						case 8:
						case 12:
						default:
							break;
						case 3:
							this.transform(51);
							break;
						case 4:
							this.transform(48);
							break;
						case 5:
							this.transform(53);
							break;
						case 6:
							this.transform(52);
							break;
						case 9:
							this.transform(57);
							break;
						case 10:
							this.transform(49);
							break;
						case 11:
							this.transform(56);
							break;
						case 13:
							this.transform(58);
							break;
						case 14:
							this.transform(55);
							break;
						case 15:
							this.transform(54);
					}

					if (--itemstack.stackSize == 0)
						entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

					this.eatingHorse();
					return true;
				}
				else if (itemstack == null || this.getType() != 60 || itemstack.getItem() != Items.record_11 && itemstack.getItem() != Items.record_13 && itemstack.getItem() != Items.record_cat && itemstack.getItem() != Items.record_chirp && itemstack.getItem() != Items.record_far && itemstack.getItem() != Items.record_mall && itemstack.getItem() != Items.record_mellohi && itemstack.getItem() != Items.record_stal && itemstack.getItem() != Items.record_strad && itemstack.getItem() != Items.record_ward)
				{
					if (itemstack != null && itemstack.getItem() == Items.wheat && !this.isMagicHorse() && !this.isUndead())
					{
						if (--itemstack.stackSize == 0)
							entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

						if (MoCreatures.isServer())
						{
							this.setTemper(this.getTemper() + 25);
							if (this.getTemper() > this.getMaxTemper())
								this.setTemper(this.getMaxTemper() - 5);
						}

						if (this.getHealth() + 5.0F > this.getMaxHealth())
							this.setHealth(this.getMaxHealth());

						this.eatingHorse();
						if (!this.getIsAdult() && this.getEdad() < 100)
							this.setEdad(this.getEdad() + 1);

						return true;
					}
					else if (itemstack != null && itemstack.getItem() == MoCreatures.sugarlump && !this.isMagicHorse() && !this.isUndead())
					{
						if (--itemstack.stackSize == 0)
							entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

						if (MoCreatures.isServer())
						{
							this.setTemper(this.getTemper() + 25);
							if (this.getTemper() > this.getMaxTemper())
								this.setTemper(this.getMaxTemper() - 5);
						}

						if (this.getHealth() + 10.0F > this.getMaxHealth())
							this.setHealth(this.getMaxHealth());

						this.eatingHorse();
						if (!this.getIsAdult() && this.getEdad() < 100)
							this.setEdad(this.getEdad() + 2);

						return true;
					}
					else if (itemstack != null && itemstack.getItem() == Items.bread && !this.isMagicHorse() && !this.isUndead())
					{
						if (--itemstack.stackSize == 0)
							entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

						if (MoCreatures.isServer())
						{
							this.setTemper(this.getTemper() + 100);
							if (this.getTemper() > this.getMaxTemper())
								this.setTemper(this.getMaxTemper() - 5);
						}

						if (this.getHealth() + 20.0F > this.getMaxHealth())
							this.setHealth(this.getMaxHealth());

						this.eatingHorse();
						if (!this.getIsAdult() && this.getEdad() < 100)
							this.setEdad(this.getEdad() + 3);

						return true;
					}
					else if (itemstack != null && (itemstack.getItem() == Items.apple || itemstack.getItem() == Items.golden_apple) && !this.isMagicHorse() && !this.isUndead())
					{
						if (--itemstack.stackSize == 0)
							entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

						if (MoCreatures.isServer())
							MoCTools.tameWithName(entityplayer, this);

						this.setHealth(this.getMaxHealth());
						this.eatingHorse();
						if (!this.getIsAdult() && this.getEdad() < 100 && MoCreatures.isServer())
							this.setEdad(this.getEdad() + 1);

						return true;
					}
					else if (itemstack != null && this.getIsTamed() && itemstack.getItem() == Item.getItemFromBlock(Blocks.chest) && this.isBagger())
					{
						if (this.getChestedHorse())
							return false;
						else
						{
							if (--itemstack.stackSize == 0)
								entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

							entityplayer.inventory.addItemStackToInventory(new ItemStack(MoCreatures.key));
							this.setChestedHorse(true);
							super.worldObj.playSoundAtEntity(this, "mob.chickenplop", 1.0F, (super.rand.nextFloat() - super.rand.nextFloat()) * 0.2F + 1.0F);
							return true;
						}
					}
					else if (itemstack != null && this.getIsTamed() && itemstack.getItem() == MoCreatures.haystack)
					{
						if (--itemstack.stackSize == 0)
							entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

						this.setEating(true);
						this.eatingHorse();
						if (!this.isMagicHorse() && !this.isUndead())
							this.setHealth(this.getMaxHealth());

						return true;
					}
					else if (itemstack != null && itemstack.getItem() == MoCreatures.key && this.getChestedHorse())
					{
    
							this.localhorsechest = new MoCAnimalChest(this, "HorseChest", this.getInventorySize());

						if (!super.worldObj.isRemote)
							entityplayer.displayGUIChest(this.localhorsechest);

						return true;
					}
					else if (itemstack == null || itemstack.getItem() != Item.getItemFromBlock(Blocks.pumpkin) && itemstack.getItem() != Items.mushroom_stew && itemstack.getItem() != Items.cake)
					{
						if (itemstack != null && itemstack.getItem() == MoCreatures.whip && this.getIsTamed() && super.riddenByEntity == null)
						{
							this.setEating(!this.getEating());
							return true;
						}
						else if (this.getIsRideable() && this.getIsAdult() && super.riddenByEntity == null)
						{
							entityplayer.rotationYaw = super.rotationYaw;
							entityplayer.rotationPitch = super.rotationPitch;
							this.setEating(false);
							if (MoCreatures.isServer())
								entityplayer.mountEntity(this);

							this.gestationtime = 0;
							return true;
						}
						else
							return false;
					}
					else if (this.getIsAdult() && !this.isMagicHorse() && !this.isUndead())
					{
						if (itemstack.getItem() == Items.mushroom_stew)
						{
							if (--itemstack.stackSize == 0)
								entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, new ItemStack(Items.bowl));
							else
								entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.bowl));
						}
						else if (--itemstack.stackSize == 0)
							entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);

						this.eatenpumpkin = true;
						this.setHealth(this.getMaxHealth());
						this.eatingHorse();
						return true;
					}
					else
						return false;
				}
				else
				{
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, (ItemStack) null);
					if (MoCreatures.isServer())
					{
						EntityItem entityitem1 = new EntityItem(super.worldObj, super.posX, super.posY, super.posZ, new ItemStack(MoCreatures.recordshuffle, 1));
						entityitem1.delayBeforeCanPickup = 20;
						super.worldObj.spawnEntityInWorld(entityitem1);
					}

					this.eatingHorse();
					return true;
				}
			}
		}
	}

	public boolean isAmuletHorse()
	{
		return this.getType() >= 48 && this.getType() < 60 || this.getType() == 40 || this.getType() == 39 || this.getType() == 21 || this.getType() == 22 || this.getType() == 26 || this.getType() == 27 || this.getType() == 28;
	}

	public boolean isArmored()
	{
		return this.getType() < 21;
	}

	public boolean isBagger()
	{
		return this.getType() == 66 || this.getType() == 65 || this.getType() == 67 || this.getType() == 39 || this.getType() == 40 || this.getType() == 25 || this.getType() == 28 || this.getType() >= 45 && this.getType() < 60;
	}

	public boolean isFloater()
	{
		return this.getType() == 36 || this.getType() == 27 || this.getType() == 24 || this.getType() == 22;
	}

	@Override
	public boolean isFlyer()
	{
		return this.getType() == 39 || this.getType() == 40 || this.getType() >= 45 && this.getType() < 60 || this.getType() == 32 || this.getType() == 21 || this.getType() == 25 || this.getType() == 28;
	}

	public boolean isGhost()
	{
		return this.getType() == 21 || this.getType() == 22;
	}

	public boolean isMagicHorse()
	{
		return this.getType() == 39 || this.getType() == 36 || this.getType() == 32 || this.getType() == 40 || this.getType() >= 45 && this.getType() < 60 || this.getType() == 21 || this.getType() == 22;
	}

	@Override
	protected boolean isMovementCeased()
	{
		return this.getEating() || super.riddenByEntity != null || this.standCounter != 0 || this.shuffleCounter != 0 || this.getVanishC() != 0;
	}

	public boolean isNightmare()
	{
		return this.getType() == 38;
	}

	public boolean isPureBreed()
	{
		return this.getType() > 10 && this.getType() < 21;
	}

	public boolean isUndead()
	{
		return this.getType() == 23 || this.getType() == 24 || this.getType() == 25 || this.getType() == 26 || this.getType() == 27 || this.getType() == 28;
	}

	public boolean isUnicorned()
	{
		return this.getType() == 36 || this.getType() >= 45 && this.getType() < 60 || this.getType() == 27 || this.getType() == 24;
	}

	public boolean isZebraRunning()
	{
		boolean flag = false;
		EntityPlayer ep1 = super.worldObj.getClosestPlayerToEntity(this, 8.0D);
		if (ep1 != null)
		{
			flag = true;
			if (ep1.ridingEntity != null && ep1.ridingEntity instanceof MoCEntityHorse)
			{
				MoCEntityHorse playerHorse = (MoCEntityHorse) ep1.ridingEntity;
				if (playerHorse.getType() == 16 || playerHorse.getType() == 17 || playerHorse.getType() == 60 || playerHorse.getType() == 61)
					flag = false;
			}
		}

		if (flag)
			MoCTools.runLikeHell(this, ep1);

		return flag;
	}

	public void LavaFX()
	{
		MoCreatures.proxy.LavaFX(this);
	}

	public void MaterializeFX()
	{
		MoCreatures.proxy.MaterializeFX(this);
	}

	private void moveTail()
	{
		this.tailCounter = 1;
	}

	@Override
	public int nameYOffset()
	{
		return this.getIsAdult() ? -80 : -5 - this.getEdad();
	}

	private boolean nearMusicBox()
	{
		if (!MoCreatures.isServer())
			return false;
		else
		{
			boolean flag = false;
			TileEntityJukebox jukebox = MoCTools.nearJukeBoxRecord(this, Double.valueOf(6.0D));
			if (jukebox != null && jukebox.func_145856_a() != null)
			{
				Item record = jukebox.func_145856_a().getItem();
				Item shuffleRecord = MoCreatures.recordshuffle;
				if (record == shuffleRecord)
				{
					flag = true;
					if (this.shuffleCounter > 1000)
					{
						this.shuffleCounter = 0;
						MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageShuffle(this.getEntityId(), false), new TargetPoint(super.worldObj.provider.dimensionId, super.posX, super.posY, super.posZ, 64.0D));
						BlockJukebox blockjukebox = (BlockJukebox) super.worldObj.getBlock(jukebox.xCoord, jukebox.yCoord, jukebox.zCoord);
						if (blockjukebox != null)
							blockjukebox.func_149925_e(super.worldObj, jukebox.xCoord, jukebox.yCoord, jukebox.zCoord);

						flag = false;
					}
				}
			}

			return flag;
		}
	}

	public void NightmareEffect()
	{
		int i = MathHelper.floor_double(super.posX);
		int j = MathHelper.floor_double(super.boundingBox.minY);
		int k = MathHelper.floor_double(super.posZ);
		Block block = super.worldObj.getBlock(i - 1, j, k - 1);
		int metadata = super.worldObj.getBlockMetadata(i - 1, j, k - 1);
		BreakEvent event = null;
		if (!super.worldObj.isRemote)
			event = new BreakEvent(i - 1, j, k - 1, super.worldObj, block, metadata, FakePlayerFactory.get(DimensionManager.getWorld(super.worldObj.provider.dimensionId), MoCreatures.MOCFAKEPLAYER));

		if (event != null && !event.isCanceled())
		{
			super.worldObj.setBlock(i - 1, j, k - 1, Blocks.fire, 0, 3);
			EntityPlayer entityplayer = (EntityPlayer) super.riddenByEntity;
			if (entityplayer != null && entityplayer.isBurning())
				entityplayer.extinguish();

			this.setNightmareInt(this.getNightmareInt() - 1);
		}

	}

	@Override
	public void onDeath(DamageSource damagesource)
	{
		super.onDeath(damagesource);
		if (MoCreatures.isServer())
		{
			if (super.rand.nextInt(10) == 0 && this.getType() == 23 || this.getType() == 24 || this.getType() == 25)
				MoCTools.spawnMaggots(super.worldObj, this);

			if (this.getIsTamed() && (this.isMagicHorse() || this.isPureBreed()) && !this.isGhost() && super.rand.nextInt(4) == 0)
			{
				MoCEntityHorse entityhorse1 = new MoCEntityHorse(super.worldObj);
				entityhorse1.setPosition(super.posX, super.posY, super.posZ);
				super.worldObj.spawnEntityInWorld(entityhorse1);
				MoCTools.playCustomSound(this, "appearmagic", super.worldObj);
				entityhorse1.setOwner(this.getOwnerName());
				entityhorse1.setTamed(true);
				EntityPlayer entityplayer = super.worldObj.getClosestPlayerToEntity(this, 24.0D);
				if (entityplayer != null)
					MoCTools.tameWithName(entityplayer, entityhorse1);

				entityhorse1.setAdult(false);
				entityhorse1.setEdad(1);
				int l = 22;
				if (this.isFlyer())
					l = 21;

				entityhorse1.setType(l);
			}
		}

	}

	@Override
	public void onLivingUpdate()
	{
		if ((this.isFlyer() || this.isFloater()) && !super.onGround && super.motionY < 0.0D)
			super.motionY *= 0.6D;

		if (super.jumpPending)
		{
			if (this.isFlyer() && this.wingFlapCounter == 0)
				MoCTools.playCustomSound(this, "wingflap", super.worldObj);

			this.wingFlapCounter = 1;
		}

		if (super.rand.nextInt(200) == 0)
			this.moveTail();

		if (this.getType() == 38 && super.rand.nextInt(50) == 0 && !MoCreatures.isServer())
			this.LavaFX();

		if (this.getType() == 36 && this.isOnAir() && !MoCreatures.isServer())
			this.StarFX();

		if (this.isOnAir() && this.isFlyer() && super.rand.nextInt(30) == 0)
			this.wingFlapCounter = 1;

		if (this.isUndead() && this.getType() < 26 && this.getIsAdult() && super.rand.nextInt(20) == 0)
			if (MoCreatures.isServer())
			{
				if (super.rand.nextInt(16) == 0)
					this.setEdad(this.getEdad() + 1);

				if (this.getEdad() >= 399)
					this.setType(this.getType() + 3);
			}
			else
				this.UndeadFX();

		super.onLivingUpdate();
		if (MoCreatures.isServer())
		{
			if (this.getType() == 60 && this.getIsTamed() && super.rand.nextInt(50) == 0 && this.nearMusicBox())
			{
				this.shuffle();
				MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageShuffle(this.getEntityId(), true), new TargetPoint(super.worldObj.provider.dimensionId, super.posX, super.posY, super.posZ, 64.0D));
			}

			if (super.rand.nextInt(300) == 0 && super.deathTime == 0)
			{
				this.setHealth(this.getHealth() + 1.0F);
				if (this.getHealth() > this.getMaxHealth())
					this.setHealth(this.getMaxHealth());
			}

			if (!this.getEating() && !this.getIsTamed() && super.rand.nextInt(300) == 0)
				this.setEating(true);

			if (this.getEating() && ++this.countEating > 50 && !this.getIsTamed())
			{
				this.countEating = 0;
				this.setEating(false);
			}

			if (this.getType() == 38 && super.riddenByEntity != null && this.getNightmareInt() > 0 && super.rand.nextInt(2) == 0)
				this.NightmareEffect();

			if (this.getType() == 60 && !this.getIsTamed())
				this.isZebraRunning();

			if (!this.getIsAdult() && super.rand.nextInt(200) == 0)
			{
				this.setEdad(this.getEdad() + 1);
				if (this.getEdad() >= 100)
				{
					this.setAdult(true);
					this.setBred(false);
					MoCEntityHorse mommy = this.getClosestMommy(this, 16.0D);
					if (mommy != null)
						mommy.setBred(false);
				}
			}

			if (this.sprintCounter > 0 && this.sprintCounter < 150 && this.isUnicorned() && super.riddenByEntity != null)
				MoCTools.buckleMobs(this, Double.valueOf(2.0D), super.worldObj);

			if (this.isFlyer() && super.rand.nextInt(100) == 0 && !this.isMovementCeased() && !this.getEating())
				this.wingFlap();

			if (this.getHasBred() && !this.getIsAdult() && super.roper == null && !this.getEating())
			{
				MoCEntityHorse mommy = this.getClosestMommy(this, 16.0D);
				if (mommy != null && MoCTools.getSqDistanceTo(mommy, super.posX, super.posY, super.posZ) > 4.0D)
				{
					PathEntity pathentity = super.worldObj.getPathEntityToEntity(this, mommy, 16.0F, true, false, false, true);
					this.setPathToEntity(pathentity);
				}
			}

			if (!this.ReadyforParenting(this))
				return;

			int i = 0;
			List list = super.worldObj.getEntitiesWithinAABBExcludingEntity(this, super.boundingBox.expand(8.0D, 3.0D, 8.0D));

			for (int j = 0; j < list.size(); ++j)
			{
				Entity entity = (Entity) list.get(j);
				if (entity instanceof MoCEntityHorse || entity instanceof EntityHorse)
					++i;
			}

			if (i > 1)
				return;

			List list1 = super.worldObj.getEntitiesWithinAABBExcludingEntity(this, super.boundingBox.expand(4.0D, 2.0D, 4.0D));

			for (int k = 0; k < list1.size(); ++k)
			{
				Entity horsemate = (Entity) list1.get(k);
				boolean flag = horsemate instanceof EntityHorse;
				if ((horsemate instanceof MoCEntityHorse || flag) && horsemate != this)
				{
					if (!this.ReadyforParenting(this))
						return;

					if (!flag && !this.ReadyforParenting((MoCEntityHorse) horsemate))
						return;

					if (super.rand.nextInt(100) == 0)
						++this.gestationtime;

					if (this.gestationtime % 3 == 0)
						MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageHeart(this.getEntityId()), new TargetPoint(super.worldObj.provider.dimensionId, super.posX, super.posY, super.posZ, 64.0D));

					if (this.gestationtime > 50)
					{
						MoCEntityHorse baby = new MoCEntityHorse(super.worldObj);
						baby.setPosition(super.posX, super.posY, super.posZ);
						super.worldObj.spawnEntityInWorld(baby);
						super.worldObj.playSoundAtEntity(this, "mob.chickenplop", 1.0F, (super.rand.nextFloat() - super.rand.nextFloat()) * 0.2F + 1.0F);
						this.eatenpumpkin = false;
						this.gestationtime = 0;
						this.setBred(true);
						int horsemateType;
						if (flag)
						{
							horsemateType = this.TranslateVanillaHorseType((EntityHorse) horsemate);
							if (horsemateType == -1)
								return;
						}
						else
						{
							horsemateType = ((MoCEntityHorse) horsemate).getType();
							((MoCEntityHorse) horsemate).eatenpumpkin = false;
							((MoCEntityHorse) horsemate).gestationtime = 0;
						}

						int l = this.HorseGenetics(this.getType(), horsemateType);
						if (l == 50)
						{
							MoCTools.playCustomSound(this, "appearmagic", super.worldObj);
							if (!flag)
								((MoCEntityHorse) horsemate).dissapearHorse();

							this.dissapearHorse();
						}

						baby.setOwner(this.getOwnerName());
						baby.setTamed(true);
						baby.setBred(true);
						baby.setAdult(false);
						EntityPlayer entityplayer = super.worldObj.getPlayerEntityByName(this.getOwnerName());
						if (entityplayer != null)
							MoCTools.tameWithName(entityplayer, baby);

						baby.setType(l);
						break;
					}
				}
			}
		}

	}

	private int TranslateVanillaHorseType(EntityHorse horse)
	{
		if (horse.getHorseType() == 1)
			return 65;
		else if (horse.getHorseType() == 0)
			switch ((byte) horse.getHorseVariant())
			{
				case 0:
					return 1;
				case 1:
					return 2;
				case 2:
				default:
					return 3;
				case 3:
					return 3;
				case 4:
					return 5;
				case 5:
					return 9;
				case 6:
					return 4;
			}
		else
			return -1;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if (this.shuffleCounter > 0)
		{
			++this.shuffleCounter;
			if (!MoCreatures.isServer() && this.shuffleCounter % 20 == 0)
			{
				double var2 = super.rand.nextGaussian() * 0.5D;
				double var4 = super.rand.nextGaussian() * -0.1D;
				double var6 = super.rand.nextGaussian() * 0.02D;
				super.worldObj.spawnParticle("note", super.posX + super.rand.nextFloat() * super.width * 2.0F - super.width, super.posY + 0.5D + super.rand.nextFloat() * super.height, super.posZ + super.rand.nextFloat() * super.width * 2.0F - super.width, var2, var4, var6);
			}

			if (MoCreatures.isServer() && !this.nearMusicBox())
			{
				this.shuffleCounter = 0;
				MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageShuffle(this.getEntityId(), false), new TargetPoint(super.worldObj.provider.dimensionId, super.posX, super.posY, super.posZ, 64.0D));
			}
		}

		if (this.mouthCounter > 0 && ++this.mouthCounter > 30)
			this.mouthCounter = 0;

		if (this.standCounter > 0 && ++this.standCounter > 20)
			this.standCounter = 0;

		if (this.tailCounter > 0 && ++this.tailCounter > 8)
			this.tailCounter = 0;

		if (this.getVanishC() > 0)
		{
			this.setVanishC((byte) (this.getVanishC() + 1));
			if (this.getVanishC() < 15 && !MoCreatures.isServer())
				this.VanishFX();

			if (this.getVanishC() > 100)
			{
				this.setVanishC((byte) 101);
				MoCTools.dropHorseAmulet(this);
				this.dissapearHorse();
			}

			if (this.getVanishC() == 1)
				MoCTools.playCustomSound(this, "vanish", super.worldObj);

			if (this.getVanishC() == 70)
				this.stand();
		}

		if (this.sprintCounter > 0)
		{
			++this.sprintCounter;
			if (this.sprintCounter < 150 && this.sprintCounter % 2 == 0 && !MoCreatures.isServer())
				this.StarFX();

			if (this.sprintCounter > 300)
				this.sprintCounter = 0;
		}

		if (this.wingFlapCounter > 0)
		{
			++this.wingFlapCounter;
			if (this.wingFlapCounter % 5 == 0 && !MoCreatures.isServer())
				this.StarFX();

			if (this.wingFlapCounter > 20)
				this.wingFlapCounter = 0;
		}

		if (this.transformCounter > 0)
		{
			if (this.transformCounter == 40)
				MoCTools.playCustomSound(this, "transform", super.worldObj);

			if (++this.transformCounter > 100)
			{
				this.transformCounter = 0;
				if (this.transformType != 0)
				{
					this.dropArmor();
					this.setType(this.transformType);
				}
			}
		}

		if (this.isGhost() && this.getEdad() < 10 && super.rand.nextInt(7) == 0)
			this.setEdad(this.getEdad() + 1);

		if (this.isGhost() && this.getEdad() == 9)
		{
			this.setEdad(100);
			this.setAdult(true);
		}

	}

	private void openMouth()
	{
		this.mouthCounter = 1;
	}

	public boolean ReadyforParenting(MoCEntityHorse entityhorse)
	{
		int i = entityhorse.getType();
		return entityhorse.riddenByEntity == null && entityhorse.ridingEntity == null && entityhorse.getIsTamed() && entityhorse.eatenpumpkin && entityhorse.getIsAdult() && !entityhorse.isUndead() && !entityhorse.isGhost() && i != 61 && i < 66;
	}

	@Override
	public boolean renderName()
	{
		return this.getDisplayName() && super.riddenByEntity == null;
	}

	@Override
	public boolean rideableEntity()
	{
		return true;
	}

	@Override
	public double roperYOffset()
	{
		return this.getIsAdult() ? 0.0D : (130 - this.getEdad()) * 0.01D;
	}

	@Override
	public void selectType()
	{
		this.checkSpawningBiome();
		if (this.getType() == 0)
		{
			if (super.rand.nextInt(5) == 0)
				this.setAdult(false);

			int j = super.rand.nextInt(100);
			int i = MoCreatures.proxy.zebraChance;
			if (j <= 33 - i)
				this.setType(6);
			else if (j <= 66 - i)
				this.setType(7);
			else if (j <= 99 - i)
				this.setType(8);
			else
				this.setType(60);

			this.setHealth(this.getMaxHealth());
		}

	}

	@Override
	public void setArmorType(byte i)
	{
		super.dataWatcher.updateObject(25, Integer.valueOf(i));
	}

	public void setBred(boolean flag)
	{
		byte input = (byte) (flag ? 1 : 0);
		super.dataWatcher.updateObject(26, Byte.valueOf(input));
	}

	public void setChestedHorse(boolean flag)
	{
		byte input = (byte) (flag ? 1 : 0);
		super.dataWatcher.updateObject(23, Byte.valueOf(input));
	}

	@Override
	public void setEating(boolean flag)
	{
		byte input = (byte) (flag ? 1 : 0);
		super.dataWatcher.updateObject(24, Byte.valueOf(input));
	}

	public void setNightmareInt(int i)
	{
		this.nightmareInt = i;
	}

	public void setReproduced(boolean var1)
	{
		this.hasReproduced = var1;
	}

	@Override
	public void setRideable(boolean flag)
	{
		byte input = (byte) (flag ? 1 : 0);
		super.dataWatcher.updateObject(22, Byte.valueOf(input));
	}

	public void setRoped(boolean var1)
	{
	}

	public void setVanishC(byte i)
	{
		this.vanishCounter = i;
	}

	public void shuffle()
	{
		if (this.shuffleCounter == 0)
			this.shuffleCounter = 1;

		EntityPlayer ep1 = super.worldObj.getClosestPlayerToEntity(this, 8.0D);
		if (ep1 != null)
			this.faceEntity(ep1, 30.0F, 30.0F);

	}

	private void stand()
	{
		if (super.riddenByEntity == null && !this.isOnAir())
			this.standCounter = 1;

	}

	public void StarFX()
	{
		MoCreatures.proxy.StarFX(this);
	}

	public float tFloat()
	{
		if (++this.fCounter > 60)
		{
			this.fCounter = 0;
			this.transFloat = super.rand.nextFloat() * 0.3F + 0.3F;
		}

		if (this.isGhost() && this.getEdad() < 10)
			this.transFloat = 0.0F;

		return this.transFloat;
	}

	public void transform(int tType)
	{
		if (MoCreatures.isServer())
			MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageAnimation(this.getEntityId(), tType), new TargetPoint(super.worldObj.provider.dimensionId, super.posX, super.posY, super.posZ, 64.0D));

		this.transformType = tType;
		if (super.riddenByEntity == null && this.transformType != 0)
		{
			this.dropArmor();
			this.transformCounter = 1;
		}

	}

	public void UndeadFX()
	{
		MoCreatures.proxy.UndeadFX(this);
	}

	public void VanishFX()
	{
		MoCreatures.proxy.VanishFX(this);
	}

	public void vanishHorse()
	{
		this.setPathToEntity((PathEntity) null);
		super.motionX = 0.0D;
		super.motionZ = 0.0D;
		if (this.isBagger())
		{
			MoCTools.dropInventory(this, this.localhorsechest);
			this.dropBags();
		}

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			MoCMessageHandler.INSTANCE.sendToAllAround(new MoCMessageVanish(this.getEntityId()), new TargetPoint(super.worldObj.provider.dimensionId, super.posX, super.posY, super.posZ, 64.0D));
			this.setVanishC((byte) 1);
		}

		MoCTools.playCustomSound(this, "vanish", super.worldObj);
	}

	@Override
	public void dropMyStuff()
	{
		this.dropArmor();
		MoCTools.dropSaddle(this, super.worldObj);
		if (this.isBagger())
		{
			MoCTools.dropInventory(this, this.localhorsechest);
			this.dropBags();
		}

	}

	public void wingFlap()
	{
		if (this.isFlyer() && this.wingFlapCounter == 0)
			MoCTools.playCustomSound(this, "wingflap", super.worldObj);

		this.wingFlapCounter = 1;
		super.motionY = 0.5D;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setBoolean("Saddle", this.getIsRideable());
		nbttagcompound.setBoolean("EatingHaystack", this.getEating());
		nbttagcompound.setBoolean("ChestedHorse", this.getChestedHorse());
		nbttagcompound.setBoolean("HasReproduced", this.getHasReproduced());
		nbttagcompound.setBoolean("Bred", this.getHasBred());
		nbttagcompound.setBoolean("DisplayName", this.getDisplayName());
		nbttagcompound.setInteger("ArmorType", this.getArmorType());
		if (this.getChestedHorse() && this.localhorsechest != null)
		{
			NBTTagList nbttaglist = new NBTTagList();

			for (int i = 0; i < this.localhorsechest.getSizeInventory(); ++i)
			{
				this.localstack = this.localhorsechest.getStackInSlot(i);
				if (this.localstack != null)
				{
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setByte("Slot", (byte) i);
					this.localstack.writeToNBT(nbttagcompound1);
					nbttaglist.appendTag(nbttagcompound1);
				}
			}

			nbttagcompound.setTag("Items", nbttaglist);
		}

	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readEntityFromNBT(nbttagcompound);
		this.setRideable(nbttagcompound.getBoolean("Saddle"));
		this.setEating(nbttagcompound.getBoolean("EatingHaystack"));
		this.setBred(nbttagcompound.getBoolean("Bred"));
		this.setChestedHorse(nbttagcompound.getBoolean("ChestedHorse"));
		this.setReproduced(nbttagcompound.getBoolean("HasReproduced"));
		this.setDisplayName(nbttagcompound.getBoolean("DisplayName"));
		this.setArmorType((byte) nbttagcompound.getInteger("ArmorType"));
		if (this.getChestedHorse())
		{
    
			this.localhorsechest = new MoCAnimalChest(this, "HorseChest", this.getInventorySize());

			for (int i = 0; i < nbttaglist.tagCount(); ++i)
			{
				ItemStack itemstack = this.localhorsechest.getStackInSlot(i);
				if (itemstack != null)
					this.localhorsechest.setInventorySlotContents(i, itemstack.copy());
			}
		}

	}

	@Override
	public boolean updateMount()
	{
		return this.getIsTamed();
	}

	@Override
	public boolean forceUpdates()
	{
		return this.getIsTamed();
	}

	@Override
	public void performAnimation(int animationType)
	{
		if (animationType >= 23 && animationType < 60)
		{
			this.transformType = animationType;
			this.transformCounter = 1;
		}

	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute()
	{
		return this.isUndead() ? EnumCreatureAttribute.UNDEAD : super.getCreatureAttribute();
	}

	@Override
	protected boolean canBeTrappedInNet()
	{
		return this.getIsTamed() && !this.isAmuletHorse();
	}

	public void setImmuneToFire(boolean value)
	{
		super.isImmuneToFire = value;
	}

	@Override
	public int getMaxSpawnedInChunk()
	{
		return 4;
	}
}
