<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <provider>
            <xsl:apply-templates/>
        </provider>
    </xsl:template>
    <xsl:template match="auth">
        <xsl:variable name="delegation-protocol" select="//attribute[@name='delegation-protocol']/@value"/>
        <xsl:choose>
            <!--  Institutions that use the SAML protocol for Authentication  -->
            <xsl:when test="$delegation-protocol = 'saml-shib'">
                <xsl:variable name="idp" select="//attribute[@name='shib-identity-provider']/@value"/>
                <idp>
                    <xsl:value-of select="$idp"/>
                </idp>
                <xsl:choose>
                    <!-- An example SAML-auth institution without any special features -->
                    <xsl:when test="$idp='fake-saml-idp-type-0-default'">
                        <id>fake-saml-type-0</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- An example SAML institution that has Shared SSO enabled -->
                    <xsl:when test="$idp='fake-saml-idp-type-1-shared-sso'">
                        <id>fake-saml-type-1</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <isMemberOf><xsl:value-of select="//attribute[@name='ismemberof']/@value"/></isMemberOf>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- An example SAML-auth institution that has selective SSO enabled -->
                    <xsl:when test="$idp='fake-saml-idp-type-2-selective-sso'">
                        <id>fake-saml-type-2</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <isSelectiveSso>true</isSelectiveSso>
                            <selectiveSsoFilter><xsl:value-of select="//attribute[@name='selectivessofilter']/@value"/></selectiveSsoFilter>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- An example SAML-auth institution that uses `eduPersonPrimaryOrgUnitDN` for the department attribute -->
                    <xsl:when test="$idp='fake-saml-idp-type-3-department-eduperson'">
                        <id>fake-saml-type-3</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <departmentRaw><xsl:value-of select="//attribute[@name='department']/@value"/></departmentRaw>
                            <eduPerson>true</eduPerson>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- An example SAML-auth institution that uses a customized department attribute -->
                    <xsl:when test="$idp='fake-saml-idp-type-4-department-customized'">
                        <id>fake-saml-type-4</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <departmentRaw><xsl:value-of select="//attribute[@name='department']/@value"/></departmentRaw>
                            <eduPerson>false</eduPerson>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!--  Unknown Identity Provider  -->
                    <xsl:otherwise>
                        <xsl:message terminate="yes">Error: Unknown Identity Provider '<xsl:value-of select="$idp"/>'
                        </xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <!--  Institutions that use the CAS protocol for Authentication  -->
            <xsl:when test="$delegation-protocol = 'cas-pac4j'">
                <xsl:variable name="idp" select="//attribute[@name='cas-identity-provider']/@value"/>
                <idp>
                    <xsl:value-of select="$idp"/>
                </idp>
                <xsl:choose>
                    <!--  An example CAS-auth institution without any special features  -->
                    <xsl:when test="$idp='fake-cas-idp-type-0-default'">
                        <id>fake-cas-type-0</id>
                        <user>
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
                        <xsl:message terminate="yes">Error: Unknown Identity Provider '<xsl:value-of select="$idp"/>'
                        </xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <!--  Unknown Delegation Protocol  -->
            <xsl:otherwise>
                <xsl:message terminate="yes">Error: Unknown Delegation Protocol '<xsl:value-of
                        select="$delegation-protocol"/>'
                </xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
