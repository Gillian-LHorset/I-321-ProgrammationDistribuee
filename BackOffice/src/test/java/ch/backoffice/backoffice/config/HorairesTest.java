package ch.backoffice.backoffice.config;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HorairesTest {

    // Meme structure que config.yml (lundi-jeudi 06:00-17:00, vendredi 06:00-16:00, samedi 08:00-12:00, dimanche absent)
    private static final String YAML = String.join("\n",
            "horaires:",
            "  lundi:",
            "    ouverture: \"06:00:00\"",
            "    fermeture: \"17:00:00\"",
            "  mardi:",
            "    ouverture: \"06:00:00\"",
            "    fermeture: \"17:00:00\"",
            "  mercredi:",
            "    ouverture: \"06:00:00\"",
            "    fermeture: \"17:00:00\"",
            "  jeudi:",
            "    ouverture: \"06:00:00\"",
            "    fermeture: \"17:00:00\"",
            "  vendredi:",
            "    ouverture: \"06:00:00\"",
            "    fermeture: \"16:00:00\"",
            "  samedi:",
            "    ouverture: \"08:00:00\"",
            "    fermeture: \"12:00:00\"");

    private final Horaires horaires = Horaires.parse(YAML);

    @Test
    void ouvertAuMilieuDeLaPlage() {
        // lundi 06.01.2025 a 12:00
        assertTrue(horaires.estOuvert(LocalDateTime.of(2025, 1, 6, 12, 0)));
    }

    @Test
    void ouvertALOuvertureExacte() {
        // ouverture incluse
        assertTrue(horaires.estOuvert(LocalDateTime.of(2025, 1, 6, 6, 0)));
    }

    @Test
    void fermeAvantOuverture() {
        assertFalse(horaires.estOuvert(LocalDateTime.of(2025, 1, 6, 5, 59, 59)));
    }

    @Test
    void fermeALaFermetureExacte() {
        // fermeture exclue
        assertFalse(horaires.estOuvert(LocalDateTime.of(2025, 1, 6, 17, 0)));
    }

    @Test
    void fermeApresFermeture() {
        assertFalse(horaires.estOuvert(LocalDateTime.of(2025, 1, 6, 20, 30)));
    }

    @Test
    void horairesDifferentsSelonLeJour() {
        // vendredi 10.01.2025 : ferme a 16:00, mais lundi a 16:00 c'est ouvert
        assertFalse(horaires.estOuvert(LocalDateTime.of(2025, 1, 10, 16, 0)));
        assertTrue(horaires.estOuvert(LocalDateTime.of(2025, 1, 6, 16, 0)));
    }

    @Test
    void fermeLeSamediApresMidi() {
        // samedi 11.01.2025
        assertTrue(horaires.estOuvert(LocalDateTime.of(2025, 1, 11, 8, 0)));
        assertFalse(horaires.estOuvert(LocalDateTime.of(2025, 1, 11, 12, 0)));
    }

    @Test
    void jourNonConfigureEstFerme() {
        // dimanche 12.01.2025 : absent de config.yml
        assertFalse(horaires.estOuvert(LocalDateTime.of(2025, 1, 12, 10, 0)));
    }
}
