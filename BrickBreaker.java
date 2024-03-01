package Class;
	import javax.swing.*;
	import java.awt.*;
	import java.awt.event.*;
	import java.util.ArrayList;
	import java.util.List;

public class BrickBreaker extends JFrame implements KeyListener {
	    // Constants and variables
	    private final int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
	    private final int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
	    private final int brickWidth = 96;
	    private final int brickHeight = 35;
	    private final int brickGap = 10;
	    private final int paddleHeight = 20;
	    private final int ballRadius = 10;
	    private final int ballSpeed = 6;
	    private final int paddleSpeed = 10;
	    private final int maxLives = 5;

	    private Paddle gamePaddle;
	    private Ball gameBall;
	    private List<Brick> brickList;
	    private boolean[] keys;
	    private int lives;
	    private boolean gameStarted;

	    public BrickBreaker() {
	        setTitle("Brick Breaker");
	        setSize(screenWidth, screenHeight);
	        setDefaultCloseOperation(EXIT_ON_CLOSE);
	        setResizable(false);
	        addKeyListener(this);

	        keys = new boolean[KeyEvent.KEY_LAST + 1];

	        lives = maxLives;
	        gameStarted = false;

	        gamePaddle = new Paddle();
	        gameBall = new Ball();
	        brickList = new ArrayList<>(); // Using ArrayList for bricks

	        // Initialize bricks
	        int numColumns = 15; // Change the number of columns as desired
	        int numRows = 6; // Number of rows
	        for (int row = 0; row < numRows; row++) {
	            for (int col = 0; col < numColumns; col++) {
	                brickList.add(new Brick(col * (brickWidth + brickGap), row * (brickHeight + brickGap) + 50));
	            }
	        }

	        JPanel gamePanel = new GamePanel();
	        add(gamePanel);

	        new Thread(() -> {
	            while (true) {
	                if (gameStarted) {
	                    if (keys[KeyEvent.VK_LEFT]) {
	                        gamePaddle.moveLeft();
	                    }
	                    if (keys[KeyEvent.VK_RIGHT]) {
	                        gamePaddle.moveRight();
	                    }

	                    gameBall.move();
	                    gameBall.checkCollisionWithWalls();
	                    gameBall.checkCollision(gamePaddle);
	                    if (gameBall.getY() >= screenHeight - ballRadius * 2) {
	                        lives--;
	                        if (lives == 0) {
	                            endGame("Game Over!");
	                        } else {
	                            gameBall.reset();
	                        }
	                    }

	                    boolean allBricksCleared = true;
	                    for (Brick brick : brickList) {
	                        if (brick.isVisible() && gameBall.collidesWith(brick)) {
	                            brick.setVisible(false);
	                            gameBall.reflectY();
	                        }
	                        if (brick.isVisible()) {
	                            allBricksCleared = false;
	                        }
	                    }

	                    if (allBricksCleared) {
	                        endGame("Congratulations! You win!");
	                    }
	                }

	                gamePanel.repaint();

	                try {
	                    Thread.sleep(10);
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	            }
	        }).start();
	    }

	    // KeyListener methods
	    @Override
	    public void keyTyped(KeyEvent e) {
	    }

	    @Override
	    public void keyPressed(KeyEvent e) {
	        keys[e.getKeyCode()] = true;
	        if (!gameStarted && e.getKeyCode() == KeyEvent.VK_ENTER) {
	            gameStarted = true;
	        }
	    }

	    @Override
	    public void keyReleased(KeyEvent e) {
	        keys[e.getKeyCode()] = false;
	    }

	    // Game end method
	    private void endGame(String message) {
	        JOptionPane.showMessageDialog(null, message);
	        System.exit(0);
	    }

	    // GamePanel class
	    private class GamePanel extends JPanel {
	        @Override
	        protected void paintComponent(Graphics g) {
	            super.paintComponent(g);

	            // Neon gradient background
	            Graphics2D g2d = (Graphics2D) g;
	            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	            GradientPaint gradient = new GradientPaint(0, 0, Color.BLACK, 0, getHeight(), Color.CYAN);
	            g2d.setPaint(gradient);
	            g2d.fillRect(0, 0, getWidth(), getHeight());

	            // Draw bricks
	            for (Brick brick : brickList) {
	                if (brick.isVisible()) {
	                    brick.draw(g);
	                }
	            }

	            // Draw paddle
	            g.setColor(Color.RED);
	            g.fillRect(gamePaddle.getX(), gamePaddle.getY(), gamePaddle.getWidth(), paddleHeight);

	            // Draw ball
	            g.setColor(Color.YELLOW);
	            g.fillOval(gameBall.getX() - ballRadius, gameBall.getY() - ballRadius, ballRadius * 2, ballRadius * 2);

	            // Draw lives
	            g.setColor(Color.BLUE);
	            Font font = g.getFont().deriveFont(Font.BOLD, 30); // Define font with larger size
	            g.setFont(font);
	            g.drawString("Lives left: " + lives, 10, 30);

	            // Display "Press Enter to Start" message
	            if (!gameStarted) {
	                g.setColor(Color.GREEN);
	                font = g.getFont().deriveFont(Font.BOLD, 30); // Define font with larger size
	                g.setFont(font);
	                g.drawString("Press Enter to Start", screenWidth / 2 - 150, screenHeight / 2); // Adjusted position to center
	            }
	        }

	        @Override
	        public Dimension getPreferredSize() {
	            return new Dimension(screenWidth, screenHeight);
	        }
	    }

	    // Paddle class
	    private class Paddle {
	        private int x, y;
	        private int width;

	        public Paddle() {
	            width = screenWidth / 8;
	            x = (screenWidth - width) / 2;
	            y = screenHeight - paddleHeight - 40; // Set the paddle at the bottom of the screen
	        }

	        public int getX() {
	            return x;
	        }

	        public int getY() {
	            return y;
	        }

	        public int getWidth() {
	            return width;
	        }

	        public void moveLeft() {
	            x -= paddleSpeed;
	            if (x < 0) {
	                x = 0;
	            }
	        }

	        public void moveRight() {
	            x += paddleSpeed;
	            if (x + width > screenWidth) {
	                x = screenWidth - width;
	            }
	        }
	    }

	    // Ball class
	    private class Ball {
	        private int x, y;
	        private int dx, dy;
	        public Ball() {
	            reset();
	        }

	        public void reset() {
	            x = gamePaddle.getX() + gamePaddle.getWidth() / 2 ;
	            y = gamePaddle.getY() - ballRadius * 2;
	            dx = ballSpeed;
	            dy = -ballSpeed;
	        }

	        public int getX() {
	            return x;
	        }

	        public int getY() {
	            return y;
	        }

	        public void move() {
	            x += dx;
	            y += dy;
	        }

	        public void checkCollisionWithWalls() {
	            if (x <= 0 || x >= screenWidth - ballRadius * 2) {
	                dx = -dx;
	            }
	            if (y <= 0) {
	                dy = -dy;
	            }
	        }

	        public void checkCollision(Paddle paddle) {
	            Rectangle ballRect = new Rectangle(x - ballRadius, y - ballRadius, ballRadius * 2, ballRadius * 2);
	            Rectangle paddleRect = new Rectangle(paddle.getX(), paddle.getY(), paddle.getWidth(), paddleHeight);

	            if (ballRect.intersects(paddleRect)) {
	                dy = -dy;
	                y = paddle.getY() - ballRadius * 2;
	            }
	        }

	        public boolean collidesWith(Brick brick) {
	            return x >= brick.getX() &&
	                    x <= brick.getX() + brickWidth &&
	                    y >= brick.getY() &&
	                    y <= brick.getY() + brickHeight;
	        }

	        public void reflectY() {
	            dy = -dy;
	        }
	    }

	    // Brick class
	    private class Brick {
	        private int x, y;
	        private boolean visible;

	        public Brick(int x, int y) {
	            this.x = x;
	            this.y = y;
	            visible = true;
	        }

	        public int getX() {
	            return x;
	        }

	        public int getY() {
	            return y;
	        }

	        public boolean isVisible() {
	            return visible;
	        }

	        public void setVisible(boolean visible) {
	            this.visible = visible;
	        }

	        public void draw(Graphics g) {
	            g.setColor(Color.YELLOW);
	            g.fillRect(x, y, brickWidth, brickHeight);
	        }
	    }

	    // Main method
	    public static void main(String[] args) {
	        SwingUtilities.invokeLater(() -> {
	            BrickBreaker game = new BrickBreaker();
	            game.setVisible(true);
	        });
	    }
	}

