import java.awt.image.WritableRaster;

public class CompareSides {
    public int compareRightAndleftSidesOfPuzzle(WritableRaster rightSide, WritableRaster leftSide) {
        int diff = 0;
        for (int i = 0; i < rightSide.getHeight(); i++) {

            int[] pixelR = rightSide.getPixel(rightSide.getWidth() - 1, i, new int[4]);
            int[] pixelL = leftSide.getPixel(0, i, new int[4]);

            diff += calcDifferenceBetweenThePixels(pixelR, pixelL);
        }
        return diff;

    }

    public int compareBottomAndTopSidesOfPuzzle(WritableRaster bottom, WritableRaster top) {
        int diff = 0;
        for (int i = 0; i < bottom.getWidth(); i++) {
            int[] pixelD = bottom.getPixel(i, bottom.getHeight() - 1, new int[4]);
            int[] pixelT = top.getPixel(i, 0, new int[4]);

            diff += calcDifferenceBetweenThePixels(pixelD, pixelT);
        }
        return diff;
    }

    private int calcDifferenceBetweenThePixels(int[] pixel1, int[] pixel2) {
        int red = Math.abs(pixel1[0] - pixel2[0]);
        int green = Math.abs(pixel1[1] - pixel2[1]);
        int blue = Math.abs(pixel1[2] - pixel2[2]);
        return red + green + blue;
    }
}
