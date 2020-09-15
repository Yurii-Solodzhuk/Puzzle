import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Game extends JFrame {
    private static final int NUMBER_OF_BUTTONS = 12;
    private static final String FILE_NAME = "resources//cat.jpg";
    private static final int DESIRED_WIDTH = 400;


    private JPanel panel;
    private PuzzleButton lastButton;
    private List<PuzzleButton> buttons;
    private List<Point> solution;
    private JButton autoBtn = new JButton("Auto");
    private List<Image> images;
    private int width;
    private int height;
    private BufferedImage resizedImage;


    public Game() {
        initUI();
    }

    private void initUI() {
        solution = new ArrayList<>();

        solution.add(new Point(0, 0));
        solution.add(new Point(0, 1));
        solution.add(new Point(0, 2));
        solution.add(new Point(1, 0));
        solution.add(new Point(1, 1));
        solution.add(new Point(1, 2));
        solution.add(new Point(2, 0));
        solution.add(new Point(2, 1));
        solution.add(new Point(2, 2));
        solution.add(new Point(3, 0));
        solution.add(new Point(3, 1));
        solution.add(new Point(3, 2));

        buttons = new ArrayList<>();

        panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.setLayout(new GridLayout(5, 3, 0, 0));

        loadAndResizeImage();

        add(panel, BorderLayout.CENTER);

        images = new ArrayList<>();
        createPartsOfPuzzle();

        Collections.shuffle(buttons);
        buttons.add(lastButton);
        Collections.shuffle(images);

        for (int i = 0; i < NUMBER_OF_BUTTONS; i++) {
            PuzzleButton btn = buttons.get(i);
            panel.add(btn);
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener(new ClickAction());
        }

        pack();

        setTitle("Puzzle");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        autoBtn.addActionListener(new AutoPuzzle());
        panel.add(autoBtn);
    }

    private void loadAndResizeImage() {
        try {
            BufferedImage sourceImage = loadImage();
            int h = getNewHeight(sourceImage.getWidth(), sourceImage.getHeight());
            resizedImage = resizeImage(sourceImage, h);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load image", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        width = resizedImage.getWidth(null);
        height = resizedImage.getHeight(null);
    }

    private BufferedImage loadImage() throws IOException {
        return ImageIO.read(new File(FILE_NAME));
    }

    private int getNewHeight(int w, int h) {
        double k = DESIRED_WIDTH / (double) w;
        return (int) (h * k);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int height) {
        BufferedImage resizedImage = new BufferedImage(Game.DESIRED_WIDTH, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, Game.DESIRED_WIDTH, height, null);
        g.dispose();
        return resizedImage;
    }

    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = bimage.createGraphics();
        graphics2D.drawImage(img, 0, 0, null);
        graphics2D.dispose();
        return bimage;
    }


    private void createPartsOfPuzzle() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                Image image = createImage(new FilteredImageSource(resizedImage.getSource(),
                        new CropImageFilter(j * width / 3, i * height / 4,
                                (width / 3), height / 4)));

                images.add(image);

                PuzzleButton button = new PuzzleButton(image);
                button.putClientProperty("position", new Point(i, j));

                if (i == 3 && j == 2) {
                    lastButton = new PuzzleButton();
                    lastButton.setBorderPainted(false);
                    lastButton.setContentAreaFilled(false);
                    lastButton.setLastButton();
                    lastButton.putClientProperty("position", new Point(i, j));
                } else {
                    buttons.add(button);
                }
            }
        }
    }


    private class ClickAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            checkButton(e);
            checkSolution();
        }

    }

    private void checkButton(ActionEvent e) {
        int lidx = 0;
        for (PuzzleButton button : buttons) {
            if (button.isLastButton()) {
                lidx = buttons.indexOf(button);
            }
        }

        JButton button = (JButton) e.getSource();
        int bidx = buttons.indexOf(button);

        if ((bidx - 1 == lidx) || (bidx + 1 == lidx)
                || (bidx - 3 == lidx) || (bidx + 3 == lidx)) {
            Collections.swap(buttons, bidx, lidx);
            updateButtons();
            panel.add(autoBtn);
        }
    }

    private void updateButtons() {
        panel.removeAll();
        for (JComponent btn : buttons) {
            panel.add(btn);
        }
        panel.validate();
    }

    private void checkSolution() {
        List<Point> current = new ArrayList<>();

        for (JComponent btn : buttons) {
            current.add((Point) btn.getClientProperty("position"));
        }

        if (compareList(solution, current)) {
            JOptionPane.showMessageDialog(panel, "Finished! You Win!!!",
                    "Congratulation", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static boolean compareList(java.util.List<Point> ls1, List<Point> ls2) {
        return ls1.toString().contentEquals(ls2.toString());
    }


    private class AutoPuzzle implements ActionListener {
        CompareSides compareSides = new CompareSides();

        @Override
        public void actionPerformed(ActionEvent e) {
            List<WritableRaster> rastersFromImages = getRastersFromImages(images);
            List<WritableRaster> completedPuzzle = new ArrayList<>();
            final WritableRaster currentImage = getFirstImage(rastersFromImages);
            final List<WritableRaster> imagesToCheck = getImagesToCheck(rastersFromImages, currentImage);

            completedPuzzle.add(currentImage);

            collectingPuzzle(completedPuzzle, currentImage, imagesToCheck, 0);

            List<Image> images = getImagesFromRaster(completedPuzzle);

            addButtonToPanel(images);

            updateButtons();
            checkSolution();
        }

        private void collectingPuzzle(List<WritableRaster> completedPuzzle,
                                      WritableRaster currentImage,
                                      List<WritableRaster> imagesToCheck,
                                      int topPosition) {
            if (imagesToCheck.size() > 0) {
                if (completedPuzzle.size() > 0 && completedPuzzle.size() % 3 == 0) {
                    WritableRaster bottom = getBottom(completedPuzzle.get(topPosition), imagesToCheck);
                    imagesToCheck.remove(bottom);
                    topPosition += 3;
                    completedPuzzle.add(bottom);
                    collectingPuzzle(completedPuzzle, bottom, imagesToCheck, topPosition);
                } else {
                    WritableRaster right = getRight(currentImage, imagesToCheck);
                    imagesToCheck.remove(right);
                    completedPuzzle.add(right);
                    collectingPuzzle(completedPuzzle, right, imagesToCheck, topPosition);
                }
            }
        }

        private List<Image> getImagesFromRaster(List<WritableRaster> completedPuzzle) {
            List<Image> images = new ArrayList<>();
            for (WritableRaster puzzle : completedPuzzle) {
                images.add(fromRasterToImage(puzzle));
            }
            return images;
        }

        private void addButtonToPanel(List<Image> images) {
            int x = 0;
            buttons.removeAll(buttons);
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 3; j++) {
                    PuzzleButton button = new PuzzleButton(images.get(x++));
                    button.putClientProperty("position", new Point(i, j));
                    buttons.add(button);
                }
            }
        }

        private List<WritableRaster> getRastersFromImages(List<Image> images) {
            List<WritableRaster> rasters = new ArrayList<>();
            for (Image image : images) {
                BufferedImage imageB = toBufferedImage(image);
                WritableRaster raster = imageB.getRaster();
                rasters.add(raster);
            }
            return rasters;
        }

        private WritableRaster getFirstImage(List<WritableRaster> rastersFromImages) {
            WritableRaster first = null;
            for (WritableRaster puzzle : rastersFromImages) {
                if (isFirst(puzzle, rastersFromImages)) {
                    first = puzzle;
                    break;
                }
            }
            return first;
        }

        private boolean isFirst(WritableRaster puzzle, List<WritableRaster> rastersFromImages) {
            return getLeft(puzzle, rastersFromImages) > 5000
                    && getTop(puzzle, rastersFromImages) > 5000;
        }

        private WritableRaster getRight(WritableRaster currentImage, List<WritableRaster> imagesToCheck) {
            Map<Integer, WritableRaster> comparingResults = new TreeMap<>();

            imagesToCheck.forEach(imageToCheck -> {
                int diff = compareSides.compareRightAndleftSidesOfPuzzle(currentImage, imageToCheck);
                comparingResults.put(diff, imageToCheck);
            });

            return comparingResults
                    .entrySet()
                    .iterator()
                    .next()
                    .getValue();
        }

        private Integer getLeft(WritableRaster currentImage, List<WritableRaster> imagesToCheck) {
            Map<Integer, WritableRaster> comparingResults = new TreeMap<>();

            imagesToCheck.forEach(imageToCheck -> {
                int diff = compareSides.compareRightAndleftSidesOfPuzzle(imageToCheck, currentImage);
                comparingResults.put(diff, imageToCheck);
            });

            return comparingResults
                    .entrySet()
                    .iterator()
                    .next()
                    .getKey();
        }

        private WritableRaster getBottom(WritableRaster currentImage, List<WritableRaster> imagesToCheck) {
            Map<Integer, WritableRaster> comparingResults = new TreeMap<>();

            imagesToCheck.forEach(imageToCheck -> {
                int diff = compareSides.compareBottomAndTopSidesOfPuzzle(currentImage, imageToCheck);
                comparingResults.put(diff, imageToCheck);
            });

            return comparingResults
                    .entrySet()
                    .iterator()
                    .next()
                    .getValue();
        }

        private Integer getTop(WritableRaster currentImage, List<WritableRaster> imagesToCheck) {
            Map<Integer, WritableRaster> comparingResults = new TreeMap<>();

            imagesToCheck.forEach(imageToCheck -> {
                int diff = compareSides.compareBottomAndTopSidesOfPuzzle(imageToCheck, currentImage);
                comparingResults.put(diff, imageToCheck);
            });
            return comparingResults
                    .entrySet()
                    .iterator()
                    .next()
                    .getKey();
        }

        private Image fromRasterToImage(WritableRaster currentImage) {
            return new BufferedImage(ColorModel.getRGBdefault(),
                    currentImage, getColorModel().isAlphaPremultiplied(), null);
        }

        private List<WritableRaster> getImagesToCheck(List<WritableRaster> rastersFromImages, WritableRaster currentImage) {
            return rastersFromImages
                    .stream()
                    .filter(x -> !x.equals(currentImage))
                    .collect(Collectors.toList());
        }
    }
}
