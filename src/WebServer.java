import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Creates a new web server instance listening to provided port number.
 *
 */
public class WebServer {
    private ServerSocket socket;
    // for multithreading
    private ExecutorService pool;
    private final int poolSize = 8;

    /**
     * Creates a thread pool to handle multithreading, then listens on given
     * port until a request is received. Once a request is received a new client
     * handler is instantiated in a new thread, which will parse the request and
     * return the appropriate response.
     * 
     * @param port
     *            port number to listen on
     */
    void start(int port) {
        try {
            // creating new thread pool
            pool = Executors.newFixedThreadPool(poolSize);
            socket = new ServerSocket(port);
            System.out.println("Listening on port " + port);
            while (true) {
                // create new CHandler (bing!) for the connection we just got,
                // and execute it in a thread
                pool.execute(new CHandler(socket.accept()));
            }
        } catch (Exception e) {
            System.out.println("While creating new socket and handler " + e.toString());
        }
    }
}
