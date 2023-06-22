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
-- Ajout de parametres pour les informations annuelles
-----------------------------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('nbAnneesInfosAnnuelles', 'Nombre d''années dont on affiche les informations annuelles : 1 pour année active uniquement, 2 pour n et n-1, etc.', 'STRING','5','1');





