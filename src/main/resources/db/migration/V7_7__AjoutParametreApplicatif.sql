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


update `PREFERENCES_APPLICATION` set PREF_ID = 'logoUniversiteEtu' where PREF_ID = 'logoUniversiteDesktop';

-----------------------------------------------------------
-- Ajout de parametres affDatenaissanceEnseignant et affDatenaissanceGestionnaire
-----------------------------------------------------------
INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('logoUniversiteEns', 'Path ou URL vers le logo de l''Université pour le bandeau affiché aux enseignants en vue Desktop', 'STRING','5','');

