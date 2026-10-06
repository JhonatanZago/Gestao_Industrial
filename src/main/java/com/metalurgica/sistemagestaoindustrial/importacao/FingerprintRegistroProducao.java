package com.metalurgica.sistemagestaoindustrial.importacao;

import com.metalurgica.sistemagestaoindustrial.domain.Funcionario;
import com.metalurgica.sistemagestaoindustrial.domain.Peca;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

/** Identidade canônica do conteúdo de uma produção importada. */
public final class FingerprintRegistroProducao {
    private FingerprintRegistroProducao() { }

    public static String of(Long empresaId, Long setorId, Funcionario funcionario, Peca peca,
                            BigDecimal quantidade, LocalDateTime inicio, LocalDateTime fim) {
        var canonical = String.join("|", value(empresaId), value(setorId),
                value(funcionario == null ? null : funcionario.getId()), value(peca == null ? null : peca.getId()),
                decimal(quantidade), value(inicio), value(fim));
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception error) {
            throw new IllegalStateException("Não foi possível calcular fingerprint do registro.", error);
        }
    }

    private static String decimal(BigDecimal value) { return value == null ? "<null>" : value.stripTrailingZeros().toPlainString(); }
    private static String value(Object value) { return value == null ? "<null>" : value.toString(); }
}
