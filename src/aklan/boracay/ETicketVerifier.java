package aklan.boracay;

import com.rameses.custom.impl.JsonUtil;
import com.rameses.util.Base64Coder;
import com.rameses.util.Encoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ETicketVerifier {

    private static String RANDOMKEY = "!edVk4uN|x+ntf/";
    private Base64Coder coder = new Base64Coder();
    private Scanner scanner = new Scanner(System.in);
    private Map options = new HashMap(); 
    
    public ETicketVerifier() {
        
    }
  
    public ETicketVerifier(String[] argList) {
        parseArgs(argList);
    }
  

    public void scanTicket() {
        Map input = new HashMap();
        boolean scanTicket = true;
        while (scanTicket) {
            input = getUserInput();
            validateTicket(input);
            scanTicket = isScanNewTicket();
        }
    }

    public static void main(String[] args) {
        ETicketVerifier bt = new ETicketVerifier(args);
        bt.scanTicket();
    }

    private boolean checkOrigin() {
        return get(options, "origin") != null;
    }
    
    private void validateTicket(Map params) {
        try {
            String b64str = get(params, "b64str");
            String origin = get(params, "origin");
            Map data = decode(b64str);
            validateHash(data);
            if (checkOrigin()) {
                validateOrigin(data, origin);
            }
            displayTicketInfo(data);
        } catch (Exception ex) {
            System.out.println("Ticket is invalid:");
            System.out.println("[Error] " + ex.getMessage());
        }
    }

    private Map decode(String b64str) {
        String jsonData = coder.decodeString(b64str);
        return JsonUtil.toMap(jsonData);
    }

    private String formatData(Map data) {
        StringBuilder sb = new StringBuilder();
        sb.append("ticketid:" + get(data, "ticketid") + ";");
        sb.append("traveldate:" + get(data, "traveldate") + ";");
        sb.append("indexno:" + get(data, "indexno") + ";");
        sb.append("lastname:" + get(data, "lastname") + ";");
        sb.append("firstname:" + get(data, "firstname") + ";");
        sb.append("gender:" + get(data, "gender") + ";");
        sb.append("age:" + get(data, "age") + ";");
        sb.append("filipino:" + get(data, "filipino") + ";");
        if (get(data, "filipino") == "0") {
            sb.append("country:" + get(data, "country") + ";");
            sb.append("passport:" + get(data, "passport") + ";");
        } else {
            sb.append("province:" + get(data, "province") + ";");
        }
        sb.append("origin:" + get(data, "origin") + ";");
        sb.append("destination:" + get(data, "destination") + ";");
        return sb.toString();
    }

    private void validateHash(Map data) throws Exception {
        String formattedData = formatData(data);
        String key = Encoder.SHA1.encode(formattedData, RANDOMKEY);
        String objid = get(data, "objid");
        if (!key.equalsIgnoreCase(objid)) {
            throw new Exception("Invalid key. Data is either corrupted or tampered.");
        }
    }

    private void validateOrigin(Map data, String origin) throws Exception {
        String ticketOrigin = get(data, "origin");
        if (!ticketOrigin.equalsIgnoreCase(origin)) {
            throw new Exception("Ticket origin is invalid. [Origin: " + ticketOrigin + "]");
        }
    }

    private void displayTicketInfo(Map data) {
        System.out.println("\n");
        System.out.println("=================== TICKET INFORMATION ===================");
        System.out.println("  Ticketid    : " + get(data, "ticketid"));
        System.out.println("  Traveldate  : " + get(data, "traveldate"));
        System.out.println("  Indexno     : " + get(data, "indexno"));
        System.out.println("  Lastname    : " + get(data, "lastname"));
        System.out.println("  Firstname   : " + get(data, "firstname"));
        System.out.println("  Gender      : " + get(data, "gender"));
        System.out.println("  Age         : " + get(data, "age"));
        if (get(data, "filipino") == "1") {
            System.out.println("  Country     : " + get(data, "country"));
            System.out.println("  Passport    : " + get(data, "passport"));
        } else {
            System.out.println("  Province    : " + get(data, "province"));
        }
        System.out.println("  Origin      : " + get(data, "origin"));
        System.out.println("  Destination : " + get(data, "destination"));
        System.out.println("\nPress ENTER to continue...");
    }

    private String get(Map data, String key) {
        return data.get(key) != null ? data.get(key).toString() : null;
    }
    
    private void parseArgs(String[] argList) {
        Object key = null;
        for (String arg : argList) {
            switch(arg) {
                case "-o":
                    key = "origin";
                    break;
                case "-d": 
                    key = "b64str";
                    break;
                default:
                    options.put(key, arg);
            }
        }
    }
    
    private String getInput(String caption) {
        System.out.print(caption);
        return scanner.nextLine();
    }
    
    private static void validateQrCode(String b64str) {
        if (b64str.isEmpty()) {
            System.out.println("QRCode must be specified.");
            System.exit(0);
        }
    }
    
    private String getQrCode() {
        StringBuffer sb = new StringBuffer();
        
        System.out.print("Scan QRCode  :  ");
        String newLine = scanner.nextLine();
        validateQrCode(newLine);
        sb.append(newLine.replace("\n", ""));
        while(scanner.hasNextLine()) {
            newLine = scanner.nextLine();
            if (newLine.isEmpty()) {
                break;
            }
            sb.append(newLine.replace("\n", ""));
        }
        
        validateQrCode(sb.toString());        
        return sb.toString();
    }
    
    private Map getUserInput() {
        Map input = new HashMap();
        if (get(options, "origin") != null) {
            String origin = getInput("Enter Origin :  ");
            if (origin.isEmpty()) {
                System.exit(0);
            }
            input.put("origin", origin);
        }

        String b64str = getQrCode();
        input.put("b64str", b64str);
        return input;
    }
    
    private boolean isScanNewTicket() {
        scanner.reset();
        System.out.println("\n");
        String scanNew = scanner.nextLine();
        if (scanNew.isEmpty()) { 
            scanNew = "y";
        }
        return "y".equalsIgnoreCase(scanNew);
    }
    
}
