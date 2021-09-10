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
package me.eccentric_nz.tardisschematicviewer.drawing;

import com.jogamp.opengl.GL2;
import net.querz.nbt.tag.CompoundTag;

import java.awt.*;

import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL2ES3.GL_QUADS;

/**
 * @author eccentric_nz
 */
public class ThinCube {

    public static void draw(GL2 gl, Color color, float scale, float sizeX, float sizeY, float sizeZ, Object properties, boolean transparent) {

        sizeY -= scale;
        float[] components = color.getComponents(null);

        // rotate if necessary
        float angle = 0.0f;
        if (properties instanceof String) {
            if (((String) properties).contains("facing=")) {
                if (((String) properties).contains("facing=south")) {
                    angle = 0.0f;
                } else if (((String) properties).contains("facing=east")) {
                    angle = 90.0f;
                } else if (((String) properties).contains("facing=north")) {
                    angle = 180.0f;
                } else if (((String) properties).contains("facing=west")) {
                    angle = -90.0f;
                }
            } else if (((String) properties).contains("rotation=")) {
                String rotationToEnd = ((String) properties).substring(((String) properties).indexOf("rotation=") + "rotation".length() + 1);
                int endIndex = rotationToEnd.contains(",") ? rotationToEnd.indexOf(',') : rotationToEnd.indexOf(']');
                int rotationInt = Integer.parseInt(rotationToEnd.substring(0, endIndex));
                angle = rotationInt * 22.5f;
            }
        } else if (properties instanceof CompoundTag) {
            if (((CompoundTag) properties).containsKey("facing")) {
                if (((CompoundTag) properties).getString("facing").equals("south")) {
                    angle = 0.0f;
                } else if (((CompoundTag) properties).getString("facing").equals("east")) {
                    angle = 90.0f;
                } else if (((CompoundTag) properties).getString("facing").equals("north")) {
                    angle = 180.0f;
                } else if (((CompoundTag) properties).getString("facing").equals("west")) {
                    angle = -90.0f;
                }
            } else if (((CompoundTag) properties).containsKey("rotation")) {
                int rotationInt = ((CompoundTag) properties).getInt("rotation");
                angle = rotationInt * 22.5f;
            }
        }
        gl.glRotatef(angle, 0.0f, 1.0f, 0.0f);

        if (transparent) {
            gl.glLineWidth(scale * 2);
            gl.glBegin(GL_LINES);
        } else {
            gl.glBegin(GL_QUADS);
        }

        // Front Face wide
        gl.glColor4f(components[0], components[1], components[2], components[3]);
        gl.glNormal3f(0.0f, 0.0f, scale);
        gl.glVertex3f(-sizeX, -scale, sizeZ); // bottom-left of the quad
        gl.glVertex3f(sizeX, -scale, sizeZ); // bottom-right of the quad
        gl.glVertex3f(sizeX, sizeY, sizeZ); // top-right of the quad
        gl.glVertex3f(-sizeX, sizeY, sizeZ); // top-left of the quad

        // Back Face wide
        gl.glColor4f(components[0], components[1], components[2], components[3]);
        gl.glNormal3f(0.0f, 0.0f, -scale);
        gl.glVertex3f(-sizeX, -scale, -sizeZ);
        gl.glVertex3f(-sizeX, sizeY, -sizeZ);
        gl.glVertex3f(sizeX, sizeY, -sizeZ);
        gl.glVertex3f(sizeX, -scale, -sizeZ);

        // Top Face LR
        gl.glColor4f(components[0], components[1], components[2], components[3]);
        gl.glNormal3f(0.0f, scale, 0.0f);
        gl.glVertex3f(-sizeX, sizeY, -sizeZ);
        gl.glVertex3f(-sizeX, sizeY, sizeZ);
        gl.glVertex3f(sizeX, sizeY, sizeZ);
        gl.glVertex3f(sizeX, sizeY, -sizeZ);

        // Bottom Face LR
        gl.glColor4f(components[0], components[1], components[2], components[3]);
        gl.glNormal3f(0.0f, -scale, 0.0f);
        gl.glVertex3f(-sizeX, -scale, -sizeZ);
        gl.glVertex3f(sizeX, -scale, -sizeZ);
        gl.glVertex3f(sizeX, -scale, sizeZ);
        gl.glVertex3f(-sizeX, -scale, sizeZ);

        // Right Face LR
        gl.glColor4f(components[0], components[1], components[2], components[3]);
        gl.glNormal3f(scale, 0.0f, 0.0f);
        gl.glVertex3f(sizeX, -scale, -sizeZ);
        gl.glVertex3f(sizeX, sizeY, -sizeZ);
        gl.glVertex3f(sizeX, sizeY, sizeZ);
        gl.glVertex3f(sizeX, -scale, sizeZ);

        // Left Face LR
        gl.glColor4f(components[0], components[1], components[2], components[3]);
        gl.glNormal3f(-scale, 0.0f, 0.0f);
        gl.glVertex3f(-sizeX, -sizeY, -sizeZ);
        gl.glVertex3f(-sizeX, -sizeY, sizeZ);
        gl.glVertex3f(-sizeX, sizeY, sizeZ);
        gl.glVertex3f(-sizeX, sizeY, -sizeZ);

        gl.glEnd();
    }
}