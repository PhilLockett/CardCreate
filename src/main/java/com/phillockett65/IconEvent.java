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

import java.util.EventObject;

/**
 *
 * @author Phil
 */
public class IconEvent extends EventObject {
    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    private boolean moved = false;
    private boolean resized = false;
    private boolean changed = false;
    private float h = 0F;
    private int x = 0;
    private int y = 0;
    private Item item;

    public IconEvent(IconDispatcher source) {
        super(source);
    }
    
    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public boolean isMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public float getH() {
        return h;
    }

    public void setH(float h) {
        this.h = h;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

}
