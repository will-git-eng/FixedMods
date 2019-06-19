package advsolar.client.gui;

import advsolar.common.container.ContainerMolecularTransformer;
import advsolar.common.tiles.TileEntityMolecularTransformer;
import advsolar.utils.MTRecipeManager;
import advsolar.utils.MTRecipeRecord;
import com.google.common.collect.Lists;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiMolecularTransformer extends GuiContainer
{
	public TileEntityMolecularTransformer tileentity;
	private int maxTextXPos;
	private static ResourceLocation tex = new ResourceLocation("advancedsolarpanel", "textures/gui/guiMolecularTransformerNew.png");
	public static List<captionRecord> guiTextList = Lists.newArrayList();
	public Minecraft field_146297_k = FMLClientHandler.instance().getClient();

	public GuiMolecularTransformer(InventoryPlayer inventoryplayer, TileEntityMolecularTransformer tileentitymoleculartransformer)
	{
		super(new ContainerMolecularTransformer(inventoryplayer, tileentitymoleculartransformer));
		this.tileentity = tileentitymoleculartransformer;
		this.allowUserInput = false;
		this.xSize = 220;
		this.ySize = 193;
		this.setCaptionText();
	}

	public void setCaptionText()
	{
		captionRecord textToAdd = new captionRecord();
		textToAdd.textCaption = I18n.format("gui.MolecularTransformer.input") + ": ";
		textToAdd.textWidth = this.field_146297_k.fontRenderer.getStringWidth(textToAdd.textCaption);
		guiTextList.add(textToAdd);
		textToAdd = new captionRecord();
		textToAdd.textCaption = I18n.format("gui.MolecularTransformer.output") + ": ";
		textToAdd.textWidth = this.field_146297_k.fontRenderer.getStringWidth(textToAdd.textCaption);
		guiTextList.add(textToAdd);
		textToAdd = new captionRecord();
		textToAdd.textCaption = I18n.format("gui.MolecularTransformer.energyPerOperation") + ": ";
		textToAdd.textWidth = this.field_146297_k.fontRenderer.getStringWidth(textToAdd.textCaption);
		guiTextList.add(textToAdd);
		textToAdd = new captionRecord();
		textToAdd.textCaption = I18n.format("gui.AdvancedSolarPanel.energyPerTick") + ": ";
		textToAdd.textWidth = this.field_146297_k.fontRenderer.getStringWidth(textToAdd.textCaption);
		guiTextList.add(textToAdd);
		textToAdd = new captionRecord();
		textToAdd.textCaption = I18n.format("gui.MolecularTransformer.progress") + ": ";
		textToAdd.textWidth = this.field_146297_k.fontRenderer.getStringWidth(textToAdd.textCaption);
		guiTextList.add(textToAdd);
		this.maxTextXPos = 0;

		for (int i = 0; i < guiTextList.size(); ++i)
		{
			if (guiTextList.get(i).textWidth > this.maxTextXPos)
				this.maxTextXPos = guiTextList.get(i).textWidth;
		}

	}

	public static String parsingNumber(String number)
	{
		StringBuilder tmpString = new StringBuilder();
		int count = 0;

		for (int i = number.length() - 1; i >= 0; --i)
		{
			if (count == 3)
			{
				tmpString.insert(0, ' ');
				count = 0;
			}

			++count;
			tmpString.insert(0, number.charAt(i));
		}

		return tmpString.toString();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		int xOffset = 56;
		int yOffset = 26;
		int yTextInterval = 12;
		String formatDeviceName = I18n.format("blockMolecularTransformer.name");
		int nmPos = (this.xSize - this.fontRendererObj.getStringWidth(formatDeviceName)) / 2;
		this.fontRendererObj.drawString(formatDeviceName, nmPos, 8, 16777215);

		    
		this.tileentity.lastRecipeNumber = MathHelper.clamp_int(this.tileentity.lastRecipeNumber, 0, MTRecipeManager.transformerRecipes.size() - 1);
		    

		if (this.tileentity.lastProgress <= 0 && !this.tileentity.doWork)
		{
			this.fontRendererObj.drawString(guiTextList.get(0).textCaption, xOffset + this.maxTextXPos - guiTextList.get(0).textWidth, yOffset, 16777215);
			this.fontRendererObj.drawString(guiTextList.get(1).textCaption, xOffset + this.maxTextXPos - guiTextList.get(1).textWidth, yOffset + yTextInterval, 16777215);
			this.fontRendererObj.drawString(guiTextList.get(2).textCaption, xOffset + this.maxTextXPos - guiTextList.get(2).textWidth, yOffset + yTextInterval * 2, 16777215);
			this.fontRendererObj.drawString(guiTextList.get(3).textCaption, xOffset + this.maxTextXPos - guiTextList.get(3).textWidth, yOffset + yTextInterval * 3, 16777215);
			this.fontRendererObj.drawString(guiTextList.get(4).textCaption, xOffset + this.maxTextXPos - guiTextList.get(4).textWidth, yOffset + yTextInterval * 4, 16777215);
		}
		else
		{
			MTRecipeRecord recipe = MTRecipeManager.transformerRecipes.get(this.tileentity.lastRecipeNumber);
			ItemStack inputStack = recipe.inputStack;
			ItemStack outputStack = recipe.outputStack;
			String inputEuFormated = parsingNumber(String.valueOf(this.tileentity.inputEU));
			String energyPerOpFormated = parsingNumber(String.valueOf(recipe.energyPerOperation));
			this.fontRendererObj.drawString(guiTextList.get(0).textCaption + inputStack.getDisplayName(), xOffset + this.maxTextXPos - guiTextList.get(0).textWidth, yOffset, 16777215);
			this.fontRendererObj.drawString(guiTextList.get(1).textCaption + outputStack.getDisplayName(), xOffset + this.maxTextXPos - guiTextList.get(1).textWidth, yOffset + yTextInterval, 16777215);
			this.fontRendererObj.drawString(guiTextList.get(2).textCaption + energyPerOpFormated + " EU", xOffset + this.maxTextXPos - guiTextList.get(2).textWidth, yOffset + yTextInterval * 2, 16777215);
			this.fontRendererObj.drawString(guiTextList.get(3).textCaption + inputEuFormated, xOffset + this.maxTextXPos - guiTextList.get(3).textWidth, yOffset + yTextInterval * 3, 16777215);
			this.fontRendererObj.drawString(guiTextList.get(4).textCaption + this.tileentity.lastProgress + "%", xOffset + this.maxTextXPos - guiTextList.get(4).textWidth, yOffset + yTextInterval * 4, 16777215);
		}

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.field_146297_k.renderEngine.bindTexture(tex);
		int h = (this.width - this.xSize) / 2;
		int k = (this.height - this.ySize) / 2;
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 771);
		this.drawTexturedModalRect(h, k, 0, 0, this.xSize, this.ySize);
		GL11.glDisable(3042);
		if (this.tileentity.lastProgress > 0)
		{
			int l = this.tileentity.lastProgress * 15 / 100;
			this.drawTexturedModalRect(h + 23, k + 48, 221, 7, 10, l + 1);
		}

	}

	public class captionRecord
	{
		public String textCaption;
		public int textWidth;
	}
}
