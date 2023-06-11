import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class PuzzleCreator extends JPanel {
    private static final int GAP_SIZE = 2;
    private static final int DEFAULT_ROWS = 4;
    private static final int DEFAULT_COLS = 4;
    private static final String FILEPATH = "src/main/resources/horse.jpg";

    private BufferedImage[][] puzzles;
    private final BufferedImage[][] originalPuzzles;
    private int rows;
    private int cols;
    private int gapSize;
    private int selectedPuzzleX;
    private int selectedPuzzleY;
    private int dragOffsetX;
    private int dragOffsetY;

    public PuzzleCreator(BufferedImage[][] puzzles, int gapSize) {
        this.puzzles = puzzles;
        this.rows = puzzles.length;
        this.cols = puzzles[0].length;
        this.gapSize = gapSize;

        originalPuzzles = copyPuzzles(puzzles);
        selectedPuzzleX = -1;
        selectedPuzzleY = -1;
        dragOffsetX = 0;
        dragOffsetY = 0;

        PuzzleMouseListener puzzleMouseListener = new PuzzleMouseListener();
        addMouseListener(puzzleMouseListener);
        addMouseMotionListener(puzzleMouseListener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int puzzleWidth = (getWidth() - (cols - 1) * gapSize) / cols;
        int puzzleHeight = (getHeight() - (rows - 1) * gapSize) / rows;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int puzzleX = x * (puzzleWidth + gapSize);
                int puzzleY = y * (puzzleHeight + gapSize);

                BufferedImage puzzle = puzzles[y][x];
                g.drawImage(puzzle, puzzleX, puzzleY, puzzleWidth, puzzleHeight, this);

                if (x == selectedPuzzleX && y == selectedPuzzleY) {
                    g.setColor(Color.RED);
                    g.drawRect(puzzleX, puzzleY, puzzleWidth, puzzleHeight);
                }
            }
        }
    }

    private class PuzzleMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int puzzleWidth = (getWidth() - (cols - 1) * gapSize) / cols;
            int puzzleHeight = (getHeight() - (rows - 1) * gapSize) / rows;

            int mouseX = e.getX();
            int mouseY = e.getY();

            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    int puzzleX = x * (puzzleWidth + gapSize);
                    int puzzleY = y * (puzzleHeight + gapSize);

                    if (mouseX >= puzzleX && mouseX < puzzleX + puzzleWidth
                            && mouseY >= puzzleY && mouseY < puzzleY + puzzleHeight) {
                        selectedPuzzleX = x;
                        selectedPuzzleY = y;
                        dragOffsetX = mouseX - puzzleX;
                        dragOffsetY = mouseY - puzzleY;
                        repaint();
                        return;
                    }
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int puzzleWidth = (getWidth() - (cols - 1) * gapSize) / cols;
            int puzzleHeight = (getHeight() - (rows - 1) * gapSize) / rows;

            int mouseX = e.getX();
            int mouseY = e.getY();

            int targetPuzzleX = (mouseX - dragOffsetX
                    + (puzzleWidth / 2)) / (puzzleWidth + gapSize);
            int targetPuzzleY = (mouseY - dragOffsetY
                    + (puzzleHeight / 2)) / (puzzleHeight + gapSize);

            if (targetPuzzleX >= 0 && targetPuzzleX < cols
                    && targetPuzzleY >= 0 && targetPuzzleY < rows
                    && (targetPuzzleX != selectedPuzzleX
                    || targetPuzzleY != selectedPuzzleY)) {

                BufferedImage selectedPuzzle = puzzles[selectedPuzzleY][selectedPuzzleX];
                BufferedImage targetPuzzle = puzzles[targetPuzzleY][targetPuzzleX];

                puzzles[selectedPuzzleY][selectedPuzzleX] = targetPuzzle;
                puzzles[targetPuzzleY][targetPuzzleX] = selectedPuzzle;

                selectedPuzzleX = targetPuzzleX;
                selectedPuzzleY = targetPuzzleY;

                repaint();
            }
        }
    }

    private boolean checkCompletion() {
        PuzzleValidator puzzleValidator = new PuzzleValidator(puzzles, originalPuzzles);
        boolean completed = puzzleValidator.isPuzzleCompleted();

        if (completed) {
            JOptionPane.showMessageDialog(this,
                    "Puzzles are completed!", "Completion", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Puzzles are not completed.", "Completion", JOptionPane.WARNING_MESSAGE);
        }
        return completed;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BufferedImage[][] puzzles = loadPuzzles();
            PuzzleCreator puzzleCreator = new PuzzleCreator(puzzles, GAP_SIZE);

            JFrame frame = new JFrame("Puzzle Creator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(puzzleCreator, BorderLayout.CENTER);

            JButton shuffleButton = new JButton("Shuffle");
            shuffleButton.addActionListener(e -> {
                shufflePuzzles(puzzles);
                puzzleCreator.repaint();
            });

            JButton checkButton = new JButton("Check Completion");
            checkButton.addActionListener(e -> {
                puzzleCreator.checkCompletion();
            });

            JButton restoreButton = new JButton("Restore");
            restoreButton.addActionListener(e -> {
                puzzleCreator.restorePuzzles();
            });

            JButton solveButton = new JButton("Help to solve");
            solveButton.addActionListener(e -> {
                if (!puzzleCreator.checkCompletion()) {
                    BufferedImage[][] solvedPuzzles = solvePuzzle(puzzles);
                    updatePuzzles(puzzles, solvedPuzzles);
                    puzzleCreator.repaint();
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(shuffleButton);
            buttonPanel.add(checkButton);
            buttonPanel.add(restoreButton);
            buttonPanel.add(solveButton);

            frame.add(buttonPanel, BorderLayout.SOUTH);

            int puzzleWidth = puzzles[0][0].getWidth();
            int puzzleHeight = puzzles[0][0].getHeight();

            int defaultWidth = puzzleWidth * puzzles[0].length + GAP_SIZE * (puzzles[0].length - 1);
            int defaultHeight = puzzleHeight * puzzles.length + GAP_SIZE * (puzzles.length - 1);

            frame.setSize(defaultWidth, defaultHeight);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static void updatePuzzles(BufferedImage[][] puzzles, BufferedImage[][] solvedPuzzles) {
        int rows = solvedPuzzles.length;
        int cols = solvedPuzzles[0].length;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                puzzles[y][x] = solvedPuzzles[y][x];
            }
        }
    }

    private static BufferedImage[][] solvePuzzle(BufferedImage[][] puzzles) {
        return PuzzleSolver.solvePuzzle(puzzles);
    }

    private static BufferedImage[][] loadPuzzles() {
        try {
            BufferedImage image = ImageIO.read(new File(FILEPATH));
            int rows = DEFAULT_ROWS;
            int cols = DEFAULT_COLS;
            int puzzleWidth = image.getWidth() / cols;
            int puzzleHeight = image.getHeight() / rows;

            BufferedImage[][] puzzles = new BufferedImage[rows][cols];
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    int puzzleX = x * puzzleWidth;
                    int puzzleY = y * puzzleHeight;
                    puzzles[y][x] = image.getSubimage(puzzleX, puzzleY, puzzleWidth, puzzleHeight);
                }
            }
            return puzzles;
        } catch (IOException e) {
            throw new RuntimeException("Can't load the picture.");
        }
    }

    private static void shufflePuzzles(BufferedImage[][] puzzles) {
        int rows = puzzles.length;
        int cols = puzzles[0].length;

        List<BufferedImage> puzzleList = new ArrayList<>();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                puzzleList.add(puzzles[y][x]);
            }
        }

        Collections.shuffle(puzzleList);

        int index = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                puzzles[y][x] = puzzleList.get(index++);
            }
        }
    }

    private void restorePuzzles() {
        updatePuzzles(puzzles, originalPuzzles);

        repaint();
    }

    public static BufferedImage[][] copyPuzzles(BufferedImage[][] originalPuzzles) {
        int rows = originalPuzzles.length;
        int cols = originalPuzzles[0].length;
        BufferedImage[][] copy = new BufferedImage[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                BufferedImage original = originalPuzzles[y][x];
                int width = original.getWidth();
                int height = original.getHeight();
                BufferedImage clone = new BufferedImage(width, height, original.getType());
                Graphics g = clone.getGraphics();
                g.drawImage(original, 0, 0, null);
                g.dispose();
                copy[y][x] = clone;
            }
        }
        return copy;
    }
}
