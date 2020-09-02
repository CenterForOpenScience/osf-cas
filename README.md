OSF CAS by Center for Open Science
==================================

`Master` Build Status: TBI

`Develop` Build Status: TBI

Versioning Scheme: [![CalVer Scheme](https://img.shields.io/badge/calver-YY.MINOR.MICRO-22bfda.svg)](http://calver.org)

License: [![License](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/apereo/cas/blob/master/LICENSE)

# About

OSF CAS is the centralized authentication and authorization service for the [OSF](https://osf.io/) and its services such as [OSF Preprints](https://osf.io/preprints/) and [OSF Registries](https://osf.io/registries).

## Features

* OSF username / password login
* OSF username and verification key login
* OSF two-factor authentication
* (WIP) Delegated authentication - SSO via external identity providers
* (TBI) OAuth authorization server for OSF
* (TBI) SAML Service provider

## Implementations

The implementation of OSF CAS is based on [Apereo CAS 6.2.x](https://github.com/apereo/cas/tree/6.2.x) via [CAS Overlay Template 6.2.x](https://github.com/apereo/cas-overlay-template/tree/6.2). Refer to the official [documentaion](https://apereo.github.io/cas/6.2.x/) for more details.

## Versions

- OSF CAS     `20.0.x`
- Apereo CAS  `6.2.x`
- PostgreSQL  `9.6`
- JDK         `11`

## Running OSF CAS for Development

### OSF

OSF CAS requires a working OSF running locally. Refer to [Running the OSF For Development](https://github.com/CenterForOpenScience/osf.io/blob/develop/README-docker-compose.md) for how to run OSF locally with `docker-compose`. Disable `fakeCAS` to free the `8080` port.

The default [settings](https://github.com/cslzchen/osf-cas/blob/develop/etc/cas/config/cas.properties) in section **OSF PostgreSQL Authentication** should work as it is.

### CAS DB

OSF CAS implementes the [JPA Ticket Registry](https://apereo.github.io/cas/6.2.x/ticketing/Configuring-Ticketing-Components.html#ticket-registry) for durable ticket storage; thus a database is requried. Take a look at section **JPA Ticket Registry** in the [settings](https://github.com/cslzchen/osf-cas/blob/develop/etc/cas/config/cas.properties) and set up the PostgreSQL server accordingly.

### Authentication Delegation

#### ORCiD Login

Set up a developer app at [ORCiD](https://orcid.org/developer-tools) with `http://localhost:8080/login` and `http://192.168.168.167:8080/login` as redirect URIs. Update
`cas.authn.pac4j.orcid.id` and `cas.authn.pac4j.orcid.secret` in the [settings](https://github.com/cslzchen/osf-cas/blob/develop/etc/cas/config/cas.properties) accordingly.

#### `fakeCAS` Login

With OSF CAS running locally as the authentication server for OSF, `fakeCAS` can be configured to serve as an identity provider. Simply update `fakecas` in OSF's [docker-compose.yaml](https://github.com/CenterForOpenScience/osf.io/blob/develop/docker-compose.yml) to listen on port 8081.

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

### Build and Run

It is recommended to use the `Dockerfile` and the provided scripts to build and run CAS.

```bash
./docker-build.sh
./docker-run.sh
```

Refer to Apereo's [README.md](https://github.com/apereo/cas-overlay-template/tree/6.2#cas-overlay-template-) for more options.
