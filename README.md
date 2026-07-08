# esup-mdw
Esup MonDossierWeb

Documentation wiki esup-portail : https://www.esup-portail.org/wiki/x/C4BNHg

## Version 3.x — Spring Boot 4 / Vaadin 25

Depuis la version 3.0.0, l'application repose sur Spring Boot 4, Vaadin 25 (Flow) et Java 21.

### Prérequis

- **Java 21** (LTS)
- **Maven 3.9+**
- **Tomcat 11** (Servlet 6.1) pour un déploiement WAR, ou exécution autonome via `mvn spring-boot:run`
- Base MySQL ou MariaDB (base applicative `mdw`), accès Oracle à la base Apogée

### Configuration

La configuration ne passe plus par le `context.xml` de Tomcat (JNDI) mais par un fichier
**`application.yml`** externe (Spring Boot) :

- copier `application.example.yml` en `application.yml` ;
- en développement : à la racine du projet ;
- sur un serveur : dans le répertoire de travail du process, ou n'importe où via
  `-Dspring.config.additional-location=/chemin/application.yml`.

Les valeurs par défaut sont embarquées dans `src/main/resources/application-defaults.yml`.

Points d'attention lors de la migration depuis une version 2.x :

- l'installation doit être **à jour en 2.0.x** (dernière migration Flyway V8_3 appliquée) avant
  le passage en 3.x ; Flyway repart d'une baseline 8.3 dans une nouvelle table
  `flyway_schema_history` (l'ancienne table `schema_version` peut être supprimée ensuite) ;
- pour une base **MariaDB**, utiliser une URL `jdbc:mariadb://...` (le driver MySQL Connector/J 9
  ne fonctionne plus correctement contre MariaDB) ; `jdbc:mysql://...` reste valable pour MySQL ;
- l'UI mobile dédiée (Touchkit, `/m`) a été supprimée : l'application est responsive ;
- les exports PDF utilisent désormais OpenPDF (LGPL) en remplacement d'iText 5.

### Développement

```bash
mvn spring-boot:run
```

### Build de production

```bash
mvn clean package -Pproduction
```

Le WAR produit est « léger » (sans serveur embarqué), à déployer sur Tomcat 11.
