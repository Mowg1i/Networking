import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is the Client Handler where most of the real work happens. Taking a
 * socket connection it reads the request from the client, parses it and returns
 * the appropriate response header and resource.
 *
 */
public class CHandler implements Runnable {
    // input stream to get the client's data
    private InputStream input;
    // output stream to send data back
    private DataOutputStream output;
    // this is the connection to the client
    private Socket connection;
    // to read in data from the client
    private BufferedReader reader;
    /**
     * Creates new log instance to record server and client activity.
     */
    private static Log log;

    /**
     * Used to write to the web server's log from other classes.
     * 
     * @param line
     *            takes a string to write to the log
     */
    public static void writeLog(String line) {
        try {
            log.write(line);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    CHandler(Socket connection) {
        this.connection = connection;
    }

    /**
     * Gets the client request, logs info about it, responds to the request and
     * closes the streams.
     * 
     * @return
     */
    public void run() {
        try {
            log = new Log();
            writeLog("Got request from " + connection.getInetAddress());
            try {
                // gets input stream from client
                input = connection.getInputStream();
                // gets output stream to client
                output = new DataOutputStream(connection.getOutputStream());
                // create reader to read in from input stream
                reader = new BufferedReader(new InputStreamReader(input));
            } catch (IOException e) {
                writeLog("While creating new connection handler " + e.toString());
            }
            // reads in from the client to the CHandler's request string
            String request = getClientRequest();
            // help with getting time and date:
            // https://stackoverflow.com/a/23068721
            String date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(new Date());
            writeLog("Client requested: " + request + " on " + date);
            // respond to request
            respond(request);
            // close streams
            closeAll();
            return;
        } catch (Exception e) {
            writeLog(e.toString() + " Closing connection.");
            closeAll();
            return;
        }
    }

    /**
     * Reads in the first line of the client's request and checks to make sure
     * it isn't null before returning it. If it is null, an exception is thrown
     * which will result in the connection being closed in the start() method.
     * 
     * @return the request received from the client - just the first line, since
     *         that's all we need in this case.
     * @throws Exception
     *             to show that the client has returned null, which will result
     *             in the connection being closed in the start() method.
     */
    public String getClientRequest() throws Exception {
        writeLog("Connection handler thread started.");
        // stores client's data
        // could loop to get all the lines, but we really only need the
        // header?
        String line = this.reader.readLine();
        // try to read in a line from the buffered reader, if it's null,
        // the connection is broken.
        if (line == null || line.equals("null") || line.equals("exit")) {
            throw new Exception("Client returned null.");
        }
        return line;
    }

    // closes everything nicely
    private void closeAll() {
        writeLog("Closing streams.\n");
        try {
            this.output.close();
            this.reader.close();
            this.input.close();
            this.connection.close();
        } catch (IOException e) {
            writeLog("While closing streams " + e.toString());
        }
    }

    String getContentType(String resourceName) {
        String ext = "";
        boolean foundExt = false;
        for (int i = 0; i < resourceName.length(); i++) {
            if (foundExt) {
                ext += resourceName.charAt(i);
            }
            if (resourceName.charAt(i) == '.') {
                foundExt = true;
            }
        }
        if (ext.equals("html")) {
            return "text/html";
        } else if (ext.equals("jpg")) {
            return "image/jpeg";
        } else if (ext.equals("png")) {
            return "image/png";
        } else if (ext.equals("gif")) {
            return "image/gif";
        } else if (ext.equals("pdf")) {
            return "application/pdf";
        } else {
            return "unsupported";
        }
    }

    String getResponseCode(File resource, String type) {
        if (!resource.exists()) {
            return "404 Not Found";
        } else if (type.equals("GET") || type.equals("HEAD")) {
            return "200 OK";
        } else {
            return "501 Not Implemented";
        }
    }

    void respond(String request) throws IOException {
        // my server
        String serverInfo = "My Very Own Java HTTP Server";
        // split the request into parts
        String[] reqs = request.split(" ");
        // type of request eg GET, HEAD etc
        String type = reqs[0];
        // name of resource requested
        String resourceName = reqs[1];
        // protocol normally HTTP/1.1
        String protocol = reqs[2];
        // path to file
        String resourcePath = WebServerMain.getRoot() + resourceName;
        File resource = new File(resourcePath);
        // get type of content from file extension
        String contentType = getContentType(resourceName);
        // building head response
        String headResponse = protocol + " " + getResponseCode(resource, type) + "\r\n" + "Server: " + serverInfo
                + "\r\n" + "Content-Length: " + resource.length() + "\r\n" + "Content-Type: " + contentType + "\r\n"
                + "\r\n";
        if (contentType.equals("text/html")) {
            headResponse += "\r\n";
        }
        writeLog("Server response:\n" + headResponse);
        output.writeBytes(headResponse);
        if (type.equals("GET")) {
            // write the requested resource to the output stream
            try {
                writeLog("Reading file data from stream.");
                // new input stream to read in from file
                DataInputStream fileInput = new DataInputStream(new FileInputStream(resource));
                // read the bytes in and write them to the output stream
                while (fileInput.available() > 0) {
                    output.writeByte(fileInput.readByte());
                }
                output.flush();
                fileInput.close();
                writeLog("Server sent file.");
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }
}
