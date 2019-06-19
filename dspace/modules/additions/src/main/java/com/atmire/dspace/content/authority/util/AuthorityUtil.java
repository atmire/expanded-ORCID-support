package com.atmire.dspace.content.authority.util;

import org.apache.log4j.Logger;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.AuthorityValueFinder;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.authority.orcid.Orcidv2;
import org.dspace.authority.orcid.Orcidv2AuthorityValue;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.Choices;
import org.dspace.core.Context;
import org.dspace.utils.DSpace;

import java.util.Date;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.log4j.Logger.getLogger;

public class AuthorityUtil {

    protected static final Logger log = getLogger(AuthorityUtil.class);

    private AuthorityIndexingService indexingService = new DSpace().getServiceManager()
            .getServiceByName(AuthorityIndexingService.class.getName(), AuthorityIndexingService.class);

    public void addMetadataWithOrcid(Context context, Item item, Metadatum metadatum) {

        String orcidID = metadatum.authority;
        String orcidAuthority = null;

        if (isOrcidFormat(orcidID)) {

            orcidAuthority = new AuthorityValueFinder().findByOrcidID(context, orcidID).getId();
            if (orcidAuthority == null) {
                orcidAuthority = createOrcidAuthority(metadatum, orcidID);
            }
        }

        if (orcidAuthority != null) {
            item.addMetadata(metadatum.schema, metadatum.element, metadatum.qualifier, metadatum.language,
                    metadatum.value, orcidAuthority, Choices.CF_ACCEPTED);

        } else {
            item.addMetadata(metadatum.schema, metadatum.element, metadatum.qualifier, metadatum.language,
                    metadatum.value);
        }
    }

    private String createOrcidAuthority(final Metadatum metadatum, final String orcidID) {

        return createSolrOrcidAuthority(metadatum, orcidID);
    }

    private String createSolrOrcidAuthority(final Metadatum metadatum, final String orcidID) {

        Orcidv2AuthorityValue authorityValue = Orcidv2AuthorityValue.create();
        authorityValue.setValue(metadatum.value);

        return updateOrcidAuthorityValue(orcidID, authorityValue);
    }

    public boolean isOrcidFormat(final String authority) {

        return isNotBlank(authority) && authority.matches("\\d{4}-\\d{4}-\\d{4}-(\\d{3}X|\\d{4})");
    }

    private String updateOrcidAuthorityValue(String orcidID, Orcidv2AuthorityValue value) {

        if (value == null) {
            return null;
        }

        value.setOrcid_id(orcidID);
        Date now = new Date();
        value.setLastModified(now);
        value.setCreationDate(now);
        indexingService.indexContent(value, false);
        indexingService.commit();
        return value.getOrcid_id();
    }

    public void addMetadataWithAuthority(Context context, Item item, final Metadatum metadatum) {
        String authorityValue = getAuthorityValue(context, metadatum);
        if (authorityValue != null) {
            item.addMetadata(metadatum.schema, metadatum.element, metadatum.qualifier, metadatum.language,
                    authorityValue, metadatum.authority, Choices.CF_ACCEPTED);
        } else {
            item.addMetadata(metadatum.schema, metadatum.element, metadatum.qualifier, metadatum.language,
                    metadatum.value);
        }
    }

    private String getAuthorityValue(Context context, final Metadatum metadatum) {

        AuthorityValue authority = new AuthorityValueFinder().findByUID(context, metadatum.authority);

        return authority != null ? authority.getValue() : null;
    }
}
