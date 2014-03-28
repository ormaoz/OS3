import java.io.File;

/**
 * This class reads a directory from the directory queue and 
 * lists all files in this directory. Then, it checks each file 
 * to see if the extension matches the one given. Files that has 
 * the right extension are enqueued to the results queue (to be copied).
 * 
 * @author ID: 029983111, ID: 038064556
 *
 */
public class Searcher implements Runnable {
	
	String extension;
	SynchronizedQueue<File> directoryQueue; 
	SynchronizedQueue<File> resultsQueue;
	
	/**
	 * Constructor. Initializes the searcher thread.
	 * @param extension - Pattern to look for
	 * @param directoryQueue - A queue with directories to search in (as listed by the scouter)
	 * @param resultsQueue - A queue for files found (to be copied by a copier)
	 */
	public Searcher(String extension, 
			SynchronizedQueue<File> directoryQueue, 
			SynchronizedQueue<File> resultsQueue) {
		this.directoryQueue = directoryQueue;
		
		// Adding a dot (".") in order to deal and separate between extensions like .html and .phtml
		this.extension = "." + extension;
		this.resultsQueue = resultsQueue;
		
	}
	
	/**
	 * Runs the searcher thread. 
	 * Thread will fetch a directory to search in from the 
	 * directory queue, then search all files inside it 
	 * (but will not recursively search subdirectories!). 
	 * Files that are found to have the given extension are 
	 * enqueued to the results queue. This method begins by 
	 * registering to the results queue as a producer and 
	 * when finishes, it unregisters from it.
	 */
	@Override
	public void run() {
		
		// Dequeue a directory from the directory queue
		File tempDir = directoryQueue.dequeue();
		
		// List all files and directories of the directory which was dequeued
		File[] filesArray = tempDir.listFiles();
		
		// Registering to the results queue as a producer
		resultsQueue.registerProducer();
		
		while (directoryQueue.getSize() != 0) {
			// Go over all files and directories and enqueue only files to the results queue
			for (File file : filesArray) {
				if (file.isFile() && file.getName().endsWith(extension)) {
					resultsQueue.enqueue(file);
				}
			}
		}
		
		
		// Unregisters from the results queue
		resultsQueue.unregisterProducer();

	}

}
