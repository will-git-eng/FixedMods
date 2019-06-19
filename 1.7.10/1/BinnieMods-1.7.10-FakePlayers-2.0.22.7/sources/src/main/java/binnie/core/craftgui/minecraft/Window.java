package binnie.core.craftgui.minecraft;

import binnie.Binnie;
import binnie.core.AbstractMod;
import binnie.core.BinnieCore;
import binnie.core.craftgui.*;
import binnie.core.craftgui.controls.ControlText;
import binnie.core.craftgui.controls.ControlTextCentered;
import binnie.core.craftgui.events.EventWidget;
import binnie.core.craftgui.geometry.IPoint;
import binnie.core.craftgui.minecraft.control.*;
import binnie.core.craftgui.renderer.Renderer;
import binnie.core.craftgui.resource.StyleSheetManager;
import binnie.core.craftgui.resource.Texture;
import binnie.core.craftgui.resource.minecraft.CraftGUITexture;
import binnie.core.craftgui.resource.minecraft.StandardTexture;
import binnie.core.machines.Machine;
import binnie.core.machines.inventory.IInventoryMachine;
import binnie.core.machines.network.INetwork;
import binnie.core.machines.power.PowerSystem;
import binnie.core.network.packet.MessageCraftGUI;
import binnie.core.resource.BinnieResource;
import binnie.core.resource.ResourceType;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Deque;

public abstract class Window extends TopLevelWidget implements INetwork.RecieveGuiNBT
{
	protected float titleButtonLeft;
	protected float titleButtonRight;
	private GuiCraftGUI gui;
	private ContainerCraftGUI container;
	private WindowInventory windowInventory;
	private ControlText title;
	private StandardTexture bgText1;
	private StandardTexture bgText2;
	private boolean hasBeenInitialised;
	private EntityPlayer player;
	private IInventory entityInventory;
	private Side side;

	public Window(float width, float height, EntityPlayer player, IInventory inventory, Side side)
	{
		this.side = side;
		this.titleButtonLeft = 8.0f;
		this.titleButtonRight = 8.0f;
		this.bgText1 = null;
		this.bgText2 = null;
		this.hasBeenInitialised = false;
		this.setInventories(player, inventory);
		this.container = new ContainerCraftGUI(this);
		this.windowInventory = new WindowInventory(this);
		if (side == Side.SERVER)
			return;

		this.setSize(new IPoint(width, height));
		this.gui = new GuiCraftGUI(this);
		for (EnumHighlighting h : EnumHighlighting.values())
		{
			ControlSlot.highlighting.put(h, new ArrayList<>());
		}

		CraftGUI.render = new Renderer(this.gui);
		CraftGUI.render.stylesheet(StyleSheetManager.getDefault());
		this.titleButtonLeft = -14.0f;

		if (this.showHelpButton())
			new ControlHelp(this, this.titleButtonLeft += 22.0f, 8.0f);
		if (this.showInfoButton() != null)
			new ControlInfo(this, this.titleButtonLeft += 22.0f, 8.0f, this.showInfoButton());

		this.addSelfEventHandler(new EventWidget.ChangeSize.Handler()
		{
			@Override
			public void onEvent(EventWidget.ChangeSize event)
			{
				if (Window.this.isClient() && Window.this.getGui() != null)
				{
					Window.this.getGui().resize(Window.this.getSize());
					if (Window.this.title != null)
						Window.this.title.setSize(new IPoint(Window.this.w(), Window.this.title.h()));
				}
			}
		});
	}

	    
	protected static final String NBT_KEY_UID = "UID";
	protected int itemSlot = -1;

	protected static boolean isSameItemInventory(ItemStack base, ItemStack comparison)
	{
		if (base == null || comparison == null)
			return false;

		if (base.getItem() != comparison.getItem())
			return false;

		if (!base.hasTagCompound() || !comparison.hasTagCompound())
			return false;

		String baseUID = base.getTagCompound().getString(NBT_KEY_UID);
		String comparisonUID = comparison.getTagCompound().getString(NBT_KEY_UID);
		return baseUID != null && comparisonUID != null && baseUID.equals(comparisonUID);
	}

	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return true;
	}

	public boolean checkItemSlot()
	{
		return false;
	}

	public int getItemSlot()
	{
		return this.itemSlot;
	}

	public void setItemSlot(int itemSlot)
	{
		this.itemSlot = itemSlot;
	}
	    

	public static <T extends Window> T get(IWidget widget)
	{
		return (T) widget.getSuperParent();
	}

	public void getTooltip(Tooltip tooltip)
	{
		Deque<IWidget> queue = this.calculateMousedOverWidgets();
		while (!queue.isEmpty())
		{
			IWidget widget = queue.removeFirst();
			if (widget.isEnabled() && widget.isVisible())
			{
				if (!widget.calculateIsMouseOver())
					continue;
				if (widget instanceof ITooltip)
				{
					((ITooltip) widget).getTooltip(tooltip);
					if (tooltip.exists())
						return;
				}
				if (widget.hasAttribute(WidgetAttribute.BLOCK_TOOLTIP))
					return;
			}
		}
	}

	public void getHelpTooltip(MinecraftTooltip tooltip)
	{
		Deque<IWidget> queue = this.calculateMousedOverWidgets();
		while (!queue.isEmpty())
		{
			IWidget widget = queue.removeFirst();
			if (widget.isEnabled() && widget.isVisible())
			{
				if (!widget.calculateIsMouseOver())
					continue;
				if (widget instanceof ITooltipHelp)
				{
					((ITooltipHelp) widget).getHelpTooltip(tooltip);
					if (tooltip.exists())
						return;
				}
				if (widget.hasAttribute(WidgetAttribute.BLOCK_TOOLTIP))
					return;
			}
		}
	}

	protected abstract AbstractMod getMod();

	protected abstract String getName();

	public BinnieResource getBackgroundTextureFile(int i)
	{
		return Binnie.Resource.getPNG(this.getMod(), ResourceType.GUI, this.getName() + (i == 1 ? "" : i));
	}

	public boolean showHelpButton()
	{
		return Machine.getInterface(IInventoryMachine.class, this.getInventory()) != null;
	}

	public String showInfoButton()
	{
		if (Machine.getInterface(IMachineInformation.class, this.getInventory()) != null)
			return Machine.getInterface(IMachineInformation.class, this.getInventory()).getInformation();
		return null;
	}

	public void setTitle(String title)
	{
		this.title = new ControlTextCentered(this, 12.0f, title);
		this.title.setColor(0x404040);
	}

	@SideOnly(Side.CLIENT)
	public GuiCraftGUI getGui()
	{
		return this.gui;
	}

	public ContainerCraftGUI getContainer()
	{
		return this.container;
	}

	public WindowInventory getWindowInventory()
	{
		return this.windowInventory;
	}

	public void initGui()
	{
		if (this.hasBeenInitialised)
			return;

		this.bgText1 = new StandardTexture(0, 0, 256, 256, this.getBackgroundTextureFile(1));
		if (this.getSize().x() > 256.0f)
			this.bgText2 = new StandardTexture(0, 0, 256, 256, this.getBackgroundTextureFile(2));

		if (!BinnieCore.proxy.checkTexture(this.bgText1.getTexture()))
		{
			this.bgText1 = null;
			this.bgText2 = null;
		}
		this.initialiseClient();
		this.hasBeenInitialised = true;
	}

	public abstract void initialiseClient();

	public void initialiseServer()
	{
		// ignored
	}

	@Override
	public void onRenderBackground()
	{
		CraftGUI.render.color(0xffffff);
		if (this.getBackground1() != null)
			CraftGUI.render.texture(this.getBackground1(), IPoint.ZERO);
		if (this.getBackground2() != null)
			CraftGUI.render.texture(this.getBackground2(), new IPoint(256.0f, 0.0f));
		CraftGUI.render.color(this.getColor());
		CraftGUI.render.texture(CraftGUITexture.Window, this.getArea());
	}

	@Override
	public void onUpdateClient()
	{
		ControlSlot.highlighting.get(EnumHighlighting.HELP).clear();
		ControlSlot.shiftClickActive = false;
	}

	public EntityPlayer getPlayer()
	{
		return this.player;
	}

	public GameProfile getUsername()
	{
		return this.getPlayer().getGameProfile();
	}

	public ItemStack getHeldItemStack()
	{
		if (this.player != null)
			return this.player.inventory.getItemStack();
		return null;
	}

	public void setHeldItemStack(ItemStack stack)
	{
		if (this.player != null)
			this.player.inventory.setItemStack(stack);
	}

	public IInventory getInventory()
	{
		return this.entityInventory;
	}

	public void setInventories(EntityPlayer player2, IInventory inventory)
	{
		this.player = player2;
		this.entityInventory = inventory;
	}

	public void onClose()
	{
	}

	public boolean isServer()
	{
		return !this.isClient();
	}

	public boolean isClient()
	{
		return this.side == Side.CLIENT;
	}

	public World getWorld()
	{
		if (this.getPlayer() != null)
			return this.getPlayer().worldObj;
		return BinnieCore.proxy.getWorld();
	}

	public void sendClientAction(String name, NBTTagCompound action)
	{
		action.setString("type", name);
		MessageCraftGUI packet = new MessageCraftGUI(action);
		BinnieCore.proxy.sendToServer(packet);
	}

	@Override
	public void recieveGuiNBT(Side side, EntityPlayer player, String name, NBTTagCompound nbt)
	{
		if (side == Side.CLIENT && name.equals("username"))
		{
			float w = this.w();
			float titleButtonRight = this.titleButtonRight + 16.0f;
			this.titleButtonRight = titleButtonRight;
			new ControlUser(this, w - titleButtonRight, 8.0f, nbt.getString("username"));
			this.titleButtonRight += 6.0f;
		}
		if (side == Side.CLIENT && name.equals("power-system"))
		{
			float w2 = this.w();
			float titleButtonRight2 = this.titleButtonRight + 16.0f;
			this.titleButtonRight = titleButtonRight2;
			new ControlPowerSystem(this, w2 - titleButtonRight2, 8.0f, PowerSystem.get(nbt.getByte("system")));
			this.titleButtonRight += 6.0f;
		}
	}

	public void onWindowInventoryChanged()
	{
		// ignored
	}

	public Texture getBackground1()
	{
		return this.bgText1;
	}

	public Texture getBackground2()
	{
		return this.bgText2;
	}
}
