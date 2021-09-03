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

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Locale;

/**
 * @author eccentric_nz
 */
public class UserInterface extends JPanel {

    @Serial
    private static final long serialVersionUID = -1098962567729971976L;
    private File lastDirectory;
    private SquareButton selected;
    private int currentLayer;
    private JSONObject schematic;

    private JButton browseButton;
    private JButton loadButton;
    private JTextField fileTextField;
    private JButton editButton;
    private JButton saveButton;
    private JPanel panel;
    private JLabel schematicLabel;
    private JPanel editorPanel;
    private JPanel gridPanel;
    private JLabel blockLabel;
    private JComboBox<String> blockComboBox;
    private JLabel dataLabel;
    private JTextField dataTextField;
    ActionListener actionListener = this::squareActionPerformed;
    private JButton upButton;
    private JButton downButton;
    private JLabel layerLabel;

    public UserInterface(TardisSchematicViewer viewer) {
        lastDirectory = new File(".");
        browseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                choose(fileTextField, "TARDIS Schematic", "tschm");
            }
        });
        loadButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                String path = fileTextField.getText();
                if (!path.isEmpty() && !path.equals("Select file")) {
                    viewer.setPath(fileTextField.getText());
                    schematic = viewer.getSchematic();
                    currentLayer = 0;
                }
            }
        });
        editButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!editorPanel.isVisible()) {
                    schematic = viewer.getSchematic();
                    loadLayer();
                    editorPanel.setVisible(true);
                } else {
                    editorPanel.setVisible(false);
                }
            }
        });
        editButton.addActionListener(e -> {
            // TODO Finish this.
        });
        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                viewer.setSaving(true);
                String output = viewer.getPath();
                String input = output.substring(0, output.lastIndexOf(".tschm")) + ".json";
                File file = new File(input);
                try {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file), 16 * 1024)) {
                        bufferedWriter.write(schematic.toString());
                    }
                    Gzip.zip(input, output);
                    if (!file.delete()) {
                        System.err.println("Could not delete temporary JSON file!");
                    }
                    System.out.println("Schematic saved successfully.");
                } catch (IOException e1) {
                    System.err.println("Error saving schematic: " + e1.getMessage());
                }
                viewer.setSaving(false);
            }
        });
        upButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentLayer < viewer.getHeight() - 1) {
                    currentLayer++;
                    loadLayer();
                }
            }
        });
        downButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentLayer > 0) {
                    currentLayer--;
                    loadLayer();
                }
            }
        });
        blockComboBox.addItemListener(e -> {
            if (selected != null) {
                JSONArray input = (JSONArray) schematic.get("input");
                JSONArray level = input.getJSONArray(selected.getYCoord());
                JSONArray row = (JSONArray) level.get(selected.getXCoord());
                JSONObject column = (JSONObject) row.get(selected.getZCoord());
                String data = column.getString("data");
                String blockData = data.contains("[") ? data.substring(data.indexOf('[')) : "";
                data = "minecraft:" + blockComboBox.getSelectedItem().toString().toLowerCase() + blockData;
                column.put("data", data);
                row.put(selected.getZCoord(), column);
                level.put(selected.getXCoord(), row);
                input.put(selected.getYCoord(), level);
                schematic.put("input", input);
                loadLayer();
            }
        });
        dataTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (selected != null) {
                    JSONArray input = (JSONArray) schematic.get("input");
                    JSONArray level = input.getJSONArray(selected.getYCoord());
                    JSONArray row = (JSONArray) level.get(selected.getXCoord());
                    JSONObject column = (JSONObject) row.get(selected.getZCoord());
                    String data = column.getString("data");
                    int nameEndIndex = data.contains("[") ? data.indexOf('[') : data.length();
                    String blockName = data.substring(data.indexOf(':') + 1, nameEndIndex).toUpperCase(Locale.ROOT);
                    data = blockName + dataTextField.getText();
                    column.put("data", data);
                    row.put(selected.getZCoord(), column);
                    level.put(selected.getXCoord(), row);
                    input.put(selected.getYCoord(), level);
                    schematic.put("input", input);
                    loadLayer();
                }
            }


        });
    }

    private void createUIComponents() {
        panel = this;
        blockComboBox = new JComboBox<>();
        blockComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(Block.strings()));
    }

    /**
     * Opens a file chooser.
     *
     * @param box         the text field to target
     * @param description a String describing the file type
     * @param extension   the file extension
     */
    private void choose(JTextField box, String description, String extension) {
        JFileChooser chooser = new JFileChooser(lastDirectory);
        chooser.setFileFilter(new FileNameExtensionFilter(description, extension));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.showOpenDialog(panel);

        if (chooser.getSelectedFile() != null) {
            box.setText(chooser.getSelectedFile().getPath());
            lastDirectory = chooser.getCurrentDirectory();
        }
    }

    public void loadLayer() {
        gridPanel.removeAll();
        gridPanel.setLayout(null);
        gridPanel.updateUI();
        if (schematic != null) {
            layerLabel.setText("Layer: " + currentLayer);
            JSONObject dimensions = (JSONObject) schematic.get("dimensions");
            JSONArray level = ((JSONArray) schematic.get("input")).getJSONArray(currentLayer);
            int width = dimensions.getInt("width");
            int buttonSideLength = gridPanel.getWidth() / width;
            for (int i = 0; i < width; i++) {
                JSONArray row = (JSONArray) level.get(i);
                for (int j = 0; j < width; j++) {
                    JSONObject column = (JSONObject) row.get(j);
                    String data = column.getString("data");
                    int nameEndIndex = data.contains("[") ? data.indexOf('[') : data.length();
                    String blockName = data.substring(data.indexOf(':') + 1, nameEndIndex).toUpperCase(Locale.ROOT);
                    Block block = Block.valueOf(blockName);
                    SquareButton squareButton = new SquareButton(buttonSideLength, block.getColor(), i, currentLayer, j);
                    squareButton.setText(blockName.substring(0, 1));
                    squareButton.setPreferredSize(new Dimension(buttonSideLength, buttonSideLength));
                    squareButton.setBounds(i * buttonSideLength, j * buttonSideLength, buttonSideLength, buttonSideLength);
                    squareButton.setBorder(new LineBorder(Color.BLACK));
                    squareButton.setToolTipText(data);
                    squareButton.addActionListener(actionListener);
                    gridPanel.add(squareButton);
                }
            }
        } else {
            System.err.println("Schematic was null!");
        }
    }

    private void squareActionPerformed(ActionEvent e) {
        if (selected != null) {
            // remove selected border
            selected.setBorder(new LineBorder(Color.BLACK));
        }
        selected = (SquareButton) e.getSource();
        int x = selected.getX() / 37;
        int z = selected.getY() / 37;
        System.err.println(x + "," + z);
        String data = selected.getToolTipText();
        int nameEndIndex = data.contains("[") ? data.indexOf('[') : data.length();
        String blockName = data.substring(data.indexOf(':') + 1, nameEndIndex).toUpperCase(Locale.ROOT);
        String blockData = data.contains("[") ? data.substring(data.indexOf('[')) : "";
        selected.setBorder(new LineBorder(Color.RED));
        blockComboBox.setSelectedItem(blockName);
        dataTextField.setText(blockData);
    }
}
