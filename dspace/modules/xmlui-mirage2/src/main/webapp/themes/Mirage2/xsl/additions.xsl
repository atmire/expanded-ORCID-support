<xsl:stylesheet
        xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
        xmlns:dri="http://di.tamu.edu/DRI/1.0/"
        xmlns:mets="http://www.loc.gov/METS/"
        xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
        xmlns:xlink="http://www.w3.org/TR/xlink/"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
        xmlns="http://www.w3.org/1999/xhtml"
        xmlns:xalan="http://xml.apache.org/xalan"
        xmlns:encoder="xalan://java.net.URLEncoder"
        xmlns:confman="org.dspace.core.ConfigurationManager"
        exclude-result-prefixes="i18n dri mets dim xlink xsl xalan encoder confman">

    <xsl:output indent="yes"/>

    <xsl:template name="itemSummaryView-DIM-authors-entry">
        <div>
            <xsl:if test="@authority">
                <xsl:attribute name="class"><xsl:text>ds-dc_contributor_author-authority</xsl:text></xsl:attribute>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="@orcidID">
                    <xsl:call-template name="renderDiscovery">
                        <xsl:with-param name="value" select="node()"/>
                        <xsl:with-param name="orcidID" select="@orcidID"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderORCID"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="node()"/>
                </xsl:otherwise>
            </xsl:choose>
        </div>
    </xsl:template>

    <xsl:template name="renderDiscovery">
        <xsl:param name="value"/>
        <xsl:param name="orcidID"/>
        <a>
            <xsl:attribute name="href">
                <xsl:value-of select="$context-path"/>
                <xsl:text>/discover?filtertype_1=orcidid</xsl:text>
                <xsl:text>&amp;filter_relational_operator_1=equals&amp;filter_1=</xsl:text>
                <xsl:value-of select="substring-after($orcidID, confman:getProperty('orcid.connector.url'))"/>
            </xsl:attribute>
            <xsl:value-of select="$value"/>
        </a>
    </xsl:template>

    <xsl:template name="renderORCID">
        <a class="orcid_icon" target="_blank" href="{@orcidID}">
            <xsl:text> </xsl:text>
            <img src="{$theme-path}images/orcid_icon.png" alt="cc"/>
        </a>
    </xsl:template>

    <xsl:template match="dri:referenceSet[@id='aspect.artifactbrowser.ItemViewer.referenceSet.collection-viewer']/dri:reference" mode="summaryView">
        <!-- simplified check to verify whether access rights are available in METS -->
        <xsl:variable name='METSRIGHTS-enabled' select="contains(confman:getProperty('plugin.named.org.dspace.content.crosswalk.DisseminationCrosswalk'), 'METSRIGHTS')" />
        <xsl:variable name="externalMetadataURL">
            <xsl:text>cocoon:/</xsl:text>
            <xsl:value-of select="@url"/>
            <!-- If this is an Item, display the METSRIGHTS section, so we
                 know which files have access restrictions.
                 This requires the METSRightsCrosswalk to be enabled! -->
            <xsl:if test="@type='DSpace Item' and $METSRIGHTS-enabled">
                <xsl:text>?rightsMDTypes=METSRIGHTS</xsl:text>
            </xsl:if>
        </xsl:variable>
        <!-- This comment just displays the full URL in an HTML comment, for easy reference. -->
        <xsl:comment> External Metadata URL: <xsl:value-of select="$externalMetadataURL"/> </xsl:comment>
        <xsl:apply-templates select="document($externalMetadataURL)" mode="summaryView"/>
        <!--<xsl:apply-templates /> prevents the collections section from being rendered in the default way-->
    </xsl:template>

</xsl:stylesheet>