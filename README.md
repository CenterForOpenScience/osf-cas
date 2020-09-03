OSF CAS by Center for Open Science
==================================

`Master` Build Status: **TBI**

`Develop` Build Status: **TBI**

Versioning Scheme: [![CalVer Scheme](https://img.shields.io/badge/calver-YY.MINOR.MICRO-22bfda.svg)](http://calver.org)

License: [![License](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/apereo/cas/blob/master/LICENSE)

# About

OSF CAS is the centralized authentication and authorization service for the [OSF](https://osf.io/) and its services such as [OSF Preprints](https://osf.io/preprints/) and [OSF Registries](https://osf.io/registries).

# Features

* OSF username and password login
* OSF username and verification key login
* OSF two-factor authentication
* **WIP** - Delegated authentication
* **TBI** - OAuth authorization server for OSF
* **TBI** - SAML service provider

# Implementations

The implementation of OSF CAS is based on [Apereo CAS 6.2.x](https://github.com/apereo/cas/tree/6.2.x) via [CAS Overlay Template 6.2.x](https://github.com/apereo/cas-overlay-template/tree/6.2). Refer to [CAS Documentaion 6.2.x](https://apereo.github.io/cas/6.2.x/) for more details.

## Legacy Implementations

A legacy version can be found at [CAS Overlay](https://github.com/CenterForOpenScience/cas-overlay), which was built on [Jasig CAS 4.1.x](https://github.com/apereo/cas/tree/4.1.x) via [CAS Overlay Template 4.1.x](https://github.com/apereo/cas-overlay-template/tree/4.1).

# Versions

- OSF CAS     `20.0.x`
- Apereo CAS  `6.2.x`
- PostgreSQL  `9.6`
- JDK         `11`

# Build and Run OSF CAS

## OSF

OSF CAS requires a working OSF running locally. Refer to OSF's [README-docker-compose.md](https://github.com/CenterForOpenScience/osf.io/blob/develop/README-docker-compose.md) for how to set up and run OSF with `docker-compose`. Must disable `fakeCAS` to free port `8080`.

In `cas.propeties`, global JDBC settings can be found [here](https://github.com/cslzchen/osf-cas/blob/21bb277cc38b3364fd67a632c0bc7b7a6ffc9efd/etc/cas/config/cas.properties#L69-L73) and JPA specific settings can be found [here](https://github.com/cslzchen/osf-cas/blob/21bb277cc38b3364fd67a632c0bc7b7a6ffc9efd/etc/cas/config/cas.properties#L54-L60).

## CAS DB

OSF CAS is configured to use the [JPA Ticket Registry](https://apereo.github.io/cas/6.2.x/ticketing/Configuring-Ticketing-Components.html#ticket-registry) for durable ticket storage. Thus, a relational database is required. Set up a `PostgreSQL@9.6` server and update *JPA Ticket Registry* [settings](https://github.com/cslzchen/osf-cas/blob/21bb277cc38b3364fd67a632c0bc7b7a6ffc9efd/etc/cas/config/cas.properties#L65-L113) in `cas.propeties` accordingly. Must use a port other than the already occupied `5432`.

## Signing and Encryption Keys

Refer to [settings](https://github.com/cslzchen/osf-cas/blob/21bb277cc38b3364fd67a632c0bc7b7a6ffc9efd/etc/cas/config/cas.properties#L117-L133) in `cas.properties` for signing and encrypting client session and ticket granting cookie.

## Authentication Delegation

### ORCiD Login

Set up a developer app at [ORCiD](https://orcid.org/developer-tools) with `http://localhost:8080/login` and `http://192.168.168.167:8080/login` as *redirect URIs*. Update
`cas.authn.pac4j.orcid.id` and `cas.authn.pac4j.orcid.secret` in `cas.properties` [settings](https://github.com/cslzchen/osf-cas/blob/21bb277cc38b3364fd67a632c0bc7b7a6ffc9efd/etc/cas/config/cas.properties#L186-L192).

### `fakeCAS` Login

With OSF CAS running locally as the authentication server for OSF, `fakeCAS` can be configured to serve as an identity provider. Simply update `fakecas` in OSF's [docker-compose.yaml](https://github.com/CenterForOpenScience/osf.io/blob/dc87c86b2afb7ad4e801b23c6428e3d2169e3e36/docker-compose.yml#L235-L247) to listen on port 8081.

```
fakecas:
  image: quay.io/centerforopenscience/fakecas:master
  command: fakecas -host=0.0.0.0:8081 -osfhost=localhost:5000 -dbaddress=postgres://postgres@postgres:5432/osf?sslmode=disable
  restart: unless-stopped
  ports:
    - 8081:8081
  depends_on:
    - postgres
  stdin_open: true
```

Related settings in `cas.propeties` can be found [here](https://github.com/cslzchen/osf-cas/blob/21bb277cc38b3364fd67a632c0bc7b7a6ffc9efd/etc/cas/config/cas.properties#L196-L199).

## Build and Run

It is recommended to use the `Dockerfile` and the provided scripts to build and run CAS.

```bash
./docker-build.sh
./docker-run.sh
```

Refer to Apereo's [README.md](https://github.com/apereo/cas-overlay-template/tree/6.2#cas-overlay-template-) for more options.
