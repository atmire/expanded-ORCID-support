/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.rest;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.authority.AuthorityUtil;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.AuthorityValueFinder;
import org.dspace.authority.PersonAuthorityValue;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.authority.orcid.Orcidv2AuthorityValue;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.discovery.IndexingService;
import org.dspace.discovery.SearchServiceException;
import org.dspace.rest.exceptions.ContextException;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.utils.DSpace;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static javax.ws.rs.core.Response.status;
import static org.dspace.authority.AuthorityValueGenerator.update;
import static org.dspace.authorize.AuthorizeManager.isAdmin;
import static org.dspace.core.ConfigurationManager.getBooleanProperty;

@SuppressWarnings("deprecation")
@Path("/authorities")
public class AuthoritiesResource extends Resource {

    private static final Logger log = Logger.getLogger(AuthoritiesResource.class);

    private static final AuthorityIndexingService authorityIndexingService = new DSpace().getSingletonService(AuthorityIndexingService.class);
    private static final IndexingService indexingService = new DSpace().getSingletonService(IndexingService.class);

    @PUT
    @Path("/{authority_id}/value")
    @Consumes(TEXT_PLAIN)
    public Response updateAuthorityValue(@PathParam("authority_id") String authorityId, String value,
                                         @QueryParam("userIP") String user_ip, @QueryParam("userAgent") String user_agent,
                                         @QueryParam("xforwardedfor") String xforwardedfor, @Context HttpHeaders headers, @Context HttpServletRequest request)
            throws WebApplicationException {

        if (!getBooleanProperty("authority.allow-rest-updates.person", true)) {
            throw new WebApplicationException(BAD_REQUEST);
        }

        log.info("Updating value of authority (id: " + authorityId + ") to " + value + ".");

        org.dspace.core.Context context = null;
        try {
            context = createContext(getUser(headers));

            if (!isAdmin(context)) {
                context.abort();
                throw new WebApplicationException(UNAUTHORIZED);
            }

            AuthorityValue authorityValue = new AuthorityValueFinder().findByUID(context, authorityId);

            if (!(authorityValue instanceof PersonAuthorityValue)) {
                context.abort();
                throw new IllegalArgumentException("Provided authority is not a person. Only person authorities can be updated.");
            }

            authorityValue.setValue(value);

            if (!(authorityValue instanceof Orcidv2AuthorityValue)) {
                authorityValue = update(authorityValue);
            } else if (!getBooleanProperty("authority.allow-rest-updates.orcid", true)) {
                throw new WebApplicationException(BAD_REQUEST);
            }

            authorityIndexingService.indexContent(authorityValue, true);
            new AuthorityUtil().deleteAuthorityValueById(authorityId);

            log.info("Deleted authority with id: " + authorityId + " and added authority with id: " + authorityValue.getId());

            authorityIndexingService.commit();

            DatabaseManager.updateQuery(context, "UPDATE metadatavalue SET text_value = ?, authority = ? WHERE authority = ?", value, authorityValue.getId(), authorityId);
            context.commit();
            log.info("Updated authority metadata values.");

            ItemIterator itemIterator = new AuthorityUtil().findItemsByAuthorityValue(context, authorityValue.getId());
            while (itemIterator.hasNext()) {
                Item item = itemIterator.next();
                indexingService.indexContent(context, item, true);
                item.decache();
            }
            indexingService.commit();
            log.info("Updated discovery index.");

            context.complete();

        } catch (IllegalArgumentException | SQLException | SearchServiceException | IOException | SolrServerException | ContextException e) {
            processException(
                    "Could not update value of authority (id: " + authorityId + "). Message: " + e.getMessage(), context
            );
        } finally {
            processFinally(context);
        }

        log.info("Value of authority (id: " + authorityId + ") was successfully updated.");

        return status(OK).build();
    }
}
