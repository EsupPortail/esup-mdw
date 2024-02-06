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


-- Ajout catégorie PDF
INSERT INTO `PREFERENCES_APPLICATION_CATEGORIE` (`CAT_ID`, `CAT_DESC`, `ORDRE`) VALUES (11, 'PDF', 11);

-----------------------------------------------------------
-- Ajout de parametres pour les informations annuelles
-----------------------------------------------------------
INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('calendrierSignature', 'Signer le pdf du calendrier (voir config pdf.sign dans context.xml)', 'BOOLEAN', 11, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('certificatSignature', 'Signer le pdf du certificat (voir config pdf.sign dans context.xml)', 'BOOLEAN', 11, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('quittanceSignature', 'Signer le pdf de la quittance (voir config pdf.sign dans context.xml)', 'BOOLEAN', 11, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('resumeNoteSignature', 'Signer le pdf du resumé des notes (voir config pdf.sign dans context.xml)', 'BOOLEAN', 11, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('detailNoteSignature', 'Signer le pdf du détail des notes (voir config pdf.sign dans context.xml)', 'BOOLEAN', 11, 'false');



INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('calendrierSignatureAlt', 'Signature compatible ALT pour le calendrier (voir config pdf.sign.alt.tsa dans context.xml)', 'BOOLEAN', 11, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('certificatSignatureAlt', 'Signature compatible ALT pour le certificat (voir config pdf.sign.alt.tsa dans context.xml)', 'BOOLEAN', 11, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('quittanceSignatureAlt', 'Signature compatible ALT pour la quittance (voir config pdf.sign.alt.tsa dans context.xml)', 'BOOLEAN', 11, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('resumeNoteSignatureAlt', 'Signature compatible ALT pour le resumé des notes (voir config pdf.sign.alt.tsa dans context.xml)', 'BOOLEAN', 11, 'false');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('detailNoteSignatureAlt', 'Signature compatible ALT pour le détail des notes (voir config pdf.sign.alt.tsa dans context.xml)', 'BOOLEAN', 11, 'false');



INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('calendrierSignatureAltPosition', 'Position de la signature pour le calendrier (coordonnées coin inférieur gauche puis coin inférieur droit). Laisser vide pour ne pas avoir de signature visible', 'STRING', 11, '100-100-200-200');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('certificatSignatureAltPosition', 'Position de la signature pour le calendrier (coordonnées coin inférieur gauche puis coin inférieur droit). Laisser vide pour ne pas avoir de signature visible', 'STRING', 11, '100-100-200-200');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('quittanceSignatureAltPosition', 'Position de la signature pour le calendrier (coordonnées coin inférieur gauche puis coin inférieur droit). Laisser vide pour ne pas avoir de signature visible', 'STRING', 11, '100-100-200-200');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('resumeNoteSignatureAltPosition', 'Position de la signature pour le calendrier (coordonnées coin inférieur gauche puis coin inférieur droit). Laisser vide pour ne pas avoir de signature visible', 'STRING', 11, '100-100-200-200');

INSERT INTO `PREFERENCES_APPLICATION` (`PREF_ID`, `PREF_DESC`, `TYPE`,`CAT_ID`,`VALEUR`) VALUES 
('detailNoteSignatureAltPosition', 'Position de la signature pour le calendrier (coordonnées coin inférieur gauche puis coin inférieur droit). Laisser vide pour ne pas avoir de signature visible', 'STRING', 11, '100-100-200-200');


