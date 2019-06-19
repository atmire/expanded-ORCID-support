package com.atmire.dspace.authority;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.AuthorityValueServiceImpl;
import org.dspace.authority.orcid.Orcidv2AuthorityValue;
import org.dspace.content.authority.SolrAuthority;
import org.dspace.core.Context;
import org.dspace.core.LogManager;

import java.util.ArrayList;
import java.util.List;

import static org.apache.log4j.Logger.getLogger;

public class OrcidAuthorityValueServiceImpl extends AuthorityValueServiceImpl {

    private Logger log = getLogger(OrcidAuthorityValueServiceImpl.class);

    protected List<AuthorityValue> find(Context context, String queryString) {
        List<AuthorityValue> findings = new ArrayList<AuthorityValue>();
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(filtered(queryString));
            log.debug("AuthorityValueFinder makes the query: " + queryString);
            QueryResponse queryResponse = SolrAuthority.getSearchService().search(solrQuery);
            if (queryResponse != null && queryResponse.getResults() != null && 0 < queryResponse.getResults().getNumFound()) {
                for (SolrDocument document : queryResponse.getResults()) {
                    AuthorityValue authorityValue;
                    if ("orcid".equals(document.getFieldValue("authority_type"))) {
                        authorityValue = new Orcidv2AuthorityValue(document);
                        ((Orcidv2AuthorityValue) authorityValue).setOrcid_id((String) document.getFieldValue("orcid_id"));
                    } else { authorityValue = new AuthorityValue(document);
                    }
                    findings.add(authorityValue);
                    log.debug("AuthorityValueFinder found: " + authorityValue.getValue());
                }
            }
        } catch (Exception e) {
            log.error(LogManager.getHeader(context, "Error while retrieving AuthorityValue from solr", "query: " + queryString),e);
        }

        return findings;
    }
}
