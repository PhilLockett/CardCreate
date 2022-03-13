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
public enum Loc {
    L_0 (0, 0, false),
    L_1 (0, 1, true), 
    L_2 (0, 1, false),
    L_3 (1, 1, true), 
    L_4 (2, 1, true), 
    L_5 (1, 1, false),
    L_6 (2, 1, false),
    L_7 (1, 0, false),
    L_8 (2, 0, false),
    L_9 (0, 3, false),
    L10 (0, 3, true), 
    L11 (1, 4, true), 
    L12 (2, 4, true), 
    L13 (1, 4, false),
    L14 (2, 4, false),
    L15 (0, 5, true), 
    L16 (0, 5, false);

    private final int		xIndex;
    private final int		yIndex;
    private final boolean	rotate;

    Loc(int ix, int iy, boolean rot) {
	xIndex = ix;
	yIndex = iy;
	rotate = rot;
    }

    public int getXIndex() { return xIndex; }
    public int getYIndex() { return yIndex; }
    public boolean getRotate() { return rotate; }
    
    private final float[] offsets = { 0.5F, 0F, 1F, 0.25F, 0.333333F, 0.166667F };

    public float getXOffset() { return offsets[xIndex]; }
    public float getYOffset() { return offsets[yIndex]; }

}
