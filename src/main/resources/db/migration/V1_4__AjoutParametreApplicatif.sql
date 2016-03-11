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


INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `VALEUR`) VALUES
('notesNombreAnneesExtractionApogee', 'Indique le nombre d''années couvert par l''extraction Apogée. Utile uniquement si sourceResultats=Apogee-extraction', '1');


INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `VALEUR`) VALUES
('notesPDFFormatPortrait', 'Edition des pdf des notes au format portrait : true pour activer, false sinon', 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `VALEUR`) VALUES
('notesPDFSignature', 'Ajout de la signature sur le pdf du détail des notes : true pour activer, false sinon. Actif uniquement si sourceResultats=Apogee-extraction', 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `VALEUR`) VALUES
('notesPDFLieuEdition', 'Lieu d''edition des pdf de relevé de notes. Actif uniquement si notesPDFSignature=true', '');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `VALEUR`) VALUES
('affAdressesEnseignants', 'Afficher les adresses aux enseignants : true pour activer, false sinon', 'true');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `VALEUR`) VALUES
('affInfosAnnuellesEnseignants', 'Afficher les infos annuelles aux enseignants : true pour activer, false sinon', 'true');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `VALEUR`) VALUES
('affInfosContactEnseignants', 'Afficher les infos de contact (mail perso) aux enseignants : true pour activer, false sinon', 'true');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `VALEUR`) VALUES
('affCalendrierEpreuvesEtudiants', 'Afficher le calendrier des épreuves aux étudiants : true pour activer, false sinon', 'true');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `VALEUR`) VALUES
('affCalendrierEpreuvesEnseignants', 'Afficher le calendrier des épreuves aux enseignants : true pour activer, false sinon', 'true');