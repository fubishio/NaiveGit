package NaiveGit;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Date;

public class CommitNode implements Serializable {

    private static DateFormat dateForm = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private String date;    
    private String commitMessage;
    private int id;
    private CommitNode parent;
    private HashMap<String, Integer> mappedFiles;
    
    public CommitNode() {
        date = dateForm.format(new Date());
        commitMessage = "initial commit";
        parent = null;
        id = 0;
        mappedFiles = new HashMap<String, Integer>();
    }
    
    public CommitNode(int id1, String commitMessage1, CommitNode parent1) {
        date = dateForm.format(new Date());
        commitMessage = commitMessage1;
        parent = parent1;
        id = id1;    
        mappedFiles = new HashMap<String, Integer>(parent.mappedFiles);
    }
    
    public String date() {
        return this.date;
    }
    
    public String commitMessage() {
        return this.commitMessage;
    }
    
    public void updatecommitMessage(String updated) {
        this.commitMessage = updated;
    }
    
    public int id() {
        return this.id;
    }
    
    public void updateid(int idd) {
        this.id = idd;
    }
    
    public CommitNode parent() {
        return this.parent;
    }
    
    public HashMap<String, Integer> mappedFiles() {
        return this.mappedFiles;
    }
    
    public void updateLinkedFiles(String str, int idd) {
        this.mappedFiles.put(str, idd);
    }
    
    public void rm(String str) {
        this.mappedFiles.remove(str);
    }
    
}
