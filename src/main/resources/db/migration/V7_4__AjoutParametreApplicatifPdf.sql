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
-- Ajout de parametres affDatenaissanceEnseignant et affDatenaissanceGestionnaire
-----------------------------------------------------------
INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('indentNiveauElpMobile', 'Indentation des ELPs en remplacement des indicateurs de niveau colorés sur l''affichage mobile du détail des notes', 'BOOLEAN','2','true');

