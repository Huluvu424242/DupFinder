package de.b0n.dir.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Sucht in einem gegebenen Verzeichnis und dessen Unterverzeichnissen nach
 * Dateien und sortiert diese nach Dateigröße.
 * 
 * @author Claus
 *
 */
public class DuplicateLengthFinder extends AbstractProcessor implements Runnable {

	private final File folder;
	private final DuplicateLengthFinderCallback callback;

	private final List<Future<?>> futures = new ArrayList<Future<?>>();

	/**
	 * Bereitet für das gegebene Verzeichnis die Suche nach gleich großen
	 * Dateien vor.
	 * 
	 * @param threadPool
	 *            Pool zur Ausführung der Suchen
	 * @param folder
	 *            zu durchsuchendes Verzeichnis, muss existieren und lesbar sein
	 */
	private DuplicateLengthFinder(final File folder, DuplicateLengthFinderCallback callback) {
		String exceptionMessage = checkFolder(folder);
		if (exceptionMessage != null) {
			throw new IllegalArgumentException(exceptionMessage + folder.getAbsolutePath());
		}

		this.folder = folder;
		this.callback = callback;
	}

	private String checkFolder(final File folder) {
		String exceptionMessage = null;
		if (folder.list() == null) {
			exceptionMessage = "FEHLER: Parameter <Verzeichnis> kann nicht aufgelistet werden: ";
		}
		if (!folder.canRead()) {
			exceptionMessage = "FEHLER: Parameter <Verzeichnis> ist nicht lesbar: ";
		}
		if (!folder.isDirectory()) {
			exceptionMessage = "FEHLER: Parameter <Verzeichnis> ist kein Verzeichnis: ";
		}
		if (!folder.exists()) {
			exceptionMessage = "FEHLER: Parameter <Verzeichnis> existiert nicht: ";
		}
		return exceptionMessage;
	}

	/**
	 * Iteriert durch die Elemente im Verzeichnis und legt neue Suchen für
	 * Verzeichnisse an. Dateien werden sofort der Größe nach abgelegt.
	 * Wartet die Unterverzeichnis-Suchen ab und merged deren
	 * Ergebnisdateien. Liefert das Gesamtergebnis zurück.
	 */
	@Override
	public void run() {
		List<File> contents = readContent(folder);
		if (contents == null) {
			callback.unreadableFolder(folder);
			return;
		}

		for (File file : contents) {
			if (file.isDirectory()) {
				String exceptionMessage = checkFolder(file);
				if (exceptionMessage == null) {
					futures.add(submit(new DuplicateLengthFinder(file, callback)));
					callback.enteredNewFolder(file);
				} else {
					callback.unreadableFolder(file);
				}
			}

			if (file.isFile()) {
				callback.addGroupedElement(Long.valueOf(file.length()), file);
			}
		}

		consolidate(futures);
	}

	private List<File> readContent(File folder) {
		String[] names = folder.list();
		if (names == null) {
			return null;
		}

		List<File> contents = new ArrayList<>();
		for (String fileName : names) {
			contents.add(new File(folder.getAbsolutePath() + System.getProperty("file.separator") + fileName));
		}
		return contents;
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien
	 * gleicher Größe. Verwendet einen Executors.newWorkStealingPool() als
	 * ThreadPool.
	 * 
	 * @param folder
	 *            Zu durchsuchendes Verzeichnis
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in
	 *         denen die gefundenen Dateien abgelegt sind
	 */
	public static Cluster<Long, File> getResult(final File folder) {
		Cluster<Long, File> cluster = new Cluster<>();
		DuplicateLengthFinderCallback callback = new DuplicateLengthFinderCallback() {
			
			@Override
			public void unreadableFolder(File folder) {
				return;
			}
			
			@Override
			public void enteredNewFolder(File folder) {
				return;
			}

			@Override
			public void addGroupedElement(Long size, File file) {
				cluster.addGroupedElement(size, file);
			}
		};
		
		getResult(folder, callback);
		
		return cluster;
	}

	/**
	 * Einstiegstmethode zum Durchsuchen eines Verzeichnisses nach Dateien
	 * gleicher Größe.
	 * 
	 * @param folder
	 *            Zu durchsuchendes Verzeichnis
	 * @param threadPool
	 *            Pool zur Ausführung der Suchen
	 * @param callback
	 *            Ruft den Callback bei jedem neu betretenen Verzeichnis auf
	 *            (darf null sein)
	 * @return Liefert eine Map nach Dateigröße strukturierten Queues zurück, in
	 *         denen die gefundenen Dateien abgelegt sind
	 */
	public static void getResult(final File folder, DuplicateLengthFinderCallback callback) {
		if (folder == null) {
			throw new IllegalArgumentException("folder may not be null.");
		}
		if (callback == null) {
			throw new IllegalArgumentException("callback may not be null.");
		}

		consolidate(submit(new DuplicateLengthFinder(folder, callback)));
	}
}
