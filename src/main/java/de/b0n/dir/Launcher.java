package de.b0n.dir;

import java.io.File;
import java.util.Date;
import java.util.Queue;

import de.b0n.dir.processor.*;

/**
 * Einfache Konsolenanwendung zur Ausgabe der gefundenen Dubletten in einem übergebenen Verzeichnis.
 *
 * @author Claus
 */
public class Launcher {

    protected static final String ERROR = "FEHLER: ";
    protected static final String USAGE = "\r\n Benutzung: DupFinder <Verzeichnis>\r\n<Verzeichnis> = Verzeichnis in dem rekursiv nach Duplikaten gesucht wird";
    protected static final String NO_PARAM = "Parameter <Verzeichnis> fehlt.";
    protected static final String INVALID_DIRECTORY = "Parameter <Verzeichnis> ist kein Verzeichnis.";
    protected static final String UREADABLE_DIRECTORY = "Parameter <Verzeichnis> kann nicht gelesen werden.";
    protected static final String NO_EXIST_DIRECTORY = "Parameter <Verzeichnis> existiert nicht.";


    protected final DupFinder dupFinder;

    public Launcher(final Cluster<Long, File> model) {
        this.dupFinder = new DupFinder(model);
    }

    public Queue<Queue<File>> searchDuplicatesIn(final File folder, final DupFinderCallback callback) {
        return this.dupFinder.searchDuplicatesIn(folder, callback);
    }


    /**
     * Sucht im übergebenen Verzeichnis nach Dubletten.
     *
     * @param args Erster Parameter muss ein gültiges Verzeichnis sein
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println(ERROR + NO_PARAM + USAGE);
            return;
        }

        final File folder = new File(args[0] + File.separator);

        if (!folder.exists()) {
            System.err.println(ERROR + NO_EXIST_DIRECTORY + USAGE);
            return;
        }

        if (!folder.isDirectory()) {
            System.err.println(ERROR + INVALID_DIRECTORY + USAGE);
            return;
        }

        if (!folder.canRead()) {
            System.err.println(ERROR + UREADABLE_DIRECTORY + USAGE);
            return;
        }

        final Cluster<Long, File> model = new Cluster<>();
        final Launcher launcher = new Launcher(model);
        final Queue<Queue<File>> duplicates = launcher.searchDuplicatesIn(folder, new Callback());
        launcher.printQueues(duplicates);

    }


    protected void printQueues(Queue<Queue<File>> queues) {
        System.out.println("\n\n### Liste gefundener Duplikate ###\n");
        for (Queue<File> files : queues) {
            printFiles(files);
            System.out.println();
        }
    }

    protected void printFiles(Queue<File> files) {
        if( !files.isEmpty()){
            final File firstFile=files.peek();
            System.out.println("( Orte von "+firstFile.getName()+" mit " +firstFile.length()+" Bytes Länge )");
        }

        for (File file : files) {
            printFile(file);
        }
    }

    protected void printFile(File file) {
        System.out.println( file.getAbsolutePath());
    }


    protected static class Callback implements DupFinderCallback {

        /**
         * LengthFinderCallback
         *
         */

        @Override
        public void enteredNewFolder(File folder) {
            System.out.println("Scanne "+folder.getAbsolutePath());
        }

        @Override
        public void unreadableFolder(File folder) {
            System.err.println("Warning: Folder nicht lesbar: "+folder.getAbsolutePath());

        }

        @Override
        public void skipFolder(File folder) {
            System.out.println("Warning: Folder wird übersprungen: "+folder.getAbsolutePath());
        }


        /**
         * ContentFinderCallback
         *
         */


        @Override
        public void failedFiles(int size) {
            System.out.println("Es wurden "+size+" Dateien nicht berücksichtigt (failed files).");
        }

        @Override
        public void duplicateGroup(Queue<File> duplicateGroup) {

        }

        @Override
        public void uniqueFiles(int uniqueFileCount) {
            System.out.println("Es wurden "+uniqueFileCount+" einzigartige Dateien (besitzen keine Duplikate) gefunden.");
        }

        @Override
        public void processorStartAt(ProcessorID id, Date date) {
            System.out.println("Processor "+id+" gestartet.");
        }

        @Override
        public void processorEndsAt(ProcessorID id, Date date) {
            System.out.println("Processor "+id+" beendet.");
        }
    }
}
