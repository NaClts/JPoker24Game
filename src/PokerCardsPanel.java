import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PokerCardsPanel extends JPanel {
    private BufferedImage[] cardImages;
    private static final int CARD_WIDTH = 100;
    private static final int CARD_HEIGHT = 145;
    private static final int CARD_COUNT = 4;

    public PokerCardsPanel(String[] cards) {
        // Load card images (replace with your actual image paths)
        cardImages = new BufferedImage[CARD_COUNT];
        try {
            // Example: loading placeholder images - replace with your actual card images
            cardImages[0] = ImageIO.read(new File("poker_images/"+cards[0]+"C.png"));
            cardImages[1] = ImageIO.read(new File("poker_images/"+cards[1]+"D.png"));
            cardImages[2] = ImageIO.read(new File("poker_images/"+cards[2]+"H.png"));
            cardImages[3] = ImageIO.read(new File("poker_images/"+cards[3]+"S.png"));
        } catch (IOException e) {
            e.printStackTrace();
            // Create blank cards if images fail to load
            for (int i = 0; i < CARD_COUNT; i++) {
                cardImages[i] = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = cardImages[i].createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT);
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, CARD_WIDTH-1, CARD_HEIGHT-1);
                g.dispose();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Calculate total width needed for all cards with spacing
        int totalWidth = CARD_COUNT * CARD_WIDTH + (CARD_COUNT - 1) * 10; // 10px spacing between cards
        int startX = (getWidth() - totalWidth) / 2; // Center horizontally
        
        // Draw each card
        for (int i = 0; i < CARD_COUNT; i++) {
            int x = startX + i * (CARD_WIDTH + 10);
            int y = (getHeight() - CARD_HEIGHT) / 2; // Center vertically
            g.drawImage(cardImages[i], x, y, CARD_WIDTH, CARD_HEIGHT, this);
        }
    }

    // @Override
    // public Dimension getPreferredSize() {
    //     return new Dimension(500, 200); // Preferred size of the panel
    // }

}