package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.importacao.*;
import com.metalurgica.sistemagestaoindustrial.repository.*;
import com.metalurgica.sistemagestaoindustrial.service.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportacaoAutomaticaStatusServiceTests {
    @Test
    void disabledStatusDoesNotExposeSensitiveSourceDetails() throws Exception {
        var properties = new AutomaticImportProperties();
        var health = mock(ImportacaoAutomaticaHealthService.class);
        when(health.check()).thenReturn(List.of());
        when(health.lastCheck()).thenReturn(null);
        var service = new ImportacaoAutomaticaStatusService(properties, mock(ServidorExcelWatcher.class), health,
                mock(ImportacaoProducaoRepository.class), mock(SetorRepository.class), mock(EmpresaService.class));
        assertEquals("DESATIVADO", service.status(1L).status());
        var names = java.util.Arrays.stream(service.status(1L).getClass().getRecordComponents()).map(java.lang.reflect.RecordComponent::getName).toList();
        assertFalse(names.contains("path"));
        assertFalse(names.contains("password"));
        assertFalse(names.contains("token"));
    }
}
