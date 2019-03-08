import java.util.Arrays;

class TestApp {

    public static void printUsage() {
        System.err.println("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
        System.err.println("where:\n<peer_ap>\nIs the peer's access point. ");
        System.err.println(
                "<sub_protocol>\nIs the operation the peer of the backup service must execute. It can be either the triggering of the subprotocol to test, or the retrieval of the peer's internal state. In the first case it must be one of: BACKUP, RESTORE, DELETE, RECLAIM. In the case of enhancements, you must append the substring ENH at the end of the respecive subprotocol, e.g. BACKUPENH. To retrieve the internal state, the value of this argument must be STATE");
        System.err.println(
                "<opnd_1>\nIs either the path name of the file to backup/restore/delete, for the respective 3 subprotocols, or, in the case of RECLAIM the maximum amount of disk space (in KByte) that the service can use to store the chunks. In the latter case, the peer should execute the RECLAIM protocol, upon deletion of any chunk. The STATE operation takes no operands.");
        System.err.println(
                "<opnd_2>\nThis operand is an integer that specifies the desired replication degree and applies only to the backup protocol (or its enhancement)");
    }

    public static boolean validateArguments(String[] args) {
        if (args.length < 2 || args.length > 4) {
            System.err.println("ERROR: Invalid number of arguments!");
            printUsage();
            return false;
        }

        try {
            Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.err.println("ERROR: Could not parse peer_ap!");
            printUsage();
            return false;
        }

        switch (args[1]) {
        case "BACKUP":
            if (args.length != 4) {
                System.err.println("ERROR: BACKUP takes 2 operands!");
                printUsage();
                return false;
            }
            try {
                Integer.parseInt(args[3]);
            } catch (Exception e) {
                System.err.println("ERROR: Could not parse opnd_2!");
                printUsage();
                return false;
            }
            break;
        case "RESTORE":
            if (args.length != 3) {
                System.err.println("ERROR: RESTORE takes 1 operand!");
                printUsage();
                return false;
            }
            break;
        case "DELETE":
            if (args.length != 3) {
                System.err.println("ERROR: RESTORE takes 1 operand!");
                printUsage();
                return false;
            }
            break;
        case "RECLAIM":
            if (args.length != 3) {
                System.err.println("ERROR: RECLAIM takes 1 operand!");
                printUsage();
                return false;
            }
            try {
                Integer.parseInt(args[2]);
            } catch (Exception e) {
                System.err.println("ERROR: Could not parse opnd_1!");
                printUsage();
                return false;
            }
            break;
        case "STATE":
            if (args.length != 2) {
                System.err.println("ERROR: STATE takes no arguments!");
                printUsage();
                return false;
            }
            break;
        default:
            System.err.println("ERROR: Invalid operation!");
            printUsage();
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        if(!validateArguments(args))
            return;
    }
}