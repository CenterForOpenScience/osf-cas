<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <provider>
            <xsl:apply-templates/>
        </provider>
    </xsl:template>
    <xsl:template match="auth">
        <xsl:variable name="delegation-protocol" select="//attribute[@name='delegation-protocol']/@value" />
        <xsl:choose>
            <xsl:when test="$delegation-protocol = 'saml-shib'">
                <xsl:variable name="idp" select="//attribute[@name='shib-identity-provider']/@value" />
                <idp><xsl:value-of select="$idp"/></idp>
                <xsl:choose>
                    <!-- Albion College (ALBION) -->
                    <xsl:when test="$idp='ethos01w.albion.edu'">
                        <id>albion</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Arizona State University (ASU) -->
                    <xsl:when test="$idp='urn:mace:incommon:asu.edu'">
                        <id>asu</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Boys Town [PROD & Test] (BT) -->
                    <!-- Prod and Test IdP of Boys Town share the entity ID but the metadata itself is different. -->
                    <xsl:when test="$idp='https://sts.windows.net/e2ab7419-36ab-4a95-a19f-ee90b6a9b8ac/'">
                        <id>bt</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Boston University (BU) -->
                    <xsl:when test="$idp='https://shib.bu.edu/idp/shibboleth'">
                        <id>bu</id>
                        <user>
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
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <isMemberOf><xsl:value-of select="//attribute[@name='ismemberof']/@value"/></isMemberOf>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- California Lutheran University (CALLUTHERAN)-->
                    <xsl:when test="$idp='login.callutheran.edu'">
                        <id>callutheran</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Carnegie Mellon University (CMU) -->
                    <xsl:when test="$idp='https://login.cmu.edu/idp/shibboleth'">
                        <id>cmu</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                            <departmentRaw><xsl:value-of select="//attribute[@name='primary-affiliation']/@value"/></departmentRaw>
                            <eduPerson>false</eduPerson>
                        </user>
                    </xsl:when>
                    <!-- Cornell University (CORNELL) -->
                    <xsl:when test="$idp='https://shibidp.cit.cornell.edu/idp/shibboleth'">
                        <id>cornell</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Case Western Reserve University (CWRU) -->
                    <xsl:when test="$idp='urn:mace:incommon:case.edu'">
                        <id>cwru</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Duke University (DUKE) -->
                    <xsl:when test="$idp='urn:mace:incommon:duke.edu'">
                        <id>duke</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- East Carolina University (ECU) -->
                    <xsl:when test="$idp='https://sso.ecu.edu/idp/shibboleth'">
                        <id>ecu</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Erasmus University Rotterdam (EUR) -->
                    <xsl:when test="$idp='https://sts.windows.net/715902d6-f63e-4b8d-929b-4bb170bad492/'">
                        <id>eur</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Ferris State University (FERRIS) -->
                    <xsl:when test="$idp='login.ferris.edu'">
                        <id>ferris</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Florida State University (FSU) -->
                    <xsl:when test="$idp='https://idp.fsu.edu'">
                        <id>fsu</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                            <userRoles><xsl:value-of select="//attribute[@name='userroles']/@value"/></userRoles>
                        </user>
                    </xsl:when>
                    <!-- Georgia Institute of Technology (GATECH) -->
                    <xsl:when test="$idp='https://idp.gatech.edu/idp/shibboleth'">
                        <id>gatech</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                            <departmentRaw><xsl:value-of select="//attribute[@name='department']/@value"/></departmentRaw>
                            <eduPerson>false</eduPerson>
                        </user>
                    </xsl:when>
                    <!-- George Mason University (GMU) -->
                    <xsl:when test="$idp='https://shibboleth.gmu.edu/idp/shibboleth'">
                        <id>gmu</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- The George Washington University (GWU) -->
                    <xsl:when test="$idp='https://singlesignon.gwu.edu/idp/shibboleth'">
                        <id>gwu</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Harvard University (HARVARD) -->
                    <xsl:when test="$idp='https://fed.huit.harvard.edu/idp/shibboleth'">
                        <id>harvard</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Illinois Institute of Technology (IIT) -->
                    <xsl:when test="$idp='https://login.iit.edu/cas/idp'">
                        <id>iit</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mailother']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Institut Teknologi Bandung (ITB) -->
                    <xsl:when test="$idp='https://idp.itb.ac.id/idp/shibboleth'">
                        <id>itb</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='cn']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- James Madison University (JMU) -->
                    <xsl:when test="$idp='urn:mace:incommon:jmu.edu'">
                        <id>jmu</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displaynameprintable']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Johns Hopkins University (JHU) -->
                    <xsl:when test="$idp='urn:mace:incommon:johnshopkins.edu'">
                        <id>jhu</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- KU Leuven Libraries (KULEUVEN)-->
                    <xsl:when test="$idp='urn:mace:kuleuven.be:kulassoc:kuleuven.be'">
                        <id>kuleuven</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Massachusetts Institute of Technology (MIT) -->
                    <xsl:when test="$idp='urn:mace:incommon:mit.edu'">
                        <id>mit</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Macquarie University (MQ) -->
                    <xsl:when test="$idp='http://www.okta.com/exk2dzwun7KebsDIV2p7'">
                        <id>mq</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Nesta -->
                    <xsl:when test="$idp='JumpCloud'">
                        <id>nesta</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- New York University (NYU) -->
                    <xsl:when test="$idp='urn:mace:incommon:nyu.edu'">
                        <id>nyu</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- OK State University (OKSTATE) -->
                    <xsl:when test="$idp='https://stwcas.okstate.edu/idp'">
                        <id>okstate</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- The University of Oklahoma (OU) -->
                    <xsl:when test="$idp='https://shib.ou.edu/idp/shibboleth'">
                        <id>ou</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Princeton University (PU) -->
                    <xsl:when test="$idp='https://idp.princeton.edu/idp/shibboleth'">
                        <id>pu</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                            <departmentRaw><xsl:value-of select="//attribute[@name='department']/@value"/></departmentRaw>
                            <eduPerson>false</eduPerson>
                        </user>
                    </xsl:when>
                    <!-- Purdue University (PURDUE) -->
                    <xsl:when test="$idp='https://idp.purdue.edu/idp/shibboleth'">
                        <id>purdue</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Spanish National Research Council (CSIC) -->
                    <xsl:when test="$idp='https://www.rediris.es/sir/csicidp'">
                        <id>csic</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mailother']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Temple University (TEMPLE) -->
                    <xsl:when test="$idp='https://fim.temple.edu/idp/shibboleth'">
                        <id>temple</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Tufts University (TUFTS) -->
                    <xsl:when test="$idp='https://shib-idp.tufts.edu/idp/shibboleth'">
                        <id>tufts</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Universiteit Gent (UGENT) -->
                    <xsl:when test="$idp='https://identity.ugent.be/simplesaml/saml2/idp/metadata.php'">
                        <id>ugent</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Arizona (UA) -->
                    <xsl:when test="$idp='urn:mace:incommon:arizona.edu'">
                        <id>ua</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                            <departmentRaw><xsl:value-of select="//attribute[@name='department']/@value"/></departmentRaw>
                            <eduPerson>false</eduPerson>
                        </user>
                    </xsl:when>
                    <!-- University of British Columbia (UBC) -->
                    <xsl:when test="$idp='https://authentication.ubc.ca'">
                        <id>ubc</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of California Los Angeles (UCLA) -->
                    <xsl:when test="$idp='urn:mace:incommon:ucla.edu'">
                        <id>ucla</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of California San Diego (UCSD) -->
                    <xsl:when test="$idp='urn:mace:incommon:ucsd.edu'">
                        <id>ucsd</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of California Riverside (UCR) -->
                    <xsl:when test="$idp='urn:mace:incommon:ucr.edu'">
                        <id>ucr</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Cape Town (UCT) -->
                    <xsl:when test="$idp='http://adfs.uct.ac.za/adfs/services/trust'">
                        <id>uct</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames />
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Cincinnati (UC) -->
                    <xsl:when test="$idp='https://login.uc.edu/idp/shibboleth'">
                        <id>uc</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Colorado Boulder (COLORADO) -->
                    <xsl:when test="$idp='https://fedauth.colorado.edu/idp/shibboleth'">
                        <id>colorado</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Göttingen (UGOE) -->
                    <xsl:when test="$idp='https://shibboleth-idp.uni-goettingen.de/uni/shibboleth'">
                        <id>ugoe</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Kent (UNIVERSITYOFKENT) -->
                    <xsl:when test="$idp='https://sso.id.kent.ac.uk/idp'">
                        <id>universityofkent</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Macquarie University (MQ) -->
                    <xsl:when test="$idp='http://www.okta.com/exk1xnftf4QK3mQ592p7'">
                        <id>mq</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Manchester (UOM) -->
                    <xsl:when test="$idp='https://shib.manchester.ac.uk/shibboleth'">
                        <id>uom</id>
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
                    <!-- University of Maryland, Baltimore (UMB) -->
                    <xsl:when test="$idp='https://webauth.umaryland.edu/idp/shibboleth'">
                        <id>umb</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Maryland (UMD)-->
                    <xsl:when test="$idp='urn:mace:incommon:umd.edu'">
                        <id>umd</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of North Carolina at Chapel Hill (UNC) -->
                    <xsl:when test="$idp='urn:mace:incommon:unc.edu'">
                        <id>unc</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Notre Dame (ND) -->
                    <xsl:when test="$idp='https://login.nd.edu/idp/shibboleth'">
                        <id>nd</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of London (UOL) -->
                    <xsl:when test="$idp='https://idp.uolia.london.ac.uk/shibboleth'">
                        <id>uol</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Southern California (USC) -->
                    <xsl:when test="$idp='https://shibboleth.usc.edu/shibboleth-idp'">
                        <id>usc</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mailother']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='uscdisplaygivenname']/@value"/><xsl:text> </xsl:text><xsl:value-of select="//attribute[@name='uscdisplaysn']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='uscdisplaysn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='uscdisplaygivenname']/@value"/></givenName>
                            <middleNames><xsl:value-of select="//attribute[@name='uscdisplaymiddlename']/@value"/></middleNames>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of South Carolina (SC) -->
                    <xsl:when test="$idp='https://cas.auth.sc.edu/cas/idp'">
                        <id>sc</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- The University of Texas at Dallas (UTDALLAS) -->
                    <xsl:when test="$idp='https://idp.utdallas.edu/idp/shibboleth'">
                        <id>utdallas</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Virginia (UVA) -->
                    <xsl:when test="$idp='urn:mace:incommon:virginia.edu'">
                        <id>uva</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Washington (UW) -->
                    <xsl:when test="$idp='urn:mace:incommon:washington.edu'">
                        <id>uw</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- University of Wisconsin - Stout (UWSTOUT) -->
                    <xsl:when test="$idp='https://smidp.uwstout.edu/idp/shibboleth'">
                        <id>uwstout</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Virginia Commonwealth University (VCU) -->
                    <xsl:when test="$idp='https://shibboleth.vcu.edu/idp/shibboleth'">
                        <id>vcu</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                            <departmentRaw><xsl:value-of select="//attribute[@name='department']/@value"/></departmentRaw>
                            <eduPerson>false</eduPerson>
                        </user>
                    </xsl:when>
                    <!-- Virginia Tech (VT) -->
                    <xsl:when test="$idp='urn:mace:incommon:vt.edu'">
                        <id>vt</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Vrije Universiteit Amsterdam (VUA) -->
                    <xsl:when test="$idp='http://stsfed.login.vu.nl/adfs/services/trust'">
                        <id>vua</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName/>
                            <givenName/>
                            <middleNames/>
                            <suffix/>
                            <departmentRaw><xsl:value-of select="//attribute[@name='department']/@value"/></departmentRaw>
                            <eduPerson>false</eduPerson>
                        </user>
                    </xsl:when>
                    <!-- Washington University in St. Louis (WUSTL) -->
                    <xsl:when test="$idp='https://login.wustl.edu/idp/shibboleth'">
                        <id>wustl</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='eppn']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayname']/@value"/></fullname>
                            <familyName><xsl:value-of select="//attribute[@name='sn']/@value"/></familyName>
                            <givenName><xsl:value-of select="//attribute[@name='givenname']/@value"/></givenName>
                            <middleNames/>
                            <suffix/>
                        </user>
                    </xsl:when>
                    <!-- Unknown Identity Provider -->
                    <xsl:otherwise>
                        <xsl:message terminate="yes">Error: Unknown Identity Provider '<xsl:value-of select="$idp"/>'</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="$delegation-protocol = 'cas-pac4j'">
                <xsl:variable name="idp" select="//attribute[@name='cas-identity-provider']/@value" />
                <idp><xsl:value-of select="$idp"/></idp>
                <xsl:choose>
                    <!-- Concordia College (CORD) -->
                    <xsl:when test="$idp='cord'">
                        <id>cord</id>
                        <user>
                            <username><xsl:value-of select="//attribute[@name='mail']/@value"/></username>
                            <fullname><xsl:value-of select="//attribute[@name='displayName']/@value"/></fullname>
                            <familyName />
                            <givenName />
                            <middleNames />
                            <suffix />
                        </user>
                    </xsl:when>
                    <!-- Unknown Identity Provider -->
                    <xsl:otherwise>
                        <xsl:message terminate="yes">Error: Unknown Identity Provider '<xsl:value-of select="$idp"/>'</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="yes">Error: Unknown Delegation Protocol '<xsl:value-of select="$delegation-protocol"/>'</xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
