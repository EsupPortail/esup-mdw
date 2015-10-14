-- phpMyAdmin SQL Dump
-- version 4.0.10.10
-- http://www.phpmyadmin.net
--
-- Version du serveur: 5.5.34-log
-- Version de PHP: 5.3.3


--
-- Base de donnees: `mdw`
--

-- --------------------------------------------------------

--
-- Structure de la table `administrateurs`
--

CREATE TABLE IF NOT EXISTS `administrateurs` (
  `LOGIN` varchar(20) NOT NULL,
  PRIMARY KEY (`LOGIN`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



--
-- Structure de la table `favoris`
--

CREATE TABLE IF NOT EXISTS `favoris` (
  `LOGIN` varchar(20) NOT NULL,
  `TYP_FAV` varchar(5) NOT NULL,
  `ID_FAV` varchar(20) NOT NULL,
  PRIMARY KEY (`LOGIN`,`TYP_FAV`,`ID_FAV`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



--
-- Structure de la table `preferences_application`
--

CREATE TABLE IF NOT EXISTS `preferences_application` (
  `PREF_ID` varchar(40) NOT NULL,
  `PREF_DESC` text NOT NULL,
  `VALEUR` varchar(200) NOT NULL,
  PRIMARY KEY (`PREF_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Contenu de la table `preferences_application`
--

INSERT INTO `preferences_application` (`PREF_ID`, `PREF_DESC`, `VALEUR`) VALUES
('affECTSEtudiant', 'Affichage ou non des informations credits ECTS par la page du detail des notes', 'false'),
('afficherRangEtudiant', 'Affichage du rang de l''etudiant', 'false'),
('affMentionEtudiant', 'Affichage ou non de la mention aux diplomes et etapes a la page des notes', 'false'),
('affNumPlaceExamen', 'Affichage du numero de place dans le calendrier des examens : true pour l''activer, false sinon', 'true'),
('applicationActive', 'true si l''application desktop est active, false sinon', 'true'),
('applicationMobileActive', 'true si l''application mobile est active, false sinon', 'true'),
('assistanceContactMail', 'mail de contact pour les enseignants', ''),
('assistanceDocUrl', 'url de la documentation externe a l''application (wiki par exemple)', ''),
('assistanceHelpdeskUrl', 'url directe Helpdesk pour creation d''un ticket', ''),
('certificatScolaritePDF', 'Edition pdf des certificats de scolarite : true pour l''activer, false sinon', 'true'),
('certificatScolaritePJinvalide', 'true si on ne verifie pas les pieces justificatives pour donner acces au certificat de scolarite, false si on verifie qu''il ne reste aucune PJ dans un etat non valide avant de donner acces au certificat', 'true'),
('certificatScolariteTouteAnnee', 'Indiquer true si on veut permettre l''edition des certificats de scolarite pour toutes les annees et plus seulement pour l''annee en cours', 'false'),
('certScolAutorisePersonnel', 'Autoriser les personnels a imprimer les certificats de scolarite', 'false'),
('certScolCGEDesactive', 'Liste des codes CGE pour lesquels la generation de certificat est desactivee. (balises value) ', ''),
('certScolCmpDesactive', 'Liste des codes composante pour lesquels la generation de certificat est desactivee.', ''),
('certScolCodeSignataire', 'Code du signataire des certificats de scolarite', ''),
('certScolFooter', 'URL vers le footer pour le certificats de scolarite (1240x286). A laisser vide pour ne pas importer de logo', ''),
('certScolHeaderUniv', 'URL vers le header de l''universite pour le certificats de scolarite (1240x176). A laisser vide pour ne pas importer de logo', ''),
('certScolLieuEdition', 'Lieu d''edition des certificats de scolarite', ''),
('certScolProfilDesactive', 'Liste des codes profil pour lesquels la generation de certificat est desactivee', ''),
('certScolTampon', 'Tampon des certificats de scolarite (150x150 avec transparence)', ''),
('certScolTypDiplomeDesactive', 'Liste des codes de types de diplomes pour lesquels la generation de certificat est desactivee.', ''),
('certScolUtiliseLogo', 'Ajoute le logo de l''universite dans les certificats de scolarite generes', 'true'),
('codesEtapeAffichageRang', 'Liste des codes etape dont on affiche le rang meme si affRangEtudiant=false', ''),
('extensionMailEtudiant', 'L''extension au login pour l''e-mail des etudiants', '@etu.univ.fr'),
('insertionFiligranePdfNotes', 'Insertion d''un filigrane dans le pdf des notes', 'true'),
('listeLoginsBloques', 'Liste des logins a bloquer separes par une virgule', ''),
('logoUniversitePdf', 'URL vers le logo de l''universite pour le pdf. A laisser vide pour ne pas importer de logo.', ''),
('modificationAdresses', ' true pour que l''etudiant puisse modifier son adresse. False sinon', 'false'),
('modificationCoordonneesContactPerso', 'true pour que l''etudiant puisse modifier son tel et son mail perso. False sinon', 'false'),
('notesPDF', 'Edition pdf des notes : true pour l''activer, false sinon', 'false'),
('partieEnseignantActive', 'true si on rend la partie enseignant accessible, false sinon', 'true'),
('partieEtudiantActive', 'true si on rend la partie etudiant accessible, false sinon', 'true'),
('temNotesEtuSem', 'Applique l''affichage de la descendance du semestre que s''il est a T ', 'false'),
('temoinCtlValCadEpr', 'Valeur du temoin (O ou N) temoinCtlValCadEpr (Temoin modalites controle validees) pour laquelle on veut que les notes aux epreuves soient visibles meme si l''etat de deliberation n''est pas dans la liste de ceux definis ci-dessus. Laisser vide pour ne pas prendre en compte ce temoin dans l''affichage des notes aux epreuves', ''),
('temoinFictif', '(O/N) Si temoinFictif est renseigne, seuls les elements dont tem_fictif est egal a temoinFictif seront affiches dans l''ecran du detail des notes', 'N'),
('temoinNotesEnseignant', 'Etat de validation des notes affichees aux enseignants', 'AET'),
('temoinNotesEtudiant', 'Etat de validation des notes affichees aux etudiants', 'T'),
('trombiMobileNbEtuParPage', 'Nb d''etudiant par page dans l''affichage du trombinoscope mobile. Mettre 0 pour afficher tous les etudiant sur une seule page.', '20'),
('logoutCasPropose', 'true si on affiche le bouton de déconnexion, false sinon', 'false'),
('typesEpreuveAffichageNote', 'La liste des code type Epreuve (COD_TEP) dont on affiche toujours la note', '');

-- --------------------------------------------------------

--
-- Structure de la table `preferences_utilisateur`
--

CREATE TABLE IF NOT EXISTS `preferences_utilisateur` (
  `LOGIN` varchar(20) NOT NULL,
  `PREF_ID` varchar(40) NOT NULL,
  `VALEUR` varchar(200) NOT NULL,
  PRIMARY KEY (`LOGIN`,`PREF_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Structure de la table `utilisateur_swap`
--

CREATE TABLE IF NOT EXISTS `utilisateur_swap` (
  `LOGIN_SOURCE` varchar(20) NOT NULL,
  `LOGIN_CIBLE` varchar(20) NOT NULL,
  `DAT_CRE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`LOGIN_SOURCE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



