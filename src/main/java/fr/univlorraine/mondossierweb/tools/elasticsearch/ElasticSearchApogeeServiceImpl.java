package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Recherche dans Elasticsearch via REST, compatible ES 2.x et ES 8/9. */
public class ElasticSearchApogeeServiceImpl implements ElasticSearchApogeeService {

	private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchApogeeServiceImpl.class);

	private final ObjectMapper mapper = new ObjectMapper();

	private String typCmp = "";
	private String typVet = "";
	private String typElp = "";
	private String typEtu = "";
	private String esTyp = "";

	private EsRestClient client;
	private EsQueryStrategy strategy;

	private String elasticSearchCluster;
	private String elasticSearchUrl;
	private int elasticSearchPort;
	private String elasticSearchIndex;
	private String elasticSearchChampRecherche;
	private String elasticSearchChampCodeObjet;
	private String elasticSearchChampVersionObjet;
	private String elasticSearchChampTypeObjet;

	public ElasticSearchApogeeServiceImpl() {
		super();
	}

	@Override
	public boolean initConnexion() {
		return initConnexion(elasticSearchUrl, elasticSearchPort, elasticSearchCluster);
	}

	@Override
	public boolean initConnexion(final String url, final int port, final String cluster) {
		if (client == null) {
			try {
				final EsRestClient newClient = new EsRestClient(url, port);
				final String number = mapper.readTree(newClient.getRoot()).path("version").path("number").asText();
				final EsVersion version = EsVersion.fromNumber(number);
				final List<String> types = List.of(typCmp, typVet, typElp, typEtu);
				strategy = version == EsVersion.V2
					? new Es2QueryStrategy(types, mapper)
					: new Es8QueryStrategy(elasticSearchChampTypeObjet, types, mapper);
				client = newClient;
				LOG.info("Connexion Elasticsearch établie (version {}, stratégie {})", number, version);
			} catch (final Exception ex) {
				LOG.error("problème lors de l'initialisation de la connexion à ElasticSearch", ex);
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Map<String, Object>> findObj(final String value, final int maxResult, final boolean quickSearck) {
		return findObj(value, maxResult, quickSearck, elasticSearchIndex, elasticSearchChampRecherche,
			elasticSearchChampCodeObjet, elasticSearchChampVersionObjet);
	}

	@Override
	public List<Map<String, Object>> findObj(final String value, final int maxResult, final boolean quickSearck,
		final String elasticSearchIndex, final String elasticSearchChampRecherche,
		final String elasticSearchChampCodeObjet, final String elasticSearchChampVersionObjet) {

		initConnexion();

		final List<Map<String, Object>> listeResultats = new LinkedList<>();

		if (StringUtils.hasText(value)) {
			try {
				boolean rechercherParCode = false;
				String lastWordCompletion = "*";

				String v = value.replaceAll("-", " ");
				v = v.replaceAll("\\(", " ");
				v = v.replaceAll("\\)", " ");
				while (v.contains("  ")) {
					v = v.replaceAll("  ", " ");
				}

				if (v.contains("[") && v.contains("]")) {
					v = v.replaceAll("\\[", "");
					v = v.split("\\]")[0];
					rechercherParCode = true;
				} else {
					v = v.replaceAll("\\[", "");
					v = v.replaceAll("\\]", "");
					if (v.substring(v.length() - 1).equals(" ")) {
						lastWordCompletion = "";
					}
					v = v.trim();
					v = v.replaceAll("-", "\\-");
				}
				v = v.toLowerCase();

				List<SearchHit> results = null;

				if (!rechercherParCode) {
					ObjectNode qb = EsQueryBuilder.match(elasticSearchChampRecherche, v);

					if (quickSearck) {
						final String[] mots = v.split(" ");
						if (mots.length == 1) {
							qb = EsQueryBuilder.wildcard(elasticSearchChampRecherche, mots[0] + lastWordCompletion);
						} else {
							final List<ObjectNode> clauses = new ArrayList<>();
							clauses.add(EsQueryBuilder.match(elasticSearchChampRecherche, mots[0]));
							for (int i = 1; i < mots.length; i++) {
								if (i < (mots.length - 1)) {
									clauses.add(EsQueryBuilder.match(elasticSearchChampRecherche, mots[i]));
								} else {
									clauses.add(EsQueryBuilder.wildcard(elasticSearchChampRecherche, mots[i] + lastWordCompletion));
								}
							}
							qb = EsQueryBuilder.boolMust(clauses);
						}
					}

					results = execute(elasticSearchIndex, qb);
				}

				if (rechercherParCode || ((results == null || results.isEmpty()) && !quickSearck)) {
					final ObjectNode qb = EsQueryBuilder.match(elasticSearchChampCodeObjet, v);
					results = execute(elasticSearchIndex, qb);
				}

				for (final SearchHit hit : results) {
					if (listeResultats.size() < (maxResult + 1)) {
						final Map<String, Object> result = hit.source();
						result.put(esTyp, hit.type());

						if (!rechercherParCode) {
							listeResultats.add(result);
						} else {
							final Object codresObj = result.get(elasticSearchChampCodeObjet);
							final Object vrsresObj = result.get(elasticSearchChampVersionObjet);
							if (codresObj == null || vrsresObj == null) {
								continue;
							}
							final String codres = codresObj.toString().toLowerCase();
							final int vrsres = ((Number) vrsresObj).intValue();
							if (!v.contains("/") && codres.equals(v)) {
								listeResultats.add(result);
							}
							if (v.contains("/") && (codres + "/" + vrsres).equals(v)) {
								listeResultats.add(result);
							}
						}
					}
				}
			} catch (final Exception ex) {
				LOG.error("Erreur lors de la recherche ElasticSearch", ex);
				return new LinkedList<>();
			}
		}

		return listeResultats;
	}

	private List<SearchHit> execute(final String index, final ObjectNode queryNode) throws Exception {
		final String body = strategy.buildBody(queryNode);
		return strategy.parse(client.search(strategy.searchPath(index), body));
	}

	// --- getters / setters ---

	public String getTypCmp() {
		return typCmp;
	}

	public void setTypCmp(final String typCmp) {
		this.typCmp = typCmp;
	}

	public String getTypVet() {
		return typVet;
	}

	public void setTypVet(final String typVet) {
		this.typVet = typVet;
	}

	public String getTypElp() {
		return typElp;
	}

	public void setTypElp(final String typElp) {
		this.typElp = typElp;
	}

	public String getTypEtu() {
		return typEtu;
	}

	public void setTypEtu(final String typEtu) {
		this.typEtu = typEtu;
	}

	public String getEsTyp() {
		return esTyp;
	}

	public void setEsTyp(final String esTyp) {
		this.esTyp = esTyp;
	}

	public String getElasticSearchCluster() {
		return elasticSearchCluster;
	}

	public void setElasticSearchCluster(final String elasticSearchCluster) {
		this.elasticSearchCluster = elasticSearchCluster;
	}

	public String getElasticSearchUrl() {
		return elasticSearchUrl;
	}

	public void setElasticSearchUrl(final String elasticSearchUrl) {
		this.elasticSearchUrl = elasticSearchUrl;
	}

	public int getElasticSearchPort() {
		return elasticSearchPort;
	}

	public void setElasticSearchPort(final int elasticSearchPort) {
		this.elasticSearchPort = elasticSearchPort;
	}

	public String getElasticSearchIndex() {
		return elasticSearchIndex;
	}

	public void setElasticSearchIndex(final String elasticSearchIndex) {
		this.elasticSearchIndex = elasticSearchIndex;
	}

	public String getElasticSearchChampRecherche() {
		return elasticSearchChampRecherche;
	}

	public void setElasticSearchChampRecherche(final String elasticSearchChampRecherche) {
		this.elasticSearchChampRecherche = elasticSearchChampRecherche;
	}

	public String getElasticSearchChampCodeObjet() {
		return elasticSearchChampCodeObjet;
	}

	public void setElasticSearchChampCodeObjet(final String elasticSearchChampCodeObjet) {
		this.elasticSearchChampCodeObjet = elasticSearchChampCodeObjet;
	}

	public String getElasticSearchChampVersionObjet() {
		return elasticSearchChampVersionObjet;
	}

	public void setElasticSearchChampVersionObjet(final String elasticSearchChampVersionObjet) {
		this.elasticSearchChampVersionObjet = elasticSearchChampVersionObjet;
	}

	public String getElasticSearchChampTypeObjet() {
		return elasticSearchChampTypeObjet;
	}

	public void setElasticSearchChampTypeObjet(final String elasticSearchChampTypeObjet) {
		this.elasticSearchChampTypeObjet = elasticSearchChampTypeObjet;
	}
}
