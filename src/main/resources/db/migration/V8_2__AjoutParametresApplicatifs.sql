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



-----------------------------------------------------------
-- Ajout de parametre affMentionElpEtudiant
-----------------------------------------------------------
INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES
('affMentionElpEtudiant', 'Affichage ou non de la mention sur la page des détails des notes', 'BOOLEAN','2','false');

-----------------------------------------------------------
-- Ajout de parametre affDateNaissancePdfNotesPortrait
-----------------------------------------------------------
INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES
('affDateNaissancePdfNotesPortrait', 'Affichage ou non de la date de naissance de l''etudiant dans le pdf des notes au format portrait', 'BOOLEAN','8','false');

-----------------------------------------------------------
-- Ajout de parametre affNNEPdfNotesPortrait
-----------------------------------------------------------
INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES
    ('affNNEPdfNotesPortrait', 'Affichage ou non du numéro NNE de l''etudiant dans le pdf des notes au format portrait', 'BOOLEAN','8','false');