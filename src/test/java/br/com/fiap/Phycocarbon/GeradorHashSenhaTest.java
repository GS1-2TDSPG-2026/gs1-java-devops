package br.com.fiap.Phycocarbon;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeradorHashSenhaTest {

    @Test
    void gerarHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("Admin@2026       -> " + encoder.encode("Admin@2026"));
        System.out.println("Operador@2026    -> " + encoder.encode("Operador@2026"));
        System.out.println("Investidor@2026  -> " + encoder.encode("Investidor@2026"));
        System.out.println("Comprador@2026   -> " + encoder.encode("Comprador@2026"));
        System.out.println("Usuario@2026     -> " + encoder.encode("Usuario@2026"));
    }
}