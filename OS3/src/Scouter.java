import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class is responsible to list all directories that exist under the given root directory. 
 * It enqueues all directories into the directory queue.
 * 
 * @author ID: 029983111, ID: 038064556
 *
 */
public class Scouter implements Runnable {
	
	private SynchronizedQueue<File> directoryQueue;
	private File root;
	
	/**
	 * Constructor. 
	 * @param directoryQueue - The queue in which the directories are kept
	 * @param root - A root directory to be broken down to many sub directories.
	 */
	public Scouter(SynchronizedQueue<File> directoryQueue, File root) {
		this.directoryQueue = directoryQueue;
		this.root = root;
	}

	
	/**
	 * Starts the scouter thread. 
	 * Lists directories under root directory and adds them to queue, 
	 * then lists directories in the next level and enqueues them and so on. 
	 * This method begins by registering to the directory queue as a producer and when finishes, 
	 * it unregisters from it.
	 */
	@Override
	public void run() {

		// A queue which help us to generate a BFS search on the directories.
		// Directories will be saved here level after level
		Queue<File> helper = new LinkedList<>();
		
		// Registering to the directory queue as a producer
		directoryQueue.registerProducer();
		
		// First add the root directory to helper queue and the directory queue
		helper.add(root);
		directoryQueue.enqueue(root);
		
		// As long as directories are still available to search in:
		while (!helper.isEmpty()) {
			
			// takes out the next directory in line
			File[] directories = helper.poll().listFiles();
			
			// Map all its directories (into helper queue) and add them to the directory queue. 
			for (File directory : directories) {
				if (directory.isDirectory()) {
					helper.add(directory);
					directoryQueue.enqueue(directory);
				}
			}
		}
		// Unregisters from the directory queue
		directoryQueue.unregisterProducer();
	}
}
