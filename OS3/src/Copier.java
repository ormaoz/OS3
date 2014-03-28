import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * This class reads a file from the results queue 
 * (the queue of files that has the right extension), 
 * and copies it into the specified destination directory.
 * 
 * @author ID: 029983111, ID: 038064556
 *
 */
public class Copier implements Runnable {
	
	private File destination;
	private SynchronizedQueue<File> resultsQueue;
	
	public static final int COPY_BUFFER_SIZE = 0;
		
	/**
	 * Constructor. Initializes the worker with a destination directory 
	 * and a queue of files to copy.
	 * @param destination - Destination directory
	 * @param resultsQueue - Queue of files found, to be copied
	 */
	public Copier(File destination, SynchronizedQueue<File> resultsQueue) {
		this.destination = destination;
		this.resultsQueue = resultsQueue;
	}

	/**
	 * Runs the copier thread. 
	 * Thread will fetch files from queue and copy them, 
	 * one after each other, to the destination directory. 
	 * When the queue has no more files, the thread finishes.
	 */
	@Override
	public void run() {
		while (resultsQueue.getSize() != 0) {
			File tmp = resultsQueue.dequeue();
			tmp.renameTo(destination);
			try {
				Files.copy(tmp.toPath(), destination.toPath());
			} catch (IOException e) {
				System.err.println(e);
			}
		}

	}

}
