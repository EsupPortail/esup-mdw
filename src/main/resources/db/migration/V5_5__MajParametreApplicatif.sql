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

-----------------------------------------------------------
-- Ajout de parametres dans l'onglet Extractions
-----------------------------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('logoUniversitePdfPortraitPosition', 'Positionnement (à gauche) du logo sur les PDF en mode portrait (certificat, quittance, sso)', 'STRING','8','100-750');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('notesPDFLogoUniversitePosition', 'Positionnement (à droite) du logo sur les PDF des notes en portrait', 'STRING','8','560-760');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES
('logoUniversitePdfPaysagePosition', 'Position du logo sur les PDF en mode paysage (calendrier, trombinoscope, notes)', 'STRING','8','800-528');


