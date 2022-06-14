# Connecting to the Open Science Framework (OSF) via Shibboleth-based Single Sign-On (SSO)

This article provides general information about the COS's Shibboleth-based (SSO) integration for organizations who have signed the OSF for Institutions Offer of Services letter.

## What is Single Sign-On?

In general, Single Sign-On, or SSO, allows users authenticated with one trusted system (e.g. university network) to also authenticate using those same “home” credentials with another trusted network (e.g. OSF service). In the case of the second authentication, users are not asked to log in again, but instead the authenticated credentials are shared between systems.

## Who can use Single Sign-On with Open Science Framework?

Any organization that has implemented a SAML 2.0 Identity Provider (IdP) and signed the OSF for Institutions Offer for Services can offer SSO to OSF accounts.

### A few notes:

* Current OSF users who have already set up accounts with a different login, will be able to retain those credentials and choose to login with personal or institutional credentials.

* Users’ authentication to the OSF service using SSO cannot also use the “forgot Password” link on the OSF website to remind them of their credentials, as their user credentials are specific to and managed by their organization.

## Technical Implementation

### InCommon Research & Scholarship Institutions

COS is a [Research & Scholarship Entity Category (R&S)](https://refeds.org/category/research-and-scholarship) Service Provider (SP) registered by the [InCommon Federation](https://www.incommon.org/federation/).

* Entity ID: `https://accounts.osf.io/shibboleth`
* Required Attributes: `eduPersonPrincipalName` (SAML2), `mail` (SAML2) and `displayName` (SAML2)
* Optional Attributes:
    * `givenName` and `sn` pair which specifies the user's given name and surname
    * `eduPersonOrgUnitDN` or `eduPersonPrimaryOrgUnitDN` which specifies the person's Organizational Unit(s) (i.e. the department(s))
    * `eduPersonAffiliation` or `eduPersonPrimaryAffiliation` which specifies the person's relationship(s) to the institution in broad categories such as student, faculty, staff, alum, etc.

Full technical details can be found at https://www.incommon.org/federation/research-scholarship-adopters/.

Please note that only COS's production SP is registered by InCommon. If you want to connect to COS's test / staging SP, here is the [SP metadata](https://accounts.test.osf.io/Shibboleth.sso/Metadata) as mentioned in **Other Institutions** below.

### Other Institutions

COS offers a Service Provider (SP) based on [SAML 2.0](https://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html) (the protocol) and [Shibboleth 2.0](https://wiki.shibboleth.net/confluence/display/SHIB2/Home) (the implementation). To implement and test SSO for your institution:

* Ensure that your IT administrators have loaded COS's SP metadata into your IdP.
    * Production: https://accounts.osf.io/Shibboleth.sso/Metadata
    * Test and/or staging: https://accounts.test.osf.io/Shibboleth.sso/Metadata

* Ensure that your IT administrators are releasing the three required pieces of information listed below. Optional ones are highly recommended if possible. Inform COS of the attributes you use for each of them.
    * Required
        * Unique identifier for the user (e.g. `eppn`)
        * User's institutional email (e.g. `mail`)
        * User's full name (e.g. `displayName`)
    * Optional
        * User's first and last name (e.g. a pair of `givenName` and `sn`)
        * User's department(s) at your institution (e.g. `eduPersonOrgUnitDN` or `eduPersonPrimaryOrgUnitDN`)
        * User's relationship(s) (e.g. student, faculty, staff, alum, etc.) to the institution (e.g. `eduPersonAffiliation` or `eduPersonPrimaryAffiliation`)

* Provide COS with IdP metadata for your test / stage (if available) and prod servers. A URL to the metadata is preferred over an XML file so that our SP server can periodically reload and refresh the metadata.

* It is recommended that a temporary institution test account can be created for COS engineers if possible, which will significantly aid and accelerate the process.

### For All Institutions

Inform COS of the user you would like to test with; your COS contact will ensure your account is ready to go and will send you a link to test the SSO configuration setup for your institution.

## Alternative SSO Options

COS strongly recommends using this Shibboleth-based SSO when connecting to the OSF. However, if this is not available at your institution, inform COS of alternative SSO options you have. We may support them in the future.

One alternative that COS currently supports is the CAS-based SSO.

---

# Connecting to the Open Science Framework (OSF) via CAS-based Single Sign-On (SSO)

COS's CAS-based SSO has limited functionality since it is just an alternative for institutions that can not use the Shibboleth-based SSO. Before proceeding, read [Connecting to the Open Science Framework (OSF) via Shibboleth-based Single Sign-On (SSO)](https://github.com/CenterForOpenScience/cas-overlay/blob/develop/docs/osf-institutions-sso-via-saml.md) first for non-technical information on connecting to OSF via SSO.

## Technical Implementation

This SSO is based on [`cas-4.1.x`](https://github.com/apereo/cas/tree/4.1.x) and [`pac4j-1.7.x`](https://github.com/pac4j/pac4j/tree/1.7.x). Refer to the [CAS protocol](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol.html) and the [complete specification](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol-Specification.html) for how CAS works.

When connecting to the OSF via CAS-based SSO, COS's CAS system (OSF CAS) acts as the **CAS Client** and your institution's CAS system acts as the **CAS Server**. To implement and test SSO for your institution, please follow the steps below.

### Registered Service

Add OSF CAS domain / URL to your CAS system's **Registered Service** list and allow wildcard matching for query parameters.

* Production: `https://accounts.osf.io/login?`
* Test / Staging: `https://accounts.test.osf.io/login?`

### Authentication Endpoints

Inform COS of the domain of your CAS system. More specifically, OSF CAS (as a client) expects the following endpoints to be available and functional.

* Login: `<your CAS system domain>/login`
* Logout: `<your CAS system domain>/logout`
* Validation and attribute release: `<your CAS system domain>/samlValidate`

### Service Validation and Attribute Release

OSF CAS makes a `POST` request to your CAS system's [`/samlValidate`](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol-Specification.html#42-samlvalidate-cas-30) endpoint for ticket validation and attribute release. Release the following required attributes (optional ones are highly recommended if possible) and inform us of the attribute name for each.

* Required
    * Unique identifier for the user (e.g. `eppn`)
    * User's institutional email (e.g. `mail`)
    * User's full name (e.g. `displayName`)
* Optional
    * User's first and last name (e.g. a pair of `givenName` and `sn`)
    * User's department(s) at your institution (e.g. `eduPersonOrgUnitDN` or `eduPersonPrimaryOrgUnitDN`)
    * User's relationship(s) (e.g. student, faculty, staff, alum, etc.) to the institution (e.g. `eduPersonAffiliation` or `eduPersonPrimaryAffiliation`)

Please note that OSF CAS can not use your [`/p3/serviceValidate`](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol-Specification.html#28-p3servicevalidate-cas-30) endpoint due to an old version of the library it uses, namely [`pac4j-1.7.x`](https://github.com/pac4j/pac4j/tree/1.7.x) and [`cas-server-support-pac4j-1.7.x`](https://github.com/apereo/cas/tree/4.1.x/cas-server-support-pac4j). In addition, OSF CAS does not use your [`/validate`](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol-Specification.html#24-validate-cas-10) and [`/serviceValidate`](https://apereo.github.io/cas/4.1.x/protocol/CAS-Protocol-Specification.html#25-servicevalidate-cas-20) endpoints since these two can not release required attributes.

### Test Accounts

It is highly recommended that you can create a temporary institution test account for COS engineers (if possible), which will significantly aid and accelerate the process.
