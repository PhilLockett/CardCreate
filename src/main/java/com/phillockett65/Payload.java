/*  CardGen - a playing card image generator.
 *
 *  Copyright 2020 Philip Lockett.
 *
 *  This file is part of CardGen.
 *
 *  CardGen is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CardGen is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CardGen.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.phillockett65;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Payload {

    public static final int PAINT_DISPLAY = -1;
    public static final int PAINT_FILE = -2;
    
    final Loc[][] patterns = {
        { Loc.L_3, Loc.L_5 },
        { Loc.L_0 },
        { Loc.L_1, Loc.L_2 },
        { Loc.L_1, Loc.L_0, Loc.L_2 },
        { Loc.L_3, Loc.L_4, Loc.L_5, Loc.L_6 },
        { Loc.L_3, Loc.L_4, Loc.L_0, Loc.L_5, Loc.L_6 },
        { Loc.L_3, Loc.L_4, Loc.L_5, Loc.L_6, Loc.L_7, Loc.L_8 },
        { Loc.L_3, Loc.L_4, Loc.L_5, Loc.L_6, Loc.L_7, Loc.L_8, Loc.L_9 },
        { Loc.L_3, Loc.L_4, Loc.L10, Loc.L_5, Loc.L_6, Loc.L_7, Loc.L_8, Loc.L_9 },
        { Loc.L_3, Loc.L_4, Loc.L11, Loc.L12, Loc.L_0, Loc.L_5, Loc.L_6, Loc.L13, Loc.L14 },
        { Loc.L_3, Loc.L_4, Loc.L11, Loc.L12, Loc.L15, Loc.L_5, Loc.L_6, Loc.L13, Loc.L14, Loc.L16 },
        { Loc.L_3, Loc.L_4, Loc.L10, Loc.L11, Loc.L12, Loc.L_0, Loc.L_5, Loc.L_6, Loc.L_9, Loc.L13, Loc.L14 },
        { Loc.L_1, Loc.L_3, Loc.L_4, Loc.L10, Loc.L11, Loc.L12, Loc.L_2, Loc.L_5, Loc.L_6, Loc.L_9, Loc.L13, Loc.L14 },
        { Loc.L_1, Loc.L_3, Loc.L_4, Loc.L10, Loc.L11, Loc.L12, Loc.L_0, Loc.L_2, Loc.L_5, Loc.L_6, Loc.L_9, Loc.L13, Loc.L14 },
     };

    public class Real {
        private final boolean height;
        private float decimal = 0F;
        private float percent = 0F;
        private float pixels = 0F;
        
        public Real(boolean height) {
            this.height = height;
        }
        
        private void calcPixels() {
            if (height)
                pixels = decimal * cardHeightPX;
            else
                pixels = decimal * cardWidthPX;
        }
        public void setPercent(float value) {
            percent = value;
            decimal = percent / 100;
            calcPixels();
        }

        public void setReal(float value) {
            decimal = value;
            percent = decimal * 100;
            calcPixels();
        }
        
        public void setPixels(float value) {
            pixels = value;
            if (height)
                decimal = pixels / cardHeightPX;
            else
                decimal = pixels / cardWidthPX;
            percent = decimal * 100;
        }
        
        public float getPercent() {
            return percent;
        }

        public float getReal() {
            return decimal;
        }
        
        public float getRealPixels() {
            return pixels;
        }

        public int getIntPixels() {
            return Math.round(pixels);
        }
        
        public float getRealOrigin() {
            float origin;
            if (height)
                origin = pixels - (spriteHeight.getRealPixels()/2);
            else
                origin = pixels - (spriteWidth.getRealPixels()/2);

            return origin;
        }

        public int getOrigin() {
            return Math.round(getRealOrigin());
        }

    }
    
    // "image" refers to the image in the file, 
    // "sprite" refers to the image on screen.
    private int pattern = 0;
    private final Item item;
    private final String path;
    private BufferedImage image = null;
    private int imageWidthPX = 0;
    private int imageHeightPX = 0;
    private int cardWidthPX = Default.WIDTH.intr();
    private int cardHeightPX = Default.HEIGHT.intr();

    private boolean display;
    private boolean keepAspectRatio;
    private final Real centreX;
    private final Real centreY;
    private final Real spriteHeight;
    private final Real spriteWidth;
    private float spriteScale = 1.0F;

    Payload (String path, int w, int h, int p, Item it) {

        pattern = p;
        item = it;

        cardWidthPX = w;
        cardHeightPX = h;

        display = true;
        keepAspectRatio = true;
        centreX = new Real(false);
        centreY = new Real(true);
        spriteWidth = new Real(false);
        spriteHeight = new Real(true);

        // Set up default perentages.
        spriteHeight.setPercent(item.getH());
        centreX.setPercent(item.getX());
        centreY.setPercent(item.getY());

        // Set up image dependent values.
        this.path = path;
        image = loadImage(path);
        if (image != null) {
            imageWidthPX = image.getWidth();
            imageHeightPX = image.getHeight();
            spriteWidth.setPixels(spriteHeight.getRealPixels() * imageWidthPX / imageHeightPX);
            spriteScale = spriteHeight.getRealPixels() / imageHeightPX;
        }
    }

    private BufferedImage loadImage(String path) {
        BufferedImage loadedImage;
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        try {
            loadedImage = ImageIO.read(file);
        }
        catch(IOException e) {
            loadedImage = null;
            System.out.println("loadImage(" + path + ") - FILE NOT FOUND!");
        }

        return loadedImage;
    }

    private void paintImage(Graphics2D g2d, boolean generate) {

        final int pX = centreX.getIntPixels();
        final int pY = centreY.getIntPixels();
        final float winX = (float)cardWidthPX - (2*centreX.getRealPixels());
        final float winY = (float)cardHeightPX - (2*centreY.getRealPixels());

//        System.out.printf("paintImage() pX = %d,  pY = %d generate = %s\n", pX, pY, generate ? "true" : "false");
        AffineTransform at = AffineTransform.getTranslateInstance(pX, pY);

        float scaleX = winX / imageWidthPX;
        float scaleY = winY / imageHeightPX;
        float scale;
        if (keepAspectRatio) {
            if (!generate) {
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawRect(pX,pY,Math.round(winX),Math.round(winY));
            }
            if (scaleX < scaleY) {
                scale = scaleX;
                int dY = Math.round((winY - (imageHeightPX * scale)) / 2);
                at.translate(0, dY);
            }
            else {
                scale = scaleY;
                int dX = Math.round((winX - (imageWidthPX * scale)) / 2);
                at.translate(dX, 0);
            }
            at.scale(scale, scale);
        } 
        else {
            at.scale(scaleX, scaleY);
        }
        g2d.drawImage(image, at, null);
    }


    private void paintIcon(Graphics2D g2d, Loc location) {

        final float winX = (float)cardWidthPX - (2*centreX.getRealPixels());
        final float offX = location.getXOffset() * winX;
        final int pX = Math.round(centreX.getRealOrigin() + offX);

        final float winY = (float)cardHeightPX - (2*centreY.getRealPixels());
        final float offY = location.getYOffset() * winY;
        final int pY = Math.round(centreY.getRealOrigin() + offY);

        AffineTransform at = AffineTransform.getTranslateInstance(pX, pY);

        if (location.getRotate()) {
            at.setToTranslation(cardWidthPX-pX, cardHeightPX-pY);
            at.quadrantRotate(2);
        }
        at.scale(spriteScale, spriteScale);
        g2d.drawImage(image, at, null);
    }

    /**
     * Paint the icons associated with his payload.
     * @param g2d the 2D graphics object.
     */
    public void paintPatterns(Graphics2D g2d) {
        if (!display)
            return;
        
        if (getPattern() == PAINT_DISPLAY) {
            paintImage(g2d, false);
            return;
        }
        if (getPattern() == PAINT_FILE) {
            paintImage(g2d, true);
            return;
        }

        for (int i = 0; i < patterns[getPattern()].length; ++i) {
            paintIcon(g2d, patterns[getPattern()][i]);
        }
    }

    /**
     * Copy the attributes from another Payload object.
     * @param p the Payload object to copy.
     * @return true if the attributes were copied, false otherwise.
     */
    public boolean copyPercentages(Payload p) {
        if ((p == this) || (image == null)) {
            return false;
        }

        display = p.display;
        keepAspectRatio = p.keepAspectRatio;
        spriteHeight.setPercent(p.getSpriteH());
        spriteWidth.setPixels(spriteHeight.getRealPixels() * imageWidthPX / imageHeightPX);
        centreX.setPercent(p.getSpriteX());
        centreY.setPercent(p.getSpriteY());
        spriteScale = spriteHeight.getRealPixels() / imageHeightPX;
        
//        System.out.printf("copyPercentages(payload) spriteSize = %f,  spriteScale = %f\n", p.getSpriteH(), spriteScale);
        return true;
    }

    private void resizePercentages() {
        if (image == null) {
            return;
        }

        spriteWidth.setPixels(spriteHeight.getRealPixels() * imageWidthPX / imageHeightPX);
        spriteScale = spriteHeight.getRealPixels() / imageHeightPX;
    }
    
    public void resizeCard(int w, int h) {
        if (image == null) {
            return;
        }
//        System.out.printf("resizeCard() Width = %d,  Height = %d\n", w, h);

        cardWidthPX = w;
        cardHeightPX = h;
    }

    /**
     * Set the X co-ordinate of the centre of the sprite.
     * @param value as a percentage of the card width.
     */
    public void setX(float value) {
        if ((value < 0f) || (value > 100f))
            return;

        centreX.setPercent(value);
    }

    /**
     * Set the Y co-ordinate of the centre of the sprite.
     * @param value as a percentage of the card height.
     */
    public void setY(float value) {
        if ((value < 0f) || (value > 100f))
            return;

        centreY.setPercent(value);
    }

    /**
     * Set the size of the sprite.
     * @param size as a percentage of the card height.
     */
    public void setSize(float size) {
        if ((size < 0f) || (size > 100f))
            return;

//        System.out.printf("setSize(%f)\n", size);
        spriteHeight.setPercent(size);
        resizePercentages();
    }

    /**
     * Increase the size of the sprite.
     * @return the new size as a percentage of the card height.
     */
    public float incSize() {
        float size = spriteHeight.getPercent();
        if (size != 100f) {
            size += 0.5F;
            if (size > 100f)
                size = 100f;
            spriteHeight.setPercent(size);
            resizePercentages();
        }
        
        return size;
    }

    /**
     * Decrease the size of the sprite.
     * @return the new size as a percentage of the card height.
     */
    public float decSize() {
        float size = spriteHeight.getPercent();
        if (size != 0f) {
            size -= 0.5F;
            if (size < 0f)
                size = 0f;
            spriteHeight.setPercent(size);
            resizePercentages();
        }
        
        return size;
    }

    public void moveBy(int dx, int dy) {
        centreX.setPixels((spriteWidth.getRealPixels()/2) + centreX.getOrigin() + dx);
        centreY.setPixels((spriteHeight.getRealPixels()/2) + centreY.getOrigin() + dy);
    }

    public int getX() {
        return centreX.getOrigin();
    }

    public int getY() {
        return centreY.getOrigin();
    }

    public int getCentreX() {
        return centreX.getIntPixels();
    }

    public int getCentreY() {
        return centreY.getIntPixels();
    }

    /**
     * @return the width of the image on disc.
     */
    public int getWidthPX() {
        return imageWidthPX;
    } 

    /**
     * @return the height of the image on disc.
     */
    public int getHeightPX() {
        return imageHeightPX;
    }

    /**
     * @return the centre X co-ordinate of the sprite as a percentage of the card width.
     */
    public float getSpriteX() {
        return centreX.getPercent();
    }

    /**
     * @return the centre Y co-ordinate of the sprite as a percentage of the card height.
     */
    public float getSpriteY() {
        return centreY.getPercent();
    }

    /**
     * @return the width of the sprite as a percentage of the card width.
     */
    public float getSpriteW() {
        return spriteWidth.getPercent();
    }

    /**
     * @return the height of the sprite as a percentage of the card height.
     */
    public float getSpriteH() {
        return spriteHeight.getPercent();
    }

    /**
     * @return the width of the sprite in pixels.
     */
    public int getSpriteWidthPX() {
        return spriteWidth.getIntPixels();
    }

    /**
     * @return the height of the sprite in pixels.
     */
    public int getSpriteHeightPX() {
        return spriteHeight.getIntPixels();
    }

    /**
     * @return the pattern being used for this Payload.
     */
    public int getPattern() {
        return pattern;
    }

    /**
     * @return the Item this Payload represents.
     */
    public Item getItem() {
        return item;
    }

    /**
     * @return the path to the image file used for this Payload.
     */
    public String getPath() {
        return path;
    }

    /**
     * Indicates if the payload has an associated image file.
     * @return true if the Payload has an image file.
     */
    public boolean hasImage() {
        return (image != null);
    }

    /**
     * Indicates if the associated image file has a landscape aspect ratio.
     * @return true if the image file is landscape.
     */
    public boolean isLandscape() {
        if (image != null) {
            return imageHeightPX < imageWidthPX;
        }

        return false;
    }

    /**
     * Set the arrangement of the standard pips to use for this payload.
     * @param pattern indicating the arrangement to use.
     */
    public void setPattern(int pattern) {
        this.pattern = pattern;
    }

    /**
     * Flag whether the Payload image should be drawn.
     * @param display the pattern if true.
     */
    public void setVisible(boolean display) {
        this.display = display;
    }

    /**
     * Indicates whether the Payload image should be drawn.
     * @return true if the image should be drawn, false otherwise.
     */
    public boolean isVisible() {
        return this.display;
    }

    /**
     * Flag whether the Payload image should maintain it's aspect ratio.
     * @param keepAspectRatio when displaying the image if true.
     */
    public void setKeepAspectRatio(boolean keepAspectRatio) {
        this.keepAspectRatio = keepAspectRatio;
    }
}
