package com.buuz135.dysoncubeproject.client.gui;

import com.buuz135.dysoncubeproject.Config;
import com.buuz135.dysoncubeproject.DCPContent;
import com.buuz135.dysoncubeproject.util.NumberUtils;
import com.buuz135.dysoncubeproject.world.ClientDysonSphere;
import com.buuz135.dysoncubeproject.world.DysonSphereStructure;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.util.AssetUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DysonProgressGuiAddon extends BasicScreenAddon {

    private String dysonID;

    public DysonProgressGuiAddon(String dysonID, int posX, int posY) {
        super(posX, posY);
        this.dysonID = dysonID;
    }

    @Override
    public int getXSize() {
        return 112;
    }

    @Override
    public int getYSize() {
        return 8 * 6 + 4;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        var dyson = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSpheres().computeIfAbsent(dysonID, s -> new DysonSphereStructure());
        var y = 0;
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.dysoncubeproject.dyson_information").withStyle(ChatFormatting.BLUE), this.getPosX() + guiX, this.getPosY() + guiY, 0xFFFFFF, false);
        ++y;
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.dysoncubeproject.progress", new DecimalFormat().format(dyson.getProgress() * 100)).withStyle(ChatFormatting.BLUE), this.getPosX() + guiX, this.getPosY() + guiY + Minecraft.getInstance().font.lineHeight * y, 0xFFFFFF, false);
        ++y;
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.dysoncubeproject.power_gen", NumberUtils.getFormatedBigNumber((long) dyson.getSolarPanels() * Config.POWER_PER_SAIL)).withStyle(ChatFormatting.BLUE), this.getPosX() + guiX, this.getPosY() + guiY + Minecraft.getInstance().font.lineHeight * y, 0xFFFFFF, false);
        ++y;
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.dysoncubeproject.power_con", NumberUtils.getFormatedBigNumber(dyson.getLastConsumedPower())).withStyle(ChatFormatting.BLUE), this.getPosX() + guiX, this.getPosY() + guiY + Minecraft.getInstance().font.lineHeight * y, 0xFFFFFF, false);
        ++y;
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.dysoncubeproject.beams", new DecimalFormat().format(dyson.getBeams())).withStyle(ChatFormatting.BLUE), this.getPosX() + guiX, this.getPosY() + guiY + Minecraft.getInstance().font.lineHeight * y, 0xFFFFFF, false);
        ++y;
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.dysoncubeproject.sails", new DecimalFormat().format(dyson.getSolarPanels()), NumberUtils.getFormatedBigNumber(dyson.getMaxSolarPanels())).withStyle(ChatFormatting.BLUE), this.getPosX() + guiX, this.getPosY() + guiY + Minecraft.getInstance().font.lineHeight * y, 0xFFFFFF, false);
        ++y;
        if (dyson.getSolarPanels() >= dyson.getMaxSolarPanels() && dyson.getSolarPanels() < Config.MAX_SOLAR_PANELS) {
            guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.dysoncubeproject.needs_more_beams").withStyle(ChatFormatting.RED), this.getPosX() + guiX, this.getPosY() + guiY + Minecraft.getInstance().font.lineHeight * y, 0xFFFFFF, false);
            ++y;
        }


        Rectangle area = new Rectangle(this.getPosX() + guiX - 4, this.getPosY() + guiY - 4, 112, Minecraft.getInstance().font.lineHeight * y + 4);
        AssetUtil.drawHorizontalLine(guiGraphics, area.x, area.x + area.width, area.y, DCPContent.CYAN_COLOR);
        AssetUtil.drawHorizontalLine(guiGraphics, area.x, area.x + area.width, area.y + area.height, DCPContent.CYAN_COLOR);
        AssetUtil.drawVerticalLine(guiGraphics, area.x, area.y, area.y + area.height, DCPContent.CYAN_COLOR);
        AssetUtil.drawVerticalLine(guiGraphics, area.x + area.width, area.y, area.y + area.height, DCPContent.CYAN_COLOR);


    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public List<Component> getTooltipLines() {
        var list = new ArrayList<Component>();
        var dyson = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSpheres().computeIfAbsent(dysonID, s -> new DysonSphereStructure());
        list.add(Component.translatable("gui.dysoncubeproject.dyson_information").withStyle(ChatFormatting.BLUE));
        list.add(Component.translatable("gui.dysoncubeproject.progress", new DecimalFormat().format(dyson.getProgress() * 100)).withStyle(ChatFormatting.BLUE));
        list.add(Component.translatable("gui.dysoncubeproject.power_gen", new DecimalFormat().format(dyson.getSolarPanels() * Config.POWER_PER_SAIL)).withStyle(ChatFormatting.BLUE));
        list.add(Component.translatable("gui.dysoncubeproject.power_con", new DecimalFormat().format(dyson.getLastConsumedPower())).withStyle(ChatFormatting.BLUE));
        list.add(Component.translatable("gui.dysoncubeproject.beams", new DecimalFormat().format(dyson.getBeams())).withStyle(ChatFormatting.BLUE));
        list.add(Component.translatable("gui.dysoncubeproject.sails", new DecimalFormat().format(dyson.getSolarPanels()), new DecimalFormat().format(dyson.getMaxSolarPanels())).withStyle(ChatFormatting.BLUE));
        if (dyson.getSolarPanels() >= dyson.getMaxSolarPanels() && dyson.getSolarPanels() < Config.MAX_SOLAR_PANELS) {
            list.add(Component.translatable("gui.dysoncubeproject.needs_more_beams").withStyle(ChatFormatting.RED));
        }
        return list;
    }
}
