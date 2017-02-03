package de.b0n.dir.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.b0n.dir.ClusterCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by huluvu424242 on 07.01.17.
 */
public class DuplicateLengthFinderTest {

	private static final String PATH_SAME_SIZE_FILES_IN_TREE_FOLDER = "src/test/resources/duplicateTree";
	private static final String PATH_FILE = "src/test/resources/Test1.txt";
	private static final String PATH_INVALID_FOLDER = "src/test/resourcesInvalid/";
	private static final String PATH_NO_SAME_SIZE_FOLDER = "src/test/resources/noDuplicates";
	private static final String PATH_EMPTY_FOLDER = "src/test/resources/emptyFolder";
	private static final String PATH_PLENTY_SAME_SIZE_FOLDER = "src/test/resources/";
	private static final String PATH_SAME_SIZE_IN_FLAT_FOLDER = "src/test/resources/folderOnlyFolder/flatDuplicateTree";
	private static final String PATH_FOLDER_ONLY_FOLDER = "src/test/resources/folderOnlyFolder";

	@Before
	public void setUp() {

	}

	@After
	public void tearDown() {

	}

	@Test(expected = IllegalArgumentException.class)
	public void noArgumentsFolder() {
		DuplicateLengthFinder.getResult(null,new Cluster<Long, File>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void noThreadPool() {
		ExecutorService executorService = null;
		DuplicateLengthFinder.getResult(new File("."), executorService,new Cluster<Long, File>());
	}

	//TODO
//	@Test
//	public void noCallback() {
//		DuplicateLengthFinder.getResult(new File("."));
//	}

	@Test(expected = IllegalArgumentException.class)
	public void noFolderButThreadPool() {
		final ExecutorService threadPool = Executors.newWorkStealingPool();
		DuplicateLengthFinder.getResult(null, threadPool,new Cluster<Long, File>());
	}

	//TODO
//	@Test(expected = IllegalArgumentException.class)
//	public void noFolderButCallback() {
//		DuplicateLengthFinder.getResult(null);
//	}

	@Test(expected = IllegalArgumentException.class)
	public void scanInvalidFolder() {
		final File folder = new File(PATH_INVALID_FOLDER);
		DuplicateLengthFinder.getResult(folder,new Cluster<Long, File>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void scanFile() {
		final File folder = new File(PATH_FILE);
		DuplicateLengthFinder.getResult(folder,new Cluster<Long, File>());
	}

	@Test
	public void scanFlatFolder() {
		final File folder = new File(PATH_SAME_SIZE_IN_FLAT_FOLDER);
		final ClusterCallback<Long, File> result = DuplicateLengthFinder.getResult(folder,new Cluster<Long, File>());
		assertNotNull(result);
		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 1, result.values().size());
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2,
				result.values().iterator().next().size());
	}

	@Test
	public void scanFolderOnlyFolder() {
		final File folder = new File(PATH_FOLDER_ONLY_FOLDER);
		final ClusterCallback<Long, File> result = DuplicateLengthFinder.getResult(folder,new Cluster<Long, File>());
		assertNotNull(result);
		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 1, result.values().size());
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2,
				result.values().iterator().next().size());
	}

	@Test
	public void scanEmptyFolder() {
		final File folder = new File(PATH_EMPTY_FOLDER);
		if (folder.mkdir()) {
			final ClusterCallback<Long, File> result = DuplicateLengthFinder.getResult(folder,new Cluster<Long, File>());
			assertNotNull(result);
			assertEquals(0, result.values().size());
			folder.delete();
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void scanUnreadableFolder() {
		System.out.println("OS calls itself: " + System.getProperty("os.name"));
		assumeTrue(System.getProperty("os.name").contains("Linux"));
		File folder = new File("/root");
		DuplicateLengthFinder.getResult(folder,new Cluster<Long, File>());
	}

	@Test
	public void scanNoDuplicates() {
		final File folder = new File(PATH_NO_SAME_SIZE_FOLDER);
		final ClusterCallback<Long, File> result = DuplicateLengthFinder.getResult(folder,new Cluster<Long, File>());
		assertNotNull(result);
		assertEquals(0, result.values().size());
	}

	@Test
	public void scanDuplicatesInTree() {
		final File folder = new File(PATH_SAME_SIZE_FILES_IN_TREE_FOLDER);
		final ClusterCallback<Long, File> result = DuplicateLengthFinder.getResult(folder,new Cluster<Long, File>());
		assertNotNull(result);
		Iterator<Queue<File>> elementsIterator = result.values().iterator(); 
		assertEquals(1, result.values().size());
		assertEquals(2, elementsIterator.next().size());
	}

	@Test
	public void scanDuplicatesInBiggerTree() {
		final File folder = new File(PATH_PLENTY_SAME_SIZE_FOLDER);
		final ClusterCallback<Long, File> result = DuplicateLengthFinder.getResult(folder,new Cluster<Long, File>());
		assertNotNull(result);
		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 2, result.values().size());
		Iterator<Queue<File>> elementsIterator = result.values().iterator(); 
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2, elementsIterator.next().size());
		assertEquals("falsche Anzahl von 91 Byte-Datei Vorkommen bestimmt", 4, elementsIterator.next().size());
	}
}
