package com.example.remoteimageviewerdesktop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class Main extends JFrame implements DropTargetListener {

	private JLabel imgLabel;
	private URL imgFile;

	public Main() {
		super("RemoteImageViewer (Desktop)");
		setIconImage(new ImageIcon(getClass().getResource("icon.png")).getImage());
		imgLabel = new JLabel();
		imgLabel.setBackground(Color.WHITE);
		imgLabel.setOpaque(true);
		imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
		imgLabel.setText("Darg a picture in the file explorer to drop it here");
		imgLabel.setPreferredSize(new Dimension(500, 500));
		add(imgLabel, "North");
		setDropTarget(new DropTarget(imgLabel, this));
		pack();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void dragEnter(DropTargetDragEvent dtde) {
		try {
			StringTokenizer st = new StringTokenizer((String) dtde.getTransferable().getTransferData(new DataFlavor("text/uri-list;class=java.lang.String")), "\r\n");
			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				if (!token.startsWith("#") && !token.isEmpty()) {
					URL file = new URI(token).toURL();
					if (isImage(file)) {
						imgFile = file;
						break;
					}
				}
			}
		} catch (Exception ex) {
			handle(ex);
		}
	}

	public void dragOver(DropTargetDragEvent dtde) {
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void drop(DropTargetDropEvent dtde) {
		if (isImage(imgFile)) {
			dtde.acceptDrop(DnDConstants.ACTION_LINK);
			Socket sock;
			try {
				sock = new Socket("192.168.178.27", 55555);
				ImageIO.write(ImageIO.read(imgFile), "png", sock.getOutputStream());
				sock.close();
			} catch (Exception ex) {
				handle(ex);
			}
			imgLabel.setIcon(new ImageIcon(imgFile));
			imgLabel.setText("");
		} else
			dtde.rejectDrop();
	}

	private boolean isImage(URL f) {
		String n = f.toString().toLowerCase();
		return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".gif") || n.endsWith(".bmp");
	}
	
	private void handle(Exception ex) {
		JOptionPane.showMessageDialog(this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
		}
		new Main();
	}
}