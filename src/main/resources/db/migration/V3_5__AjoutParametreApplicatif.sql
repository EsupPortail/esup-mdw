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
('certificatScolariteDossierNonValide', 'true pour proposer le certificat de scolarité même si le dossier d''inscription n''est pas valide (tem_dos_iaa_pj)','BOOLEAN',4, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`, `CAT_ID`, `VALEUR`) VALUES
('quittanceDossierNonValide', 'true pour proposer la quittance même si le dossier d''inscription n''est pas valide (tem_dos_iaa_pj)','BOOLEAN',9, 'false');









