<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<!--
    Rendering specific to the item display page.

    Author: art.lowel at atmire.com
    Author: lieven.droogmans at atmire.com
    Author: ben at atmire.com
    Author: Alexey Maslov

-->

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
    xmlns:util="org.dspace.app.xmlui.utils.XSLUtils"
    xmlns:jstring="java.lang.String"
    xmlns:rights="http://cosimo.stanford.edu/sdr/metsrights/"
    xmlns:confman="org.dspace.core.ConfigurationManager"
    exclude-result-prefixes="xalan encoder i18n dri mets dim xlink xsl util jstring rights confman">

    <xsl:output indent="yes"/>

    <xsl:template name="itemSummaryView-DIM-fields">
        <xsl:param name="clause" select="'1'"/>
        <xsl:param name="phase" select="'even'"/>
        <xsl:variable name="otherPhase">
            <xsl:choose>
                <xsl:when test="$phase = 'even'">
                    <xsl:text>odd</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>even</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <!-- Title row -->
            <xsl:when test="$clause = 1">

                <xsl:choose>
                    <xsl:when test="descendant::text() and (count(dim:field[@element='title'][not(@qualifier)]) &gt; 1)">
                        <!-- display first title as h1 -->
                        <h1>
                            <xsl:value-of select="dim:field[@element='title'][not(@qualifier)][1]/node()"/>
                        </h1>
                        <div class="simple-item-view-other">
                            <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-title</i18n:text>:</span>
                            <span>
                                <xsl:for-each select="dim:field[@element='title'][not(@qualifier)]">
                                    <xsl:value-of select="./node()"/>
                                    <xsl:if test="count(following-sibling::dim:field[@element='title'][not(@qualifier)]) != 0">
                                        <xsl:text>; </xsl:text>
                                        <br/>
                                    </xsl:if>
                                </xsl:for-each>
                            </span>
                        </div>
                    </xsl:when>
                    <xsl:when test="dim:field[@element='title'][descendant::text()] and count(dim:field[@element='title'][not(@qualifier)]) = 1">
                        <h1>
                            <xsl:value-of select="dim:field[@element='title'][not(@qualifier)][1]/node()"/>
                        </h1>
                    </xsl:when>
                    <xsl:otherwise>
                        <h1>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-title</i18n:text>
                        </h1>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- Author(s) row -->
            <xsl:when test="$clause = 2 and (dim:field[@element='contributor'][@qualifier='author' and descendant::text()] or dim:field[@element='creator' and descendant::text()] or dim:field[@element='contributor' and descendant::text()])">
                <div class="simple-item-view-authors">
                    <xsl:choose>
                        <xsl:when test="dim:field[@element='contributor'][@qualifier='author']">
                            <xsl:for-each select="dim:field[@element='contributor'][@qualifier='author']">
                                <span>
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
                                </span>
                                <xsl:if test="count(following-sibling::dim:field[@element='contributor'][@qualifier='author']) != 0">
                                    <xsl:text>; </xsl:text>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:when test="dim:field[@element='creator']">
                            <xsl:for-each select="dim:field[@element='creator']">
                                <xsl:copy-of select="node()"/>
                                <xsl:if test="count(following-sibling::dim:field[@element='creator']) != 0">
                                    <xsl:text>; </xsl:text>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:when test="dim:field[@element='contributor']">
                            <xsl:for-each select="dim:field[@element='contributor']">
                                <xsl:copy-of select="node()"/>
                                <xsl:if test="count(following-sibling::dim:field[@element='contributor']) != 0">
                                    <xsl:text>; </xsl:text>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <i18n:text>xmlui.dri2xhtml.METS-1.0.no-author</i18n:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- identifier.uri row -->
            <xsl:when test="$clause = 3 and (dim:field[@element='identifier' and @qualifier='uri' and descendant::text()])">
                <div class="simple-item-view-other">
                    <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-uri</i18n:text>:</span>
                    <span>
                        <xsl:for-each select="dim:field[@element='identifier' and @qualifier='uri']">
                            <a>
                                <xsl:attribute name="href">
                                    <xsl:copy-of select="./node()"/>
                                </xsl:attribute>
                                <xsl:copy-of select="./node()"/>
                            </a>
                            <xsl:if test="count(following-sibling::dim:field[@element='identifier' and @qualifier='uri']) != 0">
                                <br/>
                            </xsl:if>
                        </xsl:for-each>
                    </span>
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- date.issued row -->
            <xsl:when test="$clause = 4 and (dim:field[@element='date' and @qualifier='issued' and descendant::text()])">
                <div class="simple-item-view-other">
                    <span class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-date</i18n:text>:</span>
                    <span>
                        <xsl:for-each select="dim:field[@element='date' and @qualifier='issued']">
                            <xsl:copy-of select="substring(./node(),1,10)"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='date' and @qualifier='issued']) != 0">
                                <br/>
                            </xsl:if>
                        </xsl:for-each>
                    </span>
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- Abstract row -->
            <xsl:when test="$clause = 5 and (dim:field[@element='description' and @qualifier='abstract' and descendant::text()])">
                <div class="simple-item-view-description">
                    <h3><i18n:text>xmlui.dri2xhtml.METS-1.0.item-abstract</i18n:text>:</h3>
                    <div>
                        <xsl:if test="count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                        <xsl:for-each select="dim:field[@element='description' and @qualifier='abstract']">
                            <xsl:choose>
                                <xsl:when test="node()">
                                    <xsl:copy-of select="node()"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>&#160;</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:if test="count(following-sibling::dim:field[@element='description' and @qualifier='abstract']) != 0">
                                <div class="spacer">&#160;</div>
                            </xsl:if>
                        </xsl:for-each>
                        <xsl:if test="count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                    </div>
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <!-- Description row -->
            <xsl:when test="$clause = 6 and (dim:field[@element='description' and not(@qualifier) and descendant::text()])">
                <div class="simple-item-view-description">
                    <h3 class="bold"><i18n:text>xmlui.dri2xhtml.METS-1.0.item-description</i18n:text>:</h3>
                    <div>
                        <xsl:if test="count(dim:field[@element='description' and not(@qualifier)]) &gt; 1 and not(count(dim:field[@element='description' and @qualifier='abstract']) &gt; 1)">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                        <xsl:for-each select="dim:field[@element='description' and not(@qualifier)]">
                            <xsl:copy-of select="./node()"/>
                            <xsl:if test="count(following-sibling::dim:field[@element='description' and not(@qualifier)]) != 0">
                                <div class="spacer">&#160;</div>
                            </xsl:if>
                        </xsl:for-each>
                        <xsl:if test="count(dim:field[@element='description' and not(@qualifier)]) &gt; 1">
                            <div class="spacer">&#160;</div>
                        </xsl:if>
                    </div>
                </div>
                <xsl:call-template name="itemSummaryView-DIM-fields">
                    <xsl:with-param name="clause" select="($clause + 1)"/>
                    <xsl:with-param name="phase" select="$otherPhase"/>
                </xsl:call-template>
            </xsl:when>

            <xsl:when test="$clause = 7 and $ds_item_view_toggle_url != ''">
                <p class="ds-paragraph item-view-toggle item-view-toggle-bottom">
                    <a>
                        <xsl:attribute name="href"><xsl:value-of select="$ds_item_view_toggle_url"/></xsl:attribute>
                        <i18n:text>xmlui.ArtifactBrowser.ItemViewer.show_full</i18n:text>
                    </a>
                </p>
            </xsl:when>

            <!-- recurse without changing phase if we didn't output anything -->
            <xsl:otherwise>
                <!-- IMPORTANT: This test should be updated if clauses are added! -->
                <xsl:if test="$clause &lt; 8">
                    <xsl:call-template name="itemSummaryView-DIM-fields">
                        <xsl:with-param name="clause" select="($clause + 1)"/>
                        <xsl:with-param name="phase" select="$phase"/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>

        <!-- Generate the Creative Commons license information from the file section (DSpace deposit license hidden by default) -->
        <xsl:apply-templates select="mets:fileSec/mets:fileGrp[@USE='CC-LICENSE']"/>
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
            <img src="{$theme-path}/images/orcid_icon.png" alt="cc"/>
        </a>
    </xsl:template>

    <xsl:template match="dri:reference" mode="summaryView">
        <!-- simplified check to verify whether access rights are available in METS -->
        <xsl:variable name='METSRIGHTS-enabled' select="contains(confman:getProperty('plugin.named.org.dspace.content.crosswalk.DisseminationCrosswalk'), 'METSRIGHTS')" />
        <xsl:variable name="externalMetadataURL">
            <xsl:text>cocoon:/</xsl:text>
            <xsl:value-of select="@url"/>
            <!-- If this is an Item, display the METSRIGHTS section, so we
                 know which files have access restrictions.
                 This requires the METSRightsCrosswalk to be enabled! -->
            <xsl:if test="@type='DSpace Item' and $METSRIGHTS-enabled">
                <xsl:text>?rightsMDTypes=METSRIGHTS&amp;requireORCIDCall=true</xsl:text>
            </xsl:if>
        </xsl:variable>
        <!-- This comment just displays the full URL in an HTML comment, for easy reference. -->
        <xsl:comment> External Metadata URL: <xsl:value-of select="$externalMetadataURL"/> </xsl:comment>
        <xsl:apply-templates select="document($externalMetadataURL)" mode="summaryView"/>
        <xsl:apply-templates />
    </xsl:template>

</xsl:stylesheet>
