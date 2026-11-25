package ch.backoffice.backoffice.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Horaires d'ouverture de la boulangerie, charges depuis config.yml.
 * Un jour absent du fichier (ex: dimanche) signifie ferme.
 */
public class Horaires {

    public static class Plage {
        public final LocalTime ouverture;
        public final LocalTime fermeture;

        public Plage(LocalTime ouverture, LocalTime fermeture) {
            this.ouverture = ouverture;
            this.fermeture = fermeture;
        }
    }

    private final Map<DayOfWeek, Plage> plages;

    public Horaires(Map<DayOfWeek, Plage> plages) {
        this.plages = Collections.unmodifiableMap(new EnumMap<DayOfWeek, Plage>(plages));
    }

    public static Horaires charger(Path chemin) throws IOException {
        return parse(new String(Files.readAllBytes(chemin), StandardCharsets.UTF_8));
    }

    /**
     * Parse le format simple de config.yml :
     * horaires:
     *   lundi:
     *     ouverture: "06:00:00"
     *     fermeture: "17:00:00"
     */
    public static Horaires parse(String yaml) {
        Map<DayOfWeek, Plage> plages = new EnumMap<DayOfWeek, Plage>(DayOfWeek.class);
        String jourCourant = null;
        LocalTime ouverture = null;
        LocalTime fermeture = null;

        for (String ligneBrute : yaml.split("\r?\n")) {
            String ligne = sansCommentaire(ligneBrute).trim();
            if (ligne.isEmpty()) {
                continue;
            }
            int indent = indentation(ligneBrute);

            if (indent <= 2) {
                if (jourCourant != null) {
                    plages.put(jourEnDayOfWeek(jourCourant), new Plage(exige(jourCourant, "ouverture", ouverture),
                            exige(jourCourant, "fermeture", fermeture)));
                    jourCourant = null;
                }
                if (indent == 2) {
                    jourCourant = ligne.replace(":", "").trim();
                    ouverture = null;
                    fermeture = null;
                }
            } else if (jourCourant != null) {
                int deuxPoints = ligne.indexOf(':');
                if (deuxPoints < 0) {
                    continue;
                }
                String cle = ligne.substring(0, deuxPoints).trim();
                String valeur = ligne.substring(deuxPoints + 1).trim().replace("\"", "").replace("'", "");
                LocalTime heure = LocalTime.parse(valeur);
                if ("ouverture".equals(cle)) {
                    ouverture = heure;
                } else if ("fermeture".equals(cle)) {
                    fermeture = heure;
                }
            }
        }
        if (jourCourant != null) {
            plages.put(jourEnDayOfWeek(jourCourant), new Plage(exige(jourCourant, "ouverture", ouverture),
                    exige(jourCourant, "fermeture", fermeture)));
        }
        return new Horaires(plages);
    }

    /**
     * @return true si la boulangerie est ouverte a la date/heure indiquee
     *         (ouverture incluse, fermeture exclue).
     */
    public boolean estOuvert(LocalDateTime dateHeure) {
        Plage plage = plages.get(dateHeure.getDayOfWeek());
        if (plage == null) {
            return false;
        }
        LocalTime heure = dateHeure.toLocalTime();
        return !heure.isBefore(plage.ouverture) && heure.isBefore(plage.fermeture);
    }

    private static LocalTime exige(String jour, String cle, LocalTime valeur) {
        if (valeur == null) {
            throw new IllegalArgumentException("config.yml : " + cle + " manquante pour " + jour);
        }
        return valeur;
    }

    private static DayOfWeek jourEnDayOfWeek(String nom) {
        switch (nom.toLowerCase(Locale.ROOT)) {
            case "lundi":
                return DayOfWeek.MONDAY;
            case "mardi":
                return DayOfWeek.TUESDAY;
            case "mercredi":
                return DayOfWeek.WEDNESDAY;
            case "jeudi":
                return DayOfWeek.THURSDAY;
            case "vendredi":
                return DayOfWeek.FRIDAY;
            case "samedi":
                return DayOfWeek.SATURDAY;
            case "dimanche":
                return DayOfWeek.SUNDAY;
            default:
                throw new IllegalArgumentException("Jour inconnu dans config.yml : " + nom);
        }
    }

    private static String sansCommentaire(String ligne) {
        int idx = ligne.indexOf('#');
        return idx >= 0 ? ligne.substring(0, idx) : ligne;
    }

    private static int indentation(String ligne) {
        int i = 0;
        while (i < ligne.length() && ligne.charAt(i) == ' ') {
            i++;
        }
        return i;
    }
}
