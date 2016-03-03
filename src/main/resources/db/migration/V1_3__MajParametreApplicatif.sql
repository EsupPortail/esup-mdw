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

update preferences_application set PREF_DESC='Liste des codes CGE pour lesquels la generation de certificat est desactivee.' where PREF_ID='certScolCGEDesactive';

update preferences_application set PREF_DESC='Liste des codes de types de diplomes pour lesquels la generation de certificat est desactivee.' where PREF_ID='certScolTypDiplomeDesactive';

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `VALEUR`) VALUES
('certScolStatutDesactive', 'Liste des codes statut pour lesquels la generation de certificat est desactivee.', '');
