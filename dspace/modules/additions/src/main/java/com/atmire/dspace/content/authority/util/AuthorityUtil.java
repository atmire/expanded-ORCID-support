package com.atmire.dspace.content.authority.util;

import org.apache.log4j.Logger;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.factory.AuthorityServiceFactory;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.authority.orcid.Orcidv2AuthorityValue;
import org.dspace.authority.service.AuthorityValueService;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.utils.DSpace;

import java.sql.SQLException;
import java.util.Date;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.log4j.Logger.getLogger;
import static org.dspace.content.authority.Choices.CF_ACCEPTED;

public class AuthorityUtil {

    protected static final Logger log = getLogger(AuthorityUtil.class);

    private AuthorityIndexingService indexingService = new DSpace().getServiceManager()
            .getServiceByName(AuthorityIndexingService.class.getName(), AuthorityIndexingService.class);
    private AuthorityValueService authorityValueService =
            AuthorityServiceFactory.getInstance().getAuthorityValueService();
    private ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    public void addMetadataWithOrcid(Context context, Item item, MetadataValue metadataValue) throws SQLException {

        String orcidID = metadataValue.getAuthority();
        String orcidAuthority = null;

        if (isOrcidFormat(orcidID)) {

            orcidAuthority = authorityValueService.findByOrcidID(context, orcidID).getId();
            if (orcidAuthority == null) {
                orcidAuthority = createOrcidAuthority(metadataValue, orcidID);
            }
        }

        if (orcidAuthority != null) {
            itemService.addMetadata(context, item, metadataValue.getMetadataField(), metadataValue.getLanguage(),
                    metadataValue.getValue(), orcidAuthority, CF_ACCEPTED);

        } else {
            itemService.addMetadata(context, item, metadataValue.getMetadataField(), metadataValue.getLanguage(),
                    metadataValue.getValue());
        }
    }

    private String createOrcidAuthority(final MetadataValue metadataValue, final String orcidID) {

        return createSolrOrcidAuthority(metadataValue, orcidID);
    }

    private String createSolrOrcidAuthority(final MetadataValue metadataValue, final String orcidID) {

        Orcidv2AuthorityValue authorityValue = Orcidv2AuthorityValue.create();
        authorityValue.setValue(metadataValue.getValue());

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
        indexingService.indexContent(value);
        indexingService.commit();
        return value.getOrcid_id();
    }

    public void addMetadataWithAuthority(Context context, Item item, final MetadataValue metadataValue) throws SQLException {
        String authorityValue = getAuthorityValue(context, metadataValue);
        if (authorityValue != null) {
            itemService.addMetadata(context, item, metadataValue.getMetadataField(), metadataValue.getLanguage(),
                    authorityValue, metadataValue.getAuthority(), CF_ACCEPTED);
        } else {
            itemService.addMetadata(context, item, metadataValue.getMetadataField(), metadataValue.getLanguage(),
                    metadataValue.getValue());
        }
    }

    private String getAuthorityValue(Context context, final MetadataValue metadataValue) {

        AuthorityValue authority = authorityValueService
                .findByUID(context, metadataValue.getAuthority());

        return authority != null ? authority.getValue() : null;
    }
}
