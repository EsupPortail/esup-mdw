/**
 *
 *  ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package fr.univlorraine.mondossierweb.controllers;

import fr.univlorraine.apowsutils.ServiceProvider;
import fr.univlorraine.mondossierweb.GenericUI;
import fr.univlorraine.mondossierweb.beans.*;
import fr.univlorraine.mondossierweb.services.apogee.ElementPedagogiqueService;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeServiceImpl;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import gouv.education.apogee.commun.client.ws.PedagogiqueMetier.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


/**
 * Gestion de la récupération des notes et résultats
 */
@Component(value = "resultatController")
public class ResultatController {

    private static final String ACRONYME_CORRESPONDANCE = "COR";
    /**
     * proxy pour faire appel aux infos sur les résultats du WS .
     */
    private final PedagogiqueMetierServiceInterface pedagogiqueService = ServiceProvider.getService(PedagogiqueMetierServiceInterface.class);
    private Logger LOG = LoggerFactory.getLogger(ResultatController.class);
    /* Injections */
    @Resource
    private transient ApplicationContext applicationContext;
    @Resource
    private ElementPedagogiqueService elementPedagogiqueService;
    @Resource
    private ConfigController configController;
    @Resource
    private EtudiantController etudiantController;
    /**
     * {@link MultipleApogeeServiceImpl}
     */
    @Resource
    private MultipleApogeeService multipleApogeeService;

    /**
     * va chercher et renseigne les notes de
     * l'étudiant via le WS de l'Amue.
     */
    public void recupererNotesEtResultatsEtudiant(Etudiant e) {

        try {
            e.getDiplomes().clear();
            e.getEtapes().clear();

            String temoin = configController.getTemoinNotesEtudiant();
            if (temoin == null || temoin.equals("")) {
                temoin = "T";
            }

            String temoinEtatIae = configController.getTemoinEtatIaeNotesEtudiant();
            if (temoinEtatIae == null || temoinEtatIae.equals("")) {
                temoinEtatIae = "E";
            }

            String sourceResultat = PropertyUtils.getSourceResultats();
            if (sourceResultat == null || sourceResultat.equals("")) {
                sourceResultat = Utils.APOGEE;
            }


            // VR 09/11/2009 : Verif annee de recherche si sourceResultat = apogee-extraction :
            // Si different annee en cours => sourceResultat = Apogee
            if (sourceResultat.compareTo(Utils.APOGEE_EXTRACTION) == 0) {
                // On recupere les resultats dans cpdto avec sourceResultat=Apogee
                sourceResultat = Utils.APOGEE;
                List<ContratPedagogiqueResultatVdiVetDTO2> cpdtoResult = pedagogiqueService.recupererContratPedagogiqueResultatVdiVetV2(e.getCod_etu(), "toutes", sourceResultat, temoin, "toutes", "tous", temoinEtatIae);

                // Puis dans cpdtoExtract avec sourceResultat=Apogee-extraction
                temoin = null;
                sourceResultat = Utils.APOGEE_EXTRACTION;
                List<ContratPedagogiqueResultatVdiVetDTO2> cpdtoExtract;
                try {
                    cpdtoExtract = pedagogiqueService.recupererContratPedagogiqueResultatVdiVetV2(e.getCod_etu(), "toutes", sourceResultat, temoin, "toutes", "tous", temoinEtatIae);
                } catch (Exception ex) {
                    cpdtoExtract = null;
                }

                // Et on fusionne cpdtoResult et cpdtoExtract
                ArrayList<ContratPedagogiqueResultatVdiVetDTO2> cpdtoAl = new ArrayList<ContratPedagogiqueResultatVdiVetDTO2>();
                if (cpdtoResult != null && !cpdtoResult.isEmpty()) {
                    for (ContratPedagogiqueResultatVdiVetDTO2 cr : cpdtoResult) {
                        String anneeResultat = getAnneeContratPedagogiqueResultatVdiVet(cr);
                        if (anneeResultat != null && !utilisationExtractionApogee(anneeResultat)) {
                            cpdtoAl.add(cr);
                        }
                    }
                }
                if (cpdtoExtract != null && !cpdtoExtract.isEmpty()) {
                    for (ContratPedagogiqueResultatVdiVetDTO2 ce : cpdtoExtract) {
                        String anneeResultat = getAnneeContratPedagogiqueResultatVdiVet(ce);
                        if (anneeResultat != null && utilisationExtractionApogee(anneeResultat)) {
                            cpdtoAl.add(ce);
                        }
                    }
                }

                setNotesEtResultats(e, cpdtoAl);

            } else {

                List<ContratPedagogiqueResultatVdiVetDTO2> cpdto = pedagogiqueService.recupererContratPedagogiqueResultatVdiVetV2(e.getCod_etu(), "toutes", sourceResultat, temoin, "toutes", "tous", temoinEtatIae);
                setNotesEtResultats(e, cpdto);
            }


        } catch (Exception ex) {
            //Si on est dans un cas d'erreur non expliqué
            if (ex.getMessage() != null && ex.getMessage().contains("remoteerror")) {
                LOG.error(ex.getMessage() + " Probleme avec le WS lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(), ex);
            } else {
                LOG.info(ex.getMessage() + " Probleme avec le WS lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(), ex);
            }
        }

    }


    /**
     * va chercher et renseigne les notes de
     * l'étudiant à destination d'un enseignant via le WS de l'Amue.
     */
    public void recupererNotesEtResultats(Etudiant e, boolean isGestionnaire) {

        if (e != null && StringUtils.hasText(e.getCod_etu())) {
            try {
                e.getDiplomes().clear();
                e.getEtapes().clear();


                String temoin = null;
                String temoinEtatIae = null;
                String sourceResultat = PropertyUtils.getSourceResultats();

                if (isGestionnaire) {
                    temoin = configController.getTemoinNotesGestionnaire();
                    temoinEtatIae = configController.getTemoinEtatIaeNotesGestionnaire();
                } else {
                    temoin = configController.getTemoinNotesEnseignant();
                    temoinEtatIae = configController.getTemoinEtatIaeNotesEnseignant();
                }

                if (temoin == null || temoin.equals("")) {
                    temoin = "AET";
                }

                if (temoinEtatIae == null || temoinEtatIae.equals("")) {
                    temoinEtatIae = "E";
                }


                if (sourceResultat == null || sourceResultat.equals("")) {
                    sourceResultat = Utils.APOGEE;
                }


                // VR 09/11/2009 : Verif annee de recherche si sourceResultat = apogee-extraction :
                // Si different annee en cours => sourceResultat = Apogee
                if (sourceResultat.compareTo(Utils.APOGEE_EXTRACTION) == 0) {
                    // On recupere les resultats dans cpdto avec sourceResultat=Apogee
                    sourceResultat = Utils.APOGEE;
                    List<ContratPedagogiqueResultatVdiVetDTO2> cpdtoResult = pedagogiqueService.recupererContratPedagogiqueResultatVdiVetV2(e.getCod_etu(), "toutes", sourceResultat, temoin, "toutes", "tous", temoinEtatIae);
                    // Puis dans cpdtoExtract avec sourceResultat=Apogee-extraction pour l'année en cours
                    temoin = null;
                    sourceResultat = Utils.APOGEE_EXTRACTION;
                    List<ContratPedagogiqueResultatVdiVetDTO2> cpdtoExtract;
                    try {
                        cpdtoExtract = pedagogiqueService.recupererContratPedagogiqueResultatVdiVetV2(e.getCod_etu(), "toutes", sourceResultat, temoin, "toutes", "tous", temoinEtatIae);
                    } catch (Exception ex) {
                        cpdtoExtract = null;
                    }

                    // Et on fusionne cpdtoResult et cpdtoExtract
                    ArrayList<ContratPedagogiqueResultatVdiVetDTO2> cpdtoAl = new ArrayList<ContratPedagogiqueResultatVdiVetDTO2>();
                    if (cpdtoResult != null && !cpdtoResult.isEmpty()) {
                        for (ContratPedagogiqueResultatVdiVetDTO2 cr : cpdtoResult) {
                            String anneeResultat = getAnneeContratPedagogiqueResultatVdiVet(cr);
                            if (anneeResultat != null && !utilisationExtractionApogee(anneeResultat)) {
                                cpdtoAl.add(cr);
                            }
                        }
                    }
                    if (cpdtoExtract != null && !cpdtoExtract.isEmpty()) {
                        for (ContratPedagogiqueResultatVdiVetDTO2 ce : cpdtoExtract) {
                            String anneeResultat = getAnneeContratPedagogiqueResultatVdiVet(ce);
                            if (anneeResultat != null && utilisationExtractionApogee(anneeResultat)) {
                                cpdtoAl.add(ce);
                            }
                        }
                    }
                    setNotesEtResultats(e, cpdtoAl);

                } else {

                    List<ContratPedagogiqueResultatVdiVetDTO2> cpdto = pedagogiqueService.recupererContratPedagogiqueResultatVdiVetV2(e.getCod_etu(), "toutes", sourceResultat, temoin, "toutes", "tous", temoinEtatIae);

                    setNotesEtResultats(e, cpdto);
                }
            } catch (Exception ex) {
                //Si on est dans un cas d'erreur non expliqué
                if (ex.getMessage().contains("remoteerror")) {
                    LOG.error(ex.getMessage() + " Probleme avec le WS lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(), ex);
                } else {
                    LOG.info(ex.getMessage() + " Probleme avec le WS lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(), ex);
                }
            }
        }
    }

    private String getAnneeContratPedagogiqueResultatVdiVet(ContratPedagogiqueResultatVdiVetDTO2 ct) {
        //Si l'année du contrat est non null
        if (ct.getAnnee() != null) {
            // on retourne l'année du contrat
            return ct.getAnnee();
        }
        //Si l'année du premier item de la liste "etapes" est non null
        if (ct.getEtapes() != null && ct.getEtapes().getItem() != null
                && !ct.getEtapes().getItem().isEmpty() && ct.getEtapes().getItem().get(0) != null
                && ct.getEtapes().getItem().get(0).getCodAnu() != null) {
            //On retourne l'année du premier item de la liste "etapes"
            return ct.getEtapes().getItem().get(0).getCodAnu();
        }
        return null;
    }

    /**
     * renseigne les attributs concernant les notes et résultats obtenus.
     *
     * @param e
     * @param resultatVdiVet
     */
    public void setNotesEtResultats(Etudiant e, List<ContratPedagogiqueResultatVdiVetDTO2> resultatVdiVet) {
        try {

            if (e.getDiplomes() != null) {
                e.getDiplomes().clear();
            } else {
                e.setDiplomes(new LinkedList<Diplome>());
            }

            if (e.getEtapes() != null) {
                e.getEtapes().clear();
            } else {
                e.setEtapes(new LinkedList<Etape>());
            }

            //Si on a configure pour toujours afficher le rang, on affichera les rangs de l'étudiant.
            e.setAfficherRang(configController.isAffRangEtudiant());

            if (resultatVdiVet != null && !resultatVdiVet.isEmpty()) {
                for (ContratPedagogiqueResultatVdiVetDTO2 rdto : resultatVdiVet) {
                    //information sur le diplome:

                    if (rdto.getDiplome() != null) {
                        Diplome d = new Diplome();

                        d.setLib_web_vdi(rdto.getDiplome().getLibWebVdi());
                        d.setCod_dip(rdto.getDiplome().getCodDip());
                        d.setCod_vrs_vdi(rdto.getDiplome().getCodVrsVdi().toString());

                        int annee2 = new Integer(rdto.getAnnee()) + 1;


                        d.setAnnee(rdto.getAnnee() + "/" + annee2);
                        //information sur les résultats obtenus au diplome:
                        TableauResultatVdiDto tabres = rdto.getResultatVdi();


                        if (tabres != null && tabres.getItem() != null && !tabres.getItem().isEmpty()) {


                            for (ResultatVdiDTO res : tabres.getItem()) {
                                Resultat r = new Resultat();

                                r.setCodeSession(Integer.parseInt(res.getSession().getCodSes()));
                                r.setSession(res.getSession().getLibSes());
                                if (res.getNatureResultat() != null && res.getNatureResultat().getCodAdm() != null && res.getNatureResultat().getCodAdm().equals("0")) {
                                    //on est en Admissibilité à l'étape.Pas en admission.
                                    //on le note pour que ce soit plus clair pour l'étudiant
                                    r.setNote(res.getNatureResultat().getLibAdm());
                                }

                                //recuperation de la mention
                                if (res.getMention() != null) {
                                    r.setCodMention(res.getMention().getCodMen());
                                    r.setLibMention(res.getMention().getLibMen());
                                }

                                String result = "";
                                if (res.getTypResultat() != null) {
                                    result = res.getTypResultat().getCodTre();
                                    r.setAdmission(result);
                                }
                                if (res.getNotVdi() != null) {
                                    r.setNote(res.getNotVdi().toString());
                                    //ajout pour note Jury
                                    if (res.getNotPntJurVdi() != null && !res.getNotPntJurVdi().equals(new BigDecimal(0))) {
                                        r.setNote(r.getNote() + "(+" + res.getNotPntJurVdi() + ")");
                                    }
                                } else {
                                    if (result.equals("DEF")) {
                                        r.setNote("DEF");
                                    }
                                }

                                //Gestion du barème:
                                if (res.getBarNotVdi() != null) {
                                    r.setBareme(res.getBarNotVdi());
                                }


                                //ajout de la signification du résultat dans la map
                                if ((result != null && !result.equals("")) && !e.getSignificationResultats().containsKey(r.getAdmission())) {
                                    e.getSignificationResultats().put(r.getAdmission(), res.getTypResultat().getLibTre());
                                }

                                //ajout du résultat au diplome:
                                d.getResultats().add(r);
                                if (res.getNbrRngEtuVdi() != null && !res.getNbrRngEtuVdi().equals("")) {
                                    d.setRang(res.getNbrRngEtuVdi() + "/" + res.getNbrRngEtuVdiTot());
                                    //On indique si on affiche le rang du diplome.
                                    d.setAfficherRang(configController.isAffRangEtudiant());

                                }
                            }
                            //ajout du diplome si on a au moins un résultat
                            //e.getDiplomes().add(0, d);
                        }
                        e.getDiplomes().add(0, d);
                    }
                    //information sur les etapes:
                    TableauEtapeResVdiVetDto2 etapes = rdto.getEtapes();
                    if (etapes != null && etapes.getItem() != null && !etapes.getItem().isEmpty()) {

                        for (EtapeResVdiVetDTO2 etape : etapes.getItem()) {

                            Etape et = new Etape();
                            int anneeEtape = new Integer(etape.getCodAnu());
                            et.setAnnee(anneeEtape + "/" + (anneeEtape + 1));
                            et.setCode(etape.getEtape().getCodEtp());
                            et.setVersion(etape.getEtape().getCodVrsVet().toString());
                            et.setLibelle(etape.getEtape().getLibWebVet());

                            //ajout 16/02/2012 pour WS exposés pour la version mobile en HttpInvoker
                            if (rdto.getDiplome() != null) {
                                et.setCod_dip(rdto.getDiplome().getCodDip());
                                et.setVers_dip(rdto.getDiplome().getCodVrsVdi());
                            }

                            //résultats de l'étape:
                            TableauResultatVetDto tabresetape = etape.getResultatVet();
                            if (tabresetape != null && tabresetape.getItem() != null && !tabresetape.getItem().isEmpty()) {
                                for (ResultatVetDTO ret : tabresetape.getItem()) {

                                    Resultat r = new Resultat();
                                    if (!ret.getEtatDelib().getCodEtaAvc().equals("T")) {
                                        et.setDeliberationTerminee(false);
                                    } else {
                                        et.setDeliberationTerminee(true);
                                    }

                                    r.setCodeSession(Integer.parseInt(ret.getSession().getCodSes()));
                                    r.setSession(ret.getSession().getLibSes());
                                    if (ret.getNatureResultat() != null && ret.getNatureResultat().getCodAdm() != null && ret.getNatureResultat().getCodAdm().equals("0")) {
                                        //on est en Admissibilité à l'étape.Pas en admission.
                                        //on le note pour que ce soit plus clair pour l'étudiant
                                        r.setNote(ret.getNatureResultat().getLibAdm());

                                    }
                                    //recuperation de la mention
                                    if (ret.getMention() != null) {
                                        r.setCodMention(ret.getMention().getCodMen());
                                        r.setLibMention(ret.getMention().getLibMen());
                                    }

                                    String result = "";
                                    if (ret.getTypResultat() != null) {
                                        result = ret.getTypResultat().getCodTre();
                                        r.setAdmission(result);
                                    }
                                    if (ret.getNotVet() != null) {
                                        r.setNote(ret.getNotVet().toString());
                                        //ajout note jury
                                        if (ret.getNotPntJurVet() != null && !ret.getNotPntJurVet().equals(new BigDecimal(0))) {
                                            r.setNote(r.getNote() + "(+" + ret.getNotPntJurVet() + ")");
                                        }

                                    } else {
                                        if (result.equals("DEF")) {
                                            r.setNote("DEF");
                                        }
                                    }

                                    //Gestion du barème:
                                    if (ret.getBarNotVet() != null) {
                                        r.setBareme(ret.getBarNotVet());
                                    }

                                    //ajout de la signification du résultat dans la map
                                    if (result != null && !result.equals("") && !e.getSignificationResultats().containsKey(r.getAdmission())) {
                                        e.getSignificationResultats().put(r.getAdmission(), ret.getTypResultat().getLibTre());
                                    }


                                    //ajout du résultat par ordre de code session (Juillet 2014)
                                    //ajout du resultat en fin de liste
                                    //et.getResultats().add(r);
                                    try {
                                        int session = Integer.parseInt(ret.getSession().getCodSes());
                                        if (et.getResultats().size() > 0 && et.getResultats().size() >= session) {
                                            //ajout du résultat à la bonne place dans la liste
                                            et.getResultats().add((session - 1), r);
                                        } else {
                                            //ajout du résultat en fin de liste
                                            et.getResultats().add(r);
                                        }
                                    } catch (Exception excep) {
                                        et.getResultats().add(r);
                                    }

                                    //ajout du rang
                                    if (ret.getNbrRngEtuVet() != null && !ret.getNbrRngEtuVet().equals("")) {
                                        et.setRang(ret.getNbrRngEtuVet() + "/" + ret.getNbrRngEtuVetTot());
                                        //On calcule si on affiche ou non le rang.
                                        boolean cetteEtapeDoitEtreAffiche = false;

                                        List<String> codesAutorises = configController.getListeCodesEtapeAffichageRang();
                                        if (codesAutorises != null && codesAutorises.contains(et.getCode())) {
                                            cetteEtapeDoitEtreAffiche = true;
                                        }
                                        if (configController.isAffRangEtudiant() || cetteEtapeDoitEtreAffiche) {
                                            //On affichera le rang de l'étape.
                                            et.setAfficherRang(true);
                                            //On remonte au niveau de l'étudiant qu'on affiche le rang
                                            e.setAfficherRang(true);
                                        }
                                    }

                                }
                            }

                            //ajout de l'étape a la liste d'étapes de l'étudiant:
                            //e.getEtapes().add(0, et);
                            //en attendant la maj du WS :
                            insererEtapeDansListeTriee(e, et);


                        }
                    }

                }
            }
        } catch (Exception ex) {
            //Si on est dans un cas d'erreur non expliqué
            if (ex.getMessage().contains("remoteerror")) {
                LOG.error("Probleme avec le WS lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(), ex);
            } else {
                LOG.info("Probleme avec le WS lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(), ex);
            }
        }

    }


    private void insererEtapeDansListeTriee(Etudiant e, Etape et) {

        boolean insere = false;
        int rang = 0;
        int anneeEtape = new Integer(et.getAnnee().substring(0, 4));
        while (!insere && rang < e.getEtapes().size()) {

            int anneeEtapeEnCours = new Integer(e.getEtapes().get(rang).getAnnee().substring(0, 4));
            if (anneeEtape > anneeEtapeEnCours) {
                e.getEtapes().add(rang, et);
                insere = true;
            }
            rang++;
        }
        if (!insere) {
            e.getEtapes().add(et);
        }
    }


    /**
     * Récupère les données retournées par le WS et les trie pour les afficher
     *
     * @param e               etudiant
     * @param et              etape
     * @param reedto          objet retourne par le WS
     * @param temoinEtatDelib
     */
    public void setNotesElpEpr(Etudiant e, Etape et, List<ContratPedagogiqueResultatElpEprDTO5> reedto, String temoinEtatDelib, int anneeResultat, boolean sourceExtractionApogee) {
        try {

            e.getElementsPedagogiques().clear();
            //liste intermédiaire pour trié les éléments pédagogiques:
            List<ElementPedagogique> liste1 = new ArrayList<ElementPedagogique>();


            if (reedto != null && !reedto.isEmpty()) {
                // Témoin de session unique sur la VET
                Boolean temSesUniVet = null;
                //On parcourt les ELP:
                for (ContratPedagogiqueResultatElpEprDTO5 cpree : reedto) {

                    ElementPedagogique elp = new ElementPedagogique();
                    elp.setCode(cpree.getElp().getCodElp());
                    elp.setLevel(cpree.getRngElp());
                    elp.setCodElpSup(cpree.getCodElpSup());
                    elp.setLibelle(cpree.getElp().getLibElp());
                    elp.setAnnee("");
                    elp.setEpreuve(false);

                    elp.setNote1("");
                    elp.setBareme1(0);
                    elp.setRes1("");
                    elp.setNote2("");
                    elp.setBareme2(0);
                    elp.setRes2("");
                    elp.setEcts("");
                    elp.setTemFictif(cpree.getElp().getNatureElp().getTemFictif());
                    elp.setTemSemestre("N");
                    elp.setTemSemestre(cpree.getElp().getNatureElp().getTemSemestre());
                    elp.setEtatDelib("");


                    //Récupération des crédits ects de référence
                    String creditEctsElp = null;
                    //Si on a récupéré un crédit ECTS de référence
                    if (cpree.getElp().getNbrCrdElp() != null && cpree.getElp().getNbrCrdElp().toString() != null && !cpree.getElp().getNbrCrdElp().toString().equals("")) {
                        creditEctsElp = cpree.getElp().getNbrCrdElp().toString();
                    }

                    //vrai si l'ELP est il dans un etat de delib qui nous convient en session1:
                    boolean elpEtatDelibS1OK = false;

                    //vrai si l'ELP est il dans un etat de delib qui nous convient en session2:
                    boolean elpEtatDelibS2OK = false;

                    if (!sourceExtractionApogee || elpAvecResultats(cpree)) {

                        if (cpree.getElp().getNatureElp().getCodNel().equals("FICM")) {
                            //utile pour ne pas afficher les FICM par la suite
                            elp.setAnnee("FICM");
                        }

                        //contient l'année de la PRC si les résultats sont obtenus en PRC
                        String anneePrc = null;

                        //On s'occupe des résultats :
                        TableauResultatElpDto33 relpdto = cpree.getResultatsElp();
                        if (relpdto != null && relpdto.getItem() != null && !relpdto.getItem().isEmpty()) {
                            // Si l'ELP possède des résultats pour 2 MCC différentes (session unique + session double)
                            // et qu'on n'a pas déjà récupéré le tem_ses_uni de la VET
                            if (contientResultatsMixte(relpdto.getItem()) && temSesUniVet == null) {
                                // Récupération du témoin de session unique de la VET correspondante
                                temSesUniVet = multipleApogeeService.getTemSesUniVet(et.getCode(), et.getVersion());
                                LOG.debug("temoinEtatDelib " + et.getCode() + "/" + et.getVersion() + " : " + temSesUniVet);
                            }
                            //on parcourt les résultats pour l'ELP:
                            for (ResultatElpDTO3 rpd : relpdto.getItem()) {
                                // Si une session est renseignée et qu'elle est raccord avec la MCC de la VET
                                if (temSesUniVet == null || rpd == null || rpd.getSession() == null || rpd.getSession().getCodSes() == null
                                        || (temSesUniVet.booleanValue() && rpd.getSession().getCodSes().equals(Utils.COD_SES_UNI))
                                        || (!temSesUniVet.booleanValue() && !rpd.getSession().getCodSes().equals(Utils.COD_SES_UNI))) {

                                    if (rpd != null && rpd.getEtatDelib() != null && rpd.getEtatDelib().getCodEtaAvc() != null)
                                        elp.setEtatDelib(rpd.getEtatDelib().getCodEtaAvc());

                                    //on affiche pas les résultats d'admissibilité
                                    if (configController.isAffResAdmissibilite() || rpd.getNatureResultat() == null || rpd.getNatureResultat().getCodAdm() == null || !rpd.getNatureResultat().getCodAdm().equals("0")) {
                                        //29/01/10
                                        //On récupère les notes si l'ELP est dans un état de delibération compris dans la liste des témoins paramétrés.
                                        if (rpd.getEtatDelib() == null || temoinEtatDelib.contains(rpd.getEtatDelib().getCodEtaAvc())) {

                                            int codsession = 0;
                                            if (rpd.getSession() != null) {
                                                codsession = new Integer(rpd.getSession().getCodSes());
                                            } else {
                                                //Pour info, on arrive ici car on peut etre en VAC: validation d'acquis
                                            }

                                            String result = null;

                                            //le résultat:
                                            if (rpd.getTypResultat() != null) {
                                                result = rpd.getTypResultat().getCodTre();
                                                if (StringUtils.hasText(result) && !e.getSignificationResultats().containsKey(result)) {
                                                    e.getSignificationResultats().put(result, rpd.getTypResultat().getLibTre());
                                                }
                                            }
                                            //On affiche la correspondance meme si l'état de délibération n'est pas compris dans la liste des témoins paramétrés.
                                            if (rpd.getLcc() != null) {
                                                if (StringUtils.hasText(result)) {
                                                    result += "/" + ACRONYME_CORRESPONDANCE;
                                                } else {
                                                    result = ACRONYME_CORRESPONDANCE;
                                                }
                                                //ajout de la signification du résultat dans la map
                                                if (!e.getSignificationResultats().containsKey(ACRONYME_CORRESPONDANCE)) {
                                                    e.getSignificationResultats().put(ACRONYME_CORRESPONDANCE, applicationContext.getMessage("notesView.signification.type.correspondance", null, Locale.getDefault()));
                                                }
                                            }

                                            //Test sur la session traitée
                                            if (codsession < 2) {
                                                //l'elp est dans un état de delibération compris dans la liste des témoins paramétrés.
                                                elpEtatDelibS1OK = true;

                                                //1er session  : juin
                                                if (rpd.getNotElp() != null && !rpd.getNotElp().equals("null")) {
                                                    elp.setNote1(rpd.getNotElp().toString());
                                                    if (rpd.getNotPntJurElp() != null && !rpd.getNotPntJurElp().equals(new BigDecimal(0))) {
                                                        elp.setNote1(elp.getNote1() + "(+" + rpd.getNotPntJurElp() + ")");
                                                    }

                                                }
                                                if ((elp.getNote1() == null || (elp.getNote1() != null && elp.getNote1().equals(""))) && result != null && result.equals("DEF")) {
                                                    elp.setNote1("DEF");
                                                }

                                                //Gestion du barème:
                                                if (rpd.getBarNotElp() != null) {
                                                    elp.setBareme1(rpd.getBarNotElp());
                                                }

                                                //ajout du rang si pas déjà renseigné via la session de juin.
                                                if (rpd.getNbrRngEtuElp() != null && !rpd.getNbrRngEtuElp().equals("")
                                                        && (elp.getRang() == null || elp.getRang().equals(""))) {
                                                    elp.setRang(rpd.getNbrRngEtuElp() + "/" + rpd.getNbrRngEtuElpTot());
                                                }

                                                //on récupère l'année car si année!=null c'est un PRC  si pas déjà renseigné via la session de juin.
                                                if (rpd.getCodAnu() != null && !rpd.getCodAnu().equals("")
                                                        && (elp.getAnnee() == null || elp.getAnnee().equals(""))) {
                                                    elp.setAnnee(rpd.getCodAnu());
                                                    anneePrc = rpd.getCodAnu();
                                                }


                                                // Récupération des crédits ECTS version 5.20.laa
                                                // Si on a un crédit ECTS de référence et si crédit ECTS pas déjà renseigné via la session de juin.
                                                if (creditEctsElp != null && (elp.getEcts() == null || elp.getEcts().equals(""))) {
                                                    //Si on a un crédit acquis
                                                    if (rpd.getNbrCrdElp() != null && rpd.getNbrCrdElp().toString() != null && !rpd.getNbrCrdElp().toString().equals("")) {
                                                        elp.setEcts(Utils.getEctsToDisplay(rpd.getNbrCrdElp()) + "/" + creditEctsElp);
                                                    } else {
                                                        elp.setEcts("0/" + creditEctsElp);
                                                    }
                                                }

                                                elp.setRes1(result);
                                            } else {
                                                //2em session  : septembre
                                                //l'elp est dans un état de delibération compris dans la liste des témoins paramétrés.
                                                elpEtatDelibS2OK = true;

                                                if (rpd.getNotElp() != null && !rpd.getNotElp().equals("null")) {
                                                    elp.setNote2(rpd.getNotElp().toString());
                                                    if (rpd.getNotPntJurElp() != null && !rpd.getNotPntJurElp().equals(new BigDecimal(0))) {
                                                        elp.setNote2(elp.getNote2() + "(+" + rpd.getNotPntJurElp() + ")");
                                                    }
                                                }
                                                if ((elp.getNote2() == null || (elp.getNote2() != null && elp.getNote2().equals(""))) && result != null && result.equals("DEF")) {
                                                    elp.setNote2("DEF");
                                                }

                                                //Gestion du barème:
                                                if (rpd.getBarNotElp() != null) {
                                                    elp.setBareme2(rpd.getBarNotElp());
                                                }

                                                //ajout du rang
                                                if (rpd.getNbrRngEtuElp() != null && !rpd.getNbrRngEtuElp().equals("")) {
                                                    elp.setRang(rpd.getNbrRngEtuElp() + "/" + rpd.getNbrRngEtuElpTot());
                                                }
                                                //on récupère l'année car si getCodAnu()!=null c'est un PRC
                                                if (rpd.getCodAnu() != null && !rpd.getCodAnu().equals("")) {
                                                    elp.setAnnee(rpd.getCodAnu());
                                                    anneePrc = rpd.getCodAnu();
                                                }

                                                // Récupération des crédits ECTS version 5.20.laa
                                                // Si on a un crédit ECTS de référence
                                                if (creditEctsElp != null) {
                                                    //Si on a un crédit acquis
                                                    if (rpd.getNbrCrdElp() != null && rpd.getNbrCrdElp().toString() != null && !rpd.getNbrCrdElp().toString().equals("")) {
                                                        elp.setEcts(Utils.getEctsToDisplay(rpd.getNbrCrdElp()) + "/" + creditEctsElp);
                                                    } else {
                                                        elp.setEcts("0/" + creditEctsElp);
                                                    }
                                                }

                                                elp.setRes2(result);
                                            }
                                        }
                                    }
                                } else {
                                    LOG.debug("Résultat ignoré  car temSesUniVet = " + temSesUniVet + " pour VET " + et.getCode() + "/" + et.getVersion());
                                }
                            }
                        }

                        //Si il y a un PRC
                        if (anneePrc != null) {
                            //On doit vérifier que la PRC est valide
                            int anneeObtPrc = Integer.parseInt(anneePrc);
                            //Récupération de la durée de conservation de  l'élément conservable
                            int durConElp = 0;
                            if (cpree.getElp().getDurConElp() != null) {
                                durConElp = cpree.getElp().getDurConElp();
                                //On test si la conservation est encore valide
                                if ((anneeObtPrc + durConElp) < anneeResultat) {
                                    //Si ce n'est pas le cas on n'affiche pas les résulats ni l'année.
                                    elp.setAnnee("");
                                    elp.setNote1("");
                                    elp.setBareme1(0);
                                    elp.setRes1("");
                                    elp.setNote2("");
                                    elp.setBareme2(0);
                                    elp.setRes2("");
                                    elp.setEcts("");
                                    elp.setEtatDelib("");
                                }
                            }

                        }

                        //ajout de l'élément dans la liste
                        if (liste1.size() == 0 || sourceExtractionApogee) {
                            liste1.add(elp);
                        } else {
                            //ajout de l'élément dans la liste par ordre alphabétique
                            int rang = 0;
                            boolean insere = false;
                            while (rang < liste1.size() && !insere) {

                                if (liste1.get(rang).getCode().compareTo(elp.getCode()) > 0) {
                                    liste1.add(rang, elp);
                                    insere = true;
                                }

                                if (!insere) {
                                    rang++;
                                }
                            }
                            if (!insere) {
                                liste1.add(elp);
                            }
                        }
                    }

                    //les epreuves de l'élément (si il y en a )
                    TableauEpreuveElpDto24 epelpdto = cpree.getEpreuvesElp();

                    if (epelpdto != null && epelpdto.getItem() != null && !epelpdto.getItem().isEmpty()) {

                        for (EpreuveElpDTO2 epreuve : epelpdto.getItem()) {

                            boolean EprNotee = false;  //vrai si l'épreuve est notée
                            boolean EprResult = false;  //vrai si l'épreuve a un résultat
                            boolean confAffResultatsEpreuve = configController.isAffResultatsEpreuves(); //le paramètre d'affichage des resultats aux épreuves
                            ElementPedagogique elp2 = new ElementPedagogique();
                            elp2.setLibelle(epreuve.getEpreuve().getLibEpr());
                            elp2.setCode(epreuve.getEpreuve().getCodEpr());
                            elp2.setLevel(elp.getLevel() + 1);

                            //Modif 20/02/2012 pour les WS HttpInvoker
                            //elp2.setAnnee("epreuve");
                            elp2.setAnnee("");
                            elp2.setEpreuve(true);

                            elp2.setCodElpSup(elp.getCode());
                            elp2.setNote1("");
                            elp2.setBareme1(0);
                            elp2.setRes1("");
                            elp2.setNote2("");
                            elp2.setBareme2(0);
                            elp2.setRes2("");
                            TableauResultatEprDto2 repdto = epreuve.getResultatEpr();
                            //29/01/10
                            //On récupère le témoin TemCtlValCadEpr de l'épreuve
                            String TemCtlValCadEpr = epreuve.getEpreuve().getTemCtlValCadEpr();

                            if (repdto != null && repdto.getItem() != null && !repdto.getItem().isEmpty()) {
                                for (ResultatEprDTO red : repdto.getItem()) {
                                    int codsession = new Integer(red.getSession().getCodSes());
                                    //09/01/13
                                    //On recupere la note si :
                                    //  On a reseigné une liste de type épreuve à afficher et le type de l'épreuve en fait partie
                                    //  OU SI :
                                    //      le témoin d'avc fait partie de la liste des témoins paramétrés
                                    //      OU si le témoin d'avc de  l'elp pere fait partie de la liste des témoins paramétrés
                                    //      OU si le témoin TemCtlValCadEpr est égal au parametre TemoinCtlValCadEpr de monDossierWeb.xml.
                                    boolean recuperationNote = false;

                                    List<String> lTypesEpreuveAffichageNote = configController.getTypesEpreuveAffichageNote();
                                    if (lTypesEpreuveAffichageNote != null && !lTypesEpreuveAffichageNote.isEmpty()) {
                                        //On a renseigné une liste de type épreuve à afficher
                                        if (lTypesEpreuveAffichageNote.contains(epreuve.getEpreuve().getTypEpreuve().getCodTep())) {
                                            recuperationNote = true;
                                        }
                                    }
                                    if (!recuperationNote) {
                                        //Si on est dans le cas d'une extraction Apogée
                                        if (sourceExtractionApogee) {
                                            recuperationNote = true;
                                        } else {
                                            //On n'a pas renseigné de liste de type épreuve à afficher ou celui ci n'était pas dans la liste
                                            if (codsession < 2) {
                                                if ((red.getEtatDelib() != null && temoinEtatDelib.contains(red.getEtatDelib().getCodEtaAvc())) || elpEtatDelibS1OK || TemCtlValCadEpr.equals(configController.getTemoinCtlValCadEpr()))
                                                    recuperationNote = true;
                                            } else {
                                                if ((red.getEtatDelib() != null && temoinEtatDelib.contains(red.getEtatDelib().getCodEtaAvc())) || elpEtatDelibS2OK || TemCtlValCadEpr.equals(configController.getTemoinCtlValCadEpr()))
                                                    recuperationNote = true;
                                            }
                                        }
                                    }
                                    //test si on recupere la note ou pas
                                    if (recuperationNote) {
                                        if (codsession < 2) {
                                            //1er session  : juin
                                            if (red.getNotEpr() != null) {
                                                elp2.setNote1(formatNoteEpreuve(red.getNotEpr()));

                                                //Gestion du barème:
                                                if (red.getBarNotEpr() != null) {
                                                    elp2.setBareme1(red.getBarNotEpr());
                                                }
                                            }
                                            if (elp2.getNote1() != null && !elp2.getNote1().equals("")) {
                                                EprNotee = true;
                                            }

                                            //le resultat à l'épreuve
                                            if (confAffResultatsEpreuve && red.getTypResultat() != null && StringUtils.hasText(red.getTypResultat().getCodTre())) {
                                                EprResult = true;
                                                elp2.setRes1(red.getTypResultat().getCodTre());
                                            }


                                        } else {
                                            //2er session  : septembre
                                            if (red.getNotEpr() != null) {
                                                elp2.setNote2(formatNoteEpreuve(red.getNotEpr()));

                                                //Gestion du barème:
                                                if (red.getBarNotEpr() != null) {
                                                    elp2.setBareme2(red.getBarNotEpr());
                                                }
                                            }
                                            if (elp2.getNote2() != null && !elp2.getNote2().equals("")) {
                                                EprNotee = true;
                                            }

                                            //le resultat à l'épreuve
                                            if (confAffResultatsEpreuve && red.getTypResultat() != null && StringUtils.hasText(red.getTypResultat().getCodTre())) {
                                                EprResult = true;
                                                elp2.setRes2(red.getTypResultat().getCodTre());
                                            }
                                        }
                                    }
                                }
                            }
                            //ajout de l'épreuve dans la liste en tant qu'élément si elle a une note ou un résultat (si on veut afficher les résultats)
                            if (EprNotee || (confAffResultatsEpreuve && EprResult)) {
                                LOG.debug("*****Ajout epreuve à la liste : " + elp2.getCode());
                                liste1.add(elp2);
                            }
                        }
                    }
                }
            }
            //ajout des éléments dans la liste de l'étudiant en commençant par la ou les racine
            int niveauRacine = 1;
            if (liste1.size() > 0) {
                int i = 0;
                while (i < liste1.size()) {
                    ElementPedagogique el = liste1.get(i);
                    if (sourceExtractionApogee) {
                        e.getElementsPedagogiques().add(el);
                    } else {
                        if (el.getCodElpSup() == null || el.getCodElpSup().equals("")) {
                            //on a une racine:
                            if (!el.getAnnee().equals("FICM")) {
                                e.getElementsPedagogiques().add(el);
                            }

                            insererElmtPedagoFilsDansListe(el, liste1, e, niveauRacine);
                        }
                    }
                    i++;
                }
            }


            //suppression des épreuve seules et quand elles ont les mêmes notes que l'element pere:
            if (!sourceExtractionApogee && e.getElementsPedagogiques().size() > 0) {
                int i = 1;
                boolean suppr = false;
                while (i < e.getElementsPedagogiques().size()) {
                    suppr = false;
                    ElementPedagogique elp = e.getElementsPedagogiques().get(i);
                    if (elp.isEpreuve()) {
                        ElementPedagogique elp0 = e.getElementsPedagogiques().get(i - 1);
                        if (i < (e.getElementsPedagogiques().size() - 1)) {
                            ElementPedagogique elp1 = e.getElementsPedagogiques().get(i + 1);
                            if (!elp0.isEpreuve() && !elp1.isEpreuve()) {
                                if (elp0.getNote1().equals(elp.getNote1()) && elp0.getNote2().equals(elp.getNote2())) {
                                    //on supprime l'element i
                                    e.getElementsPedagogiques().remove(i);
                                    suppr = true;
                                }
                            }
                        } else {
                            if (!elp0.isEpreuve() && elp0.getNote1().equals(elp.getNote1()) && elp0.getNote2().equals(elp.getNote2())) {
                                //on supprime l'element i
                                e.getElementsPedagogiques().remove(i);
                                suppr = true;
                            }
                        }
                    }
                    if (!suppr) {
                        i++;
                    }
                }
            }


            //Gestion des temoins fictif si temoinFictif est renseigné dans monDossierWeb.xml
            if (configController.getTemoinFictif() != null && !configController.getTemoinFictif().equals("")) {
                if (e.getElementsPedagogiques().size() > 0) {
                    List<Integer> listeRangAsupprimer = new LinkedList<Integer>();
                    int rang = 0;
                    //on note les rangs des éléments à supprimer
                    for (ElementPedagogique el : e.getElementsPedagogiques()) {
                        if (el.getTemFictif() != null && !el.getTemFictif().equals("") && !el.getTemFictif().equals(configController.getTemoinFictif())) {
                            //on supprime l'élément de la liste
                            listeRangAsupprimer.add(rang);
                        }
                        rang++;
                    }
                    //on supprime les éléments de la liste
                    int NbElementSupprimes = 0;
                    for (Integer rg : listeRangAsupprimer) {
                        e.getElementsPedagogiques().remove(rg - NbElementSupprimes);
                        NbElementSupprimes++;
                    }
                }
            }

            // Gestion de la descendance des semestres si temNotesEtuSem est renseigné et à true dans monDossierWeb.xml
            // et qu'il ne s'agit pas d'une extraction
            if (configController.isTemNotesEtuSem() && !sourceExtractionApogee) {
                if (e.getElementsPedagogiques().size() > 0) {
                    List<Integer> listeRangAsupprimer = new LinkedList<Integer>();
                    int rang = 0;

                    int curSemLevel = 0;
                    boolean supDesc = false;

                    //on note les rangs des éléments à supprimer
                    for (ElementPedagogique el : e.getElementsPedagogiques()) {
                        if (el.getTemSemestre() != null && !el.getTemSemestre().equals("") && el.getTemSemestre().equals("O")) {
                            curSemLevel = new Integer(el.getLevel());
                            supDesc = el.getEtatDelib() != null && !el.getEtatDelib().equals("") && !el.getEtatDelib().equals("T");
                        } else if (el.getLevel() <= curSemLevel) {
                            supDesc = false;
                        }

                        if (supDesc && el.getLevel() > curSemLevel) {
                            //on supprime l'élément de la liste
                            listeRangAsupprimer.add(rang);
                        }
                        rang++;
                    }
                    //on supprime les éléments de la liste
                    int NbElementSupprimes = 0;
                    for (Integer rg : listeRangAsupprimer) {
                        e.getElementsPedagogiques().remove(rg - NbElementSupprimes);
                        NbElementSupprimes++;
                    }
                }
            }

            //ajout de l'étape sélectionnée en début de liste:
            ElementPedagogique ep = new ElementPedagogique();
            ep.setAnnee(et.getAnnee());
            ep.setCode(et.getCode());
            ep.setRang(et.getRang());
            ep.setLevel(1);
            ep.setLibelle(et.getLibelle());
            e.setDeliberationTerminee(et.isDeliberationTerminee());
            if (et.getResultats().size() > 0) {
                for (Resultat r : et.getResultats()) {
                    if (r != null) {
                        //Si c'est un résultat de session 1 ou de session unique
                        if (r.getCodeSession() < 2) {
                            if (r.getNote() != null) {
                                ep.setNote1(r.getNote().toString());
                                ep.setBareme1(r.getBareme());
                            }
                            if (r.getAdmission() != null)
                                ep.setRes1(r.getAdmission());
                        } else {
                            //C'est un résultat de session 2
                            if (r.getNote() != null) {
                                ep.setNote2(r.getNote().toString());
                                ep.setBareme2(r.getBareme());
                            }
                            if (r.getAdmission() != null)
                                ep.setRes2(r.getAdmission());
                        }
                    }

                }

                /* ANCIEN CODE : avant d'ajouter codeSession à Resultat.java*/
				/*if (et.getResultats().get(0).getNote() != null){
					ep.setNote1(et.getResultats().get(0).getNote().toString());
					ep.setBareme1(et.getResultats().get(0).getBareme());
				}
				if (et.getResultats().get(0).getAdmission() != null)
					ep.setRes1(et.getResultats().get(0).getAdmission());

				}
			if (et.getResultats().size() > 1) {
				if (et.getResultats().get(1).getNote() != null){
					ep.setNote2(et.getResultats().get(1).getNote().toString());
					ep.setBareme2(et.getResultats().get(1).getBareme());
				}
				if (et.getResultats().get(1).getAdmission() != null)
					ep.setRes2(et.getResultats().get(1).getAdmission());*/
            }
            e.getElementsPedagogiques().add(0, ep);

        } catch (Exception ex) {
            //Si on est dans un cas d'erreur non expliqué
            if (ex.getMessage().contains("remoteerror")) {
                LOG.error("Probleme avec le WS lors de la recherche des notes et résultats a une étape pour etudiant dont codetu est : " + e.getCod_etu(), ex);
            } else {
                LOG.info("Probleme avec le WS lors de la recherche des notes et résultats a une étape pour etudiant dont codetu est : " + e.getCod_etu(), ex);
            }
        }
    }


    private boolean contientResultatsMixte(List<ResultatElpDTO3> resultats) {
        Boolean sessionUnique = null;
        // On parcourt les résultats
        for (ResultatElpDTO3 r : resultats) {
            // Si on a une info de session
            if (r != null && r.getSession() != null && StringUtils.hasText(r.getSession().getCodSes())) {
                // Récupération de l'info de session
                Boolean su = r.getSession().getCodSes().equals(Utils.COD_SES_UNI);
                // Si c'est la première info de session récupérée
                if (sessionUnique == null) {
                    // On garde l'information dans sessionUnique
                    sessionUnique = su;
                } else {
                    // Si c'et une MCC différente de celle trouvée jusque là
                    if (sessionUnique.booleanValue() != su.booleanValue()) {
                        LOG.debug("contientResultatsMixte true");
                        return true;
                    }
                }
            }
        }
        LOG.debug("contientResultatsMixte false");
        return false;
    }


    private String formatNoteEpreuve(String notEpr) {
        String note = notEpr.replaceAll(",", ".");
        if (note.startsWith(".")) {
            note = "0" + note;
        }
        return note;
    }


    /**
     * @param elp
     * @return vrai si l'elp en paramètre a des résulats
     */
    private boolean elpAvecResultats(ContratPedagogiqueResultatElpEprDTO5 elp) {
        return elp != null && elp.getResultatsElp() != null
                && elp.getResultatsElp().getItem() != null && !elp.getResultatsElp().getItem().isEmpty();
    }

    /**
     * ajoute les éléments dans la liste d'éléments de l'étudiant en corrigeant les levels (rangs).
     *
     * @param elp
     * @param liste1
     * @param e
     * @param niveauDuPere
     */
    protected void insererElmtPedagoFilsDansListe(ElementPedagogique elp, List<ElementPedagogique> liste1, Etudiant e, int niveauDuPere) {
        for (ElementPedagogique el : liste1) {
            if (el.getCodElpSup() != null && !el.getCodElpSup().equals("")) {
                if (el.getCodElpSup().equals(elp.getCode()) && !el.getCode().equals(elp.getCode())) {
                    //on affiche pas les FICM :
                    if (!el.getAnnee().equals("FICM")) {
                        el.setLevel(niveauDuPere + 1);
                        e.getElementsPedagogiques().add(el);
                    }
                    //On test si on est pas sur une epreuve pour eviter les boucle infini dans le cas ou codEpr=CodElpPere
                    if (!el.getAnnee().equals("epreuve"))
                        insererElmtPedagoFilsDansListe(el, liste1, e, niveauDuPere + 1);
                }
            }
        }
    }


    /**
     * va chercher et renseigne les informations concernant les notes
     * et résultats des éléments de l'etape choisie
     * de l'étudiant placé en paramètre via le WS de l'Amue.
     */
    public void recupererDetailNotesEtResultatsEtudiant(Etudiant e, Etape et, boolean forceSourceApogee) {
        try {

            e.getElementsPedagogiques().clear();

            String temoin = configController.getTemoinNotesEtudiant();
            if (temoin == null || temoin.equals("")) {
                temoin = "T";
            }

            String temoinEtatIae = configController.getTemoinEtatIaeNotesEtudiant();
            if (temoinEtatIae == null || temoinEtatIae.equals("")) {
                temoinEtatIae = "E";
            }

            String sourceResultat = PropertyUtils.getSourceResultats();
            if (forceSourceApogee || sourceResultat == null || sourceResultat.equals("")) {
                sourceResultat = Utils.APOGEE;
            }

            //Si on doit se baser sur l'extraction Apogée
            if (utilisationExtractionApogee(et.getAnnee().substring(0, 4), sourceResultat)) {
                //On se base sur l'extraction apogée
                sourceResultat = Utils.APOGEE_EXTRACTION;
                temoin = null;
            } else {
                //On va chercher les résultats directement dans Apogée
                sourceResultat = Utils.APOGEE;
            }

            String anneeParam = et.getAnnee().substring(0, 4);
            int annee = Integer.parseInt(anneeParam);

            //07/09/10
            if (sourceResultat.compareTo(Utils.APOGEE_EXTRACTION) == 0) {
                //07/09/10
                //on prend le témoin pour Apogee-extraction
                List<ContratPedagogiqueResultatElpEprDTO5> cpdto = pedagogiqueService.recupererContratPedagogiqueResultatElpEprV6(e.getCod_etu(), anneeParam, et.getCode(), et.getVersion(), sourceResultat, temoin, "toutes", "tous", temoinEtatIae);
                //29/01/10
                //on est dans le cas d'une extraction apogée
                setNotesElpEpr(e, et, cpdto, "AET", annee, true);
            } else {
                //29/01/10
                //On récupère pour tout les états de délibération et on fera le trie après
                List<ContratPedagogiqueResultatElpEprDTO5> cpdto = pedagogiqueService.recupererContratPedagogiqueResultatElpEprV6(e.getCod_etu(), anneeParam, et.getCode(), et.getVersion(), sourceResultat, "AET", "toutes", "tous", temoinEtatIae);
                setNotesElpEpr(e, et, cpdto, temoin, annee, false);
            }


        } catch (Exception ex) {
            //Si on est dans un cas d'erreur non expliqué
            if (ex.getMessage().contains("remoteerror")) {
                LOG.error(ex.getMessage() + " Probleme avec le WS lors de la recherche des notes et résultats a une étape pour etudiant dont codetu est : " + e.getCod_etu(), ex);
            } else {
                LOG.info(ex.getMessage() + " Probleme avec le WS lors de la recherche des notes et résultats a une étape pour etudiant dont codetu est : " + e.getCod_etu(), ex);
            }
        }
    }

    /**
     * va chercher et renseigne les notes de
     * l'étudiant via le WS de l'Amue.
     */
    public void recupererDetailNotesEtResultats(Etudiant e, Etape et, boolean forceSourceApogee, boolean isGestionnaire) {
        try {

            e.getElementsPedagogiques().clear();

            String temoin = null;
            String temoinEtatIae = null;
            String sourceResultat = PropertyUtils.getSourceResultats();

            if (isGestionnaire) {
                temoin = configController.getTemoinNotesGestionnaire();
                temoinEtatIae = configController.getTemoinEtatIaeNotesGestionnaire();
            } else {
                temoin = configController.getTemoinNotesEnseignant();
                temoinEtatIae = configController.getTemoinEtatIaeNotesEnseignant();
            }

            if (temoin == null || temoin.equals("")) {
                temoin = "AET";
            }

            if (temoinEtatIae == null || temoinEtatIae.equals("")) {
                temoinEtatIae = "E";
            }

            if (forceSourceApogee || sourceResultat == null || sourceResultat.equals("")) {
                sourceResultat = Utils.APOGEE;
            }


            //Si on doit se baser sur l'extraction Apogée
            if (utilisationExtractionApogee(et.getAnnee().substring(0, 4), sourceResultat)) {
                //On se base sur l'extraction apogée
                sourceResultat = Utils.APOGEE_EXTRACTION;
                temoin = null;
            } else {
                //On va chercher les résultats directement dans Apogée
                sourceResultat = Utils.APOGEE;
            }


            String anneeParam = et.getAnnee().substring(0, 4);
            int annee = Integer.parseInt(anneeParam);

            // 07/12/11 récupération du fonctionnement identique à la récupéraition des notes pour les étudiants.
            if (sourceResultat.compareTo(Utils.APOGEE_EXTRACTION) == 0) {
                List<ContratPedagogiqueResultatElpEprDTO5> cpdto = pedagogiqueService.recupererContratPedagogiqueResultatElpEprV6(e.getCod_etu(), anneeParam, et.getCode(), et.getVersion(), sourceResultat, temoin, "toutes", "tous", temoinEtatIae);
                setNotesElpEpr(e, et, cpdto, "AET", annee, true);
            } else {
                List<ContratPedagogiqueResultatElpEprDTO5> cpdto = pedagogiqueService.recupererContratPedagogiqueResultatElpEprV6(e.getCod_etu(), anneeParam, et.getCode(), et.getVersion(), sourceResultat, "AET", "toutes", "tous", temoinEtatIae);
                setNotesElpEpr(e, et, cpdto, temoin, annee, false);
            }


        } catch (Exception ex) {
            //Si on est dans un cas d'erreur non expliqué
            if (ex.getMessage().equals("remoteerror")) {
                LOG.error("Probleme avec le WS lors de la recherche des notes et résultats a une étape pour etudiant dont codind est : " + e.getCod_ind(), ex);
            } else {
                LOG.info(ex.getMessage() + " pour etudiant dont codind est : " + e.getCod_ind() + " recupererDetailNotesEtResultatsEnseignant(" + et.getAnnee() + "," + et.getCode() + "/" + et.getVersion() + ")");
            }
        }

    }


    public boolean utilisationExtractionApogee(String annee) {

        int anneeEnCours = new Integer(etudiantController.getAnneeUnivEnCours(GenericUI.getCurrent()));
        int anneeDemandee = new Integer(annee);

        // Si l'application est paramétrée pour utiliser les extractions sur la dernière année ouverte au résultats
        if (configController.isNotesAnneeOuverteResExtractionApogee()) {
            int anneeRes = new Integer(etudiantController.getAnneeUnivRes(GenericUI.getCurrent()));
            // Si l'année en question est la dernière ouverte aux résultats
            if (anneeDemandee == anneeRes) {
                return true;
            }
        }

        String anneePivot = configController.getNotesAnneePivotExtractionApogee();
        // Si l'application est paramétrée pour utiliser une année pivot pour l'utilsation des extractions
        if (StringUtils.hasText(anneePivot)) {
            int premiereAnneePivot = Integer.parseInt(anneePivot);
            // Si l'année en question est supérieure ou égale à la premiere année utilisant l'extraction
            if (anneeDemandee >= premiereAnneePivot) {
                return true;
            }
        }
        // Si l'extraction Apogée couvre l'année demandée
        if (anneeDemandee >= (anneeEnCours - (configController.getNotesNombreAnneesExtractionApogee() - 1))) {
            //On peut se baser sur l'extraction apogée
            return true;
        }

        return false;
    }

    public boolean utilisationExtractionApogee(String annee, String sourceResultat) {
        // Si sourceResultat = apogee-extraction :
        if (sourceResultat.compareTo(Utils.APOGEE_EXTRACTION) == 0) {
            return utilisationExtractionApogee(annee);
        }
        return false;
    }

    public void renseigneNotesEtResultatsEtudiant(Etudiant e) {
        //On regarde si on a pas déjà les infos dans le cache:
        String rang = getRangNotesEtResultatsEnCache(true, e);

        if (rang == null) {
            recupererNotesEtResultatsEtudiant(e);
            //AJOUT DES INFOS recupérées dans le cache. true car on est en vue Etudiant
            ajouterCacheResultatVdiVet(true, e);
        } else {
            //on récupére les infos du cache grace au rang :
            recupererCacheResultatVdiVet(new Integer(rang), e);
        }
    }

    public void renseigneNotesEtResultats(Etudiant e, boolean isGestionnaire) {
        //On regarde si on a pas déjà les infos dans le cache:
        String rang = getRangNotesEtResultatsEnCache(false, e);
        if (rang == null) {
            recupererNotesEtResultats(e, isGestionnaire);
            //AJOUT DES INFOS recupérées dans le cache. true car on est en vue Etudiant
            ajouterCacheResultatVdiVet(false, e);
        } else {
            //on récupére les infos du cache grace au rang :
            recupererCacheResultatVdiVet(new Integer(rang), e);
        }
    }

    public void renseigneDetailInscriptionEtudiant(Etape etape) {
        //Récupération de la source des résultats
        String sourceResultat = PropertyUtils.getSourceResultats();
        if (sourceResultat == null || sourceResultat.equals("")) {
            sourceResultat = Utils.APOGEE;
        }

        //Si on devait se baser sur l'extraction apogée pour récupérer les notes à l'étape
        if (utilisationExtractionApogee(etape.getAnnee().substring(0, 4), sourceResultat)) {
            LOG.info("Méthode de récupération de l'IP basée sur Apogée au lieu de l'extraction");
            //On regarde si on a pas déjà les infos dans le cache:
            String rang = getRangDetailInscriptionEnCache(etape, GenericUI.getCurrent().getEtudiant());

            if (rang == null) {
                recupererDetailNotesEtResultatsEtudiant(GenericUI.getCurrent().getEtudiant(), etape, true);
                //AJOUT DES INFOS recupérées dans le cache.
                ajouterCacheDetailInscription(etape, GenericUI.getCurrent().getEtudiant());
            } else {
                //on récupére les infos du cache grace au rang :
                recupererCacheDetailInscription(new Integer(rang), GenericUI.getCurrent().getEtudiant());
            }

        } else {
            LOG.info("Méthode de récupération de l'IP identique à la récupération des notes");
            //Méthode de récupération de l'IP commune aux notes
            renseigneDetailNotesEtResultatsEtudiant(etape);
        }
    }

    public void renseigneDetailInscription(Etape etape, boolean isGestionnaire) {
        //Récupération de la source des résultats
        String sourceResultat = PropertyUtils.getSourceResultats();
        if (sourceResultat == null || sourceResultat.equals("")) {
            sourceResultat = Utils.APOGEE;
        }

        //Si on devait se baser sur l'extraction apogée pour récupérer les notes à l'étape
        if (utilisationExtractionApogee(etape.getAnnee().substring(0, 4), sourceResultat)) {
            LOG.info("Méthode de récupération de l'IP basée sur Apogée au lieu de l'extraction");
            //On regarde si on a pas déjà les infos dans le cache:
            String rang = getRangDetailInscriptionEnCache(etape, GenericUI.getCurrent().getEtudiant());

            if (rang == null) {
                recupererDetailNotesEtResultats(GenericUI.getCurrent().getEtudiant(), etape, true, isGestionnaire);
                //AJOUT DES INFOS recupérées dans le cache.
                ajouterCacheDetailInscription(etape, GenericUI.getCurrent().getEtudiant());
            } else {
                //on récupére les infos du cache grace au rang :
                recupererCacheDetailInscription(new Integer(rang), GenericUI.getCurrent().getEtudiant());
            }
        } else {
            LOG.info("Méthode de récupération de l'IP identique à la récupération des notes");
            //Méthode de récupération de l'IP commune aux notes
            renseigneDetailNotesEtResultats(etape, isGestionnaire);
        }

    }

    // Renseigne les résultats pour un utiliateur étudiant
    public void renseigneDetailNotesEtResultatsEtudiant(Etape etape) {
        //On regarde si on a pas déjà les infos dans le cache:
        String rang = getRangDetailNotesEtResultatsEnCache(etape, true, GenericUI.getCurrent().getEtudiant());

        if (rang == null) {
            recupererDetailNotesEtResultatsEtudiant(GenericUI.getCurrent().getEtudiant(), etape, false);
            //AJOUT DES INFOS recupérées dans le cache. true car on est en vue Etudiant
            ajouterCacheDetailNotesEtResultats(etape, true, GenericUI.getCurrent().getEtudiant());
        } else {
            //on récupére les infos du cache grace au rang :
            recupererCacheDetailNotesEtResultats(new Integer(rang), GenericUI.getCurrent().getEtudiant());
        }
    }

    // Renseigne les résultats pour un utilisateur enseignant ou un gestionnaire
    public void renseigneDetailNotesEtResultats(Etape etape, boolean isGestionnaire) {
        //On regarde si on a pas déjà les infos dans le cache:
        String rang = getRangDetailNotesEtResultatsEnCache(etape, false, GenericUI.getCurrent().getEtudiant());
        if (rang == null) {
            recupererDetailNotesEtResultats(GenericUI.getCurrent().getEtudiant(), etape, false, isGestionnaire);
            //AJOUT DES INFOS recupérées dans le cache. false car on est en vue Enseignant
            ajouterCacheDetailNotesEtResultats(etape, false, GenericUI.getCurrent().getEtudiant());
        } else {
            //on récupére les infos du cache grace au rang :
            recupererCacheDetailNotesEtResultats(new Integer(rang), GenericUI.getCurrent().getEtudiant());
        }
    }


    /*
     * @param etape
     * @param vueEtudiant
     * @return  le rang dans la liste des IP en cache pour la vueEtudiant
     */
    private String getRangDetailInscriptionEnCache(Etape etape, Etudiant e) {
        int rang = 0;
        boolean enCache = false;

        //on parcourt les IP pour voir si on a ce qu'on cherche:
        for (CacheIP cip : e.getCacheResultats().getIp()) {
            //Si on n'a pas déjà trouvé les infos dans le cache
            if (!enCache) {
                //test si on a les infos:
                if (cip.getEtape().getAnnee().equals(etape.getAnnee())
                        && cip.getEtape().getCode().equals(etape.getCode())
                        && cip.getEtape().getVersion().equals(etape.getVersion())) {
                    enCache = true;
                } else {
                    //on a pas trouvé, on incrémente le rang pour se placer sur le rang suivant
                    rang++;
                }
            }
        }

        //si on a pas les infos en cache:
        if (!enCache) {
            return null;
        }

        return "" + rang;

    }

    /*
     * @param etape
     * @param vueEtudiant
     * @return  le rang dans la liste des Notes et Résultat (aux elp et epr) en cache pour la vueEtudiant
     */
    private String getRangDetailNotesEtResultatsEnCache(Etape etape, boolean vueEtudiant, Etudiant e) {
        int rang = 0;
        boolean enCache = false;

        //on parcourt le résultatElpEpr pour voir si on a ce qu'on cherche:
        for (CacheResultatsElpEpr cree : e.getCacheResultats().getResultElpEpr()) {
            if (!enCache) {
                //si on a déjà les infos:
                if (cree.getEtape().getAnnee().equals(etape.getAnnee())
                        && cree.getEtape().getCode().equals(etape.getCode())
                        && cree.getEtape().getVersion().equals(etape.getVersion())
                        && cree.isVueEtudiant() == vueEtudiant) {
                    enCache = true;
                } else {
                    //on a pas trouvé, on incrémente le rang pour se placer sur le rang suivant
                    rang++;
                }
            }
        }

        //si on a pas les infos en cache:
        if (!enCache) {
            return null;
        }

        return "" + rang;

    }

    /**
     * @param vueEtudiant
     * @return le rang dans la liste des Notes et Résultat (aux diplomes et étapes) en cache pour la vueEtudiant
     */
    private String getRangNotesEtResultatsEnCache(boolean vueEtudiant, Etudiant e) {
        int rang = 0;
        boolean enCache = false;

        //on parcourt le résultatVdiVet pour voir si on a ce qu'on cherche:
        if (e.getCacheResultats() != null && e.getCacheResultats().getResultVdiVet() != null) {
            for (CacheResultatsVdiVet crvv : e.getCacheResultats().getResultVdiVet()) {
                if (!enCache) {
                    //si on a déjà les infos:
                    if (crvv.isVueEtudiant() == vueEtudiant) {
                        enCache = true;
                    } else {
                        //on a pas trouvé, on incrémente le rang pour se placer sur le rang suivant
                        rang++;
                    }
                }
            }
        }
        //si on a pas les infos en cache:
        if (!enCache) {
            return null;
        }

        return "" + rang;

    }


    /**
     * On complète les infos du cache pour les Résultats aux diplomes et étapes.
     *
     * @param vueEtudiant
     */
    public void ajouterCacheResultatVdiVet(boolean vueEtudiant, Etudiant e) {
        CacheResultatsVdiVet crvv = new CacheResultatsVdiVet();
        crvv.setVueEtudiant(vueEtudiant);
        crvv.setDiplomes(new LinkedList<Diplome>(e.getDiplomes()));
        crvv.setEtapes(new LinkedList<Etape>(e.getEtapes()));
        e.getCacheResultats().getResultVdiVet().add(crvv);
    }

    /**
     * On complète les infos du cache pour les Résultats aux elp et epr.
     *
     * @param vueEtudiant
     */
    public void ajouterCacheDetailNotesEtResultats(Etape etape, boolean vueEtudiant, Etudiant e) {
        CacheResultatsElpEpr cree = new CacheResultatsElpEpr();
        cree.setVueEtudiant(vueEtudiant);
        cree.setEtape(etape);
        if (e.getElementsPedagogiques() != null && e.getElementsPedagogiques().size() > 0) {
            cree.setElementsPedagogiques(new LinkedList<ElementPedagogique>(e.getElementsPedagogiques()));
        }
        e.getCacheResultats().getResultElpEpr().add(cree);
    }


    /**
     * On complète les infos du cache pour l'IP.
     */
    public void ajouterCacheDetailInscription(Etape etape, Etudiant e) {
        CacheIP cip = new CacheIP();
        cip.setEtape(etape);
        if (e.getElementsPedagogiques() != null && e.getElementsPedagogiques().size() > 0) {
            cip.setElementsPedagogiques(new LinkedList<ElementPedagogique>(e.getElementsPedagogiques()));
        }
        e.getCacheResultats().getIp().add(cip);
    }


    /**
     * récupère les résultat aux diplomes et etapes dans le cache (en s'indexant sur le rang)
     *
     * @param rang
     */
    private void recupererCacheResultatVdiVet(int rang, Etudiant e) {
        //1-on vide les listes existantes
        if (e.getDiplomes() != null) {
            e.getDiplomes().clear();
        }
        if (e.getEtapes() != null) {
            e.getEtapes().clear();
        }
        //2-on récupère les infos du cache.
        if (e.getCacheResultats().getResultVdiVet().get(rang).getDiplomes() != null) {
            e.setDiplomes(new LinkedList<Diplome>(e.getCacheResultats().getResultVdiVet().get(rang).getDiplomes()));
        }
        if (e.getCacheResultats().getResultVdiVet().get(rang).getEtapes() != null) {
            e.setEtapes(new LinkedList<Etape>(e.getCacheResultats().getResultVdiVet().get(rang).getEtapes()));
        }

    }

    /**
     * récupère les résultat aux Elp et Epr dans le cache (en s'indexant sur le rang)
     *
     * @param rang
     */
    private void recupererCacheDetailNotesEtResultats(int rang, Etudiant e) {
        //1-on vide la liste existante
        if (e.getElementsPedagogiques() != null) {
            e.getElementsPedagogiques().clear();
        }

        //2-on récupère les infos du cache.
        if (e.getCacheResultats().getResultElpEpr().get(rang).getElementsPedagogiques() != null) {
            e.setElementsPedagogiques(new LinkedList<ElementPedagogique>(e.getCacheResultats().getResultElpEpr().get(rang).getElementsPedagogiques()));
        }

    }

    /**
     * récupère les infos sur l'IP dans le cache (en s'indexant sur le rang)
     *
     * @param rang
     */
    private void recupererCacheDetailInscription(int rang, Etudiant e) {
        //1-on vide la liste existante
        if (e.getElementsPedagogiques() != null) {
            e.getElementsPedagogiques().clear();
        }

        //2-on récupère les infos du cache.
        if (e.getCacheResultats().getIp().get(rang).getElementsPedagogiques() != null) {
            e.setElementsPedagogiques(new LinkedList<ElementPedagogique>(e.getCacheResultats().getIp().get(rang).getElementsPedagogiques()));
        }

    }


    public void changerVueNotesEtResultats() {
        if (GenericUI.getCurrent().isVueEnseignantNotesEtResultats()) {
            GenericUI.getCurrent().setVueEnseignantNotesEtResultats(false);
        } else {
            GenericUI.getCurrent().setVueEnseignantNotesEtResultats(true);
        }
    }


    public boolean isAfficherRangElpEpr() {
        List<ElementPedagogique> lelp = GenericUI.getCurrent().getEtudiant().getElementsPedagogiques();
        if (lelp != null && lelp.size() > 0) {
            List<String> codesAutorises = configController.getListeCodesEtapeAffichageRang();
            if (codesAutorises != null && codesAutorises.contains(lelp.get(0).getCode())) {
                return true;
            }
        }
        return false;
    }


}
