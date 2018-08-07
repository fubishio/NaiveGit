package NaiveGit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class NaiveGit {
    
    static String fs = File.separator;
    static File initGitFolder = new File(".naiveGit" + fs);
    static File initSerFile = new File(".naiveGit" + fs + "data.ser");
    //Code about reading a serialized object and creating them is 
    //referenced from http://www.javapractices.com/topic/TopicAction.do?Id=57
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("No commands specified");
                return;
            }
            if (!initGitFolder.exists()) {
                if (!args[0].equals("init")) {
                    System.out.println("NaiveGit Repository has not been initiated yet");
                    return;
                }
                initSer();
                return;
            }
            if (!initSerFile.exists()) {
                if (!args[0].equals("init")) {
                    System.out.println("NaiveGit Serialized object has not been initiated yet");
                    return;
                }
                initSer();
                return;
            }
            Operations operator = readData();
            String[] input = Arrays.copyOfRange(args, 1, args.length);
            switch (args[0]) {
                case "init":
                    operator.init();
                    break;
                case "add":
                    operator.add(sentence(input));
                    break;
                case "commit":
                    operator.commit(sentence(input));
                    break;
                case "rm":
                    operator.remove(sentence(input));
                    break;
                case "log":
                    operator.log();
                    break;
                case "global-log":
                    operator.globalLog();
                    break;
                case "find":
                    operator.find(sentence(input));
                    break;
                case "status":
                    operator.status();
                    break;
                case "checkout":
                    operator.checkout(sentence(input));
                    break;
                case "branch":
                    operator.branch(sentence(input));
                    break;
                case "rm-branch":
                    operator.removeBranch(sentence(input));
                    break;
                case "reset":
                    operator.reset(Integer.parseInt(input[0]));
                    break;
                case "merge":
                    operator.merge(sentence(input));
                    break;
                case "rebase":
                    operator.rebase(sentence(input));
                    break;
                case "i-rebase":
                    operator.interactiveRebase(sentence(input));
                    break;
                default: 
                    usage();
                    return;
            }
            saveData(operator);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void initSer() {
        try {
            Operations operator = new Operations();
            operator.init();
            FileOutputStream fos = new FileOutputStream(".naiveGit" + fs + "data.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(operator);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static Operations readData() {
        try {
            FileInputStream data = new FileInputStream(".naiveGit" + fs + "data.ser");
            ObjectInputStream operating = new ObjectInputStream(data);
            Operations operator = (Operations) operating.readObject();
            data.close();
            operating.close();
            return operator;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static void saveData(Operations op) {
        try {
            FileOutputStream saveData = new FileOutputStream(".naiveGit" + fs + "data.ser");
            ObjectOutputStream saving = new ObjectOutputStream(saveData);
            saving.writeObject(op);
            saveData.close();
            saving.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String sentence(String[] x) {
        String result = "";
        for (String y : x) {
            result = result + y + " ";
        }
        result = result.substring(0, result.length() - 1);
        return result;
    }
    
    public static void usage() {
        System.out.println("Function not recognized");
    }    
}
