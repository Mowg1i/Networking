/**
 * Main class - takes user arguments and starts and runs the server.
 *
 */
public class WebServerMain {
    private static int port;
    private static String directory;

    /**
     * Takes directory and port from the user, checks to make sure arguments are
     * valid, and instantiates a web server which will listen on that port to
     * serve resources from that directory.
     *
     * @param args
     *            takes two arguments: the directory from which to serve
     *            documents, and the port to listen to.
     */
    public static void main(String[] args) {
        if ((args.length != 2)) {
            System.out.println("Usage: java WebServerMain <document_root> <port>");
            return;
        }
        try {
            directory = args[0];
            // parse int will throw an exception if port is not an int
            port = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("Usage: java WebServerMain <document_root> <port>");
            return;
        }
        // create a new webserver listening to given port
        WebServer webServer = new WebServer();
        webServer.start(port);
    }

    // returns the given directory - used in the client handler to access the
    // resource requested
    static String getRoot() {
        return directory;
    }
}
