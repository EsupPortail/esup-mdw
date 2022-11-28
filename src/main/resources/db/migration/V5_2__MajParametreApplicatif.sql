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

-- Modification description 

UPDATE `PREFERENCES_APPLICATION` set `PREF_DESC` = 'Autoriser les enseignants à imprimer les certificats de scolarité.' WHERE `PREF_ID` = 'certScolAutoriseEnseignant';
