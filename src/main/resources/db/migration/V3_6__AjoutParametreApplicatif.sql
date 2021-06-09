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

-- Déplacement des onglets pour faire de la place au futur onglet Quittance
UPDATE `PREFERENCES_APPLICATION_CATEGORIE` SET ORDRE=6 where CAT_ID=9;

UPDATE `PREFERENCES_APPLICATION_CATEGORIE` SET ORDRE=7 where CAT_ID=5;

UPDATE `PREFERENCES_APPLICATION_CATEGORIE` SET ORDRE=8 where CAT_ID=6;

UPDATE `PREFERENCES_APPLICATION_CATEGORIE` SET ORDRE=9 where CAT_ID=7;

UPDATE `PREFERENCES_APPLICATION_CATEGORIE` SET ORDRE=10 where CAT_ID=8;

-- Ajout catégorie Quittance
INSERT INTO `PREFERENCES_APPLICATION_CATEGORIE` (`CAT_ID`, `CAT_DESC`, `ORDRE`) VALUES ('10', 'Quittance', '5');

-- Déplacement des parametre de quittance dans l'onglet quittance
UPDATE `PREFERENCES_APPLICATION` SET `CAT_ID`=10 WHERE `PREF_ID`='quittanceDossierNonValide';

UPDATE `PREFERENCES_APPLICATION` SET `CAT_ID`=10 WHERE `PREF_ID`='quittanceDroitsPayes';

UPDATE `PREFERENCES_APPLICATION` SET `CAT_ID`=10 WHERE `PREF_ID`='quittanceDroitsPayesAutorisePersonnel';

UPDATE `PREFERENCES_APPLICATION` SET `CAT_ID`=10 WHERE `PREF_ID`='quittanceDroitsPayesUsageEtatCivil';

UPDATE `PREFERENCES_APPLICATION` SET `CAT_ID`=10 WHERE `PREF_ID`='afficherBoutonQuittanceNouvelleLigne';












