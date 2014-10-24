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
public class SolrServiceImpl implements SolrService{

	//private String url = "http://ldn-dubois36.univ-lorraine.fr:8181/solr/apogee";
	private HttpSolrServer server;
	private SolrQuery query;
	private final static int MAX_RESULTS = 100;
	private Client client;


	@Override
	public List<Map<String,Object>> findObj(String value, int maxResult, boolean quickSearck) {
		value=value.replaceAll("\\[", "");
		value=value.replaceAll("\\]", "");

		/*
		List<ObjSolr> beans=new LinkedList<ObjSolr>();

		if(server == null){
			server = new HttpSolrServer( PropertyUtils.getSolrUrl() );

			server.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
			server.setConnectionTimeout(5000); // 5 seconds to establish TCP

			server.setParser(new XMLResponseParser()); // binary parser is used by default
			server.setSoTimeout(5000);  // socket read timeout
			server.setDefaultMaxConnectionsPerHost(100);
			server.setMaxTotalConnections(100);
			server.setFollowRedirects(false);  // defaults to false
			server.setAllowCompression(true);


		}
		if(query == null){
			query = new SolrQuery();
			query.setStart(0);
		}
		if(maxResult>0){
			query.setRows(maxResult);
		}else{
			query.setRows(MAX_RESULTS);
		}

		query.setQuery( value);

		try {

			QueryResponse rsp =  server.query(query);
			beans = rsp.getBeans(ObjSolr.class);

		} catch (SolrServerException e) {
			e.printStackTrace();
		}

		 */

		//ElasticSearch
		List<Map<String,Object>> beans=new LinkedList<Map<String,Object>>();

		//supprime l'etoile
		value=value.replaceAll("\\*", "");

		//Comment échapper les '+' des codes ELP ex : +CML4151 ????
		// Et evenuellemnt le slash des VET

		value=value.toLowerCase();

		System.out.println("value Elastic Search for : "+value);
		if(client==null){
			Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", PropertyUtils.getElasticSearchCluster()).build();
			client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(PropertyUtils.getElasticSearchUrl(), PropertyUtils.getElasticSearchPort()));
		}


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
		//System.out.println("status response : "+response.status()+" "+response.getTotalShards()+" "+response.getSuccessfulShards()+" "+response.getHits().getTotalHits());
		SearchHit[] results = response.getHits().getHits();
		for (SearchHit hit : results) {
			//prints out the id of the document
			Map<String,Object> result = hit.getSource();   //the retrieved document
			beans.add(result);
			//System.out.println(hit.getId()+" -> "+result.get("COD_OBJ")+" : "+result.get(PropertyUtils.getElasticSearchChampRecherche())+" "+hit.getScore());
		}
		return beans;
	}







}
