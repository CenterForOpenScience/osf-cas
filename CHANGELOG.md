# Changelog

We follow the CalVer (https://calver.org/) versioning scheme: YY.MINOR.MICRO.

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

