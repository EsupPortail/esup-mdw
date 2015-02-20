package fr.univlorraine.mondossierweb.services.apogee;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Data;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.entities.solr.ObjSolr;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryBuilders.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;
import org.elasticsearch.search.suggest.Suggester;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;


@Component
@Data
public class ElasticSearchServiceImpl implements ElasticSearchService{

	//private String url = "http://ldn-dubois36.univ-lorraine.fr:8181/solr/apogee";
	private HttpSolrServer server;
	private SolrQuery query;
	private final static int MAX_RESULTS = 100;
	private Client client;


	@Override
	public void initConnexion() {
		//initialise la connexion a ES
		if(client==null){
			Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", PropertyUtils.getElasticSearchCluster()).build();
			client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(PropertyUtils.getElasticSearchUrl(), PropertyUtils.getElasticSearchPort()));
		}

	}


	@Override
	public List<Map<String,Object>> findObj(String value, int maxResult, boolean quickSearck) {
		//initialise la connexion a ES
		if(client==null){
			Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", PropertyUtils.getElasticSearchCluster()).build();
			client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(PropertyUtils.getElasticSearchUrl(), PropertyUtils.getElasticSearchPort()));
		}

		//init du retour d'ElasticSearch
		List<Map<String,Object>> beans=new LinkedList<Map<String,Object>>();

		//Si value a du texte
		if(StringUtils.hasText(value)){
			value=value.replaceAll("\\[", "");
			value=value.replaceAll("\\]", "");




			//supprime l'etoile
			value=value.replaceAll("\\*", "");

			//Comment échapper les '+' des codes ELP ex : +CML4151 ????
			// Et evenuellemnt le slash des VET

			value=value.toLowerCase();





			/*
		CompletionSuggestionBuilder completionSuggestionBuilder = new CompletionSuggestionBuilder("element");
		completionSuggestionBuilder.text(value);
		completionSuggestionBuilder.field(PropertyUtils.getElasticSearchChampRecherche());
		SuggestRequestBuilder  suggestRequestBuilder  = client.prepareSuggest("apogee").addSuggestion(completionSuggestionBuilder);
		//.execute().actionGet();

		//execute and get response
		SuggestResponse suggestresponse = suggestRequestBuilder.execute().actionGet();

		//get suggests from the response
		Suggest suggest = suggestresponse.getSuggest();
		System.out.println("suggest : "+suggest.toString()+" size : "+suggest.size());

		CompletionSuggestion compsuggestion = suggest.getSuggestion("element");
		if(compsuggestion!=null){
			List<CompletionSuggestion.Entry> entryList = compsuggestion.getEntries();
			if(entryList != null) {
				CompletionSuggestion.Entry entry = entryList.get(0);
				List<CompletionSuggestion.Entry.Option> options =entry.getOptions();
				if(options != null)  {
					CompletionSuggestion.Entry.Option option = options.get(0);
					System.out.println(""+option.getText().string());
				}
			}
		}else{
			System.out.println("compsuggestion null");
		}

			 */

			QueryBuilder qb;
			qb=QueryBuilders.matchQuery(PropertyUtils.getElasticSearchChampRecherche(), value);

			if(quickSearck){
				//qb=QueryBuilders.fuzzyQuery(PropertyUtils.getElasticSearchChampRecherche(), value);
				qb=QueryBuilders.matchPhrasePrefixQuery(PropertyUtils.getElasticSearchChampRecherche(), value);
			}
			//QueryBuilder qb =QueryBuilders.multiMatchQuery(value,PropertyUtils.getElasticSearchChampRecherche(),"COD_OBJ");
			//QueryBuilder qb = QueryBuilders.termQuery(PropertyUtils.getElasticSearchChampRecherche(), value);
			//fuzzy gere les fautes de frappes
			//QueryBuilder qb = QueryBuilders.fuzzyQuery(PropertyUtils.getElasticSearchChampRecherche(), value);
			//QueryBuilder qb = QueryBuilders.fuzzyQuery("LIB_OBJ", value);

			SearchResponse response = client.prepareSearch(PropertyUtils.getElasticSearchIndex())
					.setSearchType(SearchType.QUERY_AND_FETCH)
					.setQuery(qb)
					.setFrom(0).setSize(60).setExplain(true)
					.execute()
					.actionGet();
			SearchHit[] results = response.getHits().getHits();

			//Si aucun resultat et on est pas en quicksearch
			if((results==null || results.length==0) && !quickSearck){
				//On cherche le code de l'elp
				qb=QueryBuilders.matchQuery("COD_OBJ", value);
				response = client.prepareSearch(PropertyUtils.getElasticSearchIndex())
						.setSearchType(SearchType.QUERY_AND_FETCH)
						.setQuery(qb)
						.setFrom(0).setSize(60).setExplain(true)
						.execute()
						.actionGet();
				results = response.getHits().getHits();
			}


			for (SearchHit hit : results) {
				//prints out the id of the document
				Map<String,Object> result = hit.getSource();   //the retrieved document
				beans.add(result);
			}
			return beans;
		}

		return beans;
	}












}
