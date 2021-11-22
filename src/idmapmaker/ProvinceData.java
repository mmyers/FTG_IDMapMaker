/*
 * ProvinceData.java
 *
 * Created on Jan 12, 2008, 12:19:12 PM
 */

package idmapmaker;

import eug.parser.EUGFileIO;
import eug.shared.GenericObject;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author Michael Myers
 */
public class ProvinceData {
    
    public static int NUM_PROVINCES = 2020;
    
    private final Map<Integer, Province> allProvs = new HashMap<>(); //[NUM_PROVINCES];
    private final List<Province> extras = new ArrayList<>();
    
    private static final Pattern SEMICOLON = Pattern.compile(";");
    
    private String headerString;
    
    public ProvinceData(String filename) {
        if (filename.endsWith(".csv"))
            loadCsv(filename);
        else
            loadTxt(filename);
    }

    private void loadCsv(String filename) {
        try {
            final BufferedReader br = new BufferedReader(new FileReader(filename));
            String currLine;
            
            // City XPos;City YPos;Army XPos;ArmyYPos;PortXPos;Port YPos;Manufactory XPos; Manufactory YPos;Port/sea Adjacency;Terrain x;Terrain Y;Terrain variant;Terrain x;Terrain Y;Terrain variant;Terrain x;Terrain Y;Terrain variant;Terrain x;Terrain Y;Terrain variant;
            
            int id = -1;
            
            headerString = br.readLine(); // eat first line but save for future use
            
            while ((currLine = br.readLine()) != null) {
                if (currLine.charAt(0) == '#')
                    continue;
                
                String[] args = SEMICOLON.split(currLine, -1);
                
                try {
                    String sid = args[0];
                    
                    if (sid.length() != 0) {
                        id = Integer.parseInt(sid);
                        
                        if (id >= 0) {
                            allProvs.put(id, new ProvinceCsv(args));
                        } else {
                            extras.add(new ProvinceCsv(args));
                        }
                    } else {
                        extras.add(new ProvinceCsv(args));
                    }
                } catch (RuntimeException e) {
                    System.err.print("Error with " + id + ": ");
                    e.printStackTrace();
                }
            }
            
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadTxt(String filename) {
        GenericObject provinces = EUGFileIO.load(filename);
        
        for (GenericObject prov : provinces.getChildren("province")) {
            String idStr = prov.getString("id");
            int id = Integer.parseInt(idStr);
            String name = prov.getString("name");
            String terrain = prov.getString("terrain");
            allProvs.put(id, new ProvinceTxt(name, terrain));
        }
    }
    
    public void saveCsv(String filename) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filename));
            
            writer.write(headerString);
            writer.newLine();

            int maxProv = allProvs.keySet().stream().max(Comparator.naturalOrder()).get();
            for (int i = 0; i <= maxProv; i++) {
                Province p = getProvince(i);
                if (p == null)
                    writer.write(";;;;;;;;;;;;;;0;0;0;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
                else
                    p.writeOut(writer);
                writer.newLine();
            }
            for (Province p : extras) {
                if (p == null)
                    writer.write(";;;;;;;;;;;;;;0;0;0;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
                else
                    p.writeOut(writer);
                writer.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(ProvinceData.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(ProvinceData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public Province getProvince(int id) {
        return allProvs.get(id);
    }
    
    public boolean isLand(int id) {
        final Province p = getProvince(id);
        if (p == null)
            return false;
        
        return p.isLand();
    }
    
    public boolean isPTI(int id) {
        final Province p = getProvince(id);
        if (p == null)
            return true;
        
        return false;
    }
    
    public String getName(int id) {
        final Province p = getProvince(id);
        if (p == null)
            return "Terra Incognita";
        
        return p.getName();
    }
//    
//    public Point getCityPos(int id) {
//        return allProvs[id].getCityPos();
//    }
//    
//    public Point getArmyPos(int id) {
//        return allProvs[id].getArmyPos();
//    }
//    
//    public Point getPortPos(int id) {
//        return allProvs[id].getPortPos();
//    }
//    
//    public Point getManuPos(int id) {
//        return allProvs[id].getManuPos();
//    }
//    
//    public Point getTerrain1Pos(int id) {
//        return allProvs[id].getPos(Province.TERRAIN_1_IDX);
//    }
//    
//    public int getTerrain1Type(int id) {
//        return allProvs[id].getInt(Province.TERRAIN_1_TYPE_IDX);
//    }
//    
//    public Point getTerrain2Pos(int id) {
//        return allProvs[id].getPos(Province.TERRAIN_2_IDX);
//    }
//    
//    public int getTerrain2Type(int id) {
//        return allProvs[id].getInt(Province.TERRAIN_2_TYPE_IDX);
//    }
//    
//    public Point getTerrain3Pos(int id) {
//        return allProvs[id].getPos(Province.TERRAIN_3_IDX);
//    }
//    
//    public int getTerrain3Type(int id) {
//        return allProvs[id].getInt(Province.TERRAIN_3_TYPE_IDX);
//    }
//    
//    public Point getTerrain4Pos(int id) {
//        return allProvs[id].getPos(Province.TERRAIN_4_IDX);
//    }
//    
//    public int getTerrain4Type(int id) {
//        return allProvs[id].getInt(Province.TERRAIN_4_TYPE_IDX);
//    }
    
    public interface Province {
        String getName();
        boolean isLand();
        void writeOut(BufferedWriter writer) throws IOException;
    }
    
    private static final class ProvinceCsv implements Province {
        private final String[] entry;
        
        public static final int NAME_IDX = 1;
        public static final int TERRAIN_IDX = 13;
        // NOTE: Entries 14, 15, and 16 were added in 1.09
        public static final int CITY_IDX = 28;
        public static final int ARMY_IDX = 30;
        public static final int PORT_IDX = 32;
        public static final int MANU_IDX = 34;
        public static final int TERRAIN_1_IDX = 37;
        public static final int TERRAIN_1_TYPE_IDX = 39;
        public static final int TERRAIN_2_IDX = 40;
        public static final int TERRAIN_2_TYPE_IDX = 42;
        public static final int TERRAIN_3_IDX = 43;
        public static final int TERRAIN_3_TYPE_IDX = 45;
        public static final int TERRAIN_4_IDX = 46;
        public static final int TERRAIN_4_TYPE_IDX = 48;
        
        private ProvinceCsv(String[] entry) {
            this.entry = entry;
        }
        
        public String getString(int idx) {
            return entry[idx];
        }
        
        public int getInt(int idx) {
            return Integer.parseInt(entry[idx]);
        }
        
        public String getName() {
            return entry[NAME_IDX];
        }
        
        public boolean isLand() {
            final int terrain = Integer.parseInt(entry[TERRAIN_IDX]);
            return (terrain != 5 && terrain != 6);
        }
        
        public Point getCityPos() {
            return getPos(CITY_IDX);
        }
        
        public Point getArmyPos() {
            return getPos(ARMY_IDX);
        }
        
        public Point getPortPos() {
            return getPos(PORT_IDX);
        }
        
        public Point getManuPos() {
            return getPos(MANU_IDX);
        }
        
        public Point getPos(int xIndex) {
            int x = Integer.parseInt(entry[xIndex]);
            int y = Integer.parseInt(entry[xIndex+1]);
            if (x > 0 && y > 0)
                return new Point(x, y);
            return null;
        }
        
        public void setPos(int xIndex, Point pos) {
            entry[xIndex] = Integer.toString(pos.x);
            entry[xIndex+1] = Integer.toString(pos.y);
        }
        
        public void setString(int idx, String value) {
            entry[idx] = value;
        }
        
        public void setInt(int idx, int value) {
            entry[idx] = Integer.toString(value);
        }
        
        public void writeOut(BufferedWriter out) throws IOException {
            for (int i = 0; i < entry.length-1; i++) {
                out.write(entry[i]);
                out.write(';');
            }
            out.write(entry[entry.length-1]);
        }
    }
    
    private class ProvinceTxt implements Province {
        private final String name;
        private final String terrain;
        
        ProvinceTxt(String name, String terrain) {
            this.name = name;
            this.terrain = terrain;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isLand() {
            if (terrain.equals("sea") || terrain.equals("river"))
                return false;
            return true;
        }

        @Override
        public void writeOut(BufferedWriter writer) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
