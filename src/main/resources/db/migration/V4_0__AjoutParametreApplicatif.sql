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

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`, `VALEUR`) VALUES
('quittancePdfSignature','true pour ajouter la signature sur la quittance', 'BOOLEAN', 10, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`, `VALEUR`) VALUES
('quittanceCodeSignataire','Code du signataire des quittances', 'STRING', 10, '');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`, `VALEUR`) VALUES
('quittanceSignatureTampon','true pour utiliser la signature avec tampon pour la quittance', 'BOOLEAN', 10, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`, `VALEUR`) VALUES
('quittanceDescSignataire','Description/fonction du signataire. Si vide, utilisation de QUA_SIG et NOM_SIG de la table SIGNATAIRE', 'STRING', 10, '');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`, `VALEUR`) VALUES
('quittancePdfPositionSignature','Position de la signature sur la quittance', 'STRING', 10, '350-200');







