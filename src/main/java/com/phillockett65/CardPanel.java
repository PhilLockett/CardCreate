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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.Files.notExists;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author Phil
 */
public class CardPanel extends javax.swing.JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final Handle handle;
    private Payload index = null;
    private Payload cornerPip = null;
    private Payload standardPip = null;
    private Payload face = null;
    private Payload facePip = null;
    private Payload current = null;
    private int cardWidthPX = Default.WIDTH.intr();
    private int cardHeightPX = Default.HEIGHT.intr();
    private int suit = 0;
    private int card = 10;
    private Color colour;
    private boolean keepAspectRatio = false;

    private final String[] suits = { "C", "D", "H", "S" };
    private final String[] alts  = { "S", "H", "D", "C" };
    private final String[] cards = { "Joker", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K" };
    private String faceDirectory;
    private String indexDirectory;
    private String pipDirectory;

    private final IconDispatcher iconDispatcher;
    private final boolean generate;
    private int BOARDER_SIZE_X = 0;
    private int BOARDER_SIZE_Y = 0;

    /**
     * Creates new form CardPanel
     * @param w width of panel
     * @param h height of panel
     */
    public CardPanel() {
        initComponents();

        addMouseListener(new MouseClass());
        addMouseMotionListener(new MouseClass());

        handle = new Handle();
        iconDispatcher = new IconDispatcher();
        generate = false;
//        System.out.printf("CardPanel() Width = %d,  Height = %d\n", this.getWidth(), this.getHeight());
    }

    public void setBoarders(int x, int y) {
        BOARDER_SIZE_X = x;
        BOARDER_SIZE_Y = y;
    }

    /**
     * Creates new CardPanel specifically for generating cards.
     * @param cardPanel card panel to clone.
     */
    public CardPanel(CardPanel cardPanel) {
        initComponents();

        BOARDER_SIZE_X = cardPanel.BOARDER_SIZE_X;
        BOARDER_SIZE_Y = cardPanel.BOARDER_SIZE_Y;
        cardWidthPX = cardPanel.cardWidthPX;
        cardHeightPX = cardPanel.cardHeightPX;

        suit = 0;
        card = 1;
        colour = cardPanel.colour;
        keepAspectRatio = cardPanel.keepAspectRatio;

        faceDirectory = cardPanel.faceDirectory;
        indexDirectory = cardPanel.indexDirectory;
        pipDirectory = cardPanel.pipDirectory;
        
        if (cardPanel.index != null) {
            index = new Payload(cardPanel.index.getPath(), cardWidthPX, cardHeightPX, 0, Item.INDEX);
            index.copyPercentages(cardPanel.index);
        } else
              index = new Payload("", cardWidthPX, cardHeightPX, 0, Item.INDEX);

        if (cardPanel.cornerPip != null) {
            cornerPip = new Payload(cardPanel.cornerPip.getPath(), cardWidthPX, cardHeightPX, 0, Item.CORNER_PIP);
            cornerPip.copyPercentages(cardPanel.cornerPip);
        } else
            cornerPip = new Payload("", cardWidthPX, cardHeightPX, 0, Item.CORNER_PIP);

        if (cardPanel.standardPip != null) {
            standardPip = new Payload(cardPanel.standardPip.getPath(), cardWidthPX, cardHeightPX, card, Item.STANDARD_PIP);
            standardPip.copyPercentages(cardPanel.standardPip);
        } else
            standardPip = new Payload("", cardWidthPX, cardHeightPX, card, Item.STANDARD_PIP);

        if (cardPanel.face != null) {
            face = new Payload(cardPanel.face.getPath(), cardWidthPX, cardHeightPX, Payload.PAINT_FILE, Item.FACE);
            face.copyPercentages(cardPanel.face);
            face.setKeepAspectRatio(cardPanel.keepAspectRatio);
        } else
            face = new Payload("", cardWidthPX, cardHeightPX, Payload.PAINT_FILE, Item.FACE);
        
        if (cardPanel.facePip != null) {
            facePip = new Payload(cardPanel.facePip.getPath(), cardWidthPX, cardHeightPX, 0, Item.FACE_PIP);
            facePip.copyPercentages(cardPanel.facePip);
        } else
            facePip = new Payload("", cardWidthPX, cardHeightPX, 0, Item.FACE_PIP);

        handle = new Handle();
        iconDispatcher = null;
        generate = true;        // Make sure we generate to disc.
    }

    private class MouseClass extends MouseInputAdapter  {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isAltDown()) {
                float v = current.decSize();
                repaint();
                iconDispatcher.resizeIcon(v);
            }
            else if (e.isControlDown()) {
                float v = current.incSize();
                repaint();
                iconDispatcher.resizeIcon(v);
            }
            else if (e.isShiftDown()) {
            }
            else {
                nextPayload();
            }
        }
        @Override
        public void mousePressed(MouseEvent e) {
            grabHandle(e.getPoint());
        }
        @Override
        public void mouseDragged(MouseEvent e){
            moveHandle(e.getPoint());
            iconDispatcher.moveIcon(e.getX(), e.getY());
        }
    }

    public void addIconListener(IconListener l) {
        iconDispatcher.addIconListener(l);
    }

    public boolean isSetUp() {
        if (index == null)
            return false;
        if (cornerPip == null)
            return false;
        if (standardPip == null)
            return false;
        if (face == null)
            return false;
        if (facePip == null)
            return false;
        if (current == null)
            return false;

        return true;
    }

    private boolean setIndex(String path) {
        Payload payload = new Payload(path, cardWidthPX, cardHeightPX, 0, Item.INDEX);
        if (index != null)
            payload.copyPercentages(index);

        if ((current == null) || (current == index)) {
            current = payload;
            handle.setPayload(current);
        }

        index = payload;
        repaint();

        return true;
    }
    
    private boolean setCornerPip(String path) {
        Payload payload = new Payload(path, cardWidthPX, cardHeightPX, 0, Item.CORNER_PIP);
        if (cornerPip != null)
            payload.copyPercentages(cornerPip);

        if ((current == null) || (current == cornerPip)) {
            current = payload;
            handle.setPayload(current);
        }

        cornerPip = payload;
        repaint();

        return true;
    }
    
    private boolean setStandardPip(String path) {
        Payload payload = new Payload(path, cardWidthPX, cardHeightPX, getCard(), Item.STANDARD_PIP);
        if (standardPip != null)
            payload.copyPercentages(standardPip);

        if ((current == null) || (current == standardPip)) {
            current = payload;
            handle.setPayload(current);
        }

        standardPip = payload;
        repaint();

        return true;
    }
    
    private boolean setFace(String path) {
//        System.out.printf("setFace(%s)\n", path);
        if (!hasFaceImage()) {
//            System.out.printf("No face file found, so abort!\n", path);
            return false;
        }
        int pattern = generate ? Payload.PAINT_FILE : Payload.PAINT_DISPLAY;
        Payload payload = new Payload(path, cardWidthPX, cardHeightPX, pattern, Item.FACE);
        if (face != null)
            payload.copyPercentages(face);

        if ((current == null) || (current == face)) {
            current = payload;
            handle.setPayload(current);
        }

        face = payload;
        face.setKeepAspectRatio(keepAspectRatio);
        repaint();

        return true;
    }
    
    private boolean setFacePip(String path) {
//        System.out.printf("setFacePip(%s)\n", path);
        int width = cardWidthPX;
        int height = cardHeightPX;
//        if (face != null) {
//            width -= face.getCentreX() * 2;
//            height -= face.getCentreY() * 2;
//        }
        Payload payload = new Payload(path, width, height, 0, Item.FACE_PIP);
        if (facePip != null)
            payload.copyPercentages(facePip);

        if ((current == null) || (current == facePip)) {
            current = payload;
            handle.setPayload(current);
        }

        facePip = payload;
        repaint();

        return true;
    }
    
    public void resizeCardPanel(int w, int h) {
//        System.out.printf("resizeCardPanel() Width = %d,  Height = %d\n", w, h);
        cardWidthPX = w - BOARDER_SIZE_X;
        cardHeightPX = h - BOARDER_SIZE_Y;
        setSize(w, h);

        if (index != null)
            index.resizeCard(cardWidthPX, cardHeightPX);

        if (standardPip != null)
            standardPip.resizeCard(cardWidthPX, cardHeightPX);

        if (cornerPip != null)
            cornerPip.resizeCard(cardWidthPX, cardHeightPX);

        if (face != null)
            face.resizeCard(cardWidthPX, cardHeightPX);

        if (facePip != null)
            facePip.resizeCard(cardWidthPX, cardHeightPX);

        handle.syncToPayload();
    }

    public boolean nextPayload() {
        List<Payload> next = new ArrayList<>();

        // Build list of next items.
        if (index != null)
            next.add(index);
        if (cornerPip != null)
            next.add(cornerPip);
        if (hasFaceImage()) {
            if (face != null)
                next.add(face);
        }
        else {
            if (standardPip != null)
                next.add(standardPip);
        }
        if (isCourtCard()) {
            if (facePip != null)
                next.add(facePip);
        }
        
        // Find current in list.
        int i;
        for (i = 0; i < next.size(); i++) {
            if (next.get(i) == current)
                break;
        }

        // Find valid next item in list.
        Payload target;
        for (i++; i < next.size(); i++) {
            target = next.get(i);
            if ((target != null) && (target.isVisible()))
                break;
        }

        // Wrap around if necessary.
        if (i == next.size()) {
            for (i = 0; i < next.size(); i++) {
                target = next.get(i);
                if ((target != null) && (target.isVisible()))
                    break;
                if (target == current)
                    break;
            }
        }

        // If no other item is available return unchanged.
        if (next.get(i) == current) {
            return false;
        }

        // Set up next item.
        current = next.get(i);
        handle.setPayload(current);
        repaint();
        iconDispatcher.changeIcon(current.getItem());
        
        return true;
    }
         
    public void setItemVisible(Item selected, boolean display) {
            switch (selected) {
            case INDEX:
                index.setVisible(display);
                break;
                
            case CORNER_PIP:
                cornerPip.setVisible(display);
                break;

            case STANDARD_PIP:
                standardPip.setVisible(display);
                break;

            case FACE:
                face.setVisible(display);
                break;

            case FACE_PIP:
                facePip.setVisible(display);
                break;
        }
        repaint();
    }

    public Item recommendedItem() {
        if (current == index) {
            return Item.INDEX;
        } else
        if (current == cornerPip) {
            return Item.CORNER_PIP;
        } else
        if (current == standardPip) {
            if (hasFaceImage())
                return Item.INDEX;

            return Item.STANDARD_PIP;
        } else
        if (current == face) {
            if (!hasFaceImage())
                return Item.INDEX;

            return Item.FACE;
        } else
        if (current == facePip) {
            if (!isCourtCard())
                return Item.INDEX;

            return Item.FACE_PIP;
        } else
            return Item.INDEX;
    }
    
    public boolean setPayload(Item selected) {
        switch (selected) {
            case INDEX:
                if (index != null)
                    current = index;
                else
                    return false;
                break;
                
            case CORNER_PIP:
                if (cornerPip != null)
                    current = cornerPip;
                else
                    return false;
                break;

            case STANDARD_PIP:
                if ((!isCourtCard()) &&(standardPip != null))
                    current = standardPip;
                else
                    return false;
                break;

            case FACE:
                if ((hasFaceImage()) && (face != null))
                    current = face;
                else
                    return false;
                break;

            case FACE_PIP:
                if ((isCourtCard()) &&(facePip != null))
                    current = facePip;
                else
                    return false;
                break;
        }
        handle.setPayload(current);
        repaint();

        return true;
    }

    private Boolean isPointValid(Point p) {
        if ((p.x < 0) || (p.x > cardWidthPX))
            return false;
        
        if ((p.y < 0) || (p.y > cardHeightPX))
            return false;
        
        return true;
    }

    private void grabHandle(Point p) {
        if (!isPointValid(p))
            return;

        handle.setMouse(p.x, p.y);
//        System.out.printf("Width = %d,  Height = %d\n", this.getWidth(), this.getHeight());
    }

    private void moveHandle(Point p) {
        if (!isPointValid(p))
            return;

        final int x = p.x;
        final int y = p.y;

        if (!handle.isOver(x, y))
            return;

        // Current square state, stored as final variables 
        // to avoid repeat invocations of the same methods.
        final int MOUSE_X = handle.getXMouse();
        final int MOUSE_Y = handle.getYMouse();

        if ((MOUSE_X!=x) || (MOUSE_Y!=y)) {
            // The square is moving, repaint background 
            // over the old square location. 
            repaint();

            // Update coordinates.
            handle.setRel(x, y);

            // Repaint the square at the new location.
            repaint();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250, 500);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D)g;
        paintCard(g2d);
        handle.paint(g2d);
    }

    /**
     * Paint the components of the current card to the graphics object, which 
     * may be the JPanel or an image.
     * @param g2d 2D graphics object.
     */
    private void paintCard(Graphics2D g2d) {

        paintCardOutline(g2d);

        // Abort if not set up.
        if (!isSetUp())
            return;

        if (hasFaceImage()) {
            face.paintPatterns(g2d);
        }
        else {
            standardPip.paintPatterns(g2d);
        }

        index.paintPatterns(g2d);
        cornerPip.paintPatterns(g2d);
        if (isCourtCard())
            facePip.paintPatterns(g2d);
    }

    private void paintJoker(Graphics2D g2d) {

        paintCardOutline(g2d);

        face.paintPatterns(g2d);
        index.paintPatterns(g2d);
    }

    private void paintCardOutline(Graphics2D g2d) {

        final int w = cardWidthPX;
        final int h = cardHeightPX;

        g2d.setColor(colour);
        int r = Math.round(3.76F * h / 50);
        g2d.fillRoundRect(0, 0, w, h, r, r);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(0, 0, w, h, r, r);
    }

    /**
     * Generate the deck of cards in the outputDirectory.
     * @param baseDirectory used for finding the boneyard directory.
     * @param outputDirectory where the cards are to be generated.
     */
    public void generateDeck(String baseDirectory, String outputDirectory) {

        // Only allowed for generate versions of the CardPanel object.
        if (!generate)
            return;

        // Ensure that the output directory exists.
        try {
            Path outputDir = Paths.get(outputDirectory);
            if (notExists(outputDir, LinkOption.NOFOLLOW_LINKS)) {
                Files.createDirectories(outputDir);
            }
        } catch (IOException e) {
        }

        // Reset everything.
        suit = 0;
        card = 1;
        nextCard();
        nextSuit();
        previousCard();
        previousSuit();

        // Generate the cards.
        final int w = cardWidthPX;
        final int h = cardHeightPX;

        RenderingHints render = new RenderingHints(
             RenderingHints.KEY_RENDERING,
             RenderingHints.VALUE_RENDER_QUALITY);
        render.put(RenderingHints.KEY_DITHERING,
             RenderingHints.VALUE_DITHER_ENABLE);
        render.put(RenderingHints.KEY_ANTIALIASING,
             RenderingHints.VALUE_ANTIALIAS_ON);
        render.put(RenderingHints.KEY_STROKE_CONTROL,
             RenderingHints.VALUE_STROKE_PURE);
        BufferedImage cardImg;

        for (int s = 0; s < suits.length; s++) {
            for (int c = 1; c < cards.length; c++) {

                // Paint the card.
                cardImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = cardImg.createGraphics();
                g2d.setRenderingHints(render);
                paintCard(g2d);
                
                // Save the card to disc.
                String pathToNewImage = outputDirectory + suits[suit] + cards[card] + ".png";
                try {
                    File outputfile = new File(pathToNewImage);
                    ImageIO.write(cardImg, "png", outputfile);
                } catch (IOException e) {
                }
                
                nextCard();
            }
            nextSuit();
        }

        // Generate Jokers
        card = 0;
        standardPip.setVisible(false);
        index.setVisible(false);
        cornerPip.setVisible(false);
        facePip.setVisible(false);
        
        // Minimise the boarders for the Jokers.
        face.setX(7F);
        face.setY(5F);
        face.setKeepAspectRatio(true);

        index.setSize(30F);
        index.setY(20F);

        for (String currentSuit : suits) {
            // Paint the card.
            cardImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = cardImg.createGraphics();
            String pathToFaceImage = faceDirectory + currentSuit + cards[card] + ".png";
            File file = new File(pathToFaceImage);
            if (!file.exists()) {
                pathToFaceImage = baseDirectory + "boneyard\\Back.png";
            }
            Payload joker = new Payload(pathToFaceImage, cardWidthPX, cardHeightPX, Payload.PAINT_FILE, Item.FACE);
            joker.copyPercentages(face);
            face = joker;

            // Check for Joker indices.
            String pathToIndexImage = indexDirectory + currentSuit + cards[card] + ".png";
            File indexFile = new File(pathToIndexImage);
            if (indexFile.exists()) {
                Payload jokerIndex = new Payload(pathToIndexImage, cardWidthPX, cardHeightPX, 0, Item.INDEX);
                index.setVisible(true);
                jokerIndex.copyPercentages(index);
                index = jokerIndex;
            } else {
                index.setVisible(false);
            }

            paintJoker(g2d);

            // Save the card to disc.
            String pathToNewImage = outputDirectory + currentSuit + cards[card] + ".png";
            try {
                File outputfile = new File(pathToNewImage);
                ImageIO.write(cardImg, "png", outputfile);
            } catch (IOException e) {
            }
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setPreferredSize(new java.awt.Dimension(380, 532));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 380, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 532, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param colour the background colour of the card, not the JPanel.
     */
    public void setColour(Color colour) {
        this.colour = colour;
        repaint();
    }

    /**
     * @return the current card index.
     */
    public int getCard() {
        return card;
    }

    /**
     * @param card index of the card to set as current.
     */
    public void setCard(int card) {
        this.card = card;
        standardPip.setPattern(card);
    }

    /**
     * Checks if the current card has a face image file on disc.
     * @return true if the card has a face file, false otherwise.
     */
    public boolean hasFaceImage() {
        String pathToFaceImage = faceDirectory + suits[suit] + cards[card] + ".png";
        File file = new File(pathToFaceImage);

        return file.exists();
    }

    /**
     * Checks if the current card is a card.
     * @return true if the card is a court card, false otherwise.
     */
    public boolean isCourtCard() {

        return (card > 10);
    }

    /**
     * @return the centre X co-ordinate of the current item as a percentage of the card width.
     */
    public float getCurrentX() {
        if (current == null)
            return 10;

        return current.getSpriteX();
    }

    /**
     * @return the centre Y co-ordinate of the current item as a percentage of the card height.
     */
    public float getCurrentY() {
        if (current == null)
            return 10;

        return current.getSpriteY();
    }

    /**
     * @return the width of the current item as a percentage of the card width.
     */
    public float getCurrentW() {
        if (current == null)
            return 10;

        return current.getSpriteW();
    }

    /**
     * @return the height of the current item as a percentage of the card height.
     */
    public float getCurrentH() {
        if (current == null)
            return 10;

        return current.getSpriteH();
    }

    /**
     * Set the centre X co-ordinate of the current item as a percentage of the card width.
     * @param value as a percentage of the card width.
     */
    public void setCurrentX(float value) {
        if (current == null)
            return;

        current.setX(value);
        handle.syncToPayload();
        repaint();
    }

    /**
     * Set the centre Y co-ordinate of the current item as a percentage of the card height.
     * @param value as a percentage of the card height.
     */
    public void setCurrentY(float value) {
        if (current == null)
            return;

        current.setY(value);
        handle.syncToPayload();
        repaint();
    }

    /**
     * Set the height of the current item as a percentage of the card height.
     * @param size as a percentage of the card height.
     */
    public void setCurrentH(float size) {
//        System.out.printf("setCurrentH(%f)\n", size);
        if (current == null)
            return;

        current.setSize(size);
        handle.syncToPayload();
        repaint();
    }

   
    private void updateFaceImage() {
        String pathToFaceImage = faceDirectory + suits[suit] + cards[card] + ".png";
        setFace(pathToFaceImage);
    }

    public Item setFaceDirectory(String directory) {
        faceDirectory = directory;
        updateFaceImage();

        return recommendedItem();
    }

    private void updateIndexImage() {
        String pathToIndexImage = indexDirectory + suits[suit] + cards[card] + ".png";
        File file = new File(pathToIndexImage);
        if (!file.exists()) {
            pathToIndexImage = indexDirectory + alts[suit] + cards[card] + ".png";
        }
        setIndex(pathToIndexImage);
    }

    public Item previousCard() {
        card--;
        if (card == 0)
            card = 13;
        standardPip.setPattern(card);
        updateIndexImage();
        updateFaceImage();
        
        return recommendedItem();
    }

    public Item nextCard() {
        card++;
        if (card > 13)
            card = 1;
        standardPip.setPattern(card);
        updateIndexImage();
        updateFaceImage();
        
        return recommendedItem();
    }

    public void setIndexDirectory(String directory) {
        indexDirectory = directory;
        updateIndexImage();
    }


    /**
     * Change the Pip Payloads based on "pipDirectory" and "suit".
     */
    private void updatePipImages() {
        
        String pathToStandardPip = pipDirectory + suits[suit] + ".png";
        boolean standardPipFileExists = new File(pathToStandardPip).exists();

        String pathToCornerPip = pipDirectory + suits[suit] + "S.png";
        boolean cornerPipFileExists = new File(pathToCornerPip).exists();
        
        if ((standardPipFileExists) && (cornerPipFileExists)) {
            setStandardPip(pathToStandardPip);
            setCornerPip(pathToCornerPip);
            setFacePip(pathToStandardPip);
        } else
        if (standardPipFileExists) {
            setStandardPip(pathToStandardPip);
            setCornerPip(pathToStandardPip);
            setFacePip(pathToStandardPip);
        } else
        if (cornerPipFileExists) {
            setStandardPip(pathToCornerPip);
            setCornerPip(pathToCornerPip);
            setFacePip(pathToCornerPip);
        }
    }

    public Item previousSuit() {
        suit--;
        if (suit < 0)
            suit = 3;
        updatePipImages();
        updateIndexImage();
        updateFaceImage();
        
        return recommendedItem();
    }

    public Item nextSuit() {
        suit++;
        if (suit > 3)
            suit = 0;
        updatePipImages();
        updateIndexImage();
        updateFaceImage();
        
        return recommendedItem();
    }

    public void setPipDirectory(String directory) {
        pipDirectory = directory;
        updatePipImages();
    }

     /**
     * Flag whether the face image will maintain their aspect ratio.
     * @param keepAspectRatio if true, maintain the aspect ratio.
     */
    public void setKeepAspectRatio(boolean keepAspectRatio) {
        this.keepAspectRatio = keepAspectRatio;
        if (face != null) {
            face.setKeepAspectRatio(keepAspectRatio);
        }
        repaint();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
