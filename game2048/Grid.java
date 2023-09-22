package game2048;

import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class Grid extends JPanel implements ActionListener {
    final private int SIDE_LEN = 500;
    final private int RECT_PADDING = 10;
    final private int RECT_ARC = 0;
    final private int ANIMATE_VELOCITY = 5;

    private Block[] blocks;
    private int rowLen;
    private int colLen;
    private ArrayList<Integer> emptyPos;
    private Boolean stuck;

    // for animation and GUI
    private int rectSpace;
    private int rectSide;
    private int xOffset[];
    private int yOffset[];
    private Timer swipeTimer;
    private Image img[];

    // initiate Grid with (rowCount * columnCount - 2) empty blocks and 2 random bl
    public Grid(int rowLen, int colLen) {
        this.rowLen = rowLen;
        this.colLen = colLen;
        rectSpace = SIDE_LEN / rowLen;
        rectSide = rectSpace - (RECT_PADDING * 2);

        // load images
        img = new Image[13];
        URL imgUrl;
        for (int i = 0; i < 13; i++) {
            imgUrl = getClass().getResource("/image/" + String.valueOf(i) + ".jpeg");
            img[i] = Toolkit.getDefaultToolkit().getImage(imgUrl)
                    .getScaledInstance(rectSide, rectSide, Image.SCALE_FAST);
        }

        // set panel
        setPreferredSize(new Dimension(SIDE_LEN, SIDE_LEN));

        // start the game
        initializeGrid();
    }

    public void initializeGrid() {
        // reset timer
        swipeTimer = new Timer(1, this);

        // set empty position
        emptyPos = new ArrayList<Integer>();
        stuck = false;
        blocks = new Block[rowLen * colLen];
        for (int r = 0; r < rowLen; r++) {
            for (int c = 0; c < colLen; c++) {
                setBlock(r, c, 0);
            }
        }

        // set offset
        xOffset = new int[rowLen * colLen];
        yOffset = new int[rowLen * colLen];
        for (int i = 0; i < rowLen * colLen; i++) {
            xOffset[i] = 0;
            yOffset[i] = 0;
        }

        // randomly set 2 blocks
        addBlock();
        addBlock();

        repaint();
    }

    // do noting if no block is found
    public void setBlock(int row, int column, int exponent) {
        if (row > rowLen - 1 || row < 0 || column > colLen - 1 || column < 0) {
            return;
        }
        int i = (row * rowLen) + column; // block index is related to how constructor initialize blocks
        blocks[i] = new Block(exponent);
        Integer pos = i;
        if (exponent == 0) {
            if (emptyPos.contains(pos)) {
                System.out.println("ERROR: More than one pos element is added to emptyPos");
            }
            emptyPos.add(pos);
        } else {
            emptyPos.remove(pos);
        }
    }

    // return exponent = -1 if no block is found
    public int getBlockExp(int row, int column) {
        if (row > rowLen - 1 || row < 0 || column > colLen - 1 || column < 0) {
            return -1;
        }
        int i = (row * rowLen) + column; // block index is related to how constructor initialize blocks
        return blocks[i].getExponent();
    }

    public int getBlockValue(int row, int column) {
        if (row > rowLen - 1 || row < 0 || column > colLen - 1 || column < 0) {
            return -1;
        }
        int i = (row * rowLen) + column; // block index is related to how constructor initialize blocks
        return blocks[i].getValue();
    }

    // Return score. Parameters limit: x = -1, 0, 1; y = -1, 0, 1; x*y==0; x+y!=0
    public int swipe(int xDirection, int yDirection) {

        int score = 0;
        Boolean hasMoved, hasCombined, hasChanged;
        Boolean isValidSwipe = false;

        do {
            hasChanged = false;
            do {
                hasMoved = false;
                for (int r = 0; r < rowLen; r++) {
                    for (int c = 0; c < colLen; c++) {
                        int exp = getBlockExp(r, c);
                        int frontExp = getBlockExp(r + yDirection, c + xDirection);
                        if (exp != 0 && frontExp == 0) {
                            setBlock(r + yDirection, c + xDirection, exp);
                            setBlock(r, c, 0);
                            hasMoved = true;
                            hasChanged = true;
                            isValidSwipe = true;

                            xOffset[(r + yDirection) * rowLen + (c + xDirection)] = xOffset[r * rowLen + c]
                                    + xDirection * (rectSide + RECT_PADDING);
                            yOffset[(r + yDirection) * rowLen + (c + xDirection)] = yOffset[r * rowLen + c]
                                    + yDirection * (rectSide + RECT_PADDING);
                            xOffset[r * rowLen + c] = 0;
                            yOffset[r * rowLen + c] = 0;
                        }
                    }
                }
            } while (hasMoved);

            do {
                hasCombined = false;
                for (int r = 0; r < rowLen; r++) {
                    for (int c = 0; c < colLen; c++) {
                        int exp = getBlockExp(r, c);
                        int frontExp = getBlockExp(r + yDirection, c + xDirection);
                        int backExp = getBlockExp(r - yDirection, c - xDirection);
                        if (backExp == exp
                                && exp != frontExp
                                && exp != 0) {
                            score += (int) Math.pow(2, exp);
                            setBlock(r, c, exp + 1);
                            setBlock(r - yDirection, c - xDirection, 0);
                            hasCombined = true;
                            hasChanged = true;
                            isValidSwipe = true;

                            xOffset[r * rowLen + c] = xOffset[(r - yDirection) * rowLen + (c - xDirection)]
                                    + xDirection * (rectSide + RECT_PADDING);
                            yOffset[r * rowLen + c] = yOffset[(r - yDirection) * rowLen + (c - xDirection)]
                                    + yDirection * (rectSide + RECT_PADDING);
                            xOffset[(r - yDirection) * rowLen + (c - xDirection)] = 0;
                            yOffset[(r - yDirection) * rowLen + (c - xDirection)] = 0;
                        }
                    }
                }
            } while (hasCombined);
        } while (hasChanged);

        // run animation until all offsets is 0
        swipeTimer.start();

        if (isValidSwipe)
            addBlock();

        return score;
    }

    public Boolean isStuck() {
        return stuck;
    }

    // Add a new block to empty position with exp 1 or 2
    private void addBlock() {
        if (emptyPos.isEmpty()) {
            return;
        }

        Random random = new Random();
        int pos = emptyPos.get(random.nextInt(emptyPos.size()));
        int newBlockRow = pos / rowLen;
        int newBlockCol = pos % colLen;
        int newExp = 1 + random.nextInt(2);
        setBlock(newBlockRow, newBlockCol, newExp);
        // stuck detection
        int exp;
        if (emptyPos.isEmpty()) {
            for (int i = 0; i < rowLen; i++) {
                for (int j = 0; j < colLen; j++) {
                    exp = getBlockExp(i, j);
                    if (exp == getBlockExp(i + 1, j)
                            || exp == getBlockExp(i - 1, j)
                            || exp == getBlockExp(i, j + 1)
                            || exp == getBlockExp(i, j - 1)) {
                        stuck = false;
                        return;
                    }
                }
            }
            stuck = true;
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2D = (Graphics2D) g;

        int xPos, yPos;

        for (int i = 0; i < rowLen; i++) {
            for (int j = 0; j < colLen; j++) {
                xPos = RECT_PADDING + j * rectSpace;
                yPos = RECT_PADDING + i * rectSpace;

                // draw
                g2D.drawRoundRect(xPos, yPos, rectSide, rectSide, RECT_ARC, RECT_ARC);

                int blockValue = getBlockValue(i, j);
                int blockExp = getBlockExp(i, j);
                if (blockValue != 1) {
                    g2D.setColor(new Color(255, 255 - blockExp * 5, 204 - blockExp * 5)); 
                    g2D.fillRoundRect(xPos - xOffset[i * rowLen + j], yPos - yOffset[i * rowLen + j], rectSide,
                            rectSide, RECT_ARC, RECT_ARC);
                    // draw image
                    g2D.drawImage(img[blockExp], xPos - xOffset[i * rowLen + j], yPos - yOffset[i * rowLen + j], this);

                    // draw number string
                    g2D.setColor(Color.black);
                    g2D.drawString(String.valueOf(blockValue), xPos - xOffset[i * rowLen + j] + rectSide / 2,
                            yPos - yOffset[i * rowLen + j]);

                    g2D.setColor(Color.black);
                }

            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        repaint();
        Boolean noOffset = true;
        for (int i = 0; i < rowLen * colLen; i++) {
            if (xOffset[i] > 0) {
                xOffset[i] -= ANIMATE_VELOCITY;
                noOffset = false;
            } else if (xOffset[i] < 0) {
                xOffset[i] += ANIMATE_VELOCITY;
                noOffset = false;
            } else if (yOffset[i] > 0) {
                yOffset[i] -= ANIMATE_VELOCITY;
                noOffset = false;
            } else if (yOffset[i] < 0) {
                yOffset[i] += ANIMATE_VELOCITY;
                noOffset = false;
            }
        }
        if (noOffset) {
            swipeTimer.stop();
        }

    }

    // for debugging
    public void printGrid() {
        String s = "";
        for (int i = 0; i < rowLen; i++) {
            for (int j = 0; j < colLen; j++) {
                s += String.valueOf(getBlockExp(i, j)) + " ";
            }
            System.out.println(s);
            s = "";
        }
        System.out.println("--------");
    }
}
