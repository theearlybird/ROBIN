package openimage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import openimage.io.AlertDialog;
import openimage.io.PictureFileChooser;
import openimage.io.Utils;
import openimage.io.ZuletztGeoeffnet;

public class OpenImageWindow extends JFrame implements MouseListener, MouseMotionListener, WindowStateListener, ScaleCallback, DropTargetListener {

    private JMenuItem save, invert, blackWhite, colorize, brighter, darker, blur, scale, crop, rotateR, rotateL, flipV, flipH;
    private ZuletztGeoeffnet zg;
    private PictureFileChooser pfc;
    private BufferedImage bi;
    private JPanel canvas;
    private int cropStartX, cropStartY, imgStartX, imgStartY, currentX, currentY;
    private Color colorizeColor;
    private boolean cropping, disorderedRotation;
    private File imgFile; // for drop
    private JScrollPane sp;

    @SuppressWarnings("LeakingThisInConstructor")
    public OpenImageWindow() {
        super("OpenImage");
        setIconImage(new ImageIcon(getClass().getResource("/openimage/images/icon.png")).getImage());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

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
        blackWhite = new JMenuItem("Schwarz-weiß");
        blackWhite.setMnemonic(KeyEvent.VK_S);
        blackWhite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                blackWhite();
            }
        });
        colors.add(blackWhite);
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
        blur.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                blur();
            }
        });
        colors.add(blur);
        mb.add(colors);
        JMenu tools = new JMenu("Werkzeuge");
        tools.setMnemonic(KeyEvent.VK_W);
        scale = new JMenuItem("Skalieren");
        scale.setMnemonic(KeyEvent.VK_S);
        scale.addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("ResultOfObjectAllocationIgnored")
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
        //sp = new JScrollPane(canvas);
        //add(sp);
        add(canvas);

        setImageNeedingActionsEnabled(false);
    }

    private void open() {
        if (pfc.showOpenDialog(this) == 0) {
            open(pfc.getSelectedFile());
        }
    }

    public void openZuletztGeoeffnet(File path) {
        open(path);
        // PictureFileChooser ist im aktuellen Verzeichnis, auch wenn aus ZuletztGeoeffnet-Liste geoeffnet wurde
        pfc.setSelectedFile(path);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void open(File path) {
        try {
            BufferedImage biTmp = ImageIO.read(path);
            if (biTmp != null) {
                bi = biTmp;
                zg.add(path);
                setTitle(path + " - ROBIN");
                updateCanvas();
                setImageNeedingActionsEnabled(true);
            } else {
                new AlertDialog(this, "Datei enthält kein Bild.");
            }
        } catch (IOException ex) {
            new AlertDialog(this, ex);
        }
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void save() {
        if (pfc.showSaveDialog(this) == 0) {
            try {
                ImageIO.write(bi, pfc.getSelectedFile().toString().substring(pfc.getSelectedFile().toString().lastIndexOf('.') + 1).toLowerCase(), pfc.getSelectedFile());
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
        repaintCanvas();
    }

    private void blackWhite() {
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                Color c = new Color(bi.getRGB(j, i));
                int newColor = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                setRGBWithOldAlpha(j, i, new Color(newColor, newColor, newColor));
            }
        }
        repaintCanvas();
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
            repaintCanvas();
        }
    }

    private void brighter() {
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                setRGBWithOldAlpha(j, i, new Color(bi.getRGB(j, i)).brighter());
            }
        }
        repaintCanvas();
    }

    private void darker() {
        for (int i = 0; i < bi.getHeight(); i++) {
            for (int j = 0; j < bi.getWidth(); j++) {
                setRGBWithOldAlpha(j, i, new Color(bi.getRGB(j, i)).darker());
            }
        }
        repaintCanvas();
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
        repaintCanvas();
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

    @Override
    public void scale(int w, int h) {
        BufferedImage resizedImage = new BufferedImage(w, h, bi.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(bi, 0, 0, w, h, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bi = resizedImage;
        updateCanvas();
    }

    private void rotateR() {
        BufferedImage newBi = new BufferedImage(bi.getHeight(), bi.getWidth(), bi.getType());
        for (int i = 0; i < bi.getWidth(); i++) {
            for (int j = 0; j < bi.getHeight(); j++) {
                newBi.setRGB(j, i, bi.getRGB(i, j));
            }
        }
        bi = newBi;
        updateCanvas();
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
        updateCanvas();
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
        repaintCanvas();
    }

    private void flipH() {
        for (int i = 0; i < bi.getWidth(); i++) {
            for (int j = 0; j < bi.getHeight() / 2; j++) {
                int help = bi.getRGB(i, j);
                bi.setRGB(i, j, bi.getRGB(i, bi.getHeight() - 1 - j));
                bi.setRGB(i, bi.getHeight() - 1 - j, help);
            }
        }
        repaintCanvas();
    }

    private void updateCanvas() {
        if (bi != null) {
            canvas.setPreferredSize(new Dimension(bi.getWidth(), bi.getHeight()));
        }
        if (getExtendedState() != MAXIMIZED_BOTH) {
            pack();
            setLocationRelativeTo(null);
        }
        repaintCanvas();
    }

    // kein winziges Fenster nach un-maximising
    @Override
    public void windowStateChanged(WindowEvent we) {
        if (we.getOldState() == MAXIMIZED_BOTH) {
            updateCanvas();
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
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
            cropping = false;
            canvas.setCursor(Cursor.getDefaultCursor());
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
            repaintCanvas();
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
            repaintCanvas();
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
        save.setEnabled(b);
        invert.setEnabled(b);
        blackWhite.setEnabled(b);
        colorize.setEnabled(b);
        brighter.setEnabled(b);
        darker.setEnabled(b);
        blur.setEnabled(b);
        scale.setEnabled(b);
        crop.setEnabled(b);
        rotateR.setEnabled(b);
        rotateL.setEnabled(b);
        flipV.setEnabled(b);
        flipH.setEnabled(b);
    }

    private void repaintCanvas() {
        //setVisible(false);
        //setVisible(true);
        canvas.repaint();
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
    }
}