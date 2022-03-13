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

import java.util.Vector;

/**
 *
 * @author Phil
 */
public class IconDispatcher {
    private final Vector<IconListener> iconListeners;

    public IconDispatcher() {
        iconListeners = new Vector<>();
    }

    public void resizeIcon(float h) {
        fireIconResized(h);
    }

    public void moveIcon(int x, int y) {
        fireIconMoved(x, y);
    }

    public void changeIcon(Item item) {
        fireIconChanged(item);
    }

    public synchronized void addIconListener(IconListener l) {
        if (iconListeners.contains(l)) {
            return;
        }
        iconListeners.addElement(l);
    }

    public synchronized void removeIconListener(IconListener l) {
        iconListeners.removeElement(l);
    }

    @SuppressWarnings("unchecked")
    private void fireIconResized(float h) {
        Vector<IconListener> tl;
        synchronized (this) {
            tl = (Vector<IconListener>) iconListeners.clone();
        }
        int size = tl.size();
        if (size == 0) {
            return;
        }
        IconEvent event = new IconEvent(this);
        event.setResized(true);
        event.setH(h);
        for (int i = 0; i < size; ++i) {
            IconListener listener = tl.elementAt(i);
            listener.iconResized(event);
        }
    }

    @SuppressWarnings("unchecked")
    private void fireIconMoved(int x, int y) {
        Vector<IconListener> tl;
        synchronized (this) {
            tl = (Vector<IconListener>) iconListeners.clone();
        }
        int size = tl.size();
        if (size == 0) {
            return;
        }
        IconEvent event = new IconEvent(this);
        event.setMoved(true);
        event.setX(x);
        event.setY(y);
        for (int i = 0; i < size; ++i) {
            IconListener listener = tl.elementAt(i);
            listener.iconMoved(event);
        }
    }

    @SuppressWarnings("unchecked")
    private void fireIconChanged(Item item) {
        Vector<IconListener> tl;
        synchronized (this) {
            tl = (Vector<IconListener>) iconListeners.clone();
        }
        int size = tl.size();
        if (size == 0) {
            return;
        }
        IconEvent event = new IconEvent(this);
        event.setChanged(true);
        event.setItem(item);
        for (int i = 0; i < size; ++i) {
            IconListener listener = tl.elementAt(i);
            listener.iconChanged(event);
        }
    }
}
