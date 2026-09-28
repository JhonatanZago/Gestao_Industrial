package com.metalurgica.sistemagestaoindustrial.assistente.security;

import java.util.Set;

public record AssistenteSecurityContext(
        Long userId,
        Long empresaId,
        String profile,
        Set<String> permissions,
        Set<Long> sectorIds,
        boolean allSectors) {

    public AssistenteSecurityContext {
        if (userId == null || empresaId == null || profile == null) {
            throw new IllegalArgumentException("Contexto de segurança do assistente incompleto.");
        }
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        sectorIds = sectorIds == null ? Set.of() : Set.copyOf(sectorIds);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public boolean canAccessSector(Long sectorId) {
        return allSectors || (sectorId != null && sectorIds.contains(sectorId));
    }
}
