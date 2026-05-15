import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

// Клас Ball
class Ball {

    private Component canvas;
    private static final int XSIZE = 20;
    private static final int YSIZE = 20;
    private int x = 0;
    private int y = 0;
    private int dx = 2;
    private int dy = 2;

    // Координати лузок: 4 кути стола
    private static final int[][] POCKETS = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    static final int POCKET_RADIUS = 18;

    private Color color;

    // Конструктор з випадковою позицією (для звичайного запуску)
    public Ball(Component c, Color color) {
        this.canvas = c;
        this.color = color;
        if (Math.random() < 0.5) {
            x = new Random().nextInt(this.canvas.getWidth());
            y = 0;
        } else {
            x = 0;
            y = new Random().nextInt(this.canvas.getHeight());
        }
    }

    // Конструктор з заданою позицією і напрямком (для експерименту)
    public Ball(Component c, Color color, int startX, int startY, int dx, int dy) {
        this.canvas = c;
        this.color = color;
        this.x = startX;
        this.y = startY;
        this.dx = dx;
        this.dy = dy;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(this.color);
        g2.fill(new Ellipse2D.Double(x, y, XSIZE, YSIZE));
    }

    public void move() {
        x += dx;
        y += dy;
        if (x < 0) {
            x = 0;
            dx = -dx;
        }
        if (x + XSIZE >= this.canvas.getWidth()) {
            x = this.canvas.getWidth() - XSIZE;
            dx = -dx;
        }
        if (y < 0) {
            y = 0;
            dy = -dy;
        }
        if (y + YSIZE >= this.canvas.getHeight()) {
            y = this.canvas.getHeight() - YSIZE;
            dy = -dy;
        }
        this.canvas.repaint();
    }

    // Перевіряє, чи потрапила кулька в одну з лузок
    public boolean isInPocket() {
        int cx = x + XSIZE / 2;
        int cy = y + YSIZE / 2;
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        for (int[] p : POCKETS) {
            int px = p[0] * w;
            int py = p[1] * h;
            int dist2 = (cx - px) * (cx - px) + (cy - py) * (cy - py);
            if (dist2 < POCKET_RADIUS * POCKET_RADIUS) return true;
        }
        return false;
    }

    // Повертає координати лузок
    public static int[][] getPockets(int w, int h) {
        return new int[][]{{0, 0}, {w, 0}, {0, h}, {w, h}};
    }
}

// Клас BallCanvas
class BallCanvas extends JPanel {

    private ArrayList<Ball> balls = new ArrayList<>();

    public void add(Ball b) {
        this.balls.add(b);
    }

    public void remove(Ball b) {
        this.balls.remove(b);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // UI лузок у кутах
        int r = Ball.POCKET_RADIUS;
        for (int[] p : Ball.getPockets(getWidth(), getHeight())) {
            g2.setColor(Color.black);
            g2.fill(new Ellipse2D.Double(p[0] - r, p[1] - r, r * 2, r * 2));
        }

        for (int i = 0; i < balls.size(); i++) {
            Ball b = balls.get(i);
            b.draw(g2);
        }
    }
}

// Клас BallThread
class BallThread extends Thread {

    private Ball b;
    private BallCanvas canvas;
    private Runnable onPocket; // викликається коли кулька потрапляє в лузу

    public BallThread(Ball ball, BallCanvas canvas, int priority, Runnable onPocket) {
        this.b = ball;
        this.canvas = canvas;
        this.onPocket = onPocket;
        this.setPriority(priority);
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i < 10000; i++) {
                b.move();
                System.out.println("Thread name = " + Thread.currentThread().getName());

                // Перевіряємо лузу — якщо потрапила, кулька прибирається і потік завершується
                if (b.isInPocket()) {
                    canvas.remove(b);
                    onPocket.run();
                    return; // потік завершує роботу
                }

                Thread.sleep(5);
            }
        } catch (InterruptedException ex) {
        }
    }
}

// Клас BounceFrame
class BounceFrame extends JFrame {

    private BallCanvas canvas;
    private JTextField fieldPocketed;
    private int pocketedCount = 0;
    public static final int WIDTH  = 450;
    public static final int HEIGHT = 350;

    public BounceFrame() {
        this.setSize(WIDTH, HEIGHT);
        this.setTitle("Bounce programm");
        this.canvas = new BallCanvas();
        System.out.println("In Frame Thread name = " + Thread.currentThread().getName());

        Container content = this.getContentPane();
        content.add(this.canvas, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.lightGray);

        // Кнопка червоної кульки (MAX_PRIORITY)
        JButton buttonRed  = new JButton("Червона (HIGH)");
        buttonRed.setForeground(Color.red);
        // Кнопка синьої кульки (MIN_PRIORITY)
        JButton buttonBlue = new JButton("Синя (LOW)");
        buttonBlue.setForeground(Color.blue);
        // Кнопка експерименту
        JButton buttonExp  = new JButton("Експеримент");
        JButton buttonStop = new JButton("Stop");
        JButton buttonJoin = new JButton("Join Demo");

        JLabel label = new JLabel("У лузі:");
        fieldPocketed = new JTextField("0", 4);
        fieldPocketed.setEditable(false);
        fieldPocketed.setHorizontalAlignment(JTextField.CENTER);

        // Червона — MAX_PRIORITY, випадкова позиція
        buttonRed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBall(new Ball(canvas, Color.red), Thread.MAX_PRIORITY);
            }
        });

        // Синя — MIN_PRIORITY, випадкова позиція
        buttonBlue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBall(new Ball(canvas, Color.blue), Thread.MIN_PRIORITY);
            }
        });

        // Експеримент: N синіх + 1 червона — всі з однієї точки, в одному напрямку
        buttonExp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog(
                    BounceFrame.this,
                    "Скільки синіх кульок запустити?",
                    "Експеримент",
                    JOptionPane.QUESTION_MESSAGE
                );
                if (input == null) return;
                int n;
                try {
                    n = Integer.parseInt(input.trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(BounceFrame.this, "Введіть ціле число.");
                    return;
                }
                // Спільна стартова точка — центр поля
                int startX = canvas.getWidth()  / 2;
                int startY = canvas.getHeight() / 2;
                int dx = 2, dy = 2; // однаковий напрямок і швидкість

                // N синіх (MIN_PRIORITY)
                for (int i = 0; i < n; i++) {
                    addBall(new Ball(canvas, Color.blue, startX, startY, dx, dy), Thread.MIN_PRIORITY);
                }
                // 1 червона (MAX_PRIORITY) — та сама точка, той самий напрямок
                addBall(new Ball(canvas, Color.red, startX, startY, dx, dy), Thread.MAX_PRIORITY);

                System.out.println("Експеримент: " + n + " синіх (MIN) + 1 червона (MAX)");
            }
        });

        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Join Demo: жовта стартує одразу, зелена — тільки після join() жовтої
        buttonJoin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Ball yellowBall = new Ball(canvas, Color.yellow);
                canvas.add(yellowBall);

                // Потік жовтої кульки — рухається поки не потрапить у лузу
                BallThread yellowThread = new BallThread(yellowBall, canvas,
                    Thread.NORM_PRIORITY, new Runnable() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    pocketedCount++;
                                    fieldPocketed.setText(String.valueOf(pocketedCount));
                                    canvas.repaint();
                                }
                            });
                        }
                    });

                // Потік-спостерігач: чекає завершення жовтого через join(),
                // після чого запускає зелену кульку
                Thread observerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("Спостерігач: чекаю завершення жовтого потоку...");
                            yellowThread.join(); // блокується до завершення yellowThread
                            System.out.println("Спостерігач: жовтий завершився — запускаю зелену!");

                            // Зелена стартує тільки після того, як жовта потрапила в лузу
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    addBall(new Ball(canvas, Color.green), Thread.NORM_PRIORITY);
                                }
                            });
                        } catch (InterruptedException ex) {
                        }
                    }
                });

                yellowThread.start();
                observerThread.start();
                System.out.println("Join Demo: жовтий запущено, спостерігач чекає на join()");
            }
        });

        buttonPanel.add(buttonRed);
        buttonPanel.add(buttonBlue);
        buttonPanel.add(buttonExp);
        buttonPanel.add(buttonJoin);
        buttonPanel.add(buttonStop);
        buttonPanel.add(label);
        buttonPanel.add(fieldPocketed);
        content.add(buttonPanel, BorderLayout.SOUTH);
    }

    // Допоміжний метод — запускає кульку в потоці з заданим пріоритетом
    private void addBall(Ball b, int priority) {
        canvas.add(b);
        BallThread thread = new BallThread(b, canvas, priority, new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        pocketedCount++;
                        fieldPocketed.setText(String.valueOf(pocketedCount));
                        canvas.repaint();
                    }
                });
            }
        });
        thread.start();
        System.out.println("Thread name = " + thread.getName()
            + ", priority = " + thread.getPriority());
    }
}

// Клас Bounce (точка входу)
public class Bounce {

    public static void main(String[] args) {
        BounceFrame frame = new BounceFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        System.out.println("Thread name = " + Thread.currentThread().getName());
    }
}
