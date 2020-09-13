import java.awt.image.WritableRaster;

public class Test {
    public int compareRightAndleftSidesOfPuzzle(WritableRaster raster1, WritableRaster raster2) {
        int diff = 0;
        for (int i = 0; i < raster1.getHeight(); i++) {
            int[] pixelR = raster1.getPixel(raster1.getWidth() - 1, i, new int[4]);//right
            int[] pixelL = raster2.getPixel(0, i, new int[4]);//left

            diff += calcDifferenceBetweenThePixels(pixelR, pixelL);
        }
        return diff;
    }

    public int compareDownAndTopSidesOfPuzzle(WritableRaster raster1, WritableRaster raster2) {
        int diff = 0;
        for (int i = 0; i < raster1.getWidth(); i++) {
            int[] pixelD = raster1.getPixel(i, raster1.getHeight() - 1, new int[4]);//down
            int[] pixelT = raster2.getPixel(i, 0, new int[4]);//top

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
