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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Phil
 */
public class StartGUI extends javax.swing.JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int red;
    private int green;
    private int blue;
    private String baseDirectory;
    private boolean validBaseDirectory = false;
    private String faceDirectory;
    private String indexDirectory;
    private String pipDirectory;
    private String outputName = "";
    private final String PATHSFILE = "Files.txt";
    private final SampleGUI samplejFrame;
    private final CardPanel samplejPanel;
    private Item currentItem = Item.INDEX;
    private final int BOARDER_SIZE_X;
    private final int BOARDER_SIZE_Y;

    /**
     * Creates new form StartGUI
     */
    public StartGUI() {
        initComponents();
        this.setIconImage(new ImageIcon(getClass().getResource("CS.png")).getImage());
        red = 255;
        green = 255;
        blue = 255;
        baseDirectory = ".";

        final int w = Default.WIDTH.intr();
        final int h = Default.HEIGHT.intr();
        final Insets insets = getInsets();
        BOARDER_SIZE_X = insets.left + insets.right;
        BOARDER_SIZE_Y = insets.top + insets.bottom;

        samplejFrame = new SampleGUI();
        samplejFrame.setVisible(true);
        samplejFrame.setSize(w + BOARDER_SIZE_X, h + BOARDER_SIZE_Y);

//        System.out.printf("StartGUI() Width = %d,  Height = %d\n", w, h);
//        System.out.printf("StartGUI() BOARDER_SIZE_X = %d,  BOARDER_SIZE_Y = %d\n", BOARDER_SIZE_X, BOARDER_SIZE_Y);

        samplejPanel = new CardPanel();
        samplejPanel.setVisible(true);
        samplejPanel.setBoarders(BOARDER_SIZE_X, BOARDER_SIZE_Y);
        samplejPanel.setColour(Color.WHITE);

        samplejFrame.add(samplejPanel, BorderLayout.CENTER);
        setDefaultSize();

        if (baseDirectoryjComboBoxInit()) {
            facejComboBox.setSelectedIndex(0);
            indexjComboBox.setSelectedIndex(0);
            pipjComboBox.setSelectedIndex(0);
        } else {
            if (selectValidBaseDirectory() == false) {
                setVisible(false);
                dispose();
                System.exit(0);
            }
        }

        fixRadioButtonEnabledStates();
        samplejPanel.setKeepAspectRatio(keepAspectRatiojCheckBox.isSelected());
        updateModifyCardItemControls();
        itemModifyjPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Modify Selected Card Item (\u2030 - per mille)"));

        // Make sure card panel is set up correctly.
        samplejPanel.nextCard();
        samplejPanel.previousCard();

        CardPanelListener cardPanelListener = new CardPanelListener();
        samplejPanel.addIconListener(cardPanelListener);
    }

    private boolean selectValidBaseDirectory() {

        do {
            int n = JOptionPane.showConfirmDialog(this,
                "You need to select a valid directory which contains\n"
                + "'faces', 'indices' and 'pips' directories.\n"
                + "Continue by selecting a valid directory?",
                "Do you wish to continue?",
                JOptionPane.OK_CANCEL_OPTION);

            if (n != JOptionPane.OK_OPTION)
                return false;

            selectBaseDirectory();
        } while (validBaseDirectory == false);

        return true;
    }

    private class CardPanelListener implements IconListener {
        @Override
        public void iconResized(IconEvent e) {
            if (e.isResized()) {
                itemHeightjSpinner.setValue(Math.round(samplejPanel.getCurrentH() * 10));
            }
        }

        @Override
        public void iconMoved(IconEvent e) {
            if (e.isMoved()) {
                itemCentreXjSpinner.setValue(Math.round(samplejPanel.getCurrentX() * 10));
                itemCentreYjSpinner.setValue(Math.round(samplejPanel.getCurrentY() * 10));
            }
        }

        @Override
        public void iconChanged(IconEvent e) {
            if (e.isChanged()) {
                setSelectedItem(e.getItem());
            }
        }
    }

    private void syncSamplejPanelSize(int w, int h) {
        samplejFrame.setSize(w, h);
        samplejPanel.resizeCardPanel(w, h);
//        System.out.printf("syncSamplejPanelSize() w = %d, h = %d\n", w, h);
//        System.out.printf("syncSamplejPanelSize() getWidth() = %d, getHeight() = %d\n", samplejFrame.getWidth(), samplejFrame.getHeight());
    }

    private void setDefaultSize() {

        final int w = Default.WIDTH.intr() + BOARDER_SIZE_X;
        final int h = Default.HEIGHT.intr() + BOARDER_SIZE_Y;

        syncSamplejPanelSize(w, h);
    }

    private boolean setjComboBoxModelFromArrayList(javax.swing.JComboBox<String> comboBox, ArrayList<String> list) {
        if (list.isEmpty()) {
            return false;
        }

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
        for (String s : list)
        	model.addElement(s);
        comboBox.setModel(model);

        return true;
    }

    private boolean baseDirectoryjComboBoxInit() {
//        System.out.println("baseDirectoryjComboBoxInit()");

        // Check if PATHSFILE exists.
        File file = new File(PATHSFILE);
        if (!file.exists()) {
            file = new File(".");
            samplejPanel.setIndexDirectory(file.getPath());
            return false;
        }

        // Read path list file into array.
        ArrayList<String> pathList = new ArrayList<String>();
        try (FileReader reader = new FileReader(PATHSFILE);
            BufferedReader br = new BufferedReader(reader)) {

            String line;
            while ((line = br.readLine()) != null) {
                pathList.add(line);
//                System.out.println(line);
            }
            br.close();
        } catch (IOException e) {
//            e.printStackTrace();
        }

        // If array is not empty use it to fill in baseDirectoryjComboBox.
        if (!pathList.isEmpty()) {
            setjComboBoxModelFromArrayList(baseDirectoryjComboBox, pathList);
            baseDirectory = pathList.get(0);
            File directory = new File(baseDirectory);
            setBaseDirectory(directory);

            if (validBaseDirectory) {
                return true;
            }
        }

        return false;
    }

    private boolean baseDirectoryjComboBoxSave() {

        try (FileWriter writer = new FileWriter(PATHSFILE);
            BufferedWriter bw = new BufferedWriter(writer)) {
            for (int i = 0; i < baseDirectoryjComboBox.getItemCount(); ++i) {
                final String item = baseDirectoryjComboBox.getItemAt(i) + System.lineSeparator();
                bw.write(item);
            }
            bw.close();
        } catch (IOException e) {
//            e.printStackTrace();
        }

        return true;
    }

    private boolean baseDirectoryjComboBoxAdd(final String path) {

        // Build array with path as first item.
        ArrayList<String> pathList = new ArrayList<String>();
        pathList.add(path);

        // Add baseDirectoryjComboBox items to array, except for "path".
        for (int i = 0; i < baseDirectoryjComboBox.getItemCount(); ++i) {
            final String item = baseDirectoryjComboBox.getItemAt(i);
            if (!path.equals(item)) {
                pathList.add(item);
            }
        }

        // Use array to fill in baseDirectoryjComboBox.
        setjComboBoxModelFromArrayList(baseDirectoryjComboBox, pathList);
        baseDirectoryjComboBox.setSelectedIndex(0);

        baseDirectoryjComboBoxSave();

        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aspectRatiobuttonGroup = new javax.swing.ButtonGroup();
        ItemSelectionbuttonGroup = new javax.swing.ButtonGroup();
        generatejButton = new javax.swing.JButton();
        paddingjLabel = new javax.swing.JLabel();
        cardSizejPanel = new javax.swing.JPanel();
        pokerjRadioButton = new javax.swing.JRadioButton();
        bridgejRadioButton = new javax.swing.JRadioButton();
        freejRadioButton = new javax.swing.JRadioButton();
        widthjLabel = new javax.swing.JLabel();
        widthjSpinner = new javax.swing.JSpinner();
        widthjButton = new javax.swing.JButton();
        heightjLabel = new javax.swing.JLabel();
        heightjSpinner = new javax.swing.JSpinner();
        heightjButton = new javax.swing.JButton();
        itemSelectjPanel = new javax.swing.JPanel();
        indicesjRadioButton = new javax.swing.JRadioButton();
        cornerPipjRadioButton = new javax.swing.JRadioButton();
        standardPipjRadioButton = new javax.swing.JRadioButton();
        facejRadioButton = new javax.swing.JRadioButton();
        facePipjRadioButton = new javax.swing.JRadioButton();
        itemDisplayjPanel = new javax.swing.JPanel();
        indicesjCheckBox = new javax.swing.JCheckBox();
        cornerPipjCheckBox = new javax.swing.JCheckBox();
        standardPipjCheckBox = new javax.swing.JCheckBox();
        facejCheckBox = new javax.swing.JCheckBox();
        facePipjCheckBox = new javax.swing.JCheckBox();
        navigatiovjPanel = new javax.swing.JPanel();
        nextCardjButton = new javax.swing.JButton();
        previousSuitjButton = new javax.swing.JButton();
        previousCardjButton = new javax.swing.JButton();
        nextSuitjButton = new javax.swing.JButton();
        itemModifyjPanel = new javax.swing.JPanel();
        keepAspectRatiojCheckBox = new javax.swing.JCheckBox();
        itemHeightjLabel = new javax.swing.JLabel();
        itemCentreXjLabel = new javax.swing.JLabel();
        itemCentreYjLabel = new javax.swing.JLabel();
        itemHeightjSpinner = new javax.swing.JSpinner();
        itemCentreXjSpinner = new javax.swing.JSpinner();
        itemCentreYjSpinner = new javax.swing.JSpinner();
        itemHeightjButton = new javax.swing.JButton();
        itemCentreXjButton = new javax.swing.JButton();
        itemCentreYjButton = new javax.swing.JButton();
        backgroundColourjPanel = new javax.swing.JPanel();
        colourjTextField = new javax.swing.JTextField();
        colourjButton = new javax.swing.JButton();
        outputDirectoryjPanel = new javax.swing.JPanel();
        outputjTextField = new javax.swing.JTextField();
        outputjToggleButton = new javax.swing.JToggleButton();
        inputDirectoriesjPanel = new javax.swing.JPanel();
        baseDirectoryjComboBox = new javax.swing.JComboBox<>();
        baseDirectoryjButton = new javax.swing.JButton();
        baseDirectoryjLabel = new javax.swing.JLabel();
        facejLabel = new javax.swing.JLabel();
        facejComboBox = new javax.swing.JComboBox<>();
        indexjComboBox = new javax.swing.JComboBox<>();
        indexjLabel = new javax.swing.JLabel();
        pipjComboBox = new javax.swing.JComboBox<>();
        pipjLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Playing Card Generator 1.0");
        setResizable(false);

        generatejButton.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        generatejButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/phillockett65/icon-play.png"))); // NOI18N
        generatejButton.setToolTipText("Generate the card images to the selected output directory");
        generatejButton.setEnabled(false);
        generatejButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generatejButtonActionPerformed(evt);
            }
        });

        cardSizejPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Card Size"));
        cardSizejPanel.setName("aspectRatio"); // NOI18N

        aspectRatiobuttonGroup.add(pokerjRadioButton);
        pokerjRadioButton.setSelected(true);
        pokerjRadioButton.setText("Poker");
        pokerjRadioButton.setToolTipText("Maintain poker card aspect ratio");
        pokerjRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pokerjRadioButtonActionPerformed(evt);
            }
        });

        aspectRatiobuttonGroup.add(bridgejRadioButton);
        bridgejRadioButton.setText("Bridge");
        bridgejRadioButton.setToolTipText("Maintain bridge card aspect ratio");
        bridgejRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bridgejRadioButtonActionPerformed(evt);
            }
        });

        aspectRatiobuttonGroup.add(freejRadioButton);
        freejRadioButton.setText("Free");
        freejRadioButton.setToolTipText("independently set card width and height");
        freejRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                freejRadioButtonActionPerformed(evt);
            }
        });

        widthjLabel.setText("Card width (pixels):");
        widthjLabel.setToolTipText("Card width in pixels (default: 380)");

        widthjSpinner.setModel(new javax.swing.SpinnerNumberModel(380, 38, 3800, 1));
        widthjSpinner.setEnabled(false);
        widthjSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                widthjSpinnerStateChanged(evt);
            }
        });

        widthjButton.setText("Reset");
        widthjButton.setToolTipText("Reset Card Width to default value of 380 pixels");
        widthjButton.setEnabled(false);
        widthjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                widthjButtonActionPerformed(evt);
            }
        });

        heightjLabel.setText("Card height (pixels):");
        heightjLabel.setToolTipText("Card height in pixels (default: 532)");

        heightjSpinner.setModel(new javax.swing.SpinnerNumberModel(532, 53, 5320, 1));
        heightjSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                heightjSpinnerStateChanged(evt);
            }
        });

        heightjButton.setText("Reset");
        heightjButton.setToolTipText("Reset Card Height to default value of 532 pixels");
        heightjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                heightjButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout cardSizejPanelLayout = new javax.swing.GroupLayout(cardSizejPanel);
        cardSizejPanel.setLayout(cardSizejPanelLayout);
        cardSizejPanelLayout.setHorizontalGroup(
            cardSizejPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardSizejPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(cardSizejPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pokerjRadioButton)
                    .addComponent(freejRadioButton)
                    .addComponent(bridgejRadioButton))
                .addGap(23, 23, 23)
                .addGroup(cardSizejPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(widthjLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(heightjLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(cardSizejPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(widthjSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(heightjSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(cardSizejPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(widthjButton)
                    .addComponent(heightjButton)))
        );
        cardSizejPanelLayout.setVerticalGroup(
            cardSizejPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardSizejPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(cardSizejPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cardSizejPanelLayout.createSequentialGroup()
                        .addComponent(pokerjRadioButton)
                        .addGap(17, 17, 17)
                        .addComponent(freejRadioButton))
                    .addGroup(cardSizejPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(bridgejRadioButton))
                    .addGroup(cardSizejPanelLayout.createSequentialGroup()
                        .addComponent(widthjLabel)
                        .addGap(26, 26, 26)
                        .addComponent(heightjLabel))
                    .addGroup(cardSizejPanelLayout.createSequentialGroup()
                        .addComponent(widthjSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(heightjSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(cardSizejPanelLayout.createSequentialGroup()
                        .addComponent(widthjButton)
                        .addGap(17, 17, 17)
                        .addComponent(heightjButton))))
        );

        itemSelectjPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Card Item"));

        ItemSelectionbuttonGroup.add(indicesjRadioButton);
        indicesjRadioButton.setSelected(true);
        indicesjRadioButton.setText("Change Indices");
        indicesjRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indicesjRadioButtonActionPerformed(evt);
            }
        });

        ItemSelectionbuttonGroup.add(cornerPipjRadioButton);
        cornerPipjRadioButton.setText("Change Corner Pip");
        cornerPipjRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cornerPipjRadioButtonActionPerformed(evt);
            }
        });

        ItemSelectionbuttonGroup.add(standardPipjRadioButton);
        standardPipjRadioButton.setText("Change Standard Pip");
        standardPipjRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                standardPipjRadioButtonActionPerformed(evt);
            }
        });

        ItemSelectionbuttonGroup.add(facejRadioButton);
        facejRadioButton.setText("Change Face Boarder");
        facejRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                facejRadioButtonActionPerformed(evt);
            }
        });

        ItemSelectionbuttonGroup.add(facePipjRadioButton);
        facePipjRadioButton.setText("Change Face Pip");
        facePipjRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                facePipjRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout itemSelectjPanelLayout = new javax.swing.GroupLayout(itemSelectjPanel);
        itemSelectjPanel.setLayout(itemSelectjPanelLayout);
        itemSelectjPanelLayout.setHorizontalGroup(
            itemSelectjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemSelectjPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(itemSelectjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(indicesjRadioButton)
                    .addComponent(standardPipjRadioButton)
                    .addComponent(facejRadioButton)
                    .addComponent(facePipjRadioButton)
                    .addComponent(cornerPipjRadioButton)))
        );
        itemSelectjPanelLayout.setVerticalGroup(
            itemSelectjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemSelectjPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(indicesjRadioButton)
                .addGap(17, 17, 17)
                .addGroup(itemSelectjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(standardPipjRadioButton)
                    .addGroup(itemSelectjPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(facejRadioButton))))
            .addGroup(itemSelectjPanelLayout.createSequentialGroup()
                .addGap(94, 94, 94)
                .addComponent(facePipjRadioButton))
            .addGroup(itemSelectjPanelLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(cornerPipjRadioButton))
        );

        itemDisplayjPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Display Card Items"));

        indicesjCheckBox.setSelected(true);
        indicesjCheckBox.setText("Display Indices");
        indicesjCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indicesjCheckBoxActionPerformed(evt);
            }
        });

        cornerPipjCheckBox.setSelected(true);
        cornerPipjCheckBox.setText("Display Corner Pip");
        cornerPipjCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cornerPipjCheckBoxActionPerformed(evt);
            }
        });

        standardPipjCheckBox.setSelected(true);
        standardPipjCheckBox.setText("Display Standard Pip");
        standardPipjCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                standardPipjCheckBoxActionPerformed(evt);
            }
        });

        facejCheckBox.setSelected(true);
        facejCheckBox.setText("Display Face Image");
        facejCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                facejCheckBoxActionPerformed(evt);
            }
        });

        facePipjCheckBox.setSelected(true);
        facePipjCheckBox.setText("Display Face Pip");
        facePipjCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                facePipjCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout itemDisplayjPanelLayout = new javax.swing.GroupLayout(itemDisplayjPanel);
        itemDisplayjPanel.setLayout(itemDisplayjPanelLayout);
        itemDisplayjPanelLayout.setHorizontalGroup(
            itemDisplayjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemDisplayjPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(itemDisplayjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(standardPipjCheckBox)
                    .addComponent(cornerPipjCheckBox)
                    .addComponent(facePipjCheckBox)
                    .addComponent(indicesjCheckBox)
                    .addComponent(facejCheckBox)))
        );
        itemDisplayjPanelLayout.setVerticalGroup(
            itemDisplayjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemDisplayjPanelLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(itemDisplayjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(itemDisplayjPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(standardPipjCheckBox))
                    .addComponent(cornerPipjCheckBox))
                .addGap(17, 17, 17)
                .addComponent(facePipjCheckBox))
            .addGroup(itemDisplayjPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(indicesjCheckBox))
            .addGroup(itemDisplayjPanelLayout.createSequentialGroup()
                .addGap(74, 74, 74)
                .addComponent(facejCheckBox))
        );

        navigatiovjPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sample Navigation"));

        nextCardjButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/phillockett65/icon-right.png"))); // NOI18N
        nextCardjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextCardjButtonActionPerformed(evt);
            }
        });

        previousSuitjButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/phillockett65/icon-up.png"))); // NOI18N
        previousSuitjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousSuitjButtonActionPerformed(evt);
            }
        });

        previousCardjButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/phillockett65/icon-left.png"))); // NOI18N
        previousCardjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousCardjButtonActionPerformed(evt);
            }
        });

        nextSuitjButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/phillockett65/icon-down.png"))); // NOI18N
        nextSuitjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextSuitjButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout navigatiovjPanelLayout = new javax.swing.GroupLayout(navigatiovjPanel);
        navigatiovjPanel.setLayout(navigatiovjPanelLayout);
        navigatiovjPanelLayout.setHorizontalGroup(
            navigatiovjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(navigatiovjPanelLayout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addComponent(previousSuitjButton))
            .addGroup(navigatiovjPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(previousCardjButton)
                .addGap(35, 35, 35)
                .addComponent(nextCardjButton))
            .addGroup(navigatiovjPanelLayout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addComponent(nextSuitjButton))
        );
        navigatiovjPanelLayout.setVerticalGroup(
            navigatiovjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(navigatiovjPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(previousSuitjButton)
                .addGap(9, 9, 9)
                .addGroup(navigatiovjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(previousCardjButton)
                    .addComponent(nextCardjButton))
                .addGap(9, 9, 9)
                .addComponent(nextSuitjButton))
        );

        itemModifyjPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Modify Card Item (‰ - per mille)"));

        keepAspectRatiojCheckBox.setSelected(true);
        keepAspectRatiojCheckBox.setText("Keep Image Aspect Ratio");
        keepAspectRatiojCheckBox.setToolTipText("Keep Aspect Ratio of image s from the faces directory");
        keepAspectRatiojCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepAspectRatiojCheckBoxActionPerformed(evt);
            }
        });

        itemHeightjLabel.setText("Item Height (‰):");
        itemHeightjLabel.setToolTipText("Height of selected card item as a ‰ of card height");

        itemCentreXjLabel.setText("Item Centre X (‰):");
        itemCentreXjLabel.setToolTipText("X co-ordinate of the centre of the selected card item as a ‰ of card width");

        itemCentreYjLabel.setText("Item Centre Y (‰):");
        itemCentreYjLabel.setToolTipText("Y co-ordinate of the centre of the selected card item as a ‰ of card height");

        itemHeightjSpinner.setModel(new javax.swing.SpinnerNumberModel(10, 0, 1000, 1));
        itemHeightjSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                itemHeightjSpinnerStateChanged(evt);
            }
        });

        itemCentreXjSpinner.setModel(new javax.swing.SpinnerNumberModel(10, 0, 1000, 1));
        itemCentreXjSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                itemCentreXjSpinnerStateChanged(evt);
            }
        });

        itemCentreYjSpinner.setModel(new javax.swing.SpinnerNumberModel(10, 0, 1000, 1));
        itemCentreYjSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                itemCentreYjSpinnerStateChanged(evt);
            }
        });

        itemHeightjButton.setText("Reset");
        itemHeightjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemHeightjButtonActionPerformed(evt);
            }
        });

        itemCentreXjButton.setText("Reset");
        itemCentreXjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemCentreXjButtonActionPerformed(evt);
            }
        });

        itemCentreYjButton.setText("Reset");
        itemCentreYjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemCentreYjButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout itemModifyjPanelLayout = new javax.swing.GroupLayout(itemModifyjPanel);
        itemModifyjPanel.setLayout(itemModifyjPanelLayout);
        itemModifyjPanelLayout.setHorizontalGroup(
            itemModifyjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemModifyjPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(itemModifyjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(itemModifyjPanelLayout.createSequentialGroup()
                        .addComponent(itemHeightjLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(itemHeightjSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(itemHeightjButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, itemModifyjPanelLayout.createSequentialGroup()
                        .addComponent(itemCentreXjLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(itemCentreXjSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(itemCentreXjButton))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, itemModifyjPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(itemModifyjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(itemModifyjPanelLayout.createSequentialGroup()
                        .addComponent(itemCentreYjLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(itemCentreYjSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(keepAspectRatiojCheckBox))
                .addGap(10, 10, 10)
                .addComponent(itemCentreYjButton))
        );
        itemModifyjPanelLayout.setVerticalGroup(
            itemModifyjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemModifyjPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(itemModifyjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(itemHeightjLabel)
                    .addComponent(itemHeightjSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(itemHeightjButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemModifyjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(itemCentreXjLabel)
                    .addComponent(itemCentreXjSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(itemCentreXjButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemModifyjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(itemCentreYjButton)
                    .addGroup(itemModifyjPanelLayout.createSequentialGroup()
                        .addGroup(itemModifyjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(itemCentreYjLabel)
                            .addComponent(itemCentreYjSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(keepAspectRatiojCheckBox)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        backgroundColourjPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Background Colour"));

        colourjTextField.setEditable(false);
        colourjTextField.setText("rgb(255, 255, 255)");

        colourjButton.setText("Select...");
        colourjButton.setToolTipText("Select the background colour");
        colourjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colourjButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundColourjPanelLayout = new javax.swing.GroupLayout(backgroundColourjPanel);
        backgroundColourjPanel.setLayout(backgroundColourjPanelLayout);
        backgroundColourjPanelLayout.setHorizontalGroup(
            backgroundColourjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundColourjPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(colourjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(colourjButton))
        );
        backgroundColourjPanelLayout.setVerticalGroup(
            backgroundColourjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundColourjPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(backgroundColourjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(colourjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colourjButton)))
        );

        outputDirectoryjPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Output Directory"));

        outputjTextField.setEditable(false);
        outputjTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                outputjTextFieldFocusLost(evt);
            }
        });
        outputjTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputjTextFieldActionPerformed(evt);
            }
        });

        outputjToggleButton.setText("Manual");
        outputjToggleButton.setToolTipText("Manually enter the output directory name, otherwise use same name as slected Face");
        outputjToggleButton.setEnabled(false);
        outputjToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputjToggleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout outputDirectoryjPanelLayout = new javax.swing.GroupLayout(outputDirectoryjPanel);
        outputDirectoryjPanel.setLayout(outputDirectoryjPanelLayout);
        outputDirectoryjPanelLayout.setHorizontalGroup(
            outputDirectoryjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputDirectoryjPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addComponent(outputjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(outputjToggleButton))
        );
        outputDirectoryjPanelLayout.setVerticalGroup(
            outputDirectoryjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputDirectoryjPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(outputDirectoryjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(outputjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputjToggleButton)))
        );

        inputDirectoriesjPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Input Directories"));

        baseDirectoryjComboBox.setToolTipText("Select the Base Directory");
        baseDirectoryjComboBox.setEnabled(false);
        baseDirectoryjComboBox.setMinimumSize(new java.awt.Dimension(320, 22));
        baseDirectoryjComboBox.setPreferredSize(new java.awt.Dimension(320, 22));
        baseDirectoryjComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baseDirectoryjComboBoxActionPerformed(evt);
            }
        });

        baseDirectoryjButton.setText("Browse...");
        baseDirectoryjButton.setToolTipText("Browse to the Base Directory");
        baseDirectoryjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baseDirectoryjButtonActionPerformed(evt);
            }
        });

        baseDirectoryjLabel.setText("Base:");
        baseDirectoryjLabel.setToolTipText("Working directory that contains faces, indices and pips directories");

        facejLabel.setText("Face:");
        facejLabel.setToolTipText("Subdirectory of face image files to use (default: \"1\")");

        facejComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Set Base Directory" }));
        facejComboBox.setToolTipText("Requires the Base Directory to be correctly set");
        facejComboBox.setEnabled(false);
        facejComboBox.setMinimumSize(new java.awt.Dimension(100, 20));
        facejComboBox.setPreferredSize(new java.awt.Dimension(100, 20));
        facejComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                facejComboBoxActionPerformed(evt);
            }
        });

        indexjComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Set Base Directory" }));
        indexjComboBox.setToolTipText("Requires the Base Directory to be correctly set");
        indexjComboBox.setEnabled(false);
        indexjComboBox.setMinimumSize(new java.awt.Dimension(100, 20));
        indexjComboBox.setPreferredSize(new java.awt.Dimension(100, 20));
        indexjComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexjComboBoxActionPerformed(evt);
            }
        });

        indexjLabel.setText("Index:");
        indexjLabel.setToolTipText("Subdirectory of index image files to use (default: \"1\")");

        pipjComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Set Base Directory" }));
        pipjComboBox.setToolTipText("Requires the Base Directory to be correctly set");
        pipjComboBox.setEnabled(false);
        pipjComboBox.setMinimumSize(new java.awt.Dimension(100, 20));
        pipjComboBox.setPreferredSize(new java.awt.Dimension(100, 20));
        pipjComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pipjComboBoxActionPerformed(evt);
            }
        });

        pipjLabel.setText("Pip:");
        pipjLabel.setToolTipText("Subdirectory of pip image files to use (default: \"1\")");

        javax.swing.GroupLayout inputDirectoriesjPanelLayout = new javax.swing.GroupLayout(inputDirectoriesjPanel);
        inputDirectoriesjPanel.setLayout(inputDirectoriesjPanelLayout);
        inputDirectoriesjPanelLayout.setHorizontalGroup(
            inputDirectoriesjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputDirectoriesjPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(inputDirectoriesjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputDirectoriesjPanelLayout.createSequentialGroup()
                        .addComponent(baseDirectoryjLabel)
                        .addGap(3, 3, 3)
                        .addComponent(baseDirectoryjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(baseDirectoryjButton))
                    .addGroup(inputDirectoriesjPanelLayout.createSequentialGroup()
                        .addComponent(facejLabel)
                        .addGap(3, 3, 3)
                        .addComponent(facejComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addGroup(inputDirectoriesjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(inputDirectoriesjPanelLayout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(indexjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(indexjLabel))
                        .addGap(20, 20, 20)
                        .addComponent(pipjLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(pipjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        inputDirectoriesjPanelLayout.setVerticalGroup(
            inputDirectoriesjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputDirectoriesjPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(inputDirectoriesjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(baseDirectoryjLabel)
                    .addComponent(baseDirectoryjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(baseDirectoryjButton))
                .addGap(17, 17, 17)
                .addGroup(inputDirectoriesjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(facejLabel)
                    .addComponent(facejComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(indexjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(indexjLabel)
                    .addComponent(pipjLabel)
                    .addComponent(pipjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(inputDirectoriesjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(generatejButton))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(outputDirectoryjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cardSizejPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(backgroundColourjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(navigatiovjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(3, 3, 3))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(itemDisplayjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(20, 20, 20)
                            .addComponent(itemSelectjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(20, 20, 20)
                            .addComponent(itemModifyjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(1, 1, 1)
                .addComponent(paddingjLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(inputDirectoriesjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(generatejButton))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(outputDirectoryjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(cardSizejPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(backgroundColourjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(navigatiovjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(itemDisplayjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(itemSelectjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(itemModifyjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(layout.createSequentialGroup()
                .addGap(70, 70, 70)
                .addComponent(paddingjLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 464, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private String getColourString() {
        return String.format("rgb(%d, %d, %d)", red, green, blue);
    }

    private void outputjTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputjTextFieldActionPerformed
//        System.out.println("outputjTextFieldActionPerformed -> " + evt.getActionCommand());
        outputName = outputjTextField.getText();
//        System.out.println("outputjTextFieldActionPerformed() outputName = " + outputName);
    }//GEN-LAST:event_outputjTextFieldActionPerformed

    private boolean filljComboBox(javax.swing.JComboBox<String> comboBox, String directoryName) {
        final File style = new File(directoryName);
        ArrayList<String> styleList = new ArrayList<String>();
        for (final File styleEntry : style.listFiles()) {
            if (styleEntry.isDirectory()) {
//                System.out.println(directoryName + "\\" + styleEntry.getName());
                styleList.add(styleEntry.getName());
            }
        }
        if (!styleList.isEmpty()) {
            setjComboBoxModelFromArrayList(comboBox, styleList);

            return true;
        }

        return false;
    }

    private boolean setBaseDirectory(File directory) {
        if (!directory.isDirectory()) {
            return false;
        }

        boolean faces = false;
        boolean indices = false;
        boolean pips = false;

        for (final File fileEntry : directory.listFiles()) {
            if (fileEntry.isDirectory()) {
                String directoryName = directory.getPath() + "\\" + fileEntry.getName();
//                System.out.println(directoryName);
                switch (fileEntry.getName()) {
                    case "faces":
                        faces = filljComboBox(facejComboBox, directoryName);
                        break;
                    case "indices":
                        indices = filljComboBox(indexjComboBox, directoryName);
                        break;
                    case "pips":
                        pips = filljComboBox(pipjComboBox, directoryName);
                        break;
                }
            }
        }

        validBaseDirectory = (faces && indices && pips);
        setGenerationEnabled(directory);

        return validBaseDirectory;
    }

    // Control widgets until a valid base directory is provided.
    private boolean setGenerationEnabled(File directory) {

        if (validBaseDirectory) {
            baseDirectory = directory.getPath() + "\\";
            baseDirectoryjComboBoxAdd(baseDirectory);
            outputjTextField.setText("");
            facejComboBox.setSelectedIndex(0);
            indexjComboBox.setSelectedIndex(0);
            pipjComboBox.setSelectedIndex(0);
        }

        baseDirectoryjComboBox.setEnabled(validBaseDirectory);
        generatejButton.setEnabled(validBaseDirectory);
        outputjToggleButton.setEnabled(validBaseDirectory);
        facejComboBox.setEnabled(validBaseDirectory);
        indexjComboBox.setEnabled(validBaseDirectory);
        pipjComboBox.setEnabled(validBaseDirectory);

        return validBaseDirectory;
    }

    private boolean selectBaseDirectory() {
        JFileChooser choice = new JFileChooser(baseDirectory);
        choice.setDialogTitle("Select Base Directory");
        choice.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int selected = choice.showOpenDialog(this);
        if (selected == JFileChooser.APPROVE_OPTION) {
            File directory = choice.getSelectedFile();
            setBaseDirectory(directory);

            if (validBaseDirectory) {

                return true;
            }
        }
        return false;
    }

    private void baseDirectoryjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baseDirectoryjButtonActionPerformed
//        System.out.println("baseDirectoryjButtonActionPerformed -> " + evt.getActionCommand());
        selectBaseDirectory();
        if (!validBaseDirectory) {
            if (!selectValidBaseDirectory()) {
                // Put original base directory back.
                File directory = new File(baseDirectory);
                setBaseDirectory(directory);
            }
        }
    }//GEN-LAST:event_baseDirectoryjButtonActionPerformed

    private void colourjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colourjButtonActionPerformed
        Color colour = new Color(red, green, blue);

        Color choice = JColorChooser.showDialog(this, "Select background colour", colour);
        if (choice != null) {
            red = choice.getRed();
            green = choice.getGreen();
            blue = choice.getBlue();

            colourjTextField.setText(getColourString());
            samplejPanel.setColour(choice);
        }
    }//GEN-LAST:event_colourjButtonActionPerformed

    private void widthjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_widthjButtonActionPerformed
        widthjSpinner.setValue(380);
    }//GEN-LAST:event_widthjButtonActionPerformed

    private void heightjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heightjButtonActionPerformed
        heightjSpinner.setValue(532);
    }//GEN-LAST:event_heightjButtonActionPerformed

    private void facejComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_facejComboBoxActionPerformed
        if (!outputjToggleButton.isSelected()) {
            outputName = facejComboBox.getSelectedItem().toString();
            outputjTextField.setText(outputName);
//            System.out.println("facejComboBoxActionPerformed() outputName = " + outputName);
        }
        faceDirectory = baseDirectory + "faces\\" + facejComboBox.getSelectedItem().toString()  + "\\";
        setSelectedItem(samplejPanel.setFaceDirectory(faceDirectory));

//        System.out.printf("facejComboBoxActionPerformed(%s) -> %s\n", evt.getActionCommand(), faceDirectory);
    }//GEN-LAST:event_facejComboBoxActionPerformed

    private void outputjToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputjToggleButtonActionPerformed
        outputjTextField.setEditable(outputjToggleButton.isSelected());
        if (!outputjToggleButton.isSelected()) {
            if (facejComboBox.getSelectedIndex() != -1) {
                outputName = facejComboBox.getSelectedItem().toString();
                outputjTextField.setText(outputName);
//            System.out.println("outputjToggleButtonActionPerformed() outputName = " + outputName);
            }
        }
    }//GEN-LAST:event_outputjToggleButtonActionPerformed

    private void baseDirectoryjComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baseDirectoryjComboBoxActionPerformed
        if (baseDirectory.equals(baseDirectoryjComboBox.getSelectedItem().toString())) {
            return;
        }
        outputjTextField.setText("");
        baseDirectory = baseDirectoryjComboBox.getSelectedItem().toString();
        File directory = new File(baseDirectory);
        setBaseDirectory(directory);

    }//GEN-LAST:event_baseDirectoryjComboBoxActionPerformed

    private void outputjTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_outputjTextFieldFocusLost
        outputName = outputjTextField.getText();
//        System.out.println("outputjTextFieldFocusLost() outputName = " + outputName);

    }//GEN-LAST:event_outputjTextFieldFocusLost

    private void setPokerWidth() {
        float value = Float.parseFloat(heightjSpinner.getValue().toString()) * Default.POKER_ASPECT.real();
        widthjSpinner.setValue(Math.round(value));
    }                                                 

    private void setBridgeWidth() {
        float value = Float.parseFloat(heightjSpinner.getValue().toString()) * Default.BRIDGE_ASPECT.real();
        widthjSpinner.setValue(Math.round(value));
    }                                                 

    private void setWidth(int value) {
        syncSamplejPanelSize(value+BOARDER_SIZE_X, samplejFrame.getHeight());
    }

    private void setHeight(int value) {
        syncSamplejPanelSize(samplejFrame.getWidth(), value + BOARDER_SIZE_Y);
    }

    private void widthjSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_widthjSpinnerStateChanged
        setWidth(Integer.parseInt(widthjSpinner.getValue().toString()));
    }//GEN-LAST:event_widthjSpinnerStateChanged

    private void heightjSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_heightjSpinnerStateChanged
        setHeight(Integer.parseInt(heightjSpinner.getValue().toString()));
        if (pokerjRadioButton.isSelected()) {
            setPokerWidth();
        }
        else if (bridgejRadioButton.isSelected()) {
            setBridgeWidth();
        }
    }//GEN-LAST:event_heightjSpinnerStateChanged

    private void pokerjRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pokerjRadioButtonActionPerformed
        widthjSpinner.setEnabled(false);
        widthjButton.setEnabled(false);
        setPokerWidth();
    }//GEN-LAST:event_pokerjRadioButtonActionPerformed

    private void bridgejRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bridgejRadioButtonActionPerformed
        widthjSpinner.setEnabled(false);
        widthjButton.setEnabled(false);
        setBridgeWidth();
    }//GEN-LAST:event_bridgejRadioButtonActionPerformed

    private void freejRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_freejRadioButtonActionPerformed
        widthjSpinner.setEnabled(true);
        widthjButton.setEnabled(true);
    }//GEN-LAST:event_freejRadioButtonActionPerformed

    private void indexjComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexjComboBoxActionPerformed
        indexDirectory = baseDirectory + "indices\\" + indexjComboBox.getSelectedItem().toString()  + "\\";
        samplejPanel.setIndexDirectory(indexDirectory);
//        System.out.printf("indexjComboBoxActionPerformed(%s) -> %s\n", evt.getActionCommand(), indexDirectory);
    }//GEN-LAST:event_indexjComboBoxActionPerformed

    private void pipjComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pipjComboBoxActionPerformed
        pipDirectory = baseDirectory + "pips\\" + pipjComboBox.getSelectedItem().toString()  + "\\";
        samplejPanel.setPipDirectory(pipDirectory);
//        System.out.printf("pipjComboBoxActionPerformed(%s) -> %s\n", evt.getActionCommand(), pipDirectory);
    }//GEN-LAST:event_pipjComboBoxActionPerformed


    /**
     * Sets the Payload and fixes the "Modify Card Item" controls.
     */
    private void updatePayloadAndFixCardItemControls(Item item) {
        samplejPanel.setPayload(item);
        currentItem = item;
        updateModifyCardItemControls();

        itemHeightjButton.setToolTipText(item.getHButtonTip());
        itemCentreXjButton.setToolTipText(item.getXButtonTip());
        itemCentreYjButton.setToolTipText(item.getYButtonTip());

        itemHeightjLabel.setToolTipText(item.getHToolTip());
        itemCentreXjLabel.setToolTipText(item.getXToolTip());
        itemCentreYjLabel.setToolTipText(item.getYToolTip());
        itemHeightjLabel.setText(item.getHLabel());
        itemCentreXjLabel.setText(item.getXLabel());
        itemCentreYjLabel.setText(item.getYLabel());
    }

    /**
     * Update the "Modify Card Item" controls.
     */
    private void updateModifyCardItemControls() {
        // Set the values of the spinner controls for the currentItem card item.
        itemHeightjSpinner.setValue(Math.round(samplejPanel.getCurrentH() * 10));
        itemCentreXjSpinner.setValue(Math.round(samplejPanel.getCurrentX() * 10));
        itemCentreYjSpinner.setValue(Math.round(samplejPanel.getCurrentY() * 10));
    }

    /**
     * Change the enabled state of the Radio Buttons based on whether the
     * currentItem card is a court card or has a face image file on disc.
     */
    private void fixRadioButtonEnabledStates() {
        // Set status of Card Item radio buttons.
        boolean enable = indicesjCheckBox.isSelected();
        indicesjRadioButton.setEnabled(enable);

        enable = cornerPipjCheckBox.isSelected();
        cornerPipjRadioButton.setEnabled(enable);

        enable = standardPipjCheckBox.isSelected() && !samplejPanel.hasFaceImage();
        standardPipjRadioButton.setEnabled(enable);

        enable = facejCheckBox.isSelected() && samplejPanel.hasFaceImage();
        facejRadioButton.setEnabled(enable);

        enable = facePipjCheckBox.isSelected() && samplejPanel.isCourtCard();
        facePipjRadioButton.setEnabled(enable);
    }

    /**
     * Set the status of the height controls for the selected card item.
     * @param adjustHeight flag to indicate 
     */
    private void fixHeightControls(boolean adjustHeight) {

        itemHeightjLabel.setEnabled(adjustHeight);
        itemHeightjSpinner.setEnabled(adjustHeight);
        itemHeightjButton.setEnabled(adjustHeight);
    }

    /**
     * Set the selected item based on 'item' and call fixRadioButtonEnabledStates().
     * @param item to be set as selected.
     */
    private void setSelectedItem(Item item) {
        boolean adjustHeight = true;
        
        switch (item) {
            case INDEX:
                indicesjRadioButton.setSelected(true);
                break;
                
            case CORNER_PIP:
                cornerPipjRadioButton.setSelected(true);
                break;

            case STANDARD_PIP:
                standardPipjRadioButton.setSelected(true);
                break;

            case FACE:
                adjustHeight = false;
                facejRadioButton.setSelected(true);
                break;

            case FACE_PIP:
                facePipjRadioButton.setSelected(true);
                break;
        }
        fixRadioButtonEnabledStates();
        fixHeightControls(adjustHeight);

        updatePayloadAndFixCardItemControls(item);
    }

    private void nextCardjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextCardjButtonActionPerformed
//        System.out.printf("nextCardjButtonActionPerformed(%s)\n", evt.getActionCommand());
        setSelectedItem(samplejPanel.nextCard());
    }//GEN-LAST:event_nextCardjButtonActionPerformed

    private void previousCardjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousCardjButtonActionPerformed
//        System.out.printf("previousCardjButtonActionPerformed(%s)\n", evt.getActionCommand());
        setSelectedItem(samplejPanel.previousCard());
    }//GEN-LAST:event_previousCardjButtonActionPerformed

    private void previousSuitjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousSuitjButtonActionPerformed
//        System.out.printf("previousSuitjButtonActionPerformed(%s)\n", evt.getActionCommand());
        setSelectedItem(samplejPanel.previousSuit());
    }//GEN-LAST:event_previousSuitjButtonActionPerformed

    private void nextSuitjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextSuitjButtonActionPerformed
//        System.out.printf("nextSuitjButtonActionPerformed(%s)\n", evt.getActionCommand());
        setSelectedItem(samplejPanel.nextSuit());
    }//GEN-LAST:event_nextSuitjButtonActionPerformed

    private void indicesjRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indicesjRadioButtonActionPerformed
        updatePayloadAndFixCardItemControls(Item.INDEX);
        fixHeightControls(true);
    }//GEN-LAST:event_indicesjRadioButtonActionPerformed

    private void cornerPipjRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cornerPipjRadioButtonActionPerformed
        updatePayloadAndFixCardItemControls(Item.CORNER_PIP);
        fixHeightControls(true);
    }//GEN-LAST:event_cornerPipjRadioButtonActionPerformed

    private void standardPipjRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_standardPipjRadioButtonActionPerformed
        updatePayloadAndFixCardItemControls(Item.STANDARD_PIP);
        fixHeightControls(true);
    }//GEN-LAST:event_standardPipjRadioButtonActionPerformed

    private void facejRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_facejRadioButtonActionPerformed
        updatePayloadAndFixCardItemControls(Item.FACE);
        fixHeightControls(false);
    }//GEN-LAST:event_facejRadioButtonActionPerformed

    private void facePipjRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_facePipjRadioButtonActionPerformed
        updatePayloadAndFixCardItemControls(Item.FACE_PIP);
        fixHeightControls(true);
    }//GEN-LAST:event_facePipjRadioButtonActionPerformed

    private void indicesjCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indicesjCheckBoxActionPerformed
        final boolean enable = indicesjCheckBox.isSelected();
        indicesjRadioButton.setEnabled(enable);
        samplejPanel.setItemVisible(Item.INDEX, enable);
    }//GEN-LAST:event_indicesjCheckBoxActionPerformed

    private void cornerPipjCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cornerPipjCheckBoxActionPerformed
        final boolean enable = cornerPipjCheckBox.isSelected();
        cornerPipjRadioButton.setEnabled(enable);
        samplejPanel.setItemVisible(Item.CORNER_PIP, enable);
    }//GEN-LAST:event_cornerPipjCheckBoxActionPerformed

    private void standardPipjCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_standardPipjCheckBoxActionPerformed
        final boolean enable = standardPipjCheckBox.isSelected() && !samplejPanel.hasFaceImage();
        standardPipjRadioButton.setEnabled(enable);
        samplejPanel.setItemVisible(Item.STANDARD_PIP, standardPipjCheckBox.isSelected());
    }//GEN-LAST:event_standardPipjCheckBoxActionPerformed

    private void facejCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_facejCheckBoxActionPerformed
        final boolean enable = facejCheckBox.isSelected() && samplejPanel.hasFaceImage();
        facejRadioButton.setEnabled(enable);
        samplejPanel.setItemVisible(Item.FACE, facejCheckBox.isSelected());
    }//GEN-LAST:event_facejCheckBoxActionPerformed

    private void facePipjCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_facePipjCheckBoxActionPerformed
        final boolean enable = facePipjCheckBox.isSelected() && samplejPanel.isCourtCard();
        facePipjRadioButton.setEnabled(enable);
        samplejPanel.setItemVisible(Item.FACE_PIP, facePipjCheckBox.isSelected());
    }//GEN-LAST:event_facePipjCheckBoxActionPerformed

    private void keepAspectRatiojCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepAspectRatiojCheckBoxActionPerformed
        samplejPanel.setKeepAspectRatio(keepAspectRatiojCheckBox.isSelected());
        updateModifyCardItemControls();
    }//GEN-LAST:event_keepAspectRatiojCheckBoxActionPerformed

    private void itemHeightjSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_itemHeightjSpinnerStateChanged
        float value = Float.parseFloat(itemHeightjSpinner.getValue().toString()) / 10;
        samplejPanel.setCurrentH(value);
//        System.out.printf("itemHeightjSpinnerStateChanged()  = %f,\n", value);
    }//GEN-LAST:event_itemHeightjSpinnerStateChanged

    private void itemCentreXjSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_itemCentreXjSpinnerStateChanged
        float value = Float.parseFloat(itemCentreXjSpinner.getValue().toString()) / 10;
        samplejPanel.setCurrentX(value);
//        System.out.printf("itemCentreXjSpinnerStateChanged()  = %f,\n", value);
    }//GEN-LAST:event_itemCentreXjSpinnerStateChanged

    private void itemCentreYjSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_itemCentreYjSpinnerStateChanged
        float value = Float.parseFloat(itemCentreYjSpinner.getValue().toString()) / 10;
        samplejPanel.setCurrentY(value);
//        System.out.printf("itemCentreYjSpinnerStateChanged()  = %f,\n", value);
    }//GEN-LAST:event_itemCentreYjSpinnerStateChanged

    private void itemHeightjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemHeightjButtonActionPerformed
        itemHeightjSpinner.setValue(Math.round(currentItem.getH() * 10));
    }//GEN-LAST:event_itemHeightjButtonActionPerformed

    private void itemCentreXjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCentreXjButtonActionPerformed
        itemCentreXjSpinner.setValue(Math.round(currentItem.getX() * 10));
    }//GEN-LAST:event_itemCentreXjButtonActionPerformed

    private void itemCentreYjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemCentreYjButtonActionPerformed
        itemCentreYjSpinner.setValue(Math.round(currentItem.getY() * 10));
    }//GEN-LAST:event_itemCentreYjButtonActionPerformed

    private void generatejButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generatejButtonActionPerformed

        String outputDirectory = baseDirectory + "cards\\" + outputName + "\\";

        CardPanel generator = new CardPanel(samplejPanel);
        generator.generateDeck(baseDirectory, outputDirectory);
    }//GEN-LAST:event_generatejButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(StartGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StartGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StartGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StartGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StartGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup ItemSelectionbuttonGroup;
    private javax.swing.ButtonGroup aspectRatiobuttonGroup;
    private javax.swing.JPanel backgroundColourjPanel;
    private javax.swing.JButton baseDirectoryjButton;
    private javax.swing.JComboBox<String> baseDirectoryjComboBox;
    private javax.swing.JLabel baseDirectoryjLabel;
    private javax.swing.JRadioButton bridgejRadioButton;
    private javax.swing.JPanel cardSizejPanel;
    private javax.swing.JButton colourjButton;
    private javax.swing.JTextField colourjTextField;
    private javax.swing.JCheckBox cornerPipjCheckBox;
    private javax.swing.JRadioButton cornerPipjRadioButton;
    private javax.swing.JCheckBox facePipjCheckBox;
    private javax.swing.JRadioButton facePipjRadioButton;
    private javax.swing.JCheckBox facejCheckBox;
    private javax.swing.JComboBox<String> facejComboBox;
    private javax.swing.JLabel facejLabel;
    private javax.swing.JRadioButton facejRadioButton;
    private javax.swing.JRadioButton freejRadioButton;
    private javax.swing.JButton generatejButton;
    private javax.swing.JButton heightjButton;
    private javax.swing.JLabel heightjLabel;
    private javax.swing.JSpinner heightjSpinner;
    private javax.swing.JComboBox<String> indexjComboBox;
    private javax.swing.JLabel indexjLabel;
    private javax.swing.JCheckBox indicesjCheckBox;
    private javax.swing.JRadioButton indicesjRadioButton;
    private javax.swing.JPanel inputDirectoriesjPanel;
    private javax.swing.JButton itemCentreXjButton;
    private javax.swing.JLabel itemCentreXjLabel;
    private javax.swing.JSpinner itemCentreXjSpinner;
    private javax.swing.JButton itemCentreYjButton;
    private javax.swing.JLabel itemCentreYjLabel;
    private javax.swing.JSpinner itemCentreYjSpinner;
    private javax.swing.JPanel itemDisplayjPanel;
    private javax.swing.JButton itemHeightjButton;
    private javax.swing.JLabel itemHeightjLabel;
    private javax.swing.JSpinner itemHeightjSpinner;
    private javax.swing.JPanel itemModifyjPanel;
    private javax.swing.JPanel itemSelectjPanel;
    private javax.swing.JCheckBox keepAspectRatiojCheckBox;
    private javax.swing.JPanel navigatiovjPanel;
    private javax.swing.JButton nextCardjButton;
    private javax.swing.JButton nextSuitjButton;
    private javax.swing.JPanel outputDirectoryjPanel;
    private javax.swing.JTextField outputjTextField;
    private javax.swing.JToggleButton outputjToggleButton;
    private javax.swing.JLabel paddingjLabel;
    private javax.swing.JComboBox<String> pipjComboBox;
    private javax.swing.JLabel pipjLabel;
    private javax.swing.JRadioButton pokerjRadioButton;
    private javax.swing.JButton previousCardjButton;
    private javax.swing.JButton previousSuitjButton;
    private javax.swing.JCheckBox standardPipjCheckBox;
    private javax.swing.JRadioButton standardPipjRadioButton;
    private javax.swing.JButton widthjButton;
    private javax.swing.JLabel widthjLabel;
    private javax.swing.JSpinner widthjSpinner;
    // End of variables declaration//GEN-END:variables
}
