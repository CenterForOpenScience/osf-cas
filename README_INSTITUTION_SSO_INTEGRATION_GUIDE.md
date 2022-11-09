# Connecting to the Open Science Framework (OSF) via our Shibboleth/SAML Service Provider

This article provides general information about the COS's Shibboleth/SAML SSO integration for organizations who have signed the *OSF for Institutions Offer of Services* letter.

## What is Single Sign-On?

In general, Single Sign-On, or SSO, allows users authenticated with one trusted system (e.g. university network) to also authenticate using those same “home” credentials with another trusted network (e.g. OSF). In the case of the second authentication, users are not asked to log in again, but instead the authenticated credentials are shared between systems.

## Who can use Single Sign-On with OSF?

Organizations that have implemented a SAML 2.0 Identity Provider (IdP) and signed the *OSF for Institutions Offer of Services* are eligible to use this feature.

### A few notes:

* Current OSF users who have already set up accounts with a different login, will be able to retain those credentials and choose to login with personal or institutional credentials.

* Users’ authentication to the OSF service using SSO cannot also use the “forgot Password” link on the OSF website to remind them of their credentials, as their user credentials are specific to and managed by their organization.

* OSF also provides extra features such as Shared SSO, Selective SSO and Institutional Dashboard, which all require extra configurations. This guide does not include technical guide for them since they are institution and/or SSO specific. 

## Technical Implementation

### Institutions registered with the InCommon Federation

The InCommon Federation provides secure single sign-on access to cloud and local services, and global collaboration tools. COS is a [Research & Scholarship Entity Category (R&S)](https://refeds.org/category/research-and-scholarship) Service Provider (SP) registered with the [InCommon Federation](https://www.incommon.org/federation/).

> * SP Entity ID: `https://accounts.osf.io/shibboleth`
> * Required Attributes:
>   * `eduPersonPrincipalName` for user's institutional identity
>   * `mail` for user's email
>   * `displayName` for user's full name (or a pair of `givenName` **AND** `sn` for user's first and last name)

* Note that only COS's production SP server is registered with the InCommon Federation. Both IdP and SP being registered with InCommon makes it no longer necessary to configure a test server before going production. If you need to connect to COS's test SP server, follow the notes mentioned in **Other Institutions** below.


### Institutions registered with eduGAIN Participants

[eduGAIN](https://edugain.org/) is a global service that provides an efficient, flexible way for participating federations, and their affiliated users and services, to interconnect. The InCommon Federation is the U.S. participant. If your institution is registered with your local participant, you can use the above guide. Note that you may need to enable **interfederation** if it is an option and if it is disabled by default. Learn more about eduGAIN from [what is eduGAIN](https://edugain.org/about-edugain/what-is-edugain/), [key concepts](https://edugain.org/about-edugain/key-concepts/), [participants](https://technical.edugain.org/status) and [usage guide](https://edugain.org/participants/how-to-use-edugain/); learn more about interfederation from [InCommon's perspective](https://spaces.at.internet2.edu/display/federation/Interfederation+and+eduGAIN).

### Other Institutions

For institutions that are not registered with any participant of eduGAIN, COS offers a [SAML 2.0](https://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html) Service Provider (SP) using [Shibboleth 2.0](https://wiki.shibboleth.net/confluence/display/SHIB2/Home). Follow the following guide to manually configure your IdP server. 

* Ensure that your IT administrators have loaded COS's SP metadata into your IdP.
  * Production: https://accounts.osf.io/Shibboleth.sso/Metadata
  	* Entity ID: `https://accounts.osf.io/shibboleth`

  * Test and/or staging: https://accounts.test.osf.io/Shibboleth.sso/Metadata
    * Entity ID: `https://accounts.test.osf.io/shibboleth`

* Ensure that your IT administrators are releasing the three required pieces of information listed below. Inform COS of the attribute name you use for each of them.
  * Required Attribute
    * Unique identifier for the user
    * User's institutional email
    * Either of the follwoing two
        * User's full name
        * User's first **AND** last name
  * Attribute name and format
    * We strongly recommend using URNs that are already configured and mapped in our SP server. Athough we support many format, `eduPerson`(https://wiki.refeds.org/display/STAN/eduPerson) is the preferred one. Here are the URNs for aformentioned required attributes.
      * For identity, there are two options, please let us know which one you choose.
	    * `urn:oid:1.3.6.1.4.1.5923.1.1.1.6`: this is the `eppn` which needs to be scoped with the default delimiter `@`.
	      * This attribute looks like an email; it may or may not be an actual email; but it **SHOULD NOT** be used for the email attribute.
		* `urn:oid:0.9.2342.19200300.100.1.1`: this is the `uid` which doesn't need to be scoped
	  * For the email, use `urn:oid:0.9.2342.19200300.100.1.3`, which is the `mail`
	  * For the full name, use `urn:oid:2.16.840.1.113730.3.1.241`, which is the `displayName`
	  * For the first name, use `urn:oid:2.5.4.42`, which is the `givenName`
	  * For the last name, use `urn:oid:2.5.4.4`, which is the `sn`

* Provide COS with the metadata URL for your IdP server.

* Provide COS with the entiry ID for your IdP server, which should be the same as the one defined in your metadata.

* It is recommended that a temporary institution test account is created for COS engineers if possible, which will significantly aid and accelerate the process.

### For All Institutions

Inform COS of the user you would like to test with; your COS contact will ensure your account is ready to go and will send you a link to test the SSO configuration setup for your institution.

## Alternative SSO Options

COS strongly recommends using SAML SSO when connecting to the OSF. However, if this is not available at your institution, inform COS of alternative SSO options you have.
