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

# Configure, Build and Run OSF CAS

It is recommended to use the provided scripts to [build](https://github.com/CenterForOpenScience/osf-cas/blob/develop/docker-build.sh) and [run](https://github.com/CenterForOpenScience/osf-cas/blob/develop/docker-run.sh) CAS. Refer to Apereo's [README.md](https://github.com/apereo/cas-overlay-template/tree/6.2#cas-overlay-template-) for more options.

Use [`cas.properties`](https://github.com/CenterForOpenScience/osf-cas/blob/develop/etc/cas/config/cas.properties) and [`Dockerfile`](https://github.com/CenterForOpenScience/osf-cas/blob/develop/Dockerfile) to configure staging and production servers. Use [`cas-local.properties`](https://github.com/CenterForOpenScience/osf-cas/blob/develop/etc/cas/config/local/cas-local.properties) and [`Dockerfile-local`](https://github.com/CenterForOpenScience/osf-cas/blob/develop/Dockerfile-local) for local development.

## OSF

OSF CAS requires a working OSF running locally. Refer to OSF's [README-docker-compose.md](https://github.com/CenterForOpenScience/osf.io/blob/develop/README-docker-compose.md) for how to set up and run OSF with `docker-compose`. Must disable `fakeCAS` to free port `8080`.

### OSF DB

More specifically, CAS requires a running OSF PostgreSQL database to handle authentication for OSF. Use the authentication handler's [JPA settings](https://github.com/CenterForOpenScience/osf-cas/blob/790cac1ac5a19754c67d6ea1f53afc26e1809d23/etc/cas/config/cas.properties#L70-L86) for [`OsfPostgresAuthenticationHandler`](https://github.com/CenterForOpenScience/osf-cas/blob/develop/src/main/java/io/cos/cas/osf/authentication/handler/support/OsfPostgresAuthenticationHandler.java) to connect to and access OSF DB.

Here is an example for local development. Use `192.168.168.167` to access host outside the docker container. Set `readOnly=true` since CAS only needs read-only access ([Principle of Least Privilege](https://en.wikipedia.org/wiki/Principle_of_least_privilege)).

```yaml
# In `cas.properties` or `cas-local.properties`

cas.authn.osf-postgres.jpa.user=postgres
cas.authn.osf-postgres.jpa.password=
cas.authn.osf-postgres.jpa.driver-class=org.postgresql.Driver
cas.authn.osf-postgres.jpa.url=jdbc:postgresql://192.168.168.167:5432/osf?targetServerType=master&readOnly=true
cas.authn.osf-postgres.jpa.dialect=io.cos.cas.osf.hibernate.dialect.OsfPostgresDialect
```

## CAS DB

The implementation of OSF CAS uses the [JPA Ticket Registry](https://apereo.github.io/cas/6.2.x/ticketing/Configuring-Ticketing-Components.html#ticket-registry) for durable ticket storage and thus requires a relational database. Set up a `PostgreSQL@9.6` server and review [JPA Ticket Registry settings](https://github.com/CenterForOpenScience/osf-cas/blob/790cac1ac5a19754c67d6ea1f53afc26e1809d23/etc/cas/config/cas.properties#L90-L138). In most cases, only [Database connections](https://github.com/CenterForOpenScience/osf-cas/blob/790cac1ac5a19754c67d6ea1f53afc26e1809d23/etc/cas/config/cas.properties#L104-L108) need to be updated. In addition, [JDBC settings](https://github.com/CenterForOpenScience/osf-cas/blob/790cac1ac5a19754c67d6ea1f53afc26e1809d23/etc/cas/config/cas.properties#L96-L98) can be adjusted if necessary.

Here is an example for local development. Use `192.168.168.167` to access host outside the docker container. Use the port `54321` since the default `5432` one has been used by OSF DB. Update `pg_hba.conf` to grant proper access permission depending on the setup.

```yaml
# In `cas.properties` or `cas-local.properties`

cas.ticket.registry.jpa.user=longzechen
cas.ticket.registry.jpa.password=
cas.ticket.registry.jpa.driver-class=org.postgresql.Driver
cas.ticket.registry.jpa.url=jdbc:postgresql://192.168.168.167:54321/osf-cas?targetServerType=master
cas.ticket.registry.jpa.dialect=org.hibernate.dialect.PostgreSQL95Dialect
```

```yaml
# In `pg_hba.conf`

# TYPE  DATABASE        USER            ADDRESS                 METHOD
host    osf-cas         longzechen      192.168.168.167/24      trust
```

## Signing and Encryption Keys

Refer to [signing and encryption settings](https://github.com/CenterForOpenScience/osf-cas/blob/790cac1ac5a19754c67d6ea1f53afc26e1809d23/etc/cas/config/cas.properties#L142-L158) in `cas.properties` for signing and encrypting client sessions and ticket granting cookies. Use the following default keys **for local development only**.

```yaml
# In `cas-local.properties`

cas.webflow.crypto.signing.key=7okipSDHBKuZL2n66cfYU4OH1Z_BRYkmEJazc29hzhXCXbRvws7Hv4_hEVd4E2osMrgIEdykzV2hAVD9CCQpJw
cas.webflow.crypto.encryption.key=1A9hLtxA-Es-hbQsfxqxxw

cas.tgc.crypto.signing.key=_WTRQmXGuq-mx2pTjNCXuX5971-e8KOCFOa27Mh5I3oBobYSzyUrLS9rfSiXQQDolJrJrWv7jURID1vtouznHg
cas.tgc.crypto.encryption.key=dcjD5PIfrcqGM8tv4_FGubcay-DbqKPPoz9xQ3IHQi0
```

## Authentication Delegation

### ORCiD Login

To enable ORCiD login, update `cas.authn.pac4j.orcid.id` and `cas.authn.pac4j.orcid.secret` [here](https://github.com/CenterForOpenScience/osf-cas/blob/790cac1ac5a19754c67d6ea1f53afc26e1809d23/etc/cas/config/cas.properties#L212-L213) in delegation settings accordingly.

For local development, set up a developer app at [ORCiD](https://orcid.org/developer-tools) with `http://localhost:8080/login` and `http://192.168.168.167:8080/login` as *redirect URIs*.

### Institution Login

( ... WIP ... )

#### `fakeCAS` Login (Local Development Only)

With OSF CAS running locally as the authentication server for OSF, the previously disabled `fakeCAS` can be re-configured to serve as an identity provider. Simply update `fakecas` in OSF's [docker-compose.yaml](https://github.com/CenterForOpenScience/osf.io/blob/dc87c86b2afb7ad4e801b23c6428e3d2169e3e36/docker-compose.yml#L235-L247) to listen on port `8081`.

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

Related `cas.propeties` settings can be found [here](https://github.com/CenterForOpenScience/osf-cas/blob/790cac1ac5a19754c67d6ea1f53afc26e1809d23/etc/cas/config/local/cas-local.properties#L192-L235).

```yaml
# In `cas-local.properties`

cas.authn.osf-postgres.institution-clients[2]=${cas.authn.pac4j.cas[2].client-name}

cas.authn.pac4j.cas[2].login-url=http://192.168.168.167:8081/login
cas.authn.pac4j.cas[2].client-name=fakecas
cas.authn.pac4j.cas[2].protocol=CAS30
cas.authn.pac4j.cas[2].callback-url-type=QUERY_PARAMETER
```
