package org.egov.activity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class JsonFieldMapperGenerator {

    public static void main(String[] args) {
        String inputFile = "src/main/resources/dc_bom_fieldnames.txt";
        String outputFile = "src/main/resources/dc_bom_fieldnames_mapping.json";

        try {
            // 1️⃣ Lire le contenu du fichier texte
            String content = new String(Files.readAllBytes(Paths.get(inputFile)));

            // 2️⃣ Nettoyer et séparer les champs (suppression { } , etc.)
            List<String> fields = Arrays.stream(content
                            .replace("{", "")
                            .replace("}", "")
                            .split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            // 3️⃣ Construire la liste d’objets JSON
            List<Map<String, Object>> mappingList = new ArrayList<>();

            for (String field : fields) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("variable", field);

                Map<String, String> valueMap = new LinkedHashMap<>();
                valueMap.put("path", "$." + field);
                entry.put("value", valueMap);

                mappingList.add(entry);
            }

            // 4️⃣ Sauvegarder dans un fichier JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(outputFile), mappingList);

            System.out.println("✅ Fichier JSON généré avec succès : " + outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

