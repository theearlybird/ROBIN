package openimage;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import openimage.io.AlertDialog;
import openimage.io.PictureFileChooser;
import openimage.io.Utils;
import openimage.io.ZuletztGeoeffnet;

public final class OpenImageWindow extends JFrame implements MouseListener, MouseMotionListener, WindowStateListener, DropTargetListener {

    private JMenuItem reload, save, invert, blackWhite, blackWhiteFromColor, desaturate, blackWhiteFromRed, blackWhiteFromGreen, blackWhiteFromBlue, blackWhiteWithoutShadesOfGray, colorize, brighter, darker, blur, maximumContrast, detectEdges, scale, crop, rotateR, rotateL, flipV, flipH;
    private ZuletztGeoeffnet zg;
    private PictureFileChooser pfc;
    private BufferedImage bi;
    private boolean[][] biFinished;
    private JPanel canvas;
    private int cropStartX, cropStartY, imgStartX, imgStartY, currentX, currentY, maxCanvasWidth, maxCanvasHeight;
    private Color colorizeColor;
    private boolean cropping, disorderedRotation;
    private File imgFile; // for drop
    private JScrollPane sp;
    private static final int red = Color.red.getRGB();

    public OpenImageWindow() {
        super("ROBIN");
        setIconImage(new ImageIcon(getClass().getResource("/openimage/images/icon.png")).getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);

        pfc = new PictureFileChooser();
        addWindowStateListener(this);

        // menu
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("Datei");
        file.setMnemonic(KeyEvent.VK_D);
        JMenuItem open = new JMenuItem("Öffnen");
        open.setAccelerator(KeyStroke.getKeyStroke('O', Event.CTRL_MASK));
        open.setMnemonic(KeyEvent.VK_F);
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                open();
            }
        });
        file.add(open);
        zg = new ZuletztGeoeffnet("Zuletzt geöffnet...", this);
        zg.setMnemonic(KeyEvent.VK_Z);
        file.add(zg);
        JMenuItem screenCapture = new JMenuItem("Bildschirmfoto aufnehmen");
        screenCapture.setMnemonic(KeyEvent.VK_B);
        screenCapture.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                screenCapture();
            }
        });
        file.add(screenCapture);
        reload = new JMenuItem("Neu laden");
        reload.setAccelerator(KeyStroke.getKeyStroke('Z', Event.CTRL_MASK));
        reload.setMnemonic(KeyEvent.VK_N);
        reload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                reload();
            }
        });
        file.add(reload);
        save = new JMenuItem("Speichern");
        save.setAccelerator(KeyStroke.getKeyStroke('S', Event.CTRL_MASK));
        save.setMnemonic(KeyEvent.VK_S);
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                save();
            }
        });
        file.add(save);
        mb.add(file);
        JMenu colors = new JMenu("Farben");
        colors.setMnemonic(KeyEvent.VK_F);
        invert = new JMenuItem("Invertieren");
        invert.setMnemonic(KeyEvent.VK_I);
        invert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                invert();
            }
        });
        colors.add(invert);
        blackWhite = new JMenuItem("Schwarz-weiß (Graustufen)");
        blackWhite.setMnemonic(KeyEvent.VK_G);
        blackWhite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                blackWhite();
            }
        });
        colors.add(blackWhite);
        blackWhiteFromColor = new JMenu("Schwarz-weiß (Graustufen) von...");
        blackWhiteFromColor.setMnemonic(KeyEvent.VK_V);
        blackWhiteFromRed = new JMenuItem("Rot-Werten");
        blackWhiteFromRed.setMnemonic(KeyEvent.VK_R);
        blackWhiteFromRed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                blackWhiteFromColor('r');
            }
        });
        blackWhiteFromColor.add(blackWhiteFromRed);
        blackWhiteFromGreen = new JMenuItem("Grün-Werten");
        blackWhiteFromGreen.setMnemonic(KeyEvent.VK_G);
        blackWhiteFromGreen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                blackWhiteFromColor('g');
            }
        });
        blackWhiteFromColor.add(blackWhiteFromGreen);
        blackWhiteFromBlue = new JMenuItem("Blau-Werten");
        blackWhiteFromBlue.setMnemonic(KeyEvent.VK_B);
        blackWhiteFromBlue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                blackWhiteFromColor('b');
            }
        });
        blackWhiteFromColor.add(blackWhiteFromBlue);
        colors.add(blackWhiteFromColor);
        blackWhiteWithoutShadesOfGray = new JMenuItem("Schwarz-weiß");
        blackWhiteWithoutShadesOfGray.setMnemonic(KeyEvent.VK_S);
        blackWhiteWithoutShadesOfGray.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                new BlackWhiteDialog(OpenImageWindow.this, bi);
            }
        });
        colors.add(blackWhiteWithoutShadesOfGray);
        desaturate = new JMenuItem("Desaturieren");
        desaturate.setMnemonic(KeyEvent.VK_A);
        desaturate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                new SaturationDialog(OpenImageWindow.this, bi);
            }
        });
        colors.add(desaturate);
        colorize = new JMenuItem("Einfärben");
        colorize.setMnemonic(KeyEvent.VK_E);
        colorize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                colorize();
            }
        });
        colors.add(colorize);
        brighter = new JMenuItem("Heller");
        brighter.setMnemonic(KeyEvent.VK_H);
        brighter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                brighter();
            }
        });
        colors.add(brighter);
        darker = new JMenuItem("Dunkler");
        darker.setMnemonic(KeyEvent.VK_D);
        darker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                darker();
            }
        });
        colors.add(darker);
        blur = new JMenuItem("Weichzeichnen");
        blur.setMnemonic(KeyEvent.VK_W);
        blur.setAccelerator(KeyStroke.getKeyStroke('W', Event.CTRL_MASK));
        blur.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                blur();
            }
        });
        colors.add(blur);
        /*maximumContrast = new JMenuItem("Maximalkontrast");
        maximumContrast.setMnemonic(KeyEvent.VK_M);
        maximumContrast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                maximumContrast();
            }
        });
        colors.add(maximumContrast);*/
        detectEdges = new JMenuItem("Kanten finden");
        detectEdges.setMnemonic(KeyEvent.VK_K);
        detectEdges.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                detectEdges();
            }
        });
        colors.add(detectEdges);
        mb.add(colors);
        JMenu tools = new JMenu("Werkzeuge");
        tools.setMnemonic(KeyEvent.VK_W);
        scale = new JMenuItem("Skalieren");
        scale.setMnemonic(KeyEvent.VK_S);
        scale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                new ScaleDialog(OpenImageWindow.this, bi.getWidth(), bi.getHeight());
            }
        });
        tools.add(scale);
        crop = new JMenuItem("Freistellen");
        crop.setMnemonic(KeyEvent.VK_F);
        crop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                canvas.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            }
        });
        tools.add(crop);
        rotateR = new JMenuItem("Um 90° nach Rechts drehen");
        rotateR.setMnemonic(KeyEvent.VK_R);
        rotateR.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (disorderedRotation) {
                    rotateL();
                } else {
                    rotateR();
                }
            }
        });
        tools.add(rotateR);
        rotateL = new JMenuItem("Um 90° nach Links drehen");
        rotateL.setMnemonic(KeyEvent.VK_L);
        rotateL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (disorderedRotation) {
                    rotateR();
                } else {
                    rotateL();
                }
            }
        });
        tools.add(rotateL);
        flipH = new JMenuItem("Horizontal spiegeln");
        flipH.setMnemonic(KeyEvent.VK_H);
        flipH.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                flipH();
            }
        });
        tools.add(flipH);
        flipV = new JMenuItem("Vertikal spiegeln");
        flipV.setMnemonic(KeyEvent.VK_V);
        flipV.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                flipV();
            }
        });
        tools.add(flipV);
        mb.add(tools);
        setJMenuBar(mb);

        // open image => repaint cnavas
        canvas = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bi != null) {
                    imgStartX = (canvas.getWidth() - bi.getWidth()) / 2; // zum Umrechnen der Mousedragged/-released in buffededimage-koordinaten
                    imgStartY = (canvas.getHeight() - bi.getHeight()) / 2;
                    g.drawImage(bi, imgStartX, imgStartY, this);
                    if (cropping) {
                        int cropEndX = keepInRange(currentX - imgStartX, 0, bi.getWidth());
                        int cropEndY = keepInRange(currentY - imgStartY, 0, bi.getHeight());
                        // Wenn x2 > x1 bzw. y2 > y1
                        int x, y, h, w;
                        if (cropStartX < cropEndX) {
                            x = cropStartX;
                            w = cropEndX - cropStartX;
                        } else {
                            x = cropEndX;
                            w = cropStartX - cropEndX;
                        }
                        if (cropStartY < cropEndY) {
                            y = cropStartY;
                            h = cropEndY - cropStartY;
                        } else {
                            y = cropEndY;
                            h = cropStartY - cropEndY;
                        }

                        g.setColor(new Color(0f, 0f, 0f, 0.5f));
                        //g.fillRect(imgStartX + x, imgStartY + y, w, h);
                        g.fillRect(imgStartX, imgStartY, bi.getWidth(), y);
                        g.fillRect(imgStartX, imgStartY + y, x, h);
                        g.fillRect(imgStartX + x + w, imgStartY + y, bi.getWidth() - x - w, h);
                        g.fillRect(imgStartX, imgStartY + y + h, bi.getWidth(), bi.getHeight() - y - h);
                    }
                }
            }
        };
        canvas.setDropTarget(new DropTarget(this, this));
        // kein winziges Fenster wenn kein Bild geladen ist
        canvas.setPreferredSize(new Dimension(500, 300));
        // for cropping
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        sp = new JScrollPane(canvas);
        sp.getVerticalScrollBar().setUnitIncrement(32);
        sp.getHorizontalScrollBar().setUnitIncrement(32);
        add(sp);

        setImageNeedingActionsEnabled(false);
    }

    private void open() {
        if (pfc.showOpenDialog(this) == 0) {
            open(pfc.getSelectedFile());
        }
    }

    public void openZuletztGeoeffnet(File path) {
        if (open(path)) {
            // PictureFileChooser ist im aktuellen Verzeichnis, auch wenn aus ZuletztGeoeffnet-Liste geoeffnet wurde
            pfc.setSelectedFile(path.getAbsoluteFile());
        }
    }

    private boolean open(File path) {
        if (path != null) {
            try {
                BufferedImage biTmp = ImageIO.read(path);
                if (biTmp != null) {
                    setImage(biTmp);
                    zg.add(path);
                    setTitle(path + " - ROBIN");
                    repaintCanvasAfterSizeChange();
                    setImageNeedingActionsEnabled(true);
                    return true;
                } else {
                    new AlertDialog(this, "Datei enthält kein Bild.");
                }
            } catch (IOException ex) {
                new AlertDialog(this, ex);
            }
        }
        return false;
    }

    private void screenCapture() {
        try {
            setVisible(false);
            Thread.sleep(500); // sonst wird ROBIN-Fenster aufgenommen
            setImage(new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize())));
            // Fenster nie größer als Bildschirm
            setExtendedState(MAXIMIZED_BOTH);
            setVisible(true);
            setImageNeedingActionsEnabled(true);
            pfc.setSelectedFile(null);
        } catch (InterruptedException ex) {
        } catch (AWTException ex) {
        }
    }

    private void reload() {
        open(pfc.getSelectedFile());
    }

    private void save() {
        if (pfc.showSaveDialog(this) == 0) {
            try {
                String fileType = pfc.getSelectedFile().toString().substring(pfc.getSelectedFile().toString().lastIndexOf('.') + 1).toLowerCase();
                if ((fileType.equals("jpg") || fileType.equals("jpeg")) && bi.getType() != BufferedImage.TYPE_INT_RGB) { // JPEG unterstützt keine Transperenz
                    BufferedImage biTmp = new BufferedImage(bi.getWidth(null), bi.getHeight(null), BufferedImage.TYPE_INT_RGB);
                    biTmp.createGraphics().drawImage(bi, 0, 0, biTmp.getWidth(), biTmp.getHeight(), Color.WHITE, null);
                    ImageIO.write(biTmp, fileType, pfc.getSelectedFile());
                } else {
                    ImageIO.write(bi, fileType, pfc.getSelectedFile());
                }
            } catch (IOException ex) {
                new AlertDialog(this, ex);
            }
        }
    }

    private void invert() {
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                Color c = new Color(bi.getRGB(j, i));
                setRGBWithOldAlpha(j, i, new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));
            }
        }
        repaint();
    }

    private void blackWhite() {
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                Color c = new Color(bi.getRGB(j, i));
                int newColor = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                setRGBWithOldAlpha(j, i, new Color(newColor, newColor, newColor));
            }
        }
        repaint();
    }

    private void blackWhiteFromColor(char color) {
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                Color c = new Color(bi.getRGB(j, i));
                switch (color) {
                    case 'r':
                        setRGBWithOldAlpha(j, i, new Color(c.getRed(), c.getRed(), c.getRed()));
                        break;
                    case 'g':
                        setRGBWithOldAlpha(j, i, new Color(c.getGreen(), c.getGreen(), c.getGreen()));
                        break;
                    case 'b':
                        setRGBWithOldAlpha(j, i, new Color(c.getBlue(), c.getBlue(), c.getBlue()));
                }
            }
        }
        repaint();
    }

    public void blackWhiteWithoutShadesOfGray(int border) {
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                Color c = new Color(bi.getRGB(j, i));
                int newColor = (c.getRed() + c.getGreen() + c.getBlue()) / 3 >= border ? 255 : 0;
                setRGBWithOldAlpha(j, i, new Color(newColor, newColor, newColor));
            }
        }
        repaint();
    }

    public void posterize() {
        blur();
        blackWhiteWithoutShadesOfGray(192);
        /*int x, y;
         do {
         x = (int) (Math.random() * bi.getWidth());
         y = (int) (Math.random() * bi.getHeight());
         } while (bi.getRGB(x, y) != Color.white.getRGB());
         floodFill(x, y);*/
    }

    private void floodFill(int x, int y) {
        if (!biFinished[x][y]) {
            biFinished[x][y] = true;
            //System.out.println(x + ", " + y);

            bi.setRGB(x, y, red);
            int maxD = 100;
            boolean stop = true;
            if (y > 0 && Math.abs(getSum(x, y) - getSum(x, y - 1)) < maxD) {
                floodFill(x, y - 1);
                stop = false;
            }
            if (y < bi.getHeight() - 1 && Math.abs(getSum(x, y) - getSum(x, y + 1)) < maxD) {
                floodFill(x, y + 1);
                stop = false;
            }
            if (x > 0 && Math.abs(getSum(x, y) - getSum(x - 1, y)) < maxD) {
                floodFill(x - 1, y);
                stop = false;
            }
            if (x < bi.getWidth() - 1 && Math.abs(getSum(x, y) - getSum(x + 1, y)) < maxD) {
                floodFill(x + 1, y);
                stop = false;
            }
            if (stop) {
                repaint();
            }
        }
    }

    private int getSum(int x, int y) {
        //Color c = new Color(bi.getRGB(x, y));
        return 0;//c.getRed() + c.getGreen() + c.getBlue();
    }

    private void colorize() {
        colorizeColor = JColorChooser.showDialog(this, "Farbe auswählen...", colorizeColor);
        if (colorizeColor != null) {
            for (int i = 0; i < bi.getHeight(); i++) {
                for (int j = 0; j < bi.getWidth(); j++) {
                    Color c = new Color(bi.getRGB(j, i));
                    setRGBWithOldAlpha(j, i, new Color((c.getRed() + colorizeColor.getRed()) / 2, (c.getGreen() + colorizeColor.getGreen()) / 2, (c.getBlue() + colorizeColor.getBlue()) / 2));
                }
            }
            repaint();
        }
    }

    private void brighter() {
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                setRGBWithOldAlpha(j, i, new Color(bi.getRGB(j, i)).brighter());
            }
        }
        repaint();
    }

    private void darker() {
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                setRGBWithOldAlpha(j, i, new Color(bi.getRGB(j, i)).darker());
            }
        }
        repaint();
    }

    private void blur() {
        for (int i = 1; i < bi.getHeight() - 1; i++) {
            for (int j = 1; j < bi.getWidth() - 1; j++) {
                int r = 0, g = 0, b = 0;
                for (Color c : new Color[]{new Color(bi.getRGB(j - 1, i - 1)), new Color(bi.getRGB(j - 1, i)), new Color(bi.getRGB(j - 1, i + 1)), new Color(bi.getRGB(j, i - 1)), new Color(bi.getRGB(j, i)), new Color(bi.getRGB(j, i + 1)), new Color(bi.getRGB(j + 1, i - 1)), new Color(bi.getRGB(j + 1, i)), new Color(bi.getRGB(j + 1, i + 1))}) {
                    r += c.getRed();
                    g += c.getGreen();
                    b += c.getBlue();
                }
                setRGBWithOldAlpha(j, i, new Color(r / 9, g / 9, b / 9));
            }
        }
        repaint();
    }

    private void maximumContrast() {
        int min = 255, max = 0;
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                Color c = new Color(bi.getRGB(j, i));
                for (int color : new int[]{c.getRed(), c.getGreen(), c.getBlue()}) {
                    if (color < min) {
                        min = color;
                    }
                    if (color > max) {
                        max = color;
                    }
                }
            }
        }
        double q = 255f / (max - min);
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                Color c = new Color(bi.getRGB(j, i));
                setRGBWithOldAlpha(j, i, new Color((int) (c.getRed() * q - min), (int) (c.getGreen() * q - min), (int) (c.getBlue() * q - min)));
            }
        }
        repaint();
    }

    private void detectEdges() {
        blackWhite();
        Color[][] matrix = new Color[bi.getWidth()][bi.getHeight()];
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                int r = 0, g = 0, b = 0;
                for (Color c : new Color[]{new Color(getColorWithOutOfBoundsCheck(j - 1, i - 1)), new Color(getColorWithOutOfBoundsCheck(j - 1, i)), new Color(getColorWithOutOfBoundsCheck(j - 1, i + 1)), new Color(getColorWithOutOfBoundsCheck(j, i - 1)), new Color(getColorWithOutOfBoundsCheck(j, i + 1)), new Color(getColorWithOutOfBoundsCheck(j + 1, i - 1)), new Color(getColorWithOutOfBoundsCheck(j + 1, i)), new Color(getColorWithOutOfBoundsCheck(j + 1, i + 1))}) {
                    r += c.getRed();
                    g += c.getGreen();
                    b += c.getBlue();
                }
                Color c = new Color(bi.getRGB(j, i));
                matrix[j][i] = new Color(255 - Math.abs(c.getRed() - r / 8), 255 - Math.abs(c.getGreen() - g / 8), 255 - Math.abs(c.getBlue() - b / 8));
            }
        }
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                setRGBWithOldAlpha(j, i, matrix[j][i]);
            }
        }
        new BlackWhiteDialog(OpenImageWindow.this, bi, 248);
    }

    public void desaturate(double value) {
        value /= 100;
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                Color c = new Color(bi.getRGB(j, i));
                setRGBWithOldAlpha(j, i, y(c.getRed(), c.getGreen(), c.getBlue(), value));
            }
        }
        repaint();
    }

    private Color y(int r, int g, int b, double value) {
        if (r + g + b < 384) {
            return new Color((int) (r * (1 - value)), (int) (g * (1 - value)), (int) (b * (1 - value)));
        }
        return new Color((int) (255 - (255 - r) * (1 - value)), (int) (255 - (255 - g) * (1 - value)), (int) (255 - (255 - b) * (1 - value)));
    }

    private int getColorWithOutOfBoundsCheck(int x, int y) {
        if (x == -1) {
            x = bi.getWidth() - 1;
        }
        if (y == -1) {
            y = bi.getHeight() - 1;
        }
        if (x == bi.getWidth()) {
            x = 0;
        }
        if (y == bi.getHeight()) {
            y = 0;
        }
        return bi.getRGB(x, y);
    }

    // Transparenz bleibt erhalten
    private void setRGBWithOldAlpha(int x, int y, Color c) {
        if (bi.getAlphaRaster() != null) {
            int a = bi.getAlphaRaster().getSample(x, y, 0);
            bi.setRGB(x, y, c.getRGB());
            bi.getAlphaRaster().setSample(x, y, 0, a);
        } else {
            bi.setRGB(x, y, c.getRGB());
        }
    }

    public void scale(int w, int h) {
        // anti-alising
        boolean before = w < bi.getWidth();
        if (before) {
            blur();
        }
        BufferedImage resizedImage = new BufferedImage(w, h, bi.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(bi, 0, 0, w, h, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bi = resizedImage;
        // anti-alising
        if (!before) {
            blur();
        }
        repaintCanvasAfterSizeChange();
    }

    private void rotateR() {
        BufferedImage newBi = new BufferedImage(bi.getHeight(), bi.getWidth(), bi.getType());
        for (int i = 0; i < bi.getWidth(); i++) {
            for (int j = 0; j < bi.getHeight(); j++) {
                newBi.setRGB(j, i, bi.getRGB(i, j));
            }
        }
        bi = newBi;
        repaintCanvasAfterSizeChange();
        disorderedRotation = !disorderedRotation;
    }

    private void rotateL() {
        BufferedImage newBi = new BufferedImage(bi.getHeight(), bi.getWidth(), bi.getType());
        for (int i = 0; i < bi.getWidth(); i++) {
            for (int j = 0; j < bi.getHeight(); j++) {
                newBi.setRGB(bi.getHeight() - 1 - j, bi.getWidth() - 1 - i, bi.getRGB(i, j));
            }
        }
        bi = newBi;
        repaintCanvasAfterSizeChange();
        disorderedRotation = !disorderedRotation;
    }

    private void flipV() {
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth() / 2; j++) {
                int help = bi.getRGB(j, i);
                bi.setRGB(j, i, bi.getRGB(bi.getWidth() - 1 - j, i));
                bi.setRGB(bi.getWidth() - 1 - j, i, help);
            }
        }
        repaint();
    }

    private void flipH() {
        for (int i = 0; i < bi.getWidth(); i++) {
            for (int j = 0; j < bi.getHeight() / 2; j++) {
                int help = bi.getRGB(i, j);
                bi.setRGB(i, j, bi.getRGB(i, bi.getHeight() - 1 - j));
                bi.setRGB(i, bi.getHeight() - 1 - j, help);
            }
        }
        repaint();
    }

    // kein winziges Fenster nach un-maximising
    @Override
    public void windowStateChanged(WindowEvent we) {
        if (we.getOldState() == MAXIMIZED_BOTH) {
            repaintCanvasAfterSizeChange();
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        //biFinished = new boolean[bi.getWidth()][bi.getHeight()];
        //floodFill(me.getX() - imgStartX, me.getY() - imgStartY);
    }

    // crop
    @Override
    public void mousePressed(MouseEvent me) {
        if (canvas.getCursor().getType() == Cursor.CROSSHAIR_CURSOR) {
            cropStartX = keepInRange(me.getX() - imgStartX, 0, bi.getWidth());
            cropStartY = keepInRange(me.getY() - imgStartY, 0, bi.getHeight());
            cropping = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        if (canvas.getCursor().getType() == Cursor.CROSSHAIR_CURSOR) {
            if (JOptionPane.showConfirmDialog(this, "Markierte Fläche freistellen?", "", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null) == JOptionPane.YES_OPTION) {
                int cropEndX = keepInRange(me.getX() - imgStartX, 0, bi.getWidth());
                int cropEndY = keepInRange(me.getY() - imgStartY, 0, bi.getHeight());
                // Wenn x2 > x1 bzw. y2 > y1
                int x, y, h, w;
                if (cropStartX < cropEndX) {
                    x = cropStartX;
                    w = cropEndX - cropStartX;
                } else {
                    x = cropEndX;
                    w = cropStartX - cropEndX;
                }
                if (cropStartY < cropEndY) {
                    y = cropStartY;
                    h = cropEndY - cropStartY;
                } else {
                    y = cropEndY;
                    h = cropStartY - cropEndY;
                }
                bi = bi.getSubimage(x, y, w, h);
                repaint();
            }
            cropping = false;
            canvas.setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        if (cropping) {
            currentX = me.getX();
            currentY = me.getY();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent me) {
    }

    // verhindert werte außerhalb des erlaubten ranges
    private int keepInRange(int n, int min, int max) {
        if (n < min) {
            return min;
        }
        if (n > max) {
            return max;
        }
        return n;
    }

    private void setImageNeedingActionsEnabled(boolean b) {
        reload.setEnabled(b);
        save.setEnabled(b);
        invert.setEnabled(b);
        blackWhite.setEnabled(b);
        blackWhiteFromColor.setEnabled(b);
        desaturate.setEnabled(b);
        blackWhiteWithoutShadesOfGray.setEnabled(b);
        colorize.setEnabled(b);
        brighter.setEnabled(b);
        darker.setEnabled(b);
        blur.setEnabled(b);
        //maximumContrast.setEnabled(b);
        detectEdges.setEnabled(b);
        scale.setEnabled(b);
        crop.setEnabled(b);
        rotateR.setEnabled(b);
        rotateL.setEnabled(b);
        flipV.setEnabled(b);
        flipH.setEnabled(b);
    }

    private void repaintCanvasAfterSizeChange() {
        if (bi != null) {
            canvas.setPreferredSize(new Dimension(bi.getWidth(), bi.getHeight()));
        }
        if (getExtendedState() != MAXIMIZED_BOTH) {
            pack();
            setLocationRelativeTo(null);
        }
        repaint();
        sp.revalidate(); // WICHTIG: Scrollbars gleich anzeigen bei großen Bildern
    }

    // Kein Zuckeln wenn man versucht ein Fesnster mit zu großem Bild zu unmaximizen
    @Override
    public void setExtendedState(int state) {
        if (bi != null && (bi.getWidth() > maxCanvasWidth || bi.getHeight() > maxCanvasHeight)) {
            super.setExtendedState(MAXIMIZED_BOTH);
        } else {
            super.setExtendedState(state);
        }
    }

    // Drag & Drop Reaktionen
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        try {
            imgFile = new File("");
            // Erste passende Datei aus Liste der gedragted auswählen
            for (File file : (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
                // nur Bild-Dateien akzeptieren
                if (Utils.isImageFile(file)) {
                    imgFile = file;
                    break;
                }
            }
        } catch (UnsupportedFlavorException ex) {
        } catch (IOException ex) {
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        if (Utils.isImageFile(imgFile)) {
            dtde.acceptDrop(DnDConstants.ACTION_LINK);
            open(imgFile);
        } else {
            dtde.rejectDrop();
        }
    }

    public void saveMaxCanvasSize() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }
        maxCanvasWidth = canvas.getSize().width;
        maxCanvasHeight = canvas.getSize().height;
    }

    public void setImage(BufferedImage bi) {
        if (bi.getType() == BufferedImage.TYPE_INT_ARGB) {
            this.bi = bi;
        } else {
            this.bi = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB); // Alle Arten von Transparenz richtig verarbeiten
            this.bi.getGraphics().drawImage(bi, 0, 0, null);
        }
        repaint();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }
        OpenImageWindow oiw = new OpenImageWindow();
        // Öffnen mit.. ROBIN ermöglichen
        // Erste passende Datei aus Liste der gedragted auswählen
        for (String file : args) {
            // nur Bild-Dateien akzeptieren
            if (Utils.isImageFile(new File(file))) {
                oiw.open(new File(file));
                break;
            }
        }
        oiw.setVisible(true);
        oiw.saveMaxCanvasSize();
    }
}