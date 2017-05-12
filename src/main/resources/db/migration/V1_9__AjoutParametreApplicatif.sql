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

INSERT INTO `PREFERENCES_APPLICATION_CATEGORIE` (`CAT_ID`, `CAT_DESC`, `ORDRE`) VALUES 
(9,'Sécurité sociale',9);

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`, `CAT_ID`, `VALEUR`) VALUES 
('attestationAffiliationSSO','Edition PDF de l''attestation d''affiliation à la sécu : true pour l''activer, false sinon','BOOLEAN',9, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`, `CAT_ID`, `VALEUR`) VALUES 
('attestSsoAutorisePersonnel','Autoriser les enseignants à éditer le PDF de l''attestation d''affiliation à la sécu : true pour l''activer, false sinon','BOOLEAN',9, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`, `CAT_ID`, `VALEUR`) VALUES 
('afficherBoutonAttestSsoNouvelleLigne','Affichage du bouton de téléchargement de l''attestation sécu sur une nouvelle ligne : true pour l''activer, false sinon','BOOLEAN',9, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`, `CAT_ID`, `VALEUR`) VALUES 
('quittanceSSO','Edition PDF de versement des droits universitaires : true pour l''activer, false sinon','BOOLEAN',9, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`, `CAT_ID`, `VALEUR`) VALUES 
('quittanceSsoAutorisePersonnel','Autoriser les enseignants à éditer le PDF de versement des droits universitaires : true pour l''activer, false sinon','BOOLEAN',9, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`, `CAT_ID`, `VALEUR`) VALUES 
('afficherBoutonQuittanceSsoNouvelleLigne','Affichage du bouton de téléchargement du versement des droits universitaires sur une nouvelle ligne : true pour l''activer, false sinon','BOOLEAN',9, 'false');

UPDATE `PREFERENCES_APPLICATION_CATEGORIE` SET ORDRE=6 where CAT_ID=5;

UPDATE `PREFERENCES_APPLICATION_CATEGORIE` SET ORDRE=7 where CAT_ID=6;

UPDATE `PREFERENCES_APPLICATION_CATEGORIE` SET ORDRE=8 where CAT_ID=7;

UPDATE `PREFERENCES_APPLICATION_CATEGORIE` SET ORDRE=9 where CAT_ID=8;

UPDATE `PREFERENCES_APPLICATION_CATEGORIE` SET ORDRE=5 where CAT_ID=9;

