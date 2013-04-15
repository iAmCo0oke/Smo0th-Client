package net.minecraft.src;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;

public class TextureMap implements IconRegister
{
    /** 0 = terrain.png, 1 = items.png */
    public final int textureType;
    public final String textureName;
    public final String basePath;
    public final String textureExt;
    private final HashMap mapTexturesStiched = new HashMap();
    private BufferedImage missingImage = new BufferedImage(64, 64, 2);
    private TextureStitched missingTextureStiched;
    private Texture atlasTexture;
    private final List listTextureStiched = new ArrayList();
    private final Map textureStichedMap = new HashMap();
    private int iconGridSize = -1;
    private int iconGridCountX = -1;
    private int iconGridCountY = -1;
    private double iconGridSizeU = -1.0D;
    private double iconGridSizeV = -1.0D;
    private TextureStitched[] iconGrid = null;

    public TextureMap(int par1, String par2, String par3Str, BufferedImage par4BufferedImage)
    {
        this.textureType = par1;
        this.textureName = par2;
        this.basePath = par3Str;
        this.textureExt = ".png";
        this.missingImage = par4BufferedImage;
    }

    public void refreshTextures()
    {
        Config.dbg("Creating texture map: " + this.textureName);
        this.textureStichedMap.clear();
        Reflector.callVoid(Reflector.ForgeHooksClient_onTextureStitchedPre, new Object[] {this});
        int var1;
        int var2;

        if (this.textureType == 0)
        {
            Block[] var3 = Block.blocksList;
            var1 = var3.length;

            for (var2 = 0; var2 < var1; ++var2)
            {
                Block var4 = var3[var2];

                if (var4 != null)
                {
                    var4.registerIcons(this);
                }
            }

            Minecraft.getMinecraft().renderGlobal.registerDestroyBlockIcons(this);
            RenderManager.instance.updateIcons(this);
            ConnectedTextures.updateIcons(this);
            NaturalTextures.updateIcons(this);
        }

        Item[] var23 = Item.itemsList;
        var1 = var23.length;

        for (var2 = 0; var2 < var1; ++var2)
        {
            Item var24 = var23[var2];

            if (var24 != null && var24.getSpriteNumber() == this.textureType)
            {
                var24.registerIcons(this);
            }
        }

        HashMap var25 = new HashMap();
        Stitcher var5 = TextureManager.instance().createStitcher(this.textureName);
        this.mapTexturesStiched.clear();
        this.listTextureStiched.clear();
        Texture var6 = TextureManager.instance().makeTexture("missingno", 2, this.missingImage.getWidth(), this.missingImage.getHeight(), 10496, 6408, 9728, 9728, false, this.missingImage);
        StitchHolder var7 = new StitchHolder(var6);
        var5.addStitchHolder(var7);
        var25.put(var7, Arrays.asList(new Texture[] {var6}));
        Iterator var8 = this.textureStichedMap.keySet().iterator();
        ArrayList var9 = new ArrayList();

        while (var8.hasNext())
        {
            String var10 = (String)var8.next();
            String var11 = this.makeFullTextureName(var10) + this.textureExt;
            List var12 = TextureManager.instance().createNewTexture(var10, var11, (TextureStitched)null);
            var9.add(var12);
        }

        this.iconGridSize = this.getStandardTileSize(var9);
        Config.dbg("Icon grid size: " + this.textureName + ", " + this.iconGridSize);
        Iterator var28 = var9.iterator();
        List var26;

        while (var28.hasNext())
        {
            var26 = (List)var28.next();

            if (!var26.isEmpty())
            {
                this.scaleTextures(var26, this.iconGridSize);
            }
        }

        var28 = var9.iterator();

        while (var28.hasNext())
        {
            var26 = (List)var28.next();

            if (!var26.isEmpty())
            {
                StitchHolder var29 = new StitchHolder((Texture)var26.get(0));
                var5.addStitchHolder(var29);
                var25.put(var29, var26);
            }
        }

        try
        {
            var5.doStitch();
        }
        catch (StitcherException var22)
        {
            throw var22;
        }

        TextureStitched var33;

        if (this.atlasTexture != null)
        {
            this.atlasTexture.deleteGlTexture();
            var28 = this.textureStichedMap.values().iterator();

            while (var28.hasNext())
            {
                var33 = (TextureStitched)var28.next();
                var33.deleteGlTextures();
            }
        }

        this.atlasTexture = var5.getTexture();
        Config.dbg("Texture size: " + this.textureName + ", " + this.atlasTexture.getWidth() + "x" + this.atlasTexture.getHeight());
        this.atlasTexture.updateMipmapLevel(this.iconGridSize);
        var8 = var5.getStichSlots().iterator();

        while (var8.hasNext())
        {
            StitchSlot var27 = (StitchSlot)var8.next();
            StitchHolder var30 = var27.getStitchHolder();
            Texture var31 = var30.func_98150_a();
            String var13 = var31.getTextureName();
            List var14 = (List)var25.get(var30);
            TextureStitched var15 = (TextureStitched)this.textureStichedMap.get(var13);
            boolean var16 = false;

            if (var15 == null)
            {
                var16 = true;
                var15 = TextureStitched.makeTextureStitched(var13);

                if (!var13.equals("missingno"))
                {
                    Minecraft.getMinecraft().getLogAgent().logWarning("Couldn\'t find premade icon for " + var13 + " doing " + this.textureName);
                }
            }

            var15.init(this.atlasTexture, var14, var27.getOriginX(), var27.getOriginY(), var30.func_98150_a().getWidth(), var30.func_98150_a().getHeight(), var30.isRotated());
            this.mapTexturesStiched.put(var13, var15);

            if (!var16)
            {
                this.textureStichedMap.remove(var13);
            }

            if (var14.size() > 1)
            {
                this.listTextureStiched.add(var15);
                String var17 = this.makeFullTextureName(var13) + ".txt";
                ITexturePack var18 = Minecraft.getMinecraft().texturePackList.getSelectedTexturePack();
                boolean var19 = !var18.func_98138_b("/" + this.basePath + var13 + ".png", false);

                try
                {
                    InputStream var20 = var18.func_98137_a("/" + var17, var19);
                    Minecraft.getMinecraft().getLogAgent().logInfo("Found animation info for: " + var17);
                    var15.readAnimationInfo(new BufferedReader(new InputStreamReader(var20)));
                }
                catch (IOException var21)
                {
                    ;
                }
            }
        }

        this.missingTextureStiched = (TextureStitched)this.mapTexturesStiched.get("missingno");
        var8 = this.textureStichedMap.values().iterator();

        while (var8.hasNext())
        {
            TextureStitched var32 = (TextureStitched)var8.next();
            var32.copyFrom(this.missingTextureStiched);
        }

        this.textureStichedMap.putAll(this.mapTexturesStiched);
        this.mapTexturesStiched.clear();
        this.updateIconGrid();
        this.atlasTexture.writeImage("debug.stitched_" + this.textureName + ".png");
        Reflector.callVoid(Reflector.ForgeHooksClient_onTextureStitchedPost, new Object[] {this});
        this.atlasTexture.uploadTexture();

        if (Config.isMultiTexture())
        {
            var28 = this.textureStichedMap.values().iterator();

            while (var28.hasNext())
            {
                var33 = (TextureStitched)var28.next();
                var33.createTextures();
            }
        }
    }

    public void updateAnimations()
    {
        if (this.listTextureStiched.size() > 0)
        {
            this.getTexture().bindTexture(0);
            this.atlasTexture.setTextureBound(true);
            Iterator var1 = this.listTextureStiched.iterator();

            while (var1.hasNext())
            {
                TextureStitched var2 = (TextureStitched)var1.next();

                if (this.isAnimationActive(var2))
                {
                    var2.updateAnimation();
                }
            }

            this.atlasTexture.setTextureBound(false);

            if (Config.isMultiTexture())
            {
                for (int var4 = 0; var4 < this.listTextureStiched.size(); ++var4)
                {
                    TextureStitched var3 = (TextureStitched)this.listTextureStiched.get(var4);

                    if (this.isAnimationActive(var3))
                    {
                        var3.updateTileAnimation();
                    }
                }
            }
        }
    }

    public Texture getTexture()
    {
        return this.atlasTexture;
    }

    public Icon registerIcon(String par1Str)
    {
        if (par1Str == null)
        {
            (new RuntimeException("Don\'t register null!")).printStackTrace();
        }

        TextureStitched var2 = (TextureStitched)this.textureStichedMap.get(par1Str);

        if (var2 == null)
        {
            var2 = TextureStitched.makeTextureStitched(par1Str);
            var2.setIndexInMap(this.textureStichedMap.size());
            this.textureStichedMap.put(par1Str, var2);
        }

        return var2;
    }

    public Icon getMissingIcon()
    {
        return this.missingTextureStiched;
    }

    private String makeFullTextureName(String var1)
    {
        int var2 = var1.indexOf(":");

        if (var2 > 0)
        {
            String var3 = var1.substring(0, var2);
            String var4 = var1.substring(var2 + 1);
            return "mods/" + var3 + "/" + this.basePath + var4;
        }
        else
        {
            return var1.startsWith("ctm/") ? var1 : this.basePath + var1;
        }
    }

    public TextureStitched getIconSafe(String var1)
    {
        return (TextureStitched)this.textureStichedMap.get(var1);
    }

    private int getStandardTileSize(List var1)
    {
        int[] var2 = new int[16];
        Iterator var3 = var1.iterator();
        int var6;

        while (var3.hasNext())
        {
            List var4 = (List)var3.next();

            if (!var4.isEmpty())
            {
                Texture var5 = (Texture)var4.get(0);

                if (var5 != null)
                {
                    var6 = TextureUtils.getPowerOfTwo(var5.getWidth());
                    int var7 = TextureUtils.getPowerOfTwo(var5.getHeight());
                    int var8 = Math.max(var6, var7);

                    if (var8 < var2.length)
                    {
                        ++var2[var8];
                    }
                }
            }
        }

        int var9 = 4;
        int var10 = 0;
        int var11;

        for (var11 = 0; var11 < var2.length; ++var11)
        {
            var6 = var2[var11];

            if (var6 > var10)
            {
                var9 = var11;
                var10 = var6;
            }
        }

        if (var9 < 4)
        {
            var9 = 4;
        }

        var11 = TextureUtils.twoToPower(var9);
        return var11;
    }

    private void scaleTextures(List var1, int var2)
    {
        if (!var1.isEmpty())
        {
            Texture var3 = (Texture)var1.get(0);
            int var4 = Math.max(var3.getWidth(), var3.getHeight());

            if (var4 < var2)
            {
                for (int var5 = 0; var5 < var1.size(); ++var5)
                {
                    Texture var6 = (Texture)var1.get(var5);
                    var6.scaleUp(var2);
                }
            }
        }
    }

    public TextureStitched getTextureExtry(String var1)
    {
        return (TextureStitched)this.textureStichedMap.get(var1);
    }

    public boolean setTextureEntry(String var1, TextureStitched var2)
    {
        if (!this.textureStichedMap.containsKey(var1))
        {
            this.textureStichedMap.put(var1, var2);
            return true;
        }
        else
        {
            return false;
        }
    }

    private void updateIconGrid()
    {
        this.iconGridCountX = -1;
        this.iconGridCountY = -1;
        this.iconGrid = null;

        if (this.iconGridSize > 0)
        {
            this.iconGridCountX = this.atlasTexture.getWidth() / this.iconGridSize;
            this.iconGridCountY = this.atlasTexture.getHeight() / this.iconGridSize;
            this.iconGrid = new TextureStitched[this.iconGridCountX * this.iconGridCountY];
            this.iconGridSizeU = 1.0D / (double)this.iconGridCountX;
            this.iconGridSizeV = 1.0D / (double)this.iconGridCountY;
            Iterator var1 = this.textureStichedMap.values().iterator();

            while (var1.hasNext())
            {
                TextureStitched var2 = (TextureStitched)var1.next();
                double var3 = (double)Math.min(var2.getMinU(), var2.getMaxU());
                double var5 = (double)Math.min(var2.getMinV(), var2.getMaxV());
                double var7 = (double)Math.max(var2.getMinU(), var2.getMaxU());
                double var9 = (double)Math.max(var2.getMinV(), var2.getMaxV());
                int var11 = (int)(var3 / this.iconGridSizeU);
                int var12 = (int)(var5 / this.iconGridSizeV);
                int var13 = (int)(var7 / this.iconGridSizeU);
                int var14 = (int)(var9 / this.iconGridSizeV);

                for (int var15 = var11; var15 <= var13; ++var15)
                {
                    if (var15 >= 0 && var15 < this.iconGridCountX)
                    {
                        for (int var16 = var12; var16 <= var14; ++var16)
                        {
                            if (var16 >= 0 && var16 < this.iconGridCountX)
                            {
                                int var17 = var16 * this.iconGridCountX + var15;
                                this.iconGrid[var17] = var2;
                            }
                            else
                            {
                                Config.dbg("Invalid grid V: " + var16 + ", icon: " + var2.getIconName());
                            }
                        }
                    }
                    else
                    {
                        Config.dbg("Invalid grid U: " + var15 + ", icon: " + var2.getIconName());
                    }
                }
            }
        }
    }

    public TextureStitched getIconByUV(double var1, double var3)
    {
        if (this.iconGrid == null)
        {
            return null;
        }
        else
        {
            int var5 = (int)(var1 / this.iconGridSizeU);
            int var6 = (int)(var3 / this.iconGridSizeV);
            int var7 = var6 * this.iconGridCountX + var5;
            return var7 >= 0 && var7 <= this.iconGrid.length ? this.iconGrid[var7] : null;
        }
    }

    public TextureStitched getMissingTextureStiched()
    {
        return this.missingTextureStiched;
    }

    public int getMaxTextureIndex()
    {
        return this.textureStichedMap.size();
    }

    private boolean isAnimationActive(TextureStitched var1)
    {
        return var1 != TextureUtils.iconWater && var1 != TextureUtils.iconWaterFlow ? (var1 != TextureUtils.iconLava && var1 != TextureUtils.iconLavaFlow ? (var1 != TextureUtils.iconFire0 && var1 != TextureUtils.iconFire1 ? (var1 == TextureUtils.iconPortal ? Config.isAnimatedPortal() : true) : Config.isAnimatedFire()) : Config.isAnimatedLava()) : Config.isAnimatedWater();
    }
}
