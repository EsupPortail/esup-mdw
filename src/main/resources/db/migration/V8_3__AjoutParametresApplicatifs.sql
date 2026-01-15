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


-- ---------------------------------------------------------
-- Maj parametre affInfosContactGestionnaire => affContactTelGestionnaire
-- ---------------------------------------------------------
UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'affContactTelGestionnaire'
WHERE `PREF_ID` = 'affInfosContactGestionnaire';

-- ---------------------------------------------------------
-- Maj parametre affInfosContactEnseignant => affContactTelEnseignant
-- ---------------------------------------------------------
UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'affContactTelEnseignant'
WHERE `PREF_ID` = 'affInfosContactEnseignant';


-- ---------------------------------------------------------
-- Ajout de parametre affContactMailGestionnaire
-- ---------------------------------------------------------
INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES
    ('affContactMailGestionnaire', 'Afficher les infos de contact (mail) aux gestionnaires : true pour activer, false sinon', 'BOOLEAN','5','false');

-- ---------------------------------------------------------
-- Ajout de parametre affContactMailEnseignant
-- ---------------------------------------------------------
INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES
    ('affContactMailEnseignant', 'Afficher les infos de contact (mail) aux enseignants : true pour activer, false sinon', 'BOOLEAN','5','false');

