-- phpMyAdmin SQL Dump
-- version 4.0.10.10
-- http://www.phpmyadmin.net
--
-- Version du serveur: 5.5.34-log
-- Version de PHP: 5.3.3


-- ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
-- Base de donnees: `mdw`
--

-- --------------------------------------------------------



-- creation categorie 'Autres'
INSERT INTO `PREFERENCES_APPLICATION_CATEGORIE` (`CAT_ID`, `CAT_DESC`, `ORDRE`)
VALUES ('12', 'Autres', '12');

-----------------------------------------------------------
-- Ajout de parametres affInscriptionsAutreCursus
-----------------------------------------------------------
INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES
    ('affInscriptionsAutreCursus', 'Afficher les inscriptions dans d''autres cursus.', 'BOOLEAN','12','true');

-----------------------------------------------------------
-- Déplacement des parametres logoUniversiteEns, logoUniversiteEtu et logoUniversiteMobile dans "Autres"
-----------------------------------------------------------
UPDATE `PREFERENCES_APPLICATION` SET `CAT_ID` = 12 where `PREF_ID` = 'logoUniversiteEns';

UPDATE `PREFERENCES_APPLICATION` SET `CAT_ID` = 12 where `PREF_ID` = 'logoUniversiteEtu';

UPDATE `PREFERENCES_APPLICATION` SET `CAT_ID` = 12 where `PREF_ID` = 'logoUniversiteMobile';

-----------------------------------------------------------
-- Ajout de parametres modificationAdresseAnnuelle
-----------------------------------------------------------
INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES
    ('modificationAdresseAnnuelle', 'Si modificationAdresses=true, indiquer false pour n''autoriser que la modification de l''adresse fixe', 'BOOLEAN','6','true');

