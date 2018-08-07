package NaiveGit;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class NaiveGitTest {
        
    private static final String NAIVEGIT_DIR = ".naiveGit/";
    private static final String TESTING_DIR = "test_files/";

    /* matches either unix/mac or windows line separators */
    private static final String LINE_SEP = "\r\n|[\r\n]";
    
    @Before
    public void setUp() {
        File f = new File(NAIVEGIT_DIR);
        if (f.exists()) {
            recursiveDelete(f);
        }
        f = new File(TESTING_DIR);
        if (f.exists()) {
            recursiveDelete(f);
        }
        f.mkdirs();
    }

    @Test
    public void testBasicInitialize() throws IOException {
        naiveGit("init");
        File f = new File(NAIVEGIT_DIR);
        assertTrue(f.exists());
    }

    @Test
    public void testBasicCheckout() throws IOException {
        String bananafile = TESTING_DIR + "banana.txt";
        String bananatext = "banana";
        createFile(bananafile, bananatext);
        naiveGit("init");
        naiveGit("add", bananafile);
        naiveGit("commit", "first banana");
        writeFile(bananafile, "banana 2.0");
        naiveGit("checkout", bananafile);
        assertEquals(bananatext, getText(bananafile));
        
        writeFile(bananafile, "banana 2.0");
        naiveGit("add", bananafile);
        naiveGit("commit", "banana 2");
        naiveGit("branch", "banana branch");
        naiveGit("checkout", "banana branch");
        
        writeFile(bananafile, "banana 3.0");
        naiveGit("add", bananafile);
        naiveGit("commit", "banana 3");
        naiveGit("checkout", "master");
        assertEquals("banana 2.0", getText(bananafile));
        
        naiveGit("checkout", "2 " + bananafile);
        assertEquals("banana 2.0", getText(bananafile));
        
        writeFile(bananafile, "banana 4.0");
        naiveGit("checkout", bananafile);
        assertEquals("banana 2.0", getText(bananafile));
        
        naiveGit("reset", "1");
        assertEquals("banana", getText(bananafile));
        
    }

    @Test
    public void testBasicLog() throws IOException {
        naiveGit("init");
        String commitMessage1 = "initial commit";

        String appleFileName = TESTING_DIR + "apple.txt";
        String appleText = "This is a apple.";
        createFile(appleFileName, appleText);
        naiveGit("add", appleFileName);
        String commitMessage2 = "added apple";
        naiveGit("commit", commitMessage2);

        String logContent = naiveGit("log");
        assertArrayEquals(new String[] { commitMessage2, commitMessage1 },
                extractCommitMessages(logContent));
    }
    
    @Test
    public void testGlobalLog() throws IOException {
        String bananafile = TESTING_DIR + "banana.txt";
        String bananatext = "banana";
        createFile(bananafile, bananatext);
        naiveGit("init");
        naiveGit("add", bananafile);
        naiveGit("commit", "first banana");
        writeFile(bananafile, "banana 2.0");
        naiveGit("checkout", bananafile);
        assertEquals(bananatext, getText(bananafile));
        
        writeFile(bananafile, "banana 2.0");
        naiveGit("add", bananafile);
        naiveGit("commit", "banana 2");
        naiveGit("branch", "banana branch");
        naiveGit("checkout", "banana branch");
        
        writeFile(bananafile, "banana 3.0");
        naiveGit("add", bananafile);
        naiveGit("commit", "banana 3");
        naiveGit("checkout", "master");
        assertEquals("banana 2.0", getText(bananafile));
        
        naiveGit("reset", "1");
        String cM3 = "banana 3";
        String cM2 = "banana 2";
        String cM1 = "first banana";
        String cM0 = "initial commit";
        String cM4 = "";
        
        String logContent = naiveGit("global-log");
        String[] x = new String[] { cM0, cM1, cM2, cM3};
        assertArrayEquals(x, extractCommitMessages(logContent));
    }
    

    // helper to call naiveGit from within the test case
    private static String naiveGit(String... args) throws IOException {
        PrintStream originalOut = System.out;
        InputStream originalIn = System.in;
        ByteArrayOutputStream printingResults = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(printingResults));

            String answer = "yes";
            InputStream is = new ByteArrayInputStream(answer.getBytes());
            System.setIn(is);

            NaiveGit.main(args);

        } finally {
            // reset in case something happens
            System.setOut(originalOut);
            System.setIn(originalIn);
        }
        return printingResults.toString();
    }

    private static String getText(String fileName) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fileName));
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    private static void createFile(String fileName, String fileText) {
        File f = new File(fileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writeFile(fileName, fileText);
    }

    private static void writeFile(String fileName, String fileText) {
        FileWriter fw = null;
        try {
            File f = new File(fileName);
            fw = new FileWriter(f, false);
            fw.write(fileText);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //delete everything from folder
    private static void recursiveDelete(File d) {
        if (d.isDirectory()) {
            for (File f : d.listFiles()) {
                recursiveDelete(f);
            }
        }
        d.delete();
    }

    private static String[] extractCommitMessages(String logOutput) {
        String[] logData = logOutput.split("====");
        int messageNum = logData.length - 1;
        String[] messages = new String[messageNum];
        for (int i = 0; i < messageNum; i++) {
            System.out.println(logData[i + 1]);
            String[] logs = logData[i + 1].split(LINE_SEP);
            messages[i] = logs[3];
        }
        return messages;
    }

}
