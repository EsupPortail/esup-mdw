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


update `PREFERENCES_APPLICATION` set PREF_DESC = 'Position de la signature pour le certificat de scolarité (coordonnées coin inférieur gauche puis coin inférieur droit). Laisser vide pour ne pas avoir de signature visible' where PREF_ID = 'certificatSignatureAltPosition';

update `PREFERENCES_APPLICATION` set PREF_DESC = 'Position de la signature pour la quittance (coordonnées coin inférieur gauche puis coin inférieur droit). Laisser vide pour ne pas avoir de signature visible' where PREF_ID = 'quittanceSignatureAltPosition';

update `PREFERENCES_APPLICATION` set PREF_DESC = 'Position de la signature pour le pdf de résumé des notes (coordonnées coin inférieur gauche puis coin inférieur droit). Laisser vide pour ne pas avoir de signature visible' where PREF_ID = 'resumeNoteSignatureAltPosition';

update `PREFERENCES_APPLICATION` set PREF_DESC = 'Position de la signature pour le pdf du détail des notes (coordonnées coin inférieur gauche puis coin inférieur droit). Laisser vide pour ne pas avoir de signature visible' where PREF_ID = 'detailNoteSignatureAltPosition';

