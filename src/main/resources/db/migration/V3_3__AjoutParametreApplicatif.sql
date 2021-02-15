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

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`, `CAT_ID`, `VALEUR`) VALUES
('affECTSIPEtudiant', 'Affichage ou non des informations crédits ECTS via le détail de l''inscription pégagogique','BOOLEAN',2, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`, `CAT_ID`, `VALEUR`) VALUES
('masqueECTSIPEtudiant', 'Masquer par défaut la colonne ECTS du détail de l''IP si elle est affichée (affECTSIPEtudiant): true pour masquer, false pour afficher','BOOLEAN',2, 'false');







