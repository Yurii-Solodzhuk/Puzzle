import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game extends JFrame {
    private static final int NUMBER_OF_BUTTONS = 12;
    private static final int DESIRED_WIDTH = 400;

    private JPanel panel;
    private BufferedImage sourceImage;
    private BufferedImage resizedImage;
    private Image image;
    private PuzzleButton lastButton;
    private int width;
    private int height;
    private List<PuzzleButton> buttons;
    private List<Point> solution;


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

        add(panel, BorderLayout.CENTER);

        for (int i = 0; i < 4; i++) {

            for (int j = 0; j < 3; j++) {

                image = createImage(new FilteredImageSource(resizedImage.getSource(),
                        new CropImageFilter(j * width / 3, i * height / 4,
                                (width / 3), height / 4)));

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

        Collections.shuffle(buttons);
        buttons.add(lastButton);

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
    }

    private int getNewHeight(int w, int h) {

        double k = DESIRED_WIDTH / (double) w;
        int newHeight = (int) (h * k);
        return newHeight;
    }

    private BufferedImage loadImage() throws IOException {

        BufferedImage bufferedImage = ImageIO.read(new File("pirate_mickey.jpg"));

        return bufferedImage;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width,
                                      int height, int type) {

        BufferedImage resizedImage = new BufferedImage(width, height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }

    private class ClickAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {

            checkButton(e);
            checkSolution();
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

    public static boolean compareList(List<Point> ls1, List<Point> ls2) {
        return ls1.toString().contentEquals(ls2.toString());
    }
}
