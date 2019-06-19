package binnie.core.craftgui;

import binnie.Binnie;
import binnie.core.AbstractMod;
import binnie.core.BinnieCore;
import binnie.core.craftgui.controls.ControlText;
import binnie.core.craftgui.controls.core.Control;
import binnie.core.craftgui.events.EventHandler;
import binnie.core.craftgui.events.EventValueChanged;
import binnie.core.craftgui.geometry.IArea;
import binnie.core.craftgui.geometry.IPoint;
import binnie.core.craftgui.geometry.TextJustification;
import binnie.core.craftgui.minecraft.InventoryType;
import binnie.core.craftgui.minecraft.Window;
import binnie.core.craftgui.minecraft.control.ControlImage;
import binnie.core.craftgui.minecraft.control.ControlPlayerInventory;
import binnie.core.craftgui.minecraft.control.ControlSlot;
import binnie.core.craftgui.resource.StyleSheet;
import binnie.core.craftgui.resource.minecraft.CraftGUITexture;
import binnie.core.craftgui.resource.minecraft.PaddedTexture;
import binnie.core.craftgui.resource.minecraft.StandardTexture;
import binnie.core.machines.inventory.SlotValidator;
import binnie.core.util.I18N;
import binnie.extrabees.core.ExtraBeeTexture;
import binnie.extrabees.gui.ExtraBeeGUITexture;
import binnie.genetics.gui.ControlChromosome;
import binnie.genetics.item.ItemFieldKit;
import binnie.genetics.machine.analyser.Analyser;
import cpw.mods.fml.relauncher.Side;
import forestry.api.genetics.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;

public class WindowFieldKit extends Window
{
	private float glassOffsetX;
	private float glassOffsetY;
	private float glassVX;
	private float glassVY;
	private Random glassRand;
	private Control GlassControl;
	private ControlChromosome chromo;
	private ControlText text;
	private float analyseProgress;
	private boolean isAnalysing;
	private Map<IChromosomeType, String> info;

	    
	private ItemStack itemStack;

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return isSameItemInventory(this.itemStack, player.getHeldItem()) && super.isUseableByPlayer(player);
	}

	@Override
	public boolean checkItemSlot()
	{
		return true;
	}
	    

	public WindowFieldKit(EntityPlayer player, IInventory inventory, Side side)
	{
		super(280.0f, 230.0f, player, inventory, side);
		this.glassOffsetX = 0.0f;
		this.glassOffsetY = 0.0f;
		this.glassVX = 0.0f;
		this.glassVY = 0.0f;
		this.glassRand = new Random();
		this.analyseProgress = 1.0f;
		this.isAnalysing = false;
		this.info = new HashMap<>();
	}

	@Override
	protected AbstractMod getMod()
	{
		return BinnieCore.instance;
	}

	@Override
	protected String getName()
	{
		return I18N.localise("binniecore.gui.fieldKit");
	}

	private void setupValidators()
	{
		this.getWindowInventory().setValidator(0, new SlotValidator(null)
		{
			@Override
			public boolean isValid(ItemStack object)
			{
				return AlleleManager.alleleRegistry.isIndividual(object) || Binnie.Genetics.getConversion(object) != null;
			}

			@Override
			public String getTooltip()
			{
				return I18N.localise("binniecore.gui.tooltip.individual");
			}
		});

		this.getWindowInventory().setValidator(1, new SlotValidator(null)
		{
			@Override
			public boolean isValid(ItemStack object)
			{
				return object.getItem() == Items.paper;
			}

			@Override
			public String getTooltip()
			{
				return I18N.localise("binniecore.gui.tooltip.paper");
			}
		});
		this.getWindowInventory().disableAutoDispense(1);
	}

	@Override
	public void initialiseClient()
	{
		this.setTitle(this.getName());
		CraftGUI.render.stylesheet(new StyleSheetPunnett());
		this.getWindowInventory().createSlot(0);
		this.getWindowInventory().createSlot(1);
		this.setupValidators();
		new ControlPlayerInventory(this);
		IPoint handGlass = new IPoint(16.0f, 32.0f);
		this.GlassControl = new ControlImage(this, handGlass.x(), handGlass.y(), new StandardTexture(0, 160, 96, 96, ExtraBeeTexture.GUIPunnett));
		new ControlSlot(this, handGlass.x() + 54.0f, handGlass.y() + 26.0f).assign(InventoryType.Window, 0);
		new ControlSlot(this, 208.0f, 8.0f).assign(InventoryType.Window, 1);
		(this.text = new ControlText(this, new IPoint(232.0f, 13.0f), I18N.localise("binniecore.gui.tooltip.paper"))).setColor(0x222222);
		(this.text = new ControlText(this, new IArea(0.0f, 120.0f, this.w(), 24.0f), "", TextJustification.MIDDLE_CENTER)).setColor(0x222222);
		this.chromo = new ControlChromosome(this, 150.0f, 24.0f);

		this.addEventHandler(new EventValueChanged.Handler()
		{
			@Override
			public void onEvent(EventValueChanged event)
			{
				IChromosomeType type = (IChromosomeType) event.getValue();
				if (type != null && WindowFieldKit.this.info.containsKey(type))
				{
					String t = WindowFieldKit.this.info.get(type);
					WindowFieldKit.this.text.setValue(t);
				}
				else
					WindowFieldKit.this.text.setValue("");
			}
		}.setOrigin(EventHandler.Origin.DirectChild, this.chromo));
	}

	@Override
	public void initialiseServer()
	{
		ItemStack kit = this.getPlayer().getHeldItem();

		    
		this.itemStack = kit;
		if (kit != null && kit.getItem() instanceof ItemFieldKit)
		{
			if (!kit.hasTagCompound())
				kit.setTagCompound(new NBTTagCompound());
			NBTTagCompound nbt = kit.getTagCompound();
			if (!nbt.hasKey(NBT_KEY_UID))
				nbt.setString(NBT_KEY_UID, UUID.randomUUID().toString());
		}
		    

		int sheets = 64 - kit.getItemDamage();
		if (sheets != 0)
			this.getWindowInventory().setInventorySlotContents(1, new ItemStack(Items.paper, sheets));
		this.setupValidators();
	}

	@Override
	public void onUpdateClient()
	{
		super.onUpdateClient();
		if (this.isAnalysing)
		{
			this.analyseProgress += 0.01f;
			if (this.analyseProgress >= 1.0f)
			{
				this.isAnalysing = false;
				this.analyseProgress = 1.0f;
				ItemStack stack = this.getWindowInventory().getStackInSlot(0);
				if (stack != null)
					this.sendClientAction("analyse", new NBTTagCompound());
				this.refreshSpecies();
			}
		}

		this.glassVX += this.glassRand.nextFloat() - 0.5f - this.glassOffsetX * 0.2f;
		this.glassVY += this.glassRand.nextFloat() - 0.5f - this.glassOffsetY * 0.2f;
		this.glassOffsetX += this.glassVX;
		this.glassOffsetX *= 1.0f - this.analyseProgress;
		this.glassOffsetY += this.glassVY;
		this.glassOffsetY *= 1.0f - this.analyseProgress;
		this.GlassControl.setOffset(new IPoint(this.glassOffsetX, this.glassOffsetY));
	}

	private void refreshSpecies()
	{
		ItemStack item = this.getWindowInventory().getStackInSlot(0);
		if (item == null || !AlleleManager.alleleRegistry.isIndividual(item))
			return;

		IIndividual ind = AlleleManager.alleleRegistry.getIndividual(item);
		if (ind == null)
			return;

		ISpeciesRoot root = AlleleManager.alleleRegistry.getSpeciesRoot(item);
		this.chromo.setRoot(root);
		Random rand = new Random();
		this.info.clear();
		for (IChromosomeType type : root.getKaryotype())
		{
			if (!Binnie.Genetics.isInvalidChromosome(type))
			{
				IAllele allele = ind.getGenome().getActiveAllele(type);
				List<String> infos = new ArrayList<>();

				int i = 0;
				for (String pref = root.getUID() + ".fieldkit." + type.getName().toLowerCase() + "."; I18N.canLocalise(pref + i); ++i)
				{
					infos.add(I18N.localise(pref + i));
				}

				String text = Binnie.Genetics.getSystem(root).getAlleleName(type, allele);
				if (!infos.isEmpty())
					text = infos.get(rand.nextInt(infos.size()));

				this.info.put(type, text);
				this.chromo.setRoot(root);
			}
		}
	}

	@Override
	public void onWindowInventoryChanged()
	{
		super.onWindowInventoryChanged();
		if (this.isServer())
		{
			ItemStack kit = this.getPlayer().getHeldItem();

			    
			if (isSameItemInventory(this.itemStack, kit))
			    
			{
				int sheets = 64 - kit.getItemDamage();
				int size = this.getWindowInventory().getStackInSlot(1) == null ? 0 : this.getWindowInventory().getStackInSlot(1).stackSize;
				if (sheets != size)
					kit.setItemDamage(64 - size);
				((EntityPlayerMP) this.getPlayer()).updateHeldItem();
			}
		}

		if (!this.isClient())
			return;

		ItemStack item = this.getWindowInventory().getStackInSlot(0);
		this.text.setValue("");

		if (item != null && !Analyser.isAnalysed(item))
			if (this.getWindowInventory().getStackInSlot(1) == null)
			{
				this.text.setValue(I18N.localise("binniecore.gui.tooltip.noPaper"));
				this.isAnalysing = false;
				this.analyseProgress = 1.0f;
			}
			else
			{
				this.startAnalysing();
				this.chromo.setRoot(null);
			}
		else if (item != null)
		{
			this.isAnalysing = false;
			this.analyseProgress = 1.0f;
			this.refreshSpecies();
		}
		else
		{
			this.isAnalysing = false;
			this.analyseProgress = 1.0f;
			this.chromo.setRoot(null);
		}
	}

	private void startAnalysing()
	{
		this.glassVX = 0.0f;
		this.glassVY = 0.0f;
		this.glassOffsetX = 0.0f;
		this.glassOffsetY = 0.0f;
		this.isAnalysing = true;
		this.analyseProgress = 0.0f;
	}

	@Override
	public boolean showHelpButton()
	{
		return true;
	}

	@Override
	public String showInfoButton()
	{
		return I18N.localise("binniecore.gui.fieldKit.info");
	}

	@Override
	public void recieveGuiNBT(Side side, EntityPlayer player, String name, NBTTagCompound nbt)
	{
		super.recieveGuiNBT(side, player, name, nbt);
		if (side != Side.SERVER || !name.equals("analyse"))
			return;

		ItemStack stack = this.getWindowInventory().getStackInSlot(0);
		if (stack == null)
			return;

		this.getWindowInventory().setInventorySlotContents(0, Analyser.analyse(stack));
		this.getWindowInventory().decrStackSize(1, 1);
	}

	static class StyleSheetPunnett extends StyleSheet
	{
		public StyleSheetPunnett()
		{
			this.textures.put(CraftGUITexture.Window, new PaddedTexture(0, 0, 160, 160, 0, ExtraBeeTexture.GUIPunnett, 32, 32, 32, 32));
			this.textures.put(CraftGUITexture.Slot, new StandardTexture(160, 0, 18, 18, 0, ExtraBeeTexture.GUIPunnett));
			this.textures.put(ExtraBeeGUITexture.Chromosome, new StandardTexture(160, 36, 16, 16, 0, ExtraBeeTexture.GUIPunnett));
			this.textures.put(ExtraBeeGUITexture.Chromosome2, new StandardTexture(160, 52, 16, 16, 0, ExtraBeeTexture.GUIPunnett));
			this.textures.put(CraftGUITexture.HelpButton, new StandardTexture(178, 0, 16, 16, 0, ExtraBeeTexture.GUIPunnett));
			this.textures.put(CraftGUITexture.InfoButton, new StandardTexture(178, 16, 16, 16, 0, ExtraBeeTexture.GUIPunnett));
		}
	}
}
