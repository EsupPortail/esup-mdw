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



------------------------------------
-- temoinEtatIaeNotesGestionnaire
------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('temoinEtatIaeNotesGestionnaire', 'Etat (E,A,R) des IAE affichées dans les résultats aux gestionnaires', 'STRING','2','E');

UPDATE `PREFERENCES_APPLICATION` set `VALEUR` = (select `VALEUR` FROM  `PREFERENCES_APPLICATION` where `PREF_ID`='temoinEtatIaeNotesEnseignant')
WHERE `PREF_ID` = 'temoinEtatIaeNotesGestionnaire';

------------------------------------
-- temoinNotesGestionnaire
------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('temoinNotesGestionnaire', 'Etat de validation des notes affichees aux gestionnaires', 'STRING','2','AET');

UPDATE `PREFERENCES_APPLICATION` set `VALEUR` = (select `VALEUR` FROM  `PREFERENCES_APPLICATION` where `PREF_ID`='temoinNotesEnseignant')
WHERE `PREF_ID` = 'temoinNotesGestionnaire';

----------------------------------------
-- affCalendrierEpreuvesGestionnaire
----------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('affCalendrierEpreuvesGestionnaire', 'Afficher le calendrier des épreuves aux gestionnaires : true pour activer, false sinon', 'BOOLEAN','3','true');

UPDATE `PREFERENCES_APPLICATION` set `VALEUR` = (select `VALEUR` FROM  `PREFERENCES_APPLICATION` where `PREF_ID`='affCalendrierEpreuvesEnseignants')
WHERE `PREF_ID` = 'affCalendrierEpreuvesGestionnaire';


------------------------------------
-- certScolAutoriseGestionnaire
------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('certScolAutoriseGestionnaire', 'Autoriser les gestionnaires à imprimer les certificats de scolarité.', 'BOOLEAN','4','false');

UPDATE `PREFERENCES_APPLICATION` set `VALEUR` = (select `VALEUR` FROM  `PREFERENCES_APPLICATION` where `PREF_ID`='certScolAutoriseEnseignant')
WHERE `PREF_ID` = 'certScolAutoriseGestionnaire';


------------------------------------
-- quittanceDroitsPayesAutoriseGestionnaire
------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('quittanceDroitsPayesAutoriseGestionnaire', 'Autoriser les gestionnaires à éditer le PDF de versement des droits universitaires : true pour l''activer, false sinon', 'BOOLEAN','10','false');

UPDATE `PREFERENCES_APPLICATION` set `VALEUR` = (select `VALEUR` FROM  `PREFERENCES_APPLICATION` where `PREF_ID`='quittanceDroitsPayesAutoriseEnseignant')
WHERE `PREF_ID` = 'quittanceDroitsPayesAutoriseGestionnaire';


------------------------------------
-- attestSsoAutoriseGestionnaire
------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('attestSsoAutoriseGestionnaire', 'Autoriser les gestionnaires à éditer le PDF de l''attestation d''affiliation à la sécu : true pour l''activer, false sinon', 'BOOLEAN','9','false');

UPDATE `PREFERENCES_APPLICATION` set `VALEUR` = (select `VALEUR` FROM  `PREFERENCES_APPLICATION` where `PREF_ID`='attestSsoAutoriseEnseignant')
WHERE `PREF_ID` = 'attestSsoAutoriseGestionnaire';


------------------------------------
-- affAmenagementGestionnaire
------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('affAmenagementGestionnaire', 'Si affInfosAnnuellesGestionnaire = true, true pour afficher si l''étudiant a un aménagement d''étude, false sinon', 'BOOLEAN','5','true');

UPDATE `PREFERENCES_APPLICATION` set `VALEUR` = (select `VALEUR` FROM  `PREFERENCES_APPLICATION` where `PREF_ID`='affAmenagementEnseignants')
WHERE `PREF_ID` = 'affAmenagementGestionnaire';


------------------------------------
-- affBoursierGestionnaire
------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('affBoursierGestionnaire', 'Si affInfosAnnuellesGestionnaire = true, true pour afficher si l''étudiant est boursier, false sinon', 'BOOLEAN','5','true');

UPDATE `PREFERENCES_APPLICATION` set `VALEUR` = (select `VALEUR` FROM  `PREFERENCES_APPLICATION` where `PREF_ID`='affBoursierEnseignants')
WHERE `PREF_ID` = 'affBoursierGestionnaire';


------------------------------------
-- affInfosAnnuellesGestionnaire
------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('affInfosAnnuellesGestionnaire', 'Afficher les infos annuelles aux gestionnaires : true pour activer, false sinon', 'BOOLEAN','5','true');

UPDATE `PREFERENCES_APPLICATION` set `VALEUR` = (select `VALEUR` FROM  `PREFERENCES_APPLICATION` where `PREF_ID`='affInfosAnnuellesEnseignants')
WHERE `PREF_ID` = 'affInfosAnnuellesGestionnaire';

------------------------------------
-- affInfosContactGestionnaire
------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('affInfosContactGestionnaire', 'Afficher les infos de contact (tel portable) aux gestionnaires : true pour activer, false sinon', 'BOOLEAN','5','true');

UPDATE `PREFERENCES_APPLICATION` set `VALEUR` = (select `VALEUR` FROM  `PREFERENCES_APPLICATION` where `PREF_ID`='affInfosContactEnseignants')
WHERE `PREF_ID` = 'affInfosContactGestionnaire';


------------------------------------
-- affSalarieGestionnaire
------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('affSalarieGestionnaire', 'Si affInfosAnnuellesEnseignants = true, true pour afficher si l''étudiant est salarié, false sinon', 'BOOLEAN','5','true');

UPDATE `PREFERENCES_APPLICATION` set `VALEUR` = (select `VALEUR` FROM  `PREFERENCES_APPLICATION` where `PREF_ID`='affSalarieEnseignants')
WHERE `PREF_ID` = 'affSalarieGestionnaire';


------------------------------------
-- affAdressesGestionnaire
------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('affAdressesGestionnaire', 'Afficher les adresses aux enseignants : true pour activer, false sinon', 'BOOLEAN','6','true');

UPDATE `PREFERENCES_APPLICATION` set `VALEUR` = (select `VALEUR` FROM  `PREFERENCES_APPLICATION` where `PREF_ID`='affAdressesEnseignants')
WHERE `PREF_ID` = 'affAdressesGestionnaire';


------------------------------------
-- Renommage paramètres
------------------------------------

UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'affAdressesEnseignant' WHERE `PREF_ID` = 'affAdressesEnseignants';

UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'affAmenagementEnseignant' WHERE `PREF_ID` = 'affAmenagementEnseignants';

UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'affBoursierEnseignant' WHERE `PREF_ID` = 'affBoursierEnseignants';

UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'affCalendrierEpreuvesEnseignant' WHERE `PREF_ID` = 'affCalendrierEpreuvesEnseignants';

UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'affCalendrierEpreuvesEtudiant' WHERE `PREF_ID` = 'affCalendrierEpreuvesEtudiants';

UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'affInfosAnnuellesEnseignant' WHERE `PREF_ID` = 'affInfosAnnuellesEnseignants';

UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'affInfosContactEnseignant' WHERE `PREF_ID` = 'affInfosContactEnseignants';

UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'affSalarieEnseignant' WHERE `PREF_ID` = 'affSalarieEnseignants';





