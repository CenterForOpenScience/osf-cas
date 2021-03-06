<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <provider>
            <xsl:apply-templates/>
        </provider>
    </xsl:template>
    <xsl:template match="auth">
        <xsl:variable name="delegation-protocol" select="//attribute[@name='Delegation-Protocol']/@value" />
        <xsl:choose>
            <!--  Institutions that use the SAML protocol for Authentication  -->
            <xsl:when test="$delegation-protocol = 'saml-shib'">
                <xsl:variable name="idp" select="//attribute[@name='shib-identity-provider']/@value" />
                <idp><xsl:value-of select="$idp"/></idp>
                <xsl:choose>
                    <!--  Example SAML-auth University -->
                    <xsl:when test="$idp='example-shib-auth-univeristy-entity-id'">
                        <id>esu</id>
                        <user>
                            <!--  Each institution has its customized mapping of attributes  -->
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Brown University (BROWN) -->
                    <xsl:when test="$idp='https://sso.brown.edu/idp/shibboleth'">
                        <id>brown</id>
                        <user>
                            <username>
                                <xsl:value-of select="//attribute[@name='mail']/@value"/>
                            </username>
                            <fullname>
                                <xsl:value-of select="//attribute[@name='displayname']/@value"/>
                            </fullname>
                            <familyName>
                                <xsl:value-of select="//attribute[@name='sn']/@value"/>
                            </familyName>
                            <givenName>
                                <xsl:value-of select="//attribute[@name='givenname']/@value"/>
                            </givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Arizona (UA) -->
                    <xsl:when test="$idp='urn:mace:incommon:arizona.edu'">
                        <id>ua</id>
                        <user>
                            <username>
                                <xsl:value-of select="//attribute[@name='mail']/@value"/>
                            </username>
                            <fullname>
                                <xsl:value-of select="//attribute[@name='displayname']/@value"/>
                            </fullname>
                            <familyName>
                                <xsl:value-of select="//attribute[@name='sn']/@value"/>
                            </familyName>
                            <givenName>
                                <xsl:value-of select="//attribute[@name='givenname']/@value"/>
                            </givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!--  Unknown Identity Provider  -->
                    <xsl:otherwise>
                        <xsl:message terminate="yes">Error: Unknown Identity Provider '<xsl:value-of select="$idp"/>'</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <!--  Institutions that use the CAS protocol for Authentication  -->
            <xsl:when test="$delegation-protocol = 'cas-pac4j'">
                <xsl:variable name="idp" select="//attribute[@name='Cas-Identity-Provider']/@value" />
                <idp><xsl:value-of select="$idp"/></idp>
                <xsl:choose>
                    <!--  Example CAS-auth University  -->
                    <xsl:when test="$idp='ecu'">
                        <id>ecu</id>
                        <user>
                            <!--  Each institution has its customized mapping of attributes  -->
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when><!--  Example CAS-auth University  -->
                    <xsl:when test="$idp='fakecas'">
                        <id>fakecas</id>
                        <user>
                            <!-- Tweaked fakeCAS as an institution IdP for local development -->
                            <username><xsl:value-of select="//attribute[@name='username']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='familyName']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenName']/@value"/></givenName>
                            <fullname/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!--  Unknown Identity Provider  -->
                    <xsl:otherwise>
                        <xsl:message terminate="yes">Error: Unknown Identity Provider '<xsl:value-of select="$idp"/>'</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <!--  Unknown Delegation Protocol  -->
            <xsl:otherwise>
                <xsl:message terminate="yes">Error: Unknown Delegation Protocol '<xsl:value-of select="$delegation-protocol"/>'</xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
