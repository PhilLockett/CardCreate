/*  CardCreate - a playing card image generator.
 *
 *  Copyright 2022 Philip Lockett.
 *
 *  This file is part of CardCreate.
 *
 *  CardCreate is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CardCreate is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CardCreate.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.phillockett65;

import java.awt.Color;
import java.awt.Graphics2D;

public class Handle {

    private int xPos = 150;
    private int yPos = 50;
    private int xMouse = xPos;
    private int yMouse = yPos;
    private int xOff = 0;
    private int yOff = 0;
    private final int width = 16;
    private final int height = 16;
    private Payload payload;

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
        xPos = payload.getCentreX() - width/2;
        yPos = payload.getCentreY() - height/2;
    }

    /**
     * Synchronize handle position with current payload.
     */
    public void syncToPayload() {
        if (payload != null) {
            xPos = payload.getCentreX() - width/2;
            yPos = payload.getCentreY() - height/2;
        }
    }

    public void set(int x, int y){
        xPos = x;
        yPos = y;
    }

    public int getX(){
        if (payload != null) {
            return payload.getX();
        }

        return xPos;
    }

    public int getY(){
        if (payload != null) {
            return payload.getY();
        }

        return yPos;
    }

    public int getWidth(){
        if (payload != null) {
            return payload.getWidthPX();
        }

        return width;
    }

    public int getHeight(){
        if (payload != null) {
            return payload.getHeightPX();
        }

        return height;
    }

    public int getXMouse() {
        return xMouse;
    }

    public int getYMouse() {
        return yMouse;
    }

    public void setMouse(int x, int y) {
        if (!isOver(x, y)) {
            return;
        }

        xMouse = x;
        yMouse = y;

        xOff = x - xPos;
        yOff = y - yPos;
    }

    public void setRel(int x, int y) {
        if (payload != null) {
            final int dx = x - xMouse;
            final int dy = y - yMouse;
            payload.moveBy(dx, dy);
        }

        xMouse = x;
        yMouse = y;

        xPos = x - xOff;
        yPos = y - yOff;
    }


    public boolean isOver(int x, int y){
        final int OFFSET = 2;
        x += OFFSET;
        y += OFFSET;

        if (x < this.xPos)
            return false;
        if (x > this.xPos + this.width)
            return false;

        if (y < this.yPos)
            return false;
        if (y > this.yPos + this.height)
            return false;

        return true;
    }

    public void paint(Graphics2D g2d) {
        if (payload != null) {
//            Graphics2D g2d = (Graphics2D)g;
            payload.paintPatterns(g2d);
        }
        Color color = new Color(20, 100, 20);
        g2d.setColor(color);
        g2d.fillRect(xPos,yPos,width,height);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(xPos,yPos,width,height);
        g2d.drawLine(xPos, yPos, xPos+width, yPos+height);
        g2d.drawLine(xPos+width, yPos, xPos, yPos+height);
    }
}
