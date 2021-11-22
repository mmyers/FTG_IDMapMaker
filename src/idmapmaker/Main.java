package idmapmaker;

import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author Michael
 */
public class Main {

    private static String idFilename = null;
    private static String provFilename = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        handleArgs(args);
        JFileChooser chooser = null;
        if (provFilename == null || !(new File(provFilename).exists())) {
            chooser = new JFileChooser();
            chooser.setDialogTitle("Choose province.csv or equivalent");
            int choice = chooser.showOpenDialog(null);
            if (choice == JFileChooser.APPROVE_OPTION) {
                provFilename = chooser.getSelectedFile().getAbsolutePath();
            } else {
                return;
            }
        }
        if (idFilename == null || !(new File(idFilename).exists())) {
            if (chooser == null)
                chooser = new JFileChooser();
            chooser.setDialogTitle("Choose id.tbl or equivalent");
            int choice = chooser.showOpenDialog(null);
            if (choice == JFileChooser.APPROVE_OPTION) {
                idFilename = chooser.getSelectedFile().getAbsolutePath();
            } else {
                return;
            }
        }
        System.out.println("Reading provinces...");
        ProvinceData data = new ProvinceData(provFilename);
        System.out.println("done");
        
        System.out.println("Reading map files...");
        IdTbl id = new IdTbl(idFilename, data);
        BoundboxTbl bounds = new BoundboxTbl(new File(idFilename).getParent() + "/boundbox.tbl");
        System.out.println("done");
        
        System.out.println("Creating map image...");
        id.getImage();  // it's cached after the first call
        System.out.println("done");
        
        new MainFrame(id, bounds).setVisible(true);
    }

    private static void handleArgs(final String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (equals(arg, "-h", "--help")) {
                printHelp();
            } else if (arg.equals("-id")) {
                idFilename = stripQuotes(args[++i]);
            } else if (arg.equals("-prov")) {
                provFilename = stripQuotes(args[++i]);
            } else {
                System.err.println("Not a valid option: " + arg);
                printHelp();
            }
        }
    }
    
    private static boolean equals(String arg, String shortArg, String longArg) {
        return (arg.equals(shortArg) || arg.equalsIgnoreCase(longArg));
    }
    
    private static String stripQuotes(String str) {
        if (str.startsWith("\"")) {
            str = str.substring(1);
        }
        if (str.endsWith("\"")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }
    
    private static void printHelp() {
        System.out.println("Usage: java -jar IDMapMaker.jar [-args...]");
        System.out.println();
        System.out.println("where args include:");
        System.out.println("    -id <filename>");
        System.out.println("        The name of the id.tbl file to be read.");
        System.out.println("    -prov <filename>");
        System.out.println("        The name of the province.csv file to use.");
        System.out.println("    -h | --help");
        System.out.println("        Print this help.");
        System.out.println();
        System.out.println("Note that if either -id or -prov is not present, a file chooser will be shown.");
        System.out.println();
        System.out.println("Note also that arguments can appear in any order.");
        System.out.println();
        System.out.println("Example:");
        System.out.println("java -jar IDMapMaker.jar -id \"D:\\games\\mynewmapmod\\myid.tbl\" -prov \"D:\\games\\mynewmapmod\\newprovinces.csv\"");
    }
    
    private Main() {
    }
}
