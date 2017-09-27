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
('certificatScolariteEditionCarte','Edition du certificat de scolarite si le temoin d edition de la carte d etudiant est coche dans Apogee: true pour l''activer, false sinon','BOOLEAN',4, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`, `CAT_ID`, `VALEUR`) VALUES
('affResultatsAdmissibilite', 'Afficher le resultat meme dans le cas de l admissibilite: true pour l''activer, false sinon','BOOLEAN',2, 'false');


