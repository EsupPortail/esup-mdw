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
('notesPDFUsageEtatCivil', 'Utiliser cod_sex_eta_civ et lib_pr_eta_civ si Tem_Pr_Usage=''O'' à la place des données d''usage: true pour l''activer, false sinon','BOOLEAN',8, 'false');


INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`, `CAT_ID`, `VALEUR`) VALUES
('quittanceDroitsPayesUsageEtatCivil', 'Utiliser cod_sex_eta_civ et lib_pr_eta_civ si Tem_Pr_Usage=''O'' à la place des données d''usage: true pour l''activer, false sinon','BOOLEAN',9, 'false');





