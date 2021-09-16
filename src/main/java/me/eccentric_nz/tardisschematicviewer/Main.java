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
package me.eccentric_nz.tardisschematicviewer;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import me.eccentric_nz.tardisschematicviewer.drawing.SchematicRenderer;
import org.json.JSONException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final int FRAME_WIDTH = 1024;
    public static final int FRAME_HEIGHT = 600;
    public static Path assets = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GLProfile profile = GLProfile.getDefault();
            GLCapabilities capabilities = new GLCapabilities(profile);
            SchematicRenderer renderer = new SchematicRenderer(capabilities);
            renderer.setBackground(Color.GRAY);
            JFrame frame = new JFrame();
            UserInterface userInterface = new UserInterface(renderer);
            userInterface.setSize(1024, 85);
            frame.getContentPane().add(userInterface, BorderLayout.PAGE_START);
            frame.setTitle("TARDIS Schematic Viewer");
            try {
                frame.setIconImage(ImageIO.read(Main.class.getClassLoader().getResourceAsStream("icon.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
            frame.getContentPane().add(renderer, BorderLayout.CENTER);
            frame.setVisible(true);
            FPSAnimator animator = new FPSAnimator(renderer, 30, true);

            // by default, an AWT Frame doesn't do anything when you click
            // the close button; this bit of code will terminate the program when
            // the window is asked to close
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {

                    // Use a dedicated thread to run the stop() to ensure that the
                    // animator stops before program exits.
                    new Thread(() -> {
                        if (animator.isStarted()) {
                            animator.stop();
                        }
                        frame.dispose();
                        System.exit(0);
                    }).start();
                }
            });
            renderer.setFocusable(true);
            renderer.requestFocus();
            renderer.setVisible(true);
            animator.start();

            ArrayList<String> argList = new ArrayList<>(List.of(args));
            if (argList.contains("-assets")) {
                if (argList.size() > argList.indexOf("-assets") + 1) {
                    String assetsArg = argList.get(argList.indexOf("-assets") + 1);
                    assets = Path.of(assetsArg);
                }
            }
            if (argList.contains("-path")) {
                if (argList.size() > argList.indexOf("-path") + 1) {
                    try {
                        String path = argList.get(argList.indexOf("-path") + 1);
                        File file = new File(path);
                        userInterface.open(file);
                    } catch (JSONException e) {
                        System.err.println("Error reading schematic: " + e.getMessage());
                    }
                }
            }
        });
    }
}