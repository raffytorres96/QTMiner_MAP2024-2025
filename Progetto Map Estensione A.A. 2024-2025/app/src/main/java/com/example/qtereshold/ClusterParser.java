package com.example.qtereshold;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClusterParser {

    // Regex per trovare un cluster (con opzioni DOT_MATCHES_ALL e MULTILINE)
    private static final Pattern clusterRegex = Pattern.compile(
            "(?m)^\\s*(\\d+):\\s*Centroid=\\((.*?)\\)\\s*\\R(.*?)(?=^\\s*\\d+:\\s*Centroid=|\\z)",
            Pattern.DOTALL | Pattern.MULTILINE
    );

    // Regex per trovare la distanza media
    private static final Pattern avgRegex = Pattern.compile("AvgDistance\\s*=\\s*([0-9.+-Ee]+)");

    /**
     * Esegue il parsing della stringa grezza ricevuta dal server.
     * @param text La stringa di output del server.
     * @return Una lista di oggetti ParsedCluster.
     */
    public static List<ParsedCluster> parse(String text) {
        if (text == null || text.trim().isEmpty() || text.startsWith("Errore:") || text.startsWith("ERRORE:")) {
            return new ArrayList<>(); // Ritorna lista vuota
        }

        List<ParsedCluster> results = new ArrayList<>();
        Matcher m = clusterRegex.matcher(text);

        while (m.find()) {
            try {
                int id = Integer.parseInt(m.group(1));
                String centroid = m.group(2).trim();
                String body = m.group(3);

                List<String> examples = new ArrayList<>();
                Double avgDistance = null;

                // Estrai esempi
                String[] lines = body.split("\\R"); // Dividi per righe
                boolean examplesStarted = false;
                for (String line : lines) {
                    String trimmedLine = line.trim();
                    if (trimmedLine.toLowerCase().startsWith("examples")) {
                        examplesStarted = true;
                        continue;
                    }
                    if (trimmedLine.toLowerCase().startsWith("avgdistance")) {
                        examplesStarted = false;
                        // Estrai avgDistance
                        Matcher avgMatcher = avgRegex.matcher(trimmedLine);
                        if (avgMatcher.find()) {
                            avgDistance = Double.parseDouble(avgMatcher.group(1));
                        }
                        continue;
                    }
                    if (examplesStarted && !trimmedLine.isEmpty() && !trimmedLine.startsWith("…")) {
                        examples.add(trimmedLine);
                    }
                }

                results.add(new ParsedCluster(id, centroid, examples, avgDistance));

            } catch (Exception e) {
                // Ignora un cluster se il parsing fallisce
                System.err.println("Errore parsing cluster: " + e.getMessage());
            }
        }
        return results;
    }
}