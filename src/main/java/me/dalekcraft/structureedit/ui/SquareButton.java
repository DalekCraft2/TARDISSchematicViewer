/*
 * Copyright (C) 2021 eccentric_nz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package me.dalekcraft.structureedit.ui;

import org.jetbrains.annotations.Contract;

import javax.swing.*;

/**
 * @author eccentric_nz
 */
public class SquareButton extends JButton {

    private final int[] position = new int[3];

    public SquareButton(int x, int y, int z) {
        position[0] = x;
        position[1] = y;
        position[2] = z;
    }

    @Contract(pure = true)
    public int[] getPosition() {
        return position;
    }
}
