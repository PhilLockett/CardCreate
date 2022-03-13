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
public enum Default {
    POKER_ASPECT ((2.5F / 3.5F)),
    BRIDGE_ASPECT ((2.25F / 3.5F)),

    INDEX_HEIGHT (10.5F),
    INDEX_CENTRE_X (8.07F),
    INDEX_CENTRE_Y (9.84F),
    CORNER_PIPHEIGHT (7.5F),
    CORNER_PIPCENTRE_X (8.07F),
    CORNER_PIPCENTRE_Y (20.41F),
    STANDARD_PIPHEIGHT (18.5F),
    STANDARD_PIPCENTRE_X (25.7F),
    STANDARD_PIPCENTRE_Y (18.65F),
    FACE_HEIGHT (79.72F),
    FACE_BOARDER_X (14.54F),
    FACE_BOARDER_Y (10.14F),
    FACE_PIPHEIGHT (14.29F),
    FACE_PIPCENTRE_X (14.54F+12.63F),
    FACE_PIPCENTRE_Y (10.14F+9.77F),

    WIDTH (380F),
    HEIGHT (532F);

    private final int	integer;
    private final float	real;

    Default(float val) {
        real = val;
        integer = Math.round(val);
    }
    
    public int intr() { return integer; }
    public float real() { return real; }

}
