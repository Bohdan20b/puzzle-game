import java.awt.image.BufferedImage;

public class PuzzleSolver {
    /*
    The purpose of this class is to solve the puzzle
    by comparing the rgb difference of each pixel on the edge of
    each side of the puzzle piece.
     */
    public static BufferedImage[][] solvePuzzle(BufferedImage[][] puzzles) {
        int rows = puzzles.length;
        int cols = puzzles[0].length;
        BufferedImage[][] solvedPuzzle = new BufferedImage[rows][cols];

        // Copy the puzzle array to the solvedPuzzle array
        for (int i = 0; i < rows; i++) {
            System.arraycopy(puzzles[i], 0, solvedPuzzle[i], 0, cols);
        }

        // Sort the puzzle array by comparing edge pixels' differences
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int minDiff = Integer.MAX_VALUE;
                int minDiffIndex = -1;

                for (int k = i; k < rows; k++) {
                    for (int l = (k == i) ? j + 1 : 0; l < cols; l++) {
                        int diff = computeEdgePixelsDifference(solvedPuzzle[i][j],
                                solvedPuzzle[k][l]);

                        if (diff < minDiff) {
                            minDiff = diff;
                            minDiffIndex = l;
                        }
                    }
                }

                if (minDiffIndex != -1) {
                    BufferedImage temp = solvedPuzzle[i][j];
                    solvedPuzzle[i][j] = solvedPuzzle[i][minDiffIndex];
                    solvedPuzzle[i][minDiffIndex] = temp;
                }
            }
        }

        return solvedPuzzle;
    }

    private static int computeEdgePixelsDifference(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        int difference = 0;

        // Top edge
        for (int x = 0; x < width; x++) {
            difference += pixelDifference(img1.getRGB(x, 0), img2.getRGB(x, height - 1));
        }

        // Right edge
        for (int y = 0; y < height; y++) {
            difference += pixelDifference(img1.getRGB(width - 1, y), img2.getRGB(0, y));
        }

        // Bottom edge
        for (int x = 0; x < width; x++) {
            difference += pixelDifference(img1.getRGB(x, height - 1), img2.getRGB(x, 0));
        }

        // Left edge
        for (int y = 0; y < height; y++) {
            difference += pixelDifference(img1.getRGB(0, y), img2.getRGB(width - 1, y));
        }

        return difference;
    }

    private static int pixelDifference(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;

        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;

        // Compute the absolute difference for each color channel
        int diffR = Math.abs(r1 - r2);
        int diffG = Math.abs(g1 - g2);
        int diffB = Math.abs(b1 - b2);

        // Compute the total difference as the sum of color channel differences
        return diffR + diffG + diffB;
    }
}
