package org.dspace.authority;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.authority.factory.AuthorityServiceFactory;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.authority.orcid.Orcidv2AuthorityValue;
import org.dspace.authority.service.AuthorityValueService;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.authority.Choices;
import org.dspace.content.authority.factory.ContentAuthorityServiceFactory;
import org.dspace.content.authority.service.MetadataAuthorityService;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.utils.DSpace;

import java.io.IOException;
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

    public void addMetadataWithOrcid(Context context, Item item, MetadataField metadataField, String value, String authority, String language) throws SQLException {

        String orcidAuthorityID = null;

        if (isOrcidFormat(authority)) {

            AuthorityValue orcidAuthority = authorityValueService.findByOrcidID(context, authority);
            if (orcidAuthority == null) {

                orcidAuthorityID = createSolrOrcidAuthority(authority, value, metadataField);
            } else {
                orcidAuthorityID = orcidAuthority.getId();
            }
        }

        if (orcidAuthorityID != null) {
            itemService.addMetadata(context, item, metadataField, language, value, orcidAuthorityID, CF_ACCEPTED);

        } else {
            itemService.addMetadata(context, item, metadataField, language, value);
        }
    }

    private String createSolrOrcidAuthority(final String orcidID, String value, MetadataField metadataField) {

        Orcidv2AuthorityValue authorityValue = Orcidv2AuthorityValue.create();
        authorityValue.setValue(value);
        authorityValue.setField(metadataField.toString());

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
        return value.getId();
    }

    public void addMetadata(Context context, Item dspaceItem, String[] field, String value, String language) throws SQLException {

        MetadataField metadataField = ContentServiceFactory.getInstance().getMetadataFieldService()
                .findByElement(context, field[0], field[1], field[2]);

        String[] splitAuthority = splitAuthority(value);
        if (splitAuthority.length > 1) {

            String authority = splitAuthority[1];
            if (isOrcidFormat(authority)) {
                addMetadataWithOrcid(context, dspaceItem, metadataField, splitAuthority[0], authority, language);
            } else {
                addMetadataWithAuthority(context, dspaceItem, metadataField, splitAuthority[0], authority, language);
            }
        } else {
            addMetadataWhenNoAuthorityIsProvided(context, dspaceItem, metadataField, splitAuthority[0], language);
        }
    }

    private String[] splitAuthority(final String value) {
        return value.split("::");
    }

    public void addMetadataWithAuthority(Context context, Item item, MetadataField metadataField, String value, String authority, String language) throws SQLException {

        AuthorityValue authorityValue = authorityValueService.findByUID(context, authority);

        if (authorityValue != null) {
            itemService.addMetadata(context, item, metadataField, language,
                    authorityValue.getValue(), authority, CF_ACCEPTED);
        } else {
            itemService.addMetadata(context, item, metadataField, language, value);
        }
    }

    public void addMetadataWhenNoAuthorityIsProvided(Context context, Item item, MetadataField metadataField, String value, String language) throws SQLException {
        MetadataAuthorityService mam = ContentAuthorityServiceFactory.getInstance().getMetadataAuthorityService();
        String fieldKey = mam.makeFieldKey(metadataField);

        boolean fieldAdded = false;

        if (mam.isAuthorityControlled(fieldKey)) {
            if (isPersonAuthority(metadataField)) {

                Choices c = ContentAuthorityServiceFactory.getInstance().getChoiceAuthorityService()
                        .getBestMatch(fieldKey, value, null, null);
                if (c.values.length > 0) {
                    AuthorityValue matchedAuthority = authorityValueService.findByUID(context, c.values[0].authority);
                    if (matchedAuthority instanceof Orcidv2AuthorityValue) {

                        itemService.addMetadata(context, item, metadataField, language, value,
                                matchedAuthority.getId(), Choices.CF_ACCEPTED);

                        fieldAdded = true;

                    } else {
                        itemService.addMetadata(context, item, metadataField, language, value,
                                c.values[0].authority, c.confidence);

                        fieldAdded = true;
                    }
                }
            }
        }

        // make sure the field is always added to the metadata
        if (!fieldAdded){
            itemService.addMetadata(context, item, metadataField, language, value);
        }
    }

    public boolean isPersonAuthority(MetadataField metadataField) {

        return isPersonAuthority(metadataField.toString());
    }

    public boolean isPersonAuthority(String metadataField) {

        return AuthorityServiceFactory.getInstance().getAuthorTypes().getFieldDefaults()
                .get(metadataField) instanceof PersonAuthorityValue;
    }

    public void deleteAuthorityValueById(String id) throws IOException, SolrServerException {
        ((AuthoritySolrServiceImpl) indexingService).getSolr().deleteByQuery("id:\"" + id + "\"");
        indexingService.commit();
    }
}
