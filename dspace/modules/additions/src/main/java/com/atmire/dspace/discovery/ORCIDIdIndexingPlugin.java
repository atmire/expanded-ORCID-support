package com.atmire.dspace.discovery;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.PersonAuthorityValue;
import org.dspace.authority.factory.AuthorityServiceFactory;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.discovery.SolrServiceIndexPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by jonas - jonas@atmire.com on 07/06/2018.
 */
public class ORCIDIdIndexingPlugin implements SolrServiceIndexPlugin {

    private static final String ORCID_ID_FIELD = "orcid_id";

    @Override
    public void additionalIndex(Context context, DSpaceObject dso, SolrInputDocument document) {
        if (dso != null && dso.getType() == Constants.ITEM) {
            Item item = (Item) dso;

            List<String> personAuthorityFields = new ArrayList<>();
            for (Map.Entry<String, AuthorityValue> fieldDefaults : AuthorityServiceFactory.getInstance().getAuthorTypes().getFieldDefaults().entrySet()) {
                if (fieldDefaults.getValue() instanceof PersonAuthorityValue) {
                    personAuthorityFields.add(fieldDefaults.getKey());
                }
            }

            for (String personAuthorityFieldKey : personAuthorityFields) {
                String[] split = personAuthorityFieldKey.split("_");
                List<MetadataValue> metadata = ContentServiceFactory.getInstance().getItemService().getMetadata(item, split[0], split[1], (split.length > 2) ? split[2] : null, Item.ANY);
                for (MetadataValue metadataValue : metadata) {
                    checkAndIndexMetadata(context, document, metadataValue);
                }
            }

        }
    }

    private void checkAndIndexMetadata(Context context, SolrInputDocument document, MetadataValue metadatum) {
        String authority = metadatum.getAuthority();
        if (StringUtils.isNotBlank(authority)) {

            AuthorityValue value = AuthorityServiceFactory.getInstance().getAuthorityValueService().findByUID(context, authority);
            if (value != null) {
                String orcidId = (String) value.getSolrInputDocument().getFieldValue(ORCID_ID_FIELD);
                if (StringUtils.isNotBlank(orcidId)) {
                    Collection<Object> fieldValues = document.getFieldValues(ORCID_ID_FIELD);
                    if (fieldValues == null || !fieldValues.contains(orcidId)) {
                        document.addField(ORCID_ID_FIELD, orcidId);
                    }
                }
            }
        }
    }
}
