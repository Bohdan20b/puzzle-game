import java.awt.image.BufferedImage;

public class PuzzleValidator {
    private final BufferedImage[][] puzzles;
    private final BufferedImage[][] originalPuzzles;

    public PuzzleValidator(BufferedImage[][] puzzles, BufferedImage[][] originalPuzzles) {
        this.puzzles = puzzles;
        this.originalPuzzles = originalPuzzles;
    }

    public boolean isPuzzleCompleted() {
        for (int y = 0; y < puzzles.length; y++) {
            for (int x = 0; x < puzzles[0].length; x++) {
                if (!isPuzzleSameAsOriginal(puzzles[y][x], originalPuzzles[y][x])) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isPuzzleSameAsOriginal(BufferedImage puzzle, BufferedImage originalPuzzle) {
        if (puzzle.getWidth() != originalPuzzle.getWidth()
                || puzzle.getHeight() != originalPuzzle.getHeight()) {
            return false;
        }

        for (int y = 0; y < puzzle.getHeight(); y++) {
            for (int x = 0; x < puzzle.getWidth(); x++) {
                if (puzzle.getRGB(x, y) != originalPuzzle.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
}
