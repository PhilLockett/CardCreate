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

/**
 *
 * @author Phil
 */
public interface IconListener {
    void iconResized(IconEvent e);
    void iconMoved(IconEvent e);
    void iconChanged(IconEvent e);
}
