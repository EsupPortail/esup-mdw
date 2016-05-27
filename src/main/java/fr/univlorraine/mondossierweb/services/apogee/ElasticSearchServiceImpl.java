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
package fr.univlorraine.mondossierweb.services.apogee;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Data;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;


@Component
@Data
public class ElasticSearchServiceImpl implements ElasticSearchService{

	private Logger LOG = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);


	private Client client;


	@SuppressWarnings("resource")
	@Override
	public boolean initConnexion(boolean fullInit) {
		//initialise la connexion a ES
		if(client==null){
			try{
				Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", PropertyUtils.getElasticSearchCluster()).build();
				client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(PropertyUtils.getElasticSearchUrl(), PropertyUtils.getElasticSearchPort()));

				//Si on doit faire un init complet (avec requête à ES)
				if(fullInit){
					//requete pour initialiser l'appel à ES 
					findObj("toto54titi", 10, true);
				}
				//}catch(NoNodeAvailableException ex){
			}catch(Exception ex){
				LOG.error("problème lors de l'initialisation de la connexion à ElasticSerch", ex);
				return false;
			}
		}
		return true;
	}


	@Override
	public List<Map<String,Object>> findObj(String value, int maxResult, boolean quickSearck) {
		//initialise la connexion a ES
		initConnexion(false);

		//init du retour d'ElasticSearch
		List<Map<String,Object>> listeResultats=new LinkedList<Map<String,Object>>();

		//Si value a du texte
		if(StringUtils.hasText(value)){
			boolean rechercherParCode = false;
			String lastWordCompletion = "*";

			//On supprime les crochets dans le cas où la value soit une des lignes proposée par le champ AutoComplete (contenant le code de l'élément entre cochets)
			if(value.contains("[") && value.contains("]")){
				value=value.replaceAll("\\[", "");
				
				//Recuperation du code
				value = value.split("\\]")[0];
				//On a un code, on va donc chercher dans ES uniquement par le code
				rechercherParCode = true;
			}else{
				value=value.replaceAll("\\[", "");
				value=value.replaceAll("\\]", "");

				//Si la chaine termine par un espace : on a saisi un mot entier (il ne faudra pas mettre d'étoile à la fin de ce mot lors de la recherche)
				if(value.substring(value.length()-1).equals(" ")){
					lastWordCompletion = "";
				}
				value = value.trim();
				value=value.replaceAll("-", "\\-");
			}
			//On passe la value en minuscule
			value=value.toLowerCase();

			//On prépare la query
			QueryBuilder qb;
			SearchHit[] results=null;
			SearchResponse response=null;

			//Si on ne cherche pas uniquement par le code
			if(!rechercherParCode){
				//Configuration de la query (matchQuery) sur le champ de recherche défini dans context.xml
				qb=QueryBuilders.matchQuery(PropertyUtils.getElasticSearchChampRecherche(), value);

				//Si on est en recherche rapide (après avoir entrée une lettre dans le champAutoComplete)
				if(quickSearck){

					//On découpe les mots de la chaine saisie par l'utilisateur
					String[] mots = value.split(" ");

					//Test si il n'y a qu'un seul mot de saisi
					if(mots.length==1){
						
						//Il n'y a qu'un mot. On l'ajoute avec * si il n'y avait pas d'espace apres (lastWordCompletion)
						qb=QueryBuilders.boolQuery().must(QueryBuilders.wildcardQuery(PropertyUtils.getElasticSearchChampRecherche(), mots[0]+lastWordCompletion));

					}else{
						
						//On initialise la query avec le premier mot
						BoolQueryBuilder bqb=QueryBuilders.boolQuery().must(QueryBuilders.matchQuery(PropertyUtils.getElasticSearchChampRecherche(), mots[0]));

						//Pour chaqu'un des mots suivant
						for(int i=1; i<mots.length;i++){
							
							//Si ce n'est pas le dernier mot
							if(i<(mots.length-1)){
								//On ajoute simplement le mot dans la recherche
								bqb.must(QueryBuilders.matchQuery(PropertyUtils.getElasticSearchChampRecherche(), mots[i]));
							}else{
								//c'est le dernier, on ajoute * si il n'y avait pas d'espace apres (lastWordCompletion)
								bqb.must(QueryBuilders.wildcardQuery(PropertyUtils.getElasticSearchChampRecherche(), mots[i]+lastWordCompletion));
							}
						}
						qb = bqb;

					}
				}

				//Execution de la requête sur les composantes, les VET et les étudiants
				response = client.prepareSearch(PropertyUtils.getElasticSearchIndex())
						.setSearchType(SearchType.QUERY_AND_FETCH)
						.setTypes(Utils.CMP,Utils.VET,Utils.ETU)
						.setQuery(qb)
						.setFrom(0).setSize(60).setExplain(true)
						.execute()
						.actionGet();
				
				//Récupération des résultats dans un tableau
				results = response.getHits().getHits();
			}

			//Si aucun resultat et qu'on n'est pas en quicksearch OU qu'on recherche par code uniquement
			if(rechercherParCode || (results==null || results.length==0) && !quickSearck){
				
				//On cherche via le code (cas des elp notamment) sur composantes, vet, etudiants et elp.
				qb=QueryBuilders.matchQuery(PropertyUtils.getElasticSearchChampCodeObjet(), value);
				response = client.prepareSearch(PropertyUtils.getElasticSearchIndex())
						.setSearchType(SearchType.QUERY_AND_FETCH)
						.setTypes(Utils.CMP,Utils.VET,Utils.ELP,Utils.ETU)
						.setQuery(qb)
						.setFrom(0).setSize(60).setExplain(true)
						.execute()
						.actionGet();
				results = response.getHits().getHits();
			}

			//Pour chaque résultat du tableau de résultats
			for (SearchHit hit : results) {

				//Si on n'a pas encore récupéré le nombre de résultats demandés
				if(listeResultats.size()<(maxResult + 1)){
					//Récupération du résultat
					Map<String,Object> result = hit.getSource();  
					
					//On rajoute le type de l'objet dans la hashMap retournée
					result.put(Utils.ES_TYPE, hit.getType());

					//Si recherche par code uniquement on ne garde que les éléments qui matchent vraiment avec la valeur saisie
					if(!rechercherParCode){
						//On ne recherche pas obligatoirement par code, on garde tous les résultats
						listeResultats.add(result);
					}else{
						//On ne garde que les éléments qui matchent vraiment avec le code saisi par l'utilisateur
						
						//Récupération du code de l'objet
						String codres = (String)result.get(PropertyUtils.getElasticSearchChampCodeObjet());
						codres = codres.toLowerCase();
						
						//Récupération de la version
						int vrsres =(Integer)result.get(PropertyUtils.getElasticSearchChampVersionObjet());

						//cas hors VET
						if(!value.contains("/") && codres.equals(value)){
							//Ajout du résultat dans la liste
							listeResultats.add(result);
						}
						//cas VET
						if(value.contains("/") && (codres+"/"+vrsres).equals(value)){
							//Ajout du résultat dans la liste
							listeResultats.add(result);
						}

					}
				}
			}

		}

		//On retourne la liste de résultats
		return listeResultats;
	}












}
