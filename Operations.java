package NaiveGit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Operations implements Serializable {
    //received significant help from Mingjian Lu especially regarding the underlying data structures
    //he suggested and went over with me specific maps I needed to make this work
    //especially the 2BCommitted map with a file name and a value determining 
    //whether to add or remove
    //also went over with me how to hit the runtime requirements, mostly by creating more maps
    private int idCounter = 0;
    
    private CommitNode curryNode;
    private String curryBranchName;
    
    private HashMap<String, CommitNode> branches = new HashMap<String, CommitNode>();
    private HashMap<String, CommitNode> msg2Node = new HashMap<String, CommitNode>();
    private HashMap<String, Integer> toBCommitted = new HashMap<String, Integer>();
    private HashMap<Integer, CommitNode> id2Node = new HashMap<Integer, CommitNode>();
    
    private HashMap<String, ArrayList<CommitNode>> findFunc;
    
    private String fs = File.separator;
    
    private boolean isDangerous() {
        Scanner input = new Scanner(System.in);
        System.out.println("Warning: The command you entered may alter the "
              + "files in your working directory. Uncommitted changes may be lost. "
              + "Are you sure you want to continue? (yes/no)");
        if (input.next().equals("yes")) {
            return true;
        } else if (input.next().equals("no")) {
            return false;
        } else {
            System.out.println("Please enter yes or no next time");
            return false;
        }
    }
        
    public void init() throws IOException {
        File initGitFolder = new File(".naiveGit" + fs + 0 + fs);
        if (!initGitFolder.exists()) {
            initGitFolder.mkdirs();
            curryNode = new CommitNode();
            branches.put("master", curryNode);
            id2Node.put(idCounter, curryNode);
            curryBranchName = "master";
            ArrayList<CommitNode> temp = new ArrayList<CommitNode>();
            temp.add(curryNode);
            findFunc = new HashMap<String, ArrayList<CommitNode>>();
            findFunc.put("initial commit", temp);
        } else {
            System.out.println("A naiveGit version control "
                + "system already exists in the current directory.");
        }
    }
    
    public void add(String f) throws IOException {
       // 1. Check if file exists
        if (new File(f).exists()) {
            // 2. check if file changes
            if (curryNode.mappedFiles().containsKey(f)) {
                if (fileHasChanged(".naiveGit" + fs + curryNode.mappedFiles().get(f) + fs + f, f)) {
                    System.out.println("File has not been modified since last commit.");
                } else {
                    toBCommitted.put(f, 0);
                }
            } else {
                // added
                toBCommitted.put(f, 0);
            }
        } else {
            System.out.println("File does not exist.");
        }
    }
    
    public void remove(String str) {
        if (!toBCommitted.containsKey(str) && !curryNode.mappedFiles().containsKey(str)) {
            System.out.println("No reason to remove the file.");
            return;
        } else {
            toBCommitted.put(str, 1);
        }
    }
    
    public void commit(String str) {
        //1. Check if string is provided
        if (str.equals("") || str.isEmpty()) {
            System.out.println("Please enter a commit message.");
        }
        //2. Check if anything is in toBCommitted
        if (toBCommitted.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        //3. Increment idCounter
        idCounter = idCounter + 1;
        //4. Makes Directory
        File curryCommitFolder = new File(".naiveGit" + fs + idCounter + fs);
        curryCommitFolder.mkdirs();
        //5. Copy Files over into directory
        for (String x : toBCommitted.keySet()) {
            if (toBCommitted.get(x) == 0) {
                try {
                    copy(new File(x), new File(".naiveGit" + fs + idCounter + fs + x));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //6. Makes new CommitNode and changes to current one
        curryNode = new CommitNode(idCounter, str, curryNode);        
        //7. update branches
        branches.put(curryBranchName, curryNode);
        //8. update msg2Node
        msg2Node.put(str, curryNode);
        //9. update id2Node
        id2Node.put(idCounter, curryNode);
        //10. updating file data in node
        for (String x : toBCommitted.keySet()) {
            if (toBCommitted.get(x) == 0) {
                curryNode.updateLinkedFiles(x, idCounter);
            } else if (toBCommitted.get(x) == 1) {
                curryNode.rm(x);
            }
        }
        //11. updating map for find function
        if (!findFunc.containsKey(str)) {
            ArrayList<CommitNode> tempFindFunc = new ArrayList<CommitNode>();
            tempFindFunc.add(curryNode);
            findFunc.put(str, tempFindFunc);
        } else {
            ArrayList<CommitNode> tempFindFunc = new ArrayList<CommitNode>();
            tempFindFunc = findFunc.get(str);
            tempFindFunc.add(curryNode);
            findFunc.put(str, tempFindFunc);
        }
        
        //12. clearing toBCommitted
        toBCommitted = new HashMap<String, Integer>();
    }
    
    public void checkout(String str) {
        if (!isDangerous()) {
            return;
        }
        
        //Checkout [id] [file name]
        String[] arguments = str.split(" ");
        if (isInt(arguments[0])) {
            if (arguments.length == 1) {
                System.out.println("Please specify file you wish to checkout");
                return;
            }
            int argInt = Integer.parseInt(arguments[0]);
            if (!id2Node.containsKey(argInt)) {
                System.out.println("No commit with that id exists.");
                return;
            }
            
            if (!id2Node.get(argInt).mappedFiles().containsKey(arguments[1])) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            
            try {
                copy(new File(".naiveGit" + fs + Integer.parseInt(arguments[0])
                    + fs + arguments[1]), new File(arguments[1]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //Checkout [BranchName] Case
            if (branches.keySet().contains(str)) {
                if (curryBranchName.equals(str)) {
                    System.out.println("No need to checkout the current branch.");
                    return;
                }
                curryBranchName = str;
                curryNode = branches.get(str);
                for (String x : branches.get(str).mappedFiles().keySet()) {
                    try {
                        copy(new File(".naiveGit" + fs + branches.get(str).mappedFiles().get(x) 
                            + fs + x), new File(x));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //Checkout [FileName] Case
                if (curryNode.mappedFiles().containsKey(str)) {
                    try {
                        copy(new File(".naiveGit" + fs + curryNode.mappedFiles().get(str) 
                            + fs + str), new File(str));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("File does not exist in the "
                        + "most recent commit, or no such branch exists.");
                    return;
                }
            }
        }
        
    }
    
    public static boolean isInt(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    public void log() {
        CommitNode pointer = curryNode;
        while (pointer != null) {
            System.out.println("====");
            System.out.println("Commit " + pointer.id());
            System.out.println(pointer.date());
            System.out.println(pointer.commitMessage());
            System.out.println("");
            pointer = pointer.parent();
        }
        
    }
    
    public void globalLog() {
        for (CommitNode x : id2Node.values()) {
            System.out.println("====");
            System.out.println("Commit " + x.id());
            System.out.println(x.date());
            System.out.println(x.commitMessage());
            System.out.println("");
        }
    }
    
    public void status() {
        System.out.println("=== Branches ===");
        for (String x : branches.keySet()) {
            if (x.equals(curryBranchName)) {
                System.out.println("*" + x);
            } else {
                System.out.println(x);
            }
        }
        System.out.println("");
        
        System.out.println("=== Staged Files ===");
        for (String y : toBCommitted.keySet()) {
            if (toBCommitted.get(y) == 0) {
                System.out.println(y);
            }
        }
        System.out.println("");
        
        System.out.println("=== Files Marked for Removal");
        for (String z : toBCommitted.keySet()) {
            if (toBCommitted.get(z) == 1) {
                System.out.println(z);
            }
        }
        System.out.println("");
    }
    
    public void find(String str) {
        if (!findFunc.containsKey(str)) {
            System.out.println("Found no commit with that message.");
            return;
        } else {
            for (CommitNode x : findFunc.get(str)) {
                System.out.println(x.id());
            }
        }
    }
    
    public void branch(String str) {
        if (!branches.containsKey(str)) {
            branches.put(str, curryNode);
        } else {
            System.out.println("A branch with that name already exists.");
        }
    }
    
    public void removeBranch(String str) {
        if (!branches.containsKey(str)) {
            System.out.println("A branch with that name does not exist.");
        } else if (str.equals(curryBranchName)) {
            System.out.println("Cannot remove current branch.");
        } else {
            branches.remove(str);
        }
    }
    
    public void reset(int id) {
        if (!isDangerous()) {
            return;
        }
        
        if (!id2Node.containsKey(id)) {
            System.out.println("No commit with that id exists.");
        } else {
            for (String x : id2Node.get(id).mappedFiles().keySet()) {
                try {
                    copy(new File(".naiveGit" + fs 
                        + id2Node.get(id).mappedFiles().get(x) + fs + x), new File(x));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            branches.put(curryBranchName, id2Node.get(id));
            curryNode = id2Node.get(id);
        }
    }
    
    public void merge(String str) {
        if (!isDangerous()) {
            return;
        }
        
        if (!branches.containsKey(str)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (curryBranchName.equals(str)) {
            System.out.println("Cannot merge a branch with itself.");
        } else {
            CommitNode splitPoint = id2Node.get(findSplitPoint(curryNode, branches.get(str)));
            CommitNode mergingBranch = branches.get(str);
            //getting list of combined files
            HashSet<String> combinedFileDir = new HashSet<String>();
            combinedFileDir.addAll(curryNode.mappedFiles().keySet());
            combinedFileDir.addAll(mergingBranch.mappedFiles().keySet());
            for (String x : combinedFileDir) {
                //most of the cases are covered by the 2 if functions below but
                //i like to have both the booleans set to something that would not 
                //execute either of the copies just for my own understanding
                //it would be enough to just declare the booleans though
                boolean mergeBranchModified = false;
                boolean curryBranchModified = true;
                
                //check if file in the branch your going 
                //to merge has been changed since the split point
                if (mergingBranch.mappedFiles().containsKey(x)) {
                    if (mergingBranch.mappedFiles().get(x) == splitPoint.mappedFiles().get(x)) {
                        mergeBranchModified = false;
                    } else {
                        mergeBranchModified = true;
                    }
                } else {
                    mergeBranchModified = false;
                }
                
                //check if file in the current branch has been changed since the split point
                if (curryNode.mappedFiles().containsKey(x)) {
                    if (curryNode.mappedFiles().get(x) == splitPoint.mappedFiles().get(x)) {
                        curryBranchModified = false;
                    } else {
                        curryBranchModified = true;
                    }
                } else {
                    curryBranchModified = false;
                }
                
                //this is a safe measure just to make sure 
                //nothing goes wrong and is added on accident
                //its a bit of redundant code with the top, 
                //it's just easier to look at for me though
                //declaring the variable one more time should 
                //not slow down the code in any significant way
                if (!mergingBranch.mappedFiles().containsKey(x)) {
                    mergeBranchModified = false;
                    curryBranchModified = true;
                }
                
                //Copying the files over in a merge
                try {
                    
                    if (mergeBranchModified && !curryBranchModified) {
                        copy(new File(".naiveGit" + fs + branches.get(str).mappedFiles().get(x)
                            + fs + x), new File(x));
                        
                    } else if (mergeBranchModified && curryBranchModified) {
                        copy(new File(".naiveGit" + fs + branches.get(str).mappedFiles().get(x) 
                            + fs + x + ".conflicted"), new File(x));
                    }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }                
            }
        }
    }
    
    public int findSplitPoint(CommitNode a, CommitNode b) {
        //recording parents of a id
        HashSet<Integer> tempParents = new HashSet<Integer>();
        //parent of the very first node is null but 0 won't be added so i add it manually
        while (a != null) {
            tempParents.add(a.id());
            a = a.parent();
        }
        tempParents.add(0);
        
        //check if any of b ancestors are common with ancestors of a
        //should reallly return 0 every time but compiler needs return not in conditional
        //should always hit 0 at the end
        while (b != null) {
            if (tempParents.contains(b.id())) {
                return b.id();
            } 
            b = b.parent();
            
        }
        
        //for sake of the compiler
        return 0;
    }
    
    public void rebase(String str) {
        if (!isDangerous()) {
            return;
        }
            
        if (curryBranchName.equals(str)) {
            System.out.println("Cannot rebase a branch onto itself.");
            return;
        }
        
        if (!branches.containsKey(str)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        
        HashSet<Integer> parents = new HashSet<Integer>();
        CommitNode pointer = curryNode;
        while (pointer != null) {
            pointer = pointer.parent();
            parents.add(pointer.id());
        }
        
        if (parents.contains(branches.get(str).id())) {
            System.out.println("Already up-to-date.");
            return;
        }
        
        int splitPointID = findSplitPoint(branches.get(str), curryNode);
        //from splitpoint to currentNode
        CommitNode pointer1 = curryNode;
        while (!pointer1.equals(id2Node.get(splitPointID))) {
            return;
        }
        
        
        
    }
    
    public void interactiveRebase(String str) {
        if (!isDangerous()) {
            return;
        }
        
        if (curryBranchName.equals(str)) {
            System.out.println("Cannot rebase a branch onto itself.");
            return;
        }
        
        if (!branches.containsKey(str)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        
        HashSet<Integer> parents = new HashSet<Integer>();
        CommitNode pointer = curryNode;
        while (pointer != null) {
            pointer = pointer.parent();
            parents.add(pointer.id());
        }
        
        if (parents.contains(branches.get(str).id())) {
            System.out.println("Already up-to-date.");
            return;
        }
    }
    
    
    
    
    
    //Readalllines didn't work so I took code from
    //http://javaonlineguide.net/2014/10/compare-two-files-in-java-example-code.html
    //and modified it slightly to make it friendly to my program
    public static boolean fileHasChanged(String str1, String str2) {
        final int readLineAmount = 80;
        File f1 = new File(str1);
        File f2 = new File(str2);
        try {
            FileInputStream fis1 = new FileInputStream(f1);
            FileInputStream fis2 = new FileInputStream(f2);
            if (f1.length() == f2.length()) {
                int n = 0;
                byte[] b1;
                byte[] b2;
                while ((n = fis1.available()) > 0) {
                    if (n > readLineAmount) {
                        n = readLineAmount;
                    }
                    b1 = new byte[n];
                    b2 = new byte[n];
                    int res1 = fis1.read(b1);
                    int res2 = fis2.read(b2);
                    if (!Arrays.equals(b1, b2)) {
                        fis1.close();
                        fis2.close();
                        return false;
                    }
                }
                fis1.close();
                fis2.close();
            } else {
                fis1.close();
                fis2.close();
                return false; // length is not matched.
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    
    
    //file copying taken from stack overflow
    //http://stackoverflow.com/questions/5368724/
    //how-to-copy-a-folder-and-all-its-subfolders-and-files-into-another-folder
    public static void copy(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            copyDirectory(sourceLocation, targetLocation);
        } else {
            copyFile(sourceLocation, targetLocation);
        }
    }
    
    private static void copyDirectory(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdir();
        }
    
        for (String f : source.list()) {
            copy(new File(source, f), new File(target, f));
        }
    }
    
    private static void copyFile(File source, File target) throws IOException {        
        final int buffer = 1024;
        //Original code only covers the sub-directories and doesn't manage
        //parent directories, I needed that so I added this
        File parent = target.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        
        //Original code does not overwrite files, I changed it so it does
        if (target.exists()) {
            target.delete();
        }
        
        try (
                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(target)
        ) {
            byte[] buf = new byte[buffer];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }
}
