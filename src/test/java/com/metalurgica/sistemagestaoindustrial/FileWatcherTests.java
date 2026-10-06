package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.importacao.*;
import com.metalurgica.sistemagestaoindustrial.service.ImportacaoProducaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileWatcherTests {
    @TempDir Path temp;

    @Test
    void acceptsOnlyRelevantXlsxFiles() throws Exception {
        var valid = Files.createFile(temp.resolve("KTL.xlsx"));
        assertTrue(ServidorExcelWatcher.relevant(valid));
        assertFalse(ServidorExcelWatcher.relevant(Files.createFile(temp.resolve("KTL.xls"))));
        assertFalse(ServidorExcelWatcher.relevant(Files.createFile(temp.resolve("~$KTL.xlsx"))));
        assertFalse(ServidorExcelWatcher.relevant(Files.createFile(temp.resolve("KTL.tmp.xlsx"))));
        assertFalse(ServidorExcelWatcher.relevant(Files.createFile(temp.resolve(".KTL.xlsx"))));
    }

    @Test
    void debounceCollapsesMultipleModifyEventsPerPath() throws Exception {
        var calls = new AtomicInteger();
        var done = new CountDownLatch(1);
        try (var debounce = new FileChangeDebouncer(50, ignored -> { calls.incrementAndGet(); done.countDown(); })) {
            var path = temp.resolve("KTL.xlsx");
            debounce.signal(path); debounce.signal(path); debounce.signal(path);
            assertTrue(done.await(2, TimeUnit.SECONDS));
            Thread.sleep(80);
            assertEquals(1, calls.get());
        }
    }

    @Test
    void detectorReportsCreateAndModify() throws Exception {
        var detector = new WatchServiceFileChangeDetector(List.of(temp));
        var events = new CopyOnWriteArrayList<FileChangeDetector.FileChangeEvent>();
        try {
            detector.start(events::add);
            var file = Files.createFile(temp.resolve("KTL.xlsx"));
            Files.writeString(file, "v1");
            assertTrue(await(() -> events.stream().anyMatch(e -> e.kind() == FileChangeDetector.FileChangeEvent.Kind.CREATE)));
            Files.writeString(file, "v2", StandardOpenOption.APPEND);
            assertTrue(await(() -> events.stream().anyMatch(e -> e.kind() == FileChangeDetector.FileChangeEvent.Kind.MODIFY)));
        } finally { detector.stop(); }
    }

    @Test
    void stableFileIsReleasedAndUnstableFileTimesOut() throws Exception {
        var service = new FileStabilityService();
        var stable = Files.writeString(temp.resolve("stable.xlsx"), "stable");
        var config = new AutomaticImportProperties.Stability();
        config.setIntervalMs(10); config.setStableChecks(2); config.setTimeoutSeconds(1);
        service.awaitStable(stable, config);

        var unstable = Files.writeString(temp.resolve("unstable.xlsx"), "0");
        var writer = Executors.newSingleThreadScheduledExecutor();
        try {
            writer.scheduleAtFixedRate(() -> { try { Files.writeString(unstable, Long.toString(System.nanoTime())); } catch (Exception ignored) { } }, 0, 5, TimeUnit.MILLISECONDS);
            assertEquals("ARQUIVO_NAO_ESTABILIZOU", assertThrows(Exception.class, () -> service.awaitStable(unstable, config)).getMessage());
        } finally { writer.shutdownNow(); }
    }

    @Test
    void disabledWatcherDoesNotStartDetector() {
        var properties = new AutomaticImportProperties();
        properties.setEnabled(false);
        var service = mock(ImportacaoProducaoService.class);
        var watcher = new ServidorExcelWatcher(properties, new FileStabilityService(), service);
        watcher.start();
        watcher.stop();
        verifyNoInteractions(service);
    }

    @Test
    void configuredWatcherProcessesFileWithFileWatcherSource() throws Exception {
        var properties = new AutomaticImportProperties();
        properties.setEnabled(true); properties.setDebounceMs(10);
        properties.getStability().setIntervalMs(10); properties.getStability().setStableChecks(2); properties.getStability().setTimeoutSeconds(2);
        var directory = new AutomaticImportProperties.Directory();
        directory.setEmpresaId(10L); directory.setSetorId(20L); directory.setPath(temp.toString());
        properties.setDirectories(List.of(directory));
        var service = mock(ImportacaoProducaoService.class);
        var watcher = new ServidorExcelWatcher(properties, new FileStabilityService(), service);
        try {
            watcher.start();
            var file = Files.writeString(temp.resolve("KTL.xlsx"), "not-a-real-xlsx");
            Thread.sleep(250);
            verify(service, timeout(2000)).importFileFromWatcher(eq(10L), eq(20L), any(PathMultipartFile.class));
        } finally { watcher.stop(); }
    }

    @Test
    void eventArrivingDuringProcessingIsKeptForTheNextVersion() throws Exception {
        var properties = new AutomaticImportProperties();
        properties.setEnabled(true); properties.setDebounceMs(10);
        properties.getStability().setIntervalMs(10); properties.getStability().setStableChecks(2); properties.getStability().setTimeoutSeconds(2);
        var directory = new AutomaticImportProperties.Directory();
        directory.setEmpresaId(10L); directory.setSetorId(20L); directory.setPath(temp.toString());
        properties.setDirectories(List.of(directory));
        var firstStarted = new CountDownLatch(1); var releaseFirst = new CountDownLatch(1); var calls = new AtomicInteger();
        var service = mock(ImportacaoProducaoService.class);
        doAnswer(invocation -> {
            if (calls.incrementAndGet() == 1) { firstStarted.countDown(); releaseFirst.await(2, TimeUnit.SECONDS); }
            return null;
        }).when(service).importFileFromWatcher(eq(10L), eq(20L), any(PathMultipartFile.class));
        var watcher = new ServidorExcelWatcher(properties, new FileStabilityService(), service);
        try {
            watcher.start();
            var file = Files.writeString(temp.resolve("KTL.xlsx"), "v1");
            assertTrue(firstStarted.await(2, TimeUnit.SECONDS));
            Files.writeString(file, "v2", StandardOpenOption.APPEND);
            releaseFirst.countDown();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (calls.get() < 2 && System.nanoTime() < deadline) Thread.sleep(20);
            assertEquals(2, calls.get());
        } finally { releaseFirst.countDown(); watcher.stop(); }
    }

    private boolean await(java.util.function.BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) { if (condition.getAsBoolean()) return true; Thread.sleep(10); }
        return false;
    }
}
