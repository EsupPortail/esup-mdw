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
-- Changement id des paramètres
-----------------------------------------------------------

UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'headerPdf' WHERE `PREF_ID` = 'certScolHeaderUniv';

UPDATE `PREFERENCES_APPLICATION` set `PREF_ID` = 'footerPdf' WHERE `PREF_ID` = 'certScolFooter';

-----------------------------------------------------------
-- Changement d'onglet des paramètres
-----------------------------------------------------------

UPDATE `PREFERENCES_APPLICATION` set `CAT_ID` = '8' WHERE `PREF_ID` = 'headerPdf';

UPDATE `PREFERENCES_APPLICATION` set `CAT_ID` = '8' WHERE `PREF_ID` = 'footerPdf';

-----------------------------------------------------------
-- Changement de la description des paramètres
-----------------------------------------------------------

UPDATE `PREFERENCES_APPLICATION` set `PREF_DESC` = 'Path ou URL vers le header pour le certificat, la quittance et l''attestation SSO (1240x176). Laisser vide pour ne pas importer de logo' WHERE `PREF_ID` = 'headerPdf';

UPDATE `PREFERENCES_APPLICATION` set `PREF_DESC` = 'Path ou URL vers le footer pour le certificat, la quittance et l''attestation SSO (1240x286). Laisser vide pour ne pas importer de logo' WHERE `PREF_ID` = 'footerPdf';

UPDATE `PREFERENCES_APPLICATION` set `PREF_DESC` = 'Path ou URL vers le logo de l''université pour le pdf. A laisser vide pour ne pas importer de logo.' WHERE `PREF_ID` = 'logoUniversitePdf';

-- certificatScolaritePdfPositionSignature est maintenant utilisé même sans tampon. Il donc le mettre à vide pour obtenir le comportement initial si non utilisation du tampon
UPDATE `PREFERENCES_APPLICATION` set `PREF_DESC` = 'Position de la signature sur le certificat de scolarité (ex : 350-150). Laisser vide pour utiliser le positionnement par défaut, aligné à droite.' WHERE `PREF_ID` = 'certificatScolaritePdfPositionSignature';


-----------------------------------------------------------
-- Ajout de parametres dans l'onglet Extractions
-----------------------------------------------------------


INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('notePDFSignatureDimension', 'Nombre entier indiquant la largeur de la signature sur le PDF des notes', 'STRING','8','80');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('dimensionPDFSignature', 'Nombre entier indiquant la largeur de la signature sur les PDF signés', 'STRING','8','130');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('logoUniversitePdfDimension', 'Nombre entier indiquant la largeur du logo sur les PDF', 'STRING','8','95');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('dimensionPDFHeaderFooter', 'Nombre entier indiquant la largeur des header et footer sur les PDF', 'STRING','8','600');

-----------------------------------------------------------
-- Ajout de parametres dans l'onglet Certificat de scolarité
-----------------------------------------------------------

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('certScolTamponDimension', 'Nombre entier indiquant la largeur du tampon sur le PDF', 'STRING','4','100');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('certScolTamponPosition', 'Position du tampon sur le certificat de scolarité', 'STRING','4','415-200');


