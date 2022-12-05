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
-- Ajout de parametres dans l'onglet Notes et résultats
-----------------------------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('affNotesEnseignant', 'Afficher l''onglet notes et résultats aux enseignants : true pour activer, false sinon', 'BOOLEAN','2','true');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('affNotesEtudiant', 'Afficher l''onglet notes et résultats aux étudiants : true pour activer, false sinon', 'BOOLEAN','2','true');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('affNotesGestionnaire', 'Afficher l''onglet notes et résultats aux gestionnaires : true pour activer, false sinon', 'BOOLEAN','2','true');





