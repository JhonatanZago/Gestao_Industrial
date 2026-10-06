package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.importacao.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReconciliationTests {
    @TempDir Path temp;

    @Test
    void disabledReconciliationDoesNotScan() {
        var properties = new AutomaticImportProperties(); properties.setEnabled(false);
        var coordinator = mock(FileProcessingCoordinator.class);
        var result = new ReconciliationService(properties, coordinator).reconciliar();
        assertEquals(0, result.arquivosEncontrados()); verifyNoInteractions(coordinator);
    }

    @Test
    void scansEligibleFilesAndIgnoresTemporaryFiles() throws Exception {
        Files.writeString(temp.resolve("KTL.xlsx"), "x");
        Files.writeString(temp.resolve("~$KTL.xlsx"), "x");
        Files.writeString(temp.resolve("KTL.tmp.xlsx"), "x");
        var properties = properties(temp);
        var coordinator = mock(FileProcessingCoordinator.class);
        when(coordinator.process(any(), any(), any(), any())).thenReturn(FileProcessingCoordinator.Outcome.PROCESSED);
        var result = new ReconciliationService(properties, coordinator).reconciliar();
        assertEquals(1, result.arquivosEncontrados()); assertEquals(1, result.arquivosProcessados());
        verify(coordinator, times(1)).process(any(), any(), any(), any());
    }

    @Test
    void isolatesErrorsAcrossDirectories() throws Exception {
        var second = Files.createDirectory(temp.resolve("second"));
        Files.writeString(temp.resolve("bad.xlsx"), "x"); Files.writeString(second.resolve("good.xlsx"), "x");
        var properties = properties(temp); properties.setDirectories(new java.util.ArrayList<>(properties.getDirectories())); properties.getDirectories().add(directory(second));
        var coordinator = mock(FileProcessingCoordinator.class);
        when(coordinator.process(argThat((Path path) -> path != null && path.getFileName().toString().startsWith("bad")), any(), any(), any()))
                .thenReturn(FileProcessingCoordinator.Outcome.ERROR);
        when(coordinator.process(argThat((Path path) -> path != null && path.getFileName().toString().startsWith("good")), any(), any(), any()))
                .thenReturn(FileProcessingCoordinator.Outcome.PROCESSED);
        var result = new ReconciliationService(properties, coordinator).reconciliar();
        assertEquals(2, result.arquivosEncontrados()); assertEquals(1, result.arquivosComErro()); assertEquals(1, result.arquivosProcessados());
    }

    @Test
    void reportsInaccessibleDirectory() {
        var properties = properties(temp.resolve("missing"));
        var result = new ReconciliationService(properties, mock(FileProcessingCoordinator.class)).reconciliar();
        assertEquals(1, result.arquivosComErro());
    }

    @Test
    void exposesConfiguredDefaultScheduleAndTimezone() {
        var properties = new AutomaticImportProperties();
        assertEquals("0 0 18 * * *", properties.getReconciliation().getCron());
        assertEquals("America/Sao_Paulo", properties.getReconciliation().getZone());
        assertTrue(properties.getReconciliation().isEnabled());
    }

    private AutomaticImportProperties properties(Path path) {
        var properties = new AutomaticImportProperties(); properties.setEnabled(true);
        properties.getReconciliation().setEnabled(true);
        properties.getStability().setIntervalMs(1); properties.getStability().setStableChecks(1); properties.getStability().setTimeoutSeconds(1);
        properties.setDirectories(List.of(directory(path)));
        return properties;
    }

    private AutomaticImportProperties.Directory directory(Path path) {
        var directory = new AutomaticImportProperties.Directory(); directory.setEmpresaId(1L); directory.setSetorId(2L); directory.setPath(path.toString()); return directory;
    }
}
