package ch.backoffice.backoffice.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Charge le vrai config.yml du projet (surefire tourne a la racine du projet).
 */
class HorairesConfigFileTest {

    @Test
    void configYmlReelEstChargeEtCoherent() throws Exception {
        Horaires horaires = Horaires.charger(Path.of("config.yml"));

        // lundi 06.01.2025 : 06:00-17:00
        assertTrue(horaires.estOuvert(LocalDateTime.of(2025, 1, 6, 10, 0)));
        assertFalse(horaires.estOuvert(LocalDateTime.of(2025, 1, 6, 18, 0)));
        // vendredi 10.01.2025 : fermeture a 16:00
        assertFalse(horaires.estOuvert(LocalDateTime.of(2025, 1, 10, 16, 30)));
        // samedi 11.01.2025 : 08:00-12:00
        assertTrue(horaires.estOuvert(LocalDateTime.of(2025, 1, 11, 9, 0)));
        assertFalse(horaires.estOuvert(LocalDateTime.of(2025, 1, 11, 13, 0)));
        // dimanche 12.01.2025 : non configure -> ferme
        assertFalse(horaires.estOuvert(LocalDateTime.of(2025, 1, 12, 10, 0)));
    }
}
