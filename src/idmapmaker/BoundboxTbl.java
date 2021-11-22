/*
 * BoundboxTbl.java
 *
 * Created on Jan 23, 2008, 12:07:29 AM
 */

package idmapmaker;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to read and store data from boundbox.tbl.
 * <p>
 * File description (from inferis.org):
 * <blockquote>
 * The file contains 1615 of coordinate pairs: each coordinate pair
 * signifies the upper-left and the lower-right corner of each boundbox. <br>
 * Each value is expressed as a 4-byte unsigned integer. This means that each
 * block takes 16 bytes.
 * <p>
 * Thus, each block starts at a 16 byte offset. This means you have easy
 * access to the boundbox of a province: multiply the province id by 16,
 * read 16 bytes and you have everything you need. </p>
 * <p>
 * Each block has the following format:
 * <table border="1" cellpadding="2" cellspacing="0">
 * <tbody><tr>
 * <td>offset + 0</td>
 * <td>offset + 4 bytes</td>
 * <td>offset + 8 bytes</td>
 * <td>offset + 12 bytes</td>
 * </tr>
 * <tr>
 * <td>upper-left X value</td>
 * <td>upper-left Y value</td>
 * <td>bottom-right X value</td>
 * <td>bottom-right Y value</td>
 * </tr>
 * </tbody></table>
 * </p>
 * <p>
 * There are a few anomalies...<br>
 * Since there are 1615 blocks, but only 1613 provinces, where do those 2
 * extra blocks come from? If you check out the province.csv file, you'll
 * notice that the province ids count up to 1614. </p>
 * <p>
 * It turns out that the TI is also a province. But the zeroth block in
 * the boundbox file is a strange block: it's first coordinate is actually
 * the map size (18944,7296), and it's second coordinate is (0,0). If you
 * follow the same logic of the rest of the blocks, this yields a
 * "negative" boundbox. The same applies to the <b>Dummy</b> province (id=1614).
 * <br> I guess using negative boundboxes gives the result that those provinces
 * are not selectable, which is exactly what's needed, of course. </p>
 * </blockquote>
 * </p>
 * @author Michael
 */
public final class BoundboxTbl {
    
    private final ByteBuffer file;
    
    public BoundboxTbl(String filename) {
        this(readRawFile(filename));
    }

    public BoundboxTbl(final byte[] file) {
        this.file = ByteBuffer.wrap(file);
        this.file.order(ByteOrder.LITTLE_ENDIAN);
    }
    
    private static byte[] readRawFile(String filename) {
        FileInputStream stream = null;
        try {
            File file = new File(filename);
            byte[] buf = new byte[(int) file.length()];
            stream = new FileInputStream(file);
            if (stream.read(buf) != buf.length) {
                System.err.println("???");
            }
            stream.close();
            return buf;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BoundboxTbl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BoundboxTbl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(BoundboxTbl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    public Rectangle getBounds(int provId) {
        final int index = provId*16;
        
        int x1 = file.getInt(index);
        int y1 = file.getInt(index+4);
        int x2 = file.getInt(index+8);
        int y2 = file.getInt(index+12);
        
        return new Rectangle(x1, y1, x2-x1, y2-y1);
    }
}
