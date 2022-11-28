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

-- Renommage de paramètres 

UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'attestSsoAutoriseEnseignant' WHERE `PREF_ID` = 'attestSsoAutorisePersonnel';

UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'certScolAutoriseEnseignant' WHERE `PREF_ID` = 'certScolAutorisePersonnel';

UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'quittanceDroitsPayesAutoriseEnseignant' WHERE `PREF_ID` = 'quittanceDroitsPayesAutorisePersonnel';
