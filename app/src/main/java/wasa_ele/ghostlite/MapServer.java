package wasa_ele.ghostlite;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MapServer implements Runnable{

    private ServerSocket serverSocket = null;
    private String documentRoot;
    private int port;
    protected boolean isStopped    = false;
    protected Thread runningThread= null;

    public MapServer(final String documentRoot, final int portNum) {
        this.documentRoot = documentRoot;
        this.port = portNum;
    }

    public void run() {
        System.out.println("START locker Thread: " + Thread.currentThread().getId());
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while (!isStopped()) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if (isStopped()) return;
                throw new RuntimeException("Error accepting client connection", e);
            }
            new Thread(new WorkerRunnable(clientSocket, "Multithreaded Server", this.documentRoot)).start();
        }
        System.out.println("END locker Thread: " + Thread.currentThread().getId());
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8080", e);
        }
    }

    private synchronized boolean isStopped() {
        return this.isStopped;
    }
}