package game2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Board extends JFrame { 
    final private int ROW_LEN = 4; 
    final private int COL_LEN = 4;

    private Grid grid;
    private int score;
    private Boolean gameOver;

    private JLabel scoreLabel;
    private JLabel hintLabel;
    private JButton restartButton;
    
    public Board(String name) {
        super(name);
        setResizable(false);
        grid = new Grid(ROW_LEN, COL_LEN);
        score = 0;
        gameOver = false;
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.addComponentsToPane(this.getContentPane());

        this.pack();
        this.setVisible(true);
    }

    private void addComponentsToPane(final Container container) {

        grid.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("W"), "up");
        grid.getActionMap().put("up", new SwipeAction(0, -1));
        grid.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("A"), "left");
        grid.getActionMap().put("left", new SwipeAction(-1, 0));
        grid.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("S"), "down");
        grid.getActionMap().put("down", new SwipeAction(0, 1));
        grid.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("D"), "right");
        grid.getActionMap().put("right", new SwipeAction(1, 0));

        // score label
        scoreLabel = new JLabel("SCORE: " + String.valueOf(score));

        // game over label
        hintLabel = new JLabel(gameOver ? "Game Over" : "Use keys WASD to swipe");

        // restart button
        restartButton = new JButton("Restart");
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                score = 0;
                gameOver = false;
                grid.initializeGrid();
                updatePan();
            }
        });

        // information Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(1, 2));
        infoPanel.add(scoreLabel);
        infoPanel.add(hintLabel);
        infoPanel.add(restartButton);

        // add to main panel
        container.add(grid, BorderLayout.NORTH);
        container.add(new JSeparator(), BorderLayout.CENTER);
        container.add(infoPanel, BorderLayout.SOUTH);
    }

    private void updatePan() {
        if (gameOver) {
            return;
        }

        // update score label
        scoreLabel.setText("SCORE: " + String.valueOf(score));

        // update game over label
        if (grid.isStuck()) {
            gameOver = true;
            hintLabel.setText("Game Over!");
        } else {
            hintLabel.setText("Use keys WASD to swipe");
        }
    }

    private class SwipeAction extends AbstractAction {
        int xDirection;
        int yDirection; 
        
        SwipeAction(int xDirection, int yDirection) {
            this.xDirection = xDirection;
            this.yDirection = yDirection;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (gameOver) {
                return;
            }
            score += grid.swipe(xDirection, yDirection);
            updatePan();
        }

    }

}
