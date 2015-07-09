package fr.univlorraine.mondossierweb.services.apogee;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Data;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.controllers.AdresseController;
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
					findObj("toto", 10, true);
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
				rechercherParCode = true;
			}else{
				value=value.replaceAll("\\[", "");
				value=value.replaceAll("\\]", "");

				//on supprime l'étoile
				//value=value.replaceAll("\\*", "");

				//Si la chaine termine par un espace : on a saisi un mot entier (il ne faudra pas mettre d'étoile à la fin de ce mot lors de la recherche)
				if(value.substring(value.length()-1).equals(" ")){
					lastWordCompletion = "";
				}
				value = value.trim();

				//On remplace les tirets par espace
				//value=value.replaceAll("-", " ");

				value=value.replaceAll("-", "\\-");
			}
			//On passe la value en minuscule
			value=value.toLowerCase();


			/*

		//TESTS QUERY ALTERNATIVES via Suggestion

		CompletionSuggestionBuilder completionSuggestionBuilder = new CompletionSuggestionBuilder("element");
		completionSuggestionBuilder.text(value);
		completionSuggestionBuilder.field(PropertyUtils.getElasticSearchChampRecherche());
		SuggestRequestBuilder  suggestRequestBuilder  = client.prepareSuggest("apogee").addSuggestion(completionSuggestionBuilder);
		//.execute().actionGet();

		//execute and get response
		SuggestResponse suggestresponse = suggestRequestBuilder.execute().actionGet();

		//get suggests from the response
		Suggest suggest = suggestresponse.getSuggest();
		LOG.debug("suggest : "+suggest.toString()+" size : "+suggest.size());

		CompletionSuggestion compsuggestion = suggest.getSuggestion("element");
		if(compsuggestion!=null){
			List<CompletionSuggestion.Entry> entryList = compsuggestion.getEntries();
			if(entryList != null) {
				CompletionSuggestion.Entry entry = entryList.get(0);
				List<CompletionSuggestion.Entry.Option> options =entry.getOptions();
				if(options != null)  {
					CompletionSuggestion.Entry.Option option = options.get(0);
					LOG.debug(""+option.getText().string());
				}
			}
		}else{
			LOG.debug("compsuggestion null");
		}

			 */

			QueryBuilder qb;
			SearchHit[] results=null;
			SearchResponse response=null;

			//On ne cherchepas uniquement par le code
			if(!rechercherParCode){
				//Configuration de la query (matchQuery) sur le champ de recherche défini dans context.xml
				qb=QueryBuilders.matchQuery(PropertyUtils.getElasticSearchChampRecherche(), value);

				//Si on est en recherche rapide (après avoir entrée une lettre dans le champAutoComplete)
				if(quickSearck){
					//On paramètre la query différemment (matchPhrasePrefixQuery ou fuzzyQuery)
					//qb=QueryBuilders.matchPhrasePrefixQuery(PropertyUtils.getElasticSearchChampRecherche(), value);
					//qb=QueryBuilders.fuzzyQuery(PropertyUtils.getElasticSearchChampRecherche(), value);
					//qb=QueryBuilders.queryString("default_field : "+PropertyUtils.getElasticSearchChampRecherche()+", query : "+value);
					//qb=QueryBuilders.wildcardQuery(PropertyUtils.getElasticSearchChampRecherche(), value+"*");

					//qb=QueryBuilders.boolQuery().must(QueryBuilders.wildcardQuery(PropertyUtils.getElasticSearchChampRecherche(), "dubois")).must(QueryBuilders.wildcardQuery(PropertyUtils.getElasticSearchChampRecherche(), "c*"));

					String[] mots = value.split(" ");

					//Test si il n'y a qu'un seul mot de saisi
					if(mots.length==1){
						//Il n'y a qu'un mot. On l'ajoute avec * si il n'y avait pas d'espace apres (lastWordCompletion)
						qb=QueryBuilders.boolQuery().must(QueryBuilders.wildcardQuery(PropertyUtils.getElasticSearchChampRecherche(), mots[0]+lastWordCompletion));

					}else{
						//On init la query avec le premier mot
						BoolQueryBuilder bqb=QueryBuilders.boolQuery().must(QueryBuilders.wildcardQuery(PropertyUtils.getElasticSearchChampRecherche(), mots[0]));

						for(int i=1; i<mots.length;i++){
							//Si ce n'est pas le dernier mot
							if(i<(mots.length-1)){
								bqb.must(QueryBuilders.wildcardQuery(PropertyUtils.getElasticSearchChampRecherche(), mots[i]));
							}else{
								//c'est le dernier, on ajoute * si il n'y avait pas d'espace apres (lastWordCompletion)
								bqb.must(QueryBuilders.wildcardQuery(PropertyUtils.getElasticSearchChampRecherche(), mots[i]+lastWordCompletion));
							}
						}
						qb = bqb;

					}
				}

				/*
			 Tests d'autres types de query

			QueryBuilder qb =QueryBuilders.multiMatchQuery(value,PropertyUtils.getElasticSearchChampRecherche(),"COD_OBJ");
			QueryBuilder qb = QueryBuilders.termQuery(PropertyUtils.getElasticSearchChampRecherche(), value);
			fuzzy gere les fautes de frappes
			QueryBuilder qb = QueryBuilders.fuzzyQuery(PropertyUtils.getElasticSearchChampRecherche(), value);
			QueryBuilder qb = QueryBuilders.fuzzyQuery("LIB_OBJ", value);

				 */

				//Execution de la requête
				response = client.prepareSearch(PropertyUtils.getElasticSearchIndex())
						.setSearchType(SearchType.QUERY_AND_FETCH)
						.setTypes(Utils.CMP,Utils.VET,Utils.ELP,Utils.ETU)
						.setQuery(qb)
						.setFrom(0).setSize(60).setExplain(true)
						.execute()
						.actionGet();
				
				//Récupération des résultats dans un tableau
				results = response.getHits().getHits();
			}

			//Si aucun resultat et qu'on n'est pas en quicksearch OU qu'on recherche par code uniquement
			if(rechercherParCode || (results==null || results.length==0) && !quickSearck){
				//On cherche via le code (cas des elp)
				qb=QueryBuilders.matchQuery("COD_OBJ", value);
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

				if(listeResultats.size()<(maxResult + 1)){
					//Récupération du résultat
					Map<String,Object> result = hit.getSource();  
					
					//On rajoute le type de l'objet dans la hashMap retournée
					result.put("TYP_OBJ", hit.getType());

					//Si recherche par code uniquement on ne garde que les éléments qui matchent vraiment avec la valeur saisie
					if(!rechercherParCode){
						listeResultats.add(result);
					}else{

						String codres = (String)result.get("COD_OBJ");
						codres = codres.toLowerCase();

						int vrsres =(Integer)result.get("COD_VRS_OBJ");

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
