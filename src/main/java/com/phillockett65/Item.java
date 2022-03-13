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

/**
 *
 * @author Phil
 */
public enum Item {
    INDEX (Default.INDEX_HEIGHT.real(), Default.INDEX_CENTRE_X.real(), Default.INDEX_CENTRE_Y.real(), true, "index"),
    CORNER_PIP (Default.CORNER_PIPHEIGHT.real(), Default.CORNER_PIPCENTRE_X.real(), Default.CORNER_PIPCENTRE_Y.real(), true, "corner pip"),
    STANDARD_PIP (Default.STANDARD_PIPHEIGHT.real(), Default.STANDARD_PIPCENTRE_X.real(), Default.STANDARD_PIPCENTRE_Y.real(), true, "standard pip"),
    FACE (Default.FACE_HEIGHT.real(), Default.FACE_BOARDER_X.real(), Default.FACE_BOARDER_Y.real(), false, "face"),
    FACE_PIP (Default.FACE_PIPHEIGHT.real(), Default.FACE_PIPCENTRE_X.real(), Default.FACE_PIPCENTRE_Y.real(), true, "face pip");

    private final float height;
    private final float centreX;
    private final float centreY;
    private final boolean centre;
    private final String desc;

    Item(float h, float x, float y, boolean c, String d) {
        height = h;
        centreX = x;
        centreY = y;
        centre = c;
        desc = d;
    }

    /**
     * @return the default height for the item.
     */
    public float getH() { return height; }

    /**
     * @return the default X co-ordinate of the centre of the item.
     */
    public float getX() { return centreX; }

    /**
     * @return the default Y co-ordinate of the centre of the item.
     */
    public float getY() { return centreY; }

    /**
     * @return the description of the item.
     */
    public String getD() { return desc; }

    /**
     * @return the reset button tool tip for the height of the item.
     */
    public String getHButtonTip() {
        if (centre)
            return "Reset the " + desc + " Height to " + Math.round(height*10) + " \u2030 of card height";

        return "Not applicable";
    }

    /**
     * @return the reset button tool tip for the X co-ordinate of the centre of the item.
     */
    public String getXButtonTip() {
        if (centre)
            return "Reset X co-ordinate of the centre of the " + desc + " to " + Math.round(centreX*10) + " \u2030 of card width";

        return "Reset X Boarder of the " + desc + " to " + Math.round(centreX*10) + " \u2030 of card width";
    }

    /**
     * @return the reset button tool tip for the Y co-ordinate of the centre of the item.
     */
    public String getYButtonTip() {
        if (centre)
            return "Reset Y co-ordinate of the centre of the " + desc + " to " + Math.round(centreY*10) + " \u2030 of card height";

        return "Reset Y Boarder of the " + desc + " to " + Math.round(centreY*10) + " \u2030 of card height";
    }

    /**
     * @return the tool tip for the height of the item.
     */
    public String getHToolTip() {
        if (centre)
            return "Height of the " + desc + " as a \u2030 of card height";

        return "Height of the " + desc + " as a \u2030 of card height";
    }

    /**
     * @return the tool tip for the X co-ordinate of the centre of the item.
     */
    public String getXToolTip() {
        if (centre)
            return "X co-ordinate of the centre of the " + desc + " as a \u2030 of card width";

        return "X Boarder of the " + desc + " as a \u2030 of card width";
    }

    /**
     * @return the tool tip for the Y co-ordinate of the centre of the item.
     */
    public String getYToolTip() {
        if (centre)
            return "Y co-ordinate of the centre of the " + desc + " as a \u2030 of card height";

        return "Y Boarder of the " + desc + " as a \u2030 of card height";
    }

    /**
     * @return the label for the height of the item.
     */
    public String getHLabel() {
        if (centre)
            return "Height (\u2030):";

        return "Not applicable:";
    }

    /**
     * @return the label for the X co-ordinate of the centre of the item.
     */
    public String getXLabel() {
        if (centre)
            return "X Centre (\u2030):";

        return "X Boarder (\u2030):";
    }

    /**
     * @return the label for the Y co-ordinate of the centre of the item.
     */
    public String getYLabel() {
        if (centre)
            return "Y Centre (\u2030):";

        return "Y Boarder (\u2030):";
    }
}
