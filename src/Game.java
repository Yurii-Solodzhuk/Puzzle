import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game extends JFrame {
    private static final int NUMBER_OF_BUTTONS = 12;
    private static final String FILE_NAME = "pirate_mickey.jpg";
    private static final int DESIRED_WIDTH = 400;


    private JPanel panel;
    private Image image;
    private PuzzleButton lastButton;
    private List<PuzzleButton> buttons;
    private List<Point> solution;
    private JButton autoBtn = new JButton("Auto");
    private List<Image> images;
    private int width;
    private int height;
    private BufferedImage sourceImage;
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
        panel.setLayout(new GridLayout(4, 3, 0, 0));


        loadAndResizeImage();


        add(panel, BorderLayout.CENTER);

        images = new ArrayList<>();
        createPartsOfPazzle();

        Collections.shuffle(buttons);
        buttons.add(lastButton);
//        Collections.shuffle(images);

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

    public int getNewHeight(int w, int h) {
        double k = DESIRED_WIDTH / (double) w;

        return (int) (h * k);
    }

    public BufferedImage loadImage() throws IOException {

        return ImageIO.read(new File(FILE_NAME));
    }

    public static BufferedImage toBufferedImage(Image img) {
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

    public BufferedImage resizeImage(BufferedImage originalImage, int width, int height, int type) {
        BufferedImage resizeImage = new BufferedImage(width, height, type);
        Graphics2D g = resizeImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return resizeImage;
    }

    public void loadAndResizeImage() {
        try {
            sourceImage = loadImage();
            int h = getNewHeight(sourceImage.getWidth(), sourceImage.getHeight());
            resizedImage = resizeImage(sourceImage, DESIRED_WIDTH, h,
                    BufferedImage.TYPE_INT_ARGB);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load image", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        width = resizedImage.getWidth(null);
        height = resizedImage.getHeight(null);
    }


    private void createPartsOfPazzle() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                image = createImage(new FilteredImageSource(resizedImage.getSource(),
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
            JOptionPane.showMessageDialog(panel, "Finished",
                    "Congratulation", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static boolean compareList(java.util.List<Point> ls1, List<Point> ls2) {
        return ls1.toString().contentEquals(ls2.toString());
    }


    private class AutoPuzzle implements ActionListener {


        private List<WritableRaster> getRastersFromImages(List<Image> images) {
            List<WritableRaster> rasters = new ArrayList<>();
            for (Image image : images) {
                BufferedImage imageB = toBufferedImage(image);
                WritableRaster raster = imageB.getRaster();
                rasters.add(raster);
            }
            return rasters;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            Test test = new Test();
            List<WritableRaster> rastersFromImages = getRastersFromImages(images);
            for (int i = 0; i < rastersFromImages.size(); i++) {
                for (int j = 1; j < rastersFromImages.size(); j++) {
                    WritableRaster raster1 = rastersFromImages.get(i);
                    WritableRaster raster2 = rastersFromImages.get(j);
                    if (raster1 != raster2) {
                        int diffBetweenDownAndTopSides = test.compareDownAndTopSidesOfPuzzle(raster1, raster2);
                        int diffBetweenRightAndLeftSides = test.compareRightAndleftSidesOfPuzzle(raster1, raster2);
                    }

                }
            }

        }
    }



}

