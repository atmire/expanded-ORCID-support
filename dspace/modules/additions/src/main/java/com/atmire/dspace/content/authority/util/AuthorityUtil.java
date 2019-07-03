package com.atmire.dspace.content.authority.util;

import org.apache.log4j.Logger;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.AuthorityValueFinder;
import org.dspace.authority.PersonAuthorityValue;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.authority.orcid.Orcidv2AuthorityValue;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.ChoiceAuthorityManager;
import org.dspace.content.authority.Choices;
import org.dspace.content.authority.MetadataAuthorityManager;
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
        String orcidAuthorityID = null;

        if (isOrcidFormat(orcidID)) {

            AuthorityValue orcidAuthority = new AuthorityValueFinder().findByOrcidID(context, orcidID);
            if (orcidAuthority == null) {
                orcidAuthorityID = createOrcidAuthority(metadatum, orcidID);
            } else {
                orcidAuthorityID = orcidAuthority.getId();
            }
        }

        if (orcidAuthorityID != null) {
            item.addMetadata(metadatum.schema, metadatum.element, metadatum.qualifier, metadatum.language,
                    metadatum.value, orcidAuthorityID, Choices.CF_ACCEPTED);

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
        authorityValue.setField(metadatum.getField().replaceAll("\\.", "_"));

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
        return value.getId();
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

    public void addMetadataWhenNoAuthorityIsProvided(Context context, Item item, Metadatum metadatum) {
        MetadataAuthorityManager mam = MetadataAuthorityManager.getManager();
        String fieldKey = MetadataAuthorityManager
                .makeFieldKey(metadatum.schema, metadatum.element, metadatum.qualifier);

        boolean fieldAdded = false;

        if ( mam.isAuthorityControlled(fieldKey)) {
            if (isPersonAuthority(fieldKey)) {

                Choices c = ChoiceAuthorityManager.getManager().getBestMatch(fieldKey, metadatum.value, -1, null);
                if (c.values.length > 0) {
                    AuthorityValue matchedAuthority = new AuthorityValueFinder().findByUID(context, c.values[0].authority);
                    if (matchedAuthority instanceof Orcidv2AuthorityValue) {

                        item.addMetadata(metadatum.schema, metadatum.element, metadatum.qualifier,
                                metadatum.language,
                                metadatum.value, matchedAuthority.getId(), Choices.CF_ACCEPTED);

                        fieldAdded = true;

                    } else {
                        item.addMetadata(metadatum.schema, metadatum.element, metadatum.qualifier,
                                metadatum.language,
                                metadatum.value, c.values[0].authority, c.confidence);
                        fieldAdded = true;
                    }
                }
            }
        }

        // make sure the field is always added to the metadata
        if(!fieldAdded){
            item.addMetadata(metadatum.schema, metadatum.element, metadatum.qualifier, metadatum.language,
                    metadatum.value);
        }
    }

    public boolean isPersonAuthority(final String fieldKey) {

        return AuthorityValue.getAuthorityTypes().getFieldDefaults()
                .get(fieldKey.replaceAll("\\.", "_")) instanceof PersonAuthorityValue;
    }

    private String getAuthorityValue(Context context, final Metadatum metadatum) {

        AuthorityValue authority = new AuthorityValueFinder().findByUID(context, metadatum.authority);

        return authority != null ? authority.getValue() : null;
    }
}
