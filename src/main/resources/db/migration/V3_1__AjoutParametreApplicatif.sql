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
('masqueSession2Vide', 'Masquer les colonnes Session2 et Resultat si vides: true pour l''activer, false sinon','BOOLEAN',2, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`, `CAT_ID`, `VALEUR`) VALUES
('renommeSession1Unique', 'Renommer la colonne Session1 si la colonne Session2 est vide : true pour l''activer, false sinon','BOOLEAN',2, 'false');





