package openimage.io;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import openimage.OpenImageWindow;

public class ZuletztGeoeffnet extends JMenu implements ActionListener {

    private OpenImageWindow oiw;

    @SuppressWarnings("LeakingThisInConstructor")
    public ZuletztGeoeffnet(String s, OpenImageWindow oiw) {
        super(s);
        this.oiw = oiw;
        try {
            if (new File(System.getProperty("user.home") + File.separator + ".robin" + File.separator + "zuletztGeoeffnet.txt").exists()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(System.getProperty("user.home") + File.separator + ".robin" + File.separator + "zuletztGeoeffnet.txt")));
                String line;
                while ((line = br.readLine()) != null) {
                    JMenuItem item = new JMenuItem(line);
                    item.addActionListener(this);
                    add(item);
                }
                br.close();
            }
        } catch (IOException ex) {
        }
    }

    public void add(File path) {
        try {
            ArrayList<File> list = new ArrayList<File>();
            if (new File(System.getProperty("user.home") + File.separator + ".robin" + File.separator + "zuletztGeoeffnet.txt").exists()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(System.getProperty("user.home") + File.separator + ".robin" + File.separator + "zuletztGeoeffnet.txt")));
                String line;
                while ((line = br.readLine()) != null) {
                    list.add(new File(line));
                }
                br.close();
            }


            list.remove(path); // entfernt falls schon vorhanden
            list.add(0, path); // OBEN einfügen

            // update menu
            removeAll();
            for (File f : list) {
                JMenuItem item = new JMenuItem(f.toString());
                item.addActionListener(this);
                add(item);
            }


            // immer gleich in datei schreiben
            File dir = new File(System.getProperty("user.home") + File.separator + ".robin");
            dir.mkdir();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(System.getProperty("user.home") + File.separator + ".robin" + File.separator + "zuletztGeoeffnet.txt")));
            for (File f : list) {
                bw.write(f.toString());
                bw.newLine();
            }
            bw.close();
        } catch (IOException ex) {
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        oiw.open(new File(ae.getActionCommand()));
    }
}
