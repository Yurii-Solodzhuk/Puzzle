import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PuzzleButton extends JButton {
    private boolean isLastButton;

    public PuzzleButton() {
        super();
        initUI();
    }

    public PuzzleButton(Image image) {
        super(new ImageIcon(image));
        initUI();
    }

    public void setLastButton() {
        isLastButton = true;
    }

    public boolean isLastButton() {
        return isLastButton;
    }

    private void initUI() {
        isLastButton = false;
        BorderFactory.createLineBorder(Color.GRAY);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(BorderFactory.createLineBorder(Color.YELLOW));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }
        });
    }
}
