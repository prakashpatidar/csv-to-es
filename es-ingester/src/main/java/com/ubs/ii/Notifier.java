package com.ubs.ii;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Created by praka on 11/6/2016.
 */
public class Notifier {
    public void watch(String baseDir) throws IOException {

        WatchService watcher = FileSystems.getDefault().newWatchService();
        Path dir = Paths.get(baseDir);
        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException ex) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path fileName = ev.context();

                System.out.println(kind.name() + ": " + fileName);

                if (kind == ENTRY_MODIFY &&
                        fileName.toString().equals("DirectoryWatchDemo.java")) {
                    System.out.println("My source file has changed!!!");
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }
}
