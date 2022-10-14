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
('affBoursierEnseignants','Si affInfosAnnuellesEnseignants = true, true pour afficher si l''étudiant est boursier, false sinon', 'BOOLEAN', 5, 'true');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`, `VALEUR`) VALUES
('affSalarieEnseignants','Si affInfosAnnuellesEnseignants = true, true pour afficher si l''étudiant est salarié, false sinon', 'BOOLEAN', 5, 'true');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`, `VALEUR`) VALUES
('affAmenagementEnseignants','Si affInfosAnnuellesEnseignants = true, true pour afficher si l''étudiant a un aménagement d''étude, false sinon', 'BOOLEAN', 5, 'true');



