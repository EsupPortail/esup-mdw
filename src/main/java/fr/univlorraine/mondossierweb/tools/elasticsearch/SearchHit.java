package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.util.Map;

/** Résultat de recherche normalisé, indépendant de la version d'Elasticsearch. */
public record SearchHit(Map<String, Object> source, String type) {
}
