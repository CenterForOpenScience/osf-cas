# Changelog

We follow the CalVer (https://calver.org/) versioning scheme: YY.MINOR.MICRO.

21.2.0 (04-28-2021)
===================

newCAS Production Server Release

21.1.5 (04-14-2021)
===================

Fix invalid access type for OAuth /authorize

21.1.4 (04-14-2021)
===================

Fix personal access token revokation

21.1.3 (04-09-2021)
===================

Test server institution SSO update

21.1.2 (04-08-2021)
===================

OAuth callback authorize service update

21.1.1 (04-08-2021)
===================

Front-end fixes improvements

21.1.0 (03-30-2021)
===================

OSF CAS feature-complete release

* OSF CAS as an OAuth 2.0 server
* OSF personal access token, developer apps and oauth scopes
* Authentication failure throttling
* Customized institution logout
* Institution department
* Overlay template and core library upgrade

21.0.0 (02-03-2021)
===================

OSF CAS third release with web flow updates, institution SSO, and FE rework

* Login and logout web flow fixes and improvements
* Fully functional institution SSO, BE and FE
* FE rework and UI / UX improvements

Extra features

* Institution SSO migration
* TOS consent check
* SonarQube integraiton

20.1.0 (11-05-2020)
===================

OSF CAS second release with FE re-design, BE improvements and DevOps updates

* FE
  * Refactored styles to be consistent with both OSF and oldCAS
  * Rewrote how authentication delegation info is retrieved and used
  * Improved the behavior of inline error messages in login forms
  * Added new / Rewrote existing authentication exception pages
  * Improved UI / UX in responsive mode and on various screen dimensions

* BE
  * Implemented a dedicated OSF CAS login context and a couple of pre-login
    check actions to support ORCiD login and institution SSO
  * Implemented ORCiD sign-up auto-redirect for OSF

* DevOps
  * Updated tomcat server to work with its enclosing Shibboleth server
  * Separated staging / production and local configurations
  * Rewrote cas.properties into a helm charts template
  * Improved build / run / reload for local development

* Other
  * Replaced Apereo branding with COS / OSF in multiple places

20.0.0 (09-02-2020)
===================

OSF CAS first release with basic authentication features for OSF

* Username and password login
* Username and verification key login
* Two-factor authenticaion
* Long-term authentication
* ORCiD login

Technical details

* JSON service registry
* JPA ticket regisrtry with PostgreSQL
* JPA PostgreSQL authentication backend
* Customized login web flow and authentication including:
  * OSF credential and metadata populator
  * OSF non-interactive authentication action
  * OSF PostgreSQL authentication handler
  * Pac4j authentication delegation
  * Two-factor authentication using time-based one-time password
  * Customized authentication exception handling
  * Customized user interface

