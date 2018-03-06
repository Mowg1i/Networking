import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

/**
 * Logs information about client and server activity to a text file, and also
 * prints out the same info to console.
 *
 */
public class Log {
    private File log;

    Log() {
        // creates text file to write to
        log = new File("log.txt");
    }

    void write(String line) {
        try {
            // adds newline char to line, for ease of readability
            line += "\n";
            // creates new writer
            Writer output = new BufferedWriter(new FileWriter(log, true));
            // writes line to log
            output.append(line);
            output.close();
            System.out.println(line);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
