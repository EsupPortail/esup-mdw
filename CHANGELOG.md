# Changelog

## 3.0.0

Migration majeure de la pile technique :

- **Spring Boot 4.0.5** (Spring Framework 7, Spring Security 7) — l'application était
  auparavant en Spring Framework « pur » sans Boot.
- **Vaadin 25.1 (Flow)** — réécriture de l'UI depuis Vaadin 7/8 compatibility :
  - `Navigator`/`@SpringView` → routing Flow (`@Route`), `Window` → `Dialog`,
    `Table` → `Grid`, `TreeTable` → `TreeGrid` ;
  - UI unique responsive (AppLayout) ; l'**UI mobile Touchkit (`/m`) est supprimée** ;
  - thème SCSS valo-ul remplacé par du CSS statique Lumo (`META-INF/resources/css/styles.css`).
- **Java 21** minimum, **Tomcat 11** (Servlet 6.1) pour le déploiement WAR.
- **Hibernate 7** remplace EclipseLink (plus de static weaving ni de génération de
  persistence.xml au build).
- **Configuration** : le `context.xml` (JNDI Tomcat) est remplacé par un fichier
  `application.yml` externe (voir `application.example.yml`). Les noms des paramètres sont
  conservés (sans le préfixe `context.`). Les datasources se configurent via
  `spring.datasource.mdw.*` et `spring.datasource.apogee.*`.
- **Flyway 11** : nouvelle table `flyway_schema_history` avec baseline 8.3 — l'installation
  doit être à jour en 2.0.x avant migration.
- **Exports PDF** : iText 5 (AGPL) → **OpenPDF** (LGPL), signature électronique conservée
  (BouncyCastle, TSA).
- **MariaDB** : utiliser `jdbc:mariadb://...` (driver MariaDB fourni) ; MySQL reste supporté
  via `jdbc:mysql://...`.
- Statistiques Matomo/Piwik réimplémentées pour Flow (propriétés `piwik.*` conservées).
- Le mode debug logback s'active désormais via le profil Spring `debug`
  (`spring.profiles.active=debug`) au lieu du paramètre `debugMode`.
- Nettoyage : suppression des servlets JMeter/Touchkit, du push d'entités, des composants
  Vaadin 7 utilitaires (BusyIndicator, AutoComplete...), du paramètre `startServletMobile`.

### Impacts exploitation

- **Requêtes personnalisées `apogeeRequest.xml`** : Hibernate est plus strict qu'EclipseLink —
  les requêtes retournant des entités (`Inscrit`, `Examen`...) doivent fournir TOUTES les
  colonnes mappées de l'entité (au besoin via `to_char(null) alias`), et les requêtes
  « Map » (quittances, paiements...) doivent conserver des alias de colonnes non quotés
  (remontés en MAJUSCULES).

- Adapter les serveurs : JDK 21, Tomcat 11, suppression des ressources JNDI, mise en place
  du fichier `application.yml` externe.
- Adapter les jobs CI/CD (Jenkins) : JDK 21, build production `mvn clean package -Pproduction`.
- Vérifier la validation CAS en environnement réel après déploiement.
