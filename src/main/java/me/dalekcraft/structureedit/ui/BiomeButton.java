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

import javafx.scene.control.Button;
import me.dalekcraft.structureedit.schematic.container.Biome;

/**
 * @author eccentric_nz
 */
public class BiomeButton extends Button {

    private final Biome biome;
    private final int[] position;

    public BiomeButton(Biome biome, int[] position) {
        this(biome, position[0], position[1], position[2]);
    }

    public BiomeButton(Biome biome, int x, int y, int z) {
        this.biome = biome;
        position = new int[]{x, y, z};
    }

    public Biome getBiome() {
        return biome;
    }

    public int[] getPosition() {
        return position;
    }
}
