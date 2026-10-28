package wtf.tatp.meowtils.manager.log;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.meowtils.GUI;
import wtf.tatp.meowtils.util.ColorUtil;

public class LogFrame {
    private static final Color BOTTOM_BAR_COLOR = new Color(30, 33, 41);
    private static final Color CONSOLE_BACKGROUND_COLOR = new Color(20, 20, 20);
    private static final Color SLIDER_BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color SLIDER_COLOR = new Color(80, 80, 80);
    private static final Color DISABLED_BUTTON_COLOR = new Color(180, 180, 180);
    private static final Color HOVERED_BUTTON_COLOR = new Color(255, 255, 255);
    private static final Pattern LOG_PATTERN = Pattern.compile("^\\[(\\d{2}:\\d{2}:\\d{2})] \\[([A-Z]+)]: (.+)$");
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private static final int WIDTH = (int)(SCREEN_SIZE.width * 0.5);
    private static final int HEIGHT = (int)(SCREEN_SIZE.height * 0.5);
    private static Point mouseDown;
    private static Point resizeOrigin;
    private static Dimension resizeStart;
    private static JTextPane logPane;
    private static JFrame frame;
    private static boolean timestamps = true;

    public static void open() {
        if (frame != null && frame.isVisible()) {
            SwingUtilities.invokeLater(() -> frame.toFront());
        } else {
            SwingUtilities.invokeLater(() -> {
                buildFrame();
                readLog();
            });
        }
    }

    private static void buildFrame() {
        frame = new JFrame();
        frame.setSize(WIDTH, HEIGHT);
        frame.setUndecorated(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(2);
        frame.setBackground(new Color(0, 0, 0, 0));
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
        };
        panel.setBackground(BOTTOM_BAR_COLOR);
        frame.setContentPane(panel);
        panel.add(buildActionBar(), "North");
        panel.add(buildLogPane(), "Center");
        panel.add(buildToolbar(), "South");
        frame.setVisible(true);
    }

    private static JPanel buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setPreferredSize(new Dimension(0, 32));
        bar.setBackground(getThemeColor());
        bar.setOpaque(true);
        JLabel title = new JLabel("  Meowtils Log");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Open Sans", 1, 13));
        JButton close = toolbarButton("x");
        close.addActionListener(e -> {
            frame.dispose();
            frame = null;
            logPane = null;
        });
        bar.add(title, "West");
        bar.add(close, "East");
        bar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                LogFrame.mouseDown = e.getPoint();
            }
        });
        bar.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = e.getLocationOnScreen();
                LogFrame.frame.setLocation(p.x - LogFrame.mouseDown.x, p.y - LogFrame.mouseDown.y);
            }
        });
        return bar;
    }

    private static JScrollPane buildLogPane() {
        logPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        logPane.setEditable(false);
        logPane.setFocusable(false);
        logPane.setBackground(CONSOLE_BACKGROUND_COLOR);
        logPane.setFont(new Font("Segoe UI", 0, 12));
        logPane.setMargin(new Insets(8, 10, 8, 10));
        JScrollPane scrollPane = new JScrollPane(logPane, 20, 31);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(CONSOLE_BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(CONSOLE_BACKGROUND_COLOR);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = LogFrame.SLIDER_COLOR;
                this.trackColor = LogFrame.SLIDER_BACKGROUND_COLOR;
            }

            @Override
            protected JButton createDecreaseButton(int o) {
                return this.zeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int o) {
                return this.zeroButton();
            }

            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
        return scrollPane;
    }

    private static JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(0, 6, 4));
        bar.setBackground(BOTTOM_BAR_COLOR);
        bar.setPreferredSize(new Dimension(0, 34));
        final Color enabledColor = getThemeColor();
        final Color disabledColor = DISABLED_BUTTON_COLOR;
        final JButton timestampButton = toolbarButton("Timestamps");
        timestampButton.setForeground(enabledColor);
        timestampButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                timestampButton.setForeground(LogFrame.HOVERED_BUTTON_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                timestampButton.setForeground(LogFrame.timestamps ? enabledColor : disabledColor);
            }
        });
        timestampButton.addActionListener(e -> {
            timestamps = !timestamps;
            timestampButton.setForeground(timestamps ? enabledColor : disabledColor);
        });
        JButton copyButton = toolbarButton("Copy");
        copyButton.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(logPane.getText()), null);
            Meowtils.info("Log copied to clipboard.");
            Meowtils.addMessage("Log copied to clipboard.");
        });
        JButton buttonPost = toolbarButton("Upload");
        buttonPost.addActionListener(e -> LogManager.postLog());
        bar.add(timestampButton);
        bar.add(copyButton);
        bar.add(buttonPost);
        JPanel resizePoint = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(80, 80, 80));

                for (int i = 1; i <= 3; i++) {
                    int p = i * 4;
                    g.drawLine(this.getWidth() - p, this.getHeight() - 1, this.getWidth() - 1, this.getHeight() - p);
                }
            }
        };
        resizePoint.setOpaque(false);
        resizePoint.setPreferredSize(new Dimension(16, 34));
        resizePoint.setCursor(Cursor.getPredefinedCursor(5));
        resizePoint.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                LogFrame.resizeOrigin = e.getLocationOnScreen();
                LogFrame.resizeStart = LogFrame.frame.getSize();
            }
        });
        resizePoint.addMouseMotionListener(
            new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    Point p = e.getLocationOnScreen();
                    LogFrame.frame
                        .setSize(
                            Math.max(400, LogFrame.resizeStart.width + (p.x - LogFrame.resizeOrigin.x)),
                            Math.max(260, LogFrame.resizeStart.height + (p.y - LogFrame.resizeOrigin.y))
                        );
                }
            }
        );
        JPanel panelS = new JPanel(new BorderLayout());
        panelS.setOpaque(false);
        panelS.add(bar, "Center");
        panelS.add(resizePoint, "East");
        return panelS;
    }

    private static void readLog() {
        File logFile = LogManager.findLog();
        if (logFile != null) {
            new Thread(() -> {
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(logFile.toPath()), StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        final String currentLine = line;
                        SwingUtilities.invokeLater(() -> {
                            Matcher m = LOG_PATTERN.matcher(currentLine);
                            if (m.matches()) {
                                appendLog(m.group(2), m.group(3), m.group(1));
                            } else {
                                appendLog("INFO", currentLine, null);
                            }
                        });
                    }
                } catch (IOException var15) {
                    Meowtils.error("Unable to read log file: " + var15);
                }
            }, "Meowtils-Log-Read").start();
        }
    }

    public static void appendLog(String level, String message, String timestamp) {
        if (logPane != null) {
            String time = level.toUpperCase();
            Color levelColor;
            switch (time) {
                case "FATAL":
                    levelColor = ColorUtil.DARK_RED;
                    break;
                case "ERROR":
                    levelColor = ColorUtil.RED;
                    break;
                case "WARN":
                    levelColor = ColorUtil.GOLD;
                    break;
                default:
                    levelColor = ColorUtil.WHITE;
            }

            time = timestamp != null && timestamps ? "[" + timestamp + "] " : "";
            String line = time + "[" + level.toUpperCase() + "] " + message + "\n";
            Color color = levelColor;
            SwingUtilities.invokeLater(() -> {
                StyledDocument doc = logPane.getStyledDocument();
                Style style = logPane.addStyle(null, null);
                StyleConstants.setForeground(style, color);

                try {
                    doc.insertString(doc.getLength(), line, style);
                } catch (BadLocationException var5) {
                    Meowtils.error("Cannot insert string: \"" + line + "\" to log.");
                }
            });
        }
    }

    private static Color getThemeColor() {
        GUI g = Module.get(GUI.class);
        return new Color(g.red, g.green, g.blue);
    }

    private static JButton toolbarButton(String label) {
        final JButton b = new JButton(label);
        b.setFont(new Font("Open Sans", 0, 11));
        b.setForeground(DISABLED_BUTTON_COLOR);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(12));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setForeground(LogFrame.HOVERED_BUTTON_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setForeground(LogFrame.DISABLED_BUTTON_COLOR);
            }
        });
        return b;
    }
}
