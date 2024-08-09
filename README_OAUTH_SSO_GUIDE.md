# OSF CAS as an OAuth Server

## About

OSF CAS serves as an OAuth 2.0 authorization server for OSF in addition to its primary role as a CAS authentication server.

### The OAuth 2.0 Protocol

* [RFC 6749](https://tools.ietf.org/html/rfc6749)
* [OAuth 2.0](https://oauth.net/2/)
* [OAuth 2.0 Simplified](https://aaronparecki.com/oauth-2-simplified/)

### Parties

| Party                | Who               |
|----------------------|-------------------|
| Client Application   | A web application |
| Authorization Server | OSF CAS           |
| Resource Owner       | OSF Users         |
| Resource Server      | OSF API           |

## Features

### General

* Authorize client applications
* Exchange authorization code for access and refresh token
* Refresh access token using refresh token
* Revoke access and refresh tokens

### Client Application Owners

* Revoke all tokens of the application for all users

### Resource Owners

* List all authorized applications
* Revoke all tokens of an authorized application for the owner

<br>

## Endpoints

### `GET /oauth2/authorize`

#### Authorize a Client Application

Securely allows or denies the client application's request to access the resource user's information with specified scopes. It returns a one-time and short-lived authorization code, which will be used to follow up with the token-code exchange.

* Authorization Web Flow
  * (1) The client application issues the initial authorization request to `oauth2/authorize` with the above parameters;
  * (2) If the user is not logged in, redirects to the primary CAS login with `oauth2/callbackAuthorize` as service;
  * (3) Both step 1 and 2 end up redirecting the user to `oauth2/callbackAuthorize` for service validation;
  * (4) After validation, redirects to `oauth2/callbackAuthorize` one more time, which checks previous decisions and asks the user to allow or deny the authorization if necessary.
  * (5) If denied, redirects to `/oauth2/callbackAuthorizeAction?action=DENY`; if allowed, redirects to `/oauth2/callbackAuthorizeAction?action=ALLOW`
  * (6) Finally, for both decisions in step 5, redirects the user to the *Redirect URI*: `https://my.app.io/oauth2/callback?` with different query parameters as shown below.

* Request in Step (1)

```
https://accounts.osf.io/oauth2/authorize
```

| Parameter       | Value / Example                     | Notes                          |
|-----------------|-------------------------------------|--------------------------------|
| response_type   | code                                |                                |
| client_id       | `ffe5247b810045a8a9277d3b3b4edc7a`  |                                |
| redirect_uri    | `https://my.app.io/oauth2/callback` |                                |
| scope           | `osf.full_read`                     |                                |
| access_type     | **`online`** / `offline`            | Optional with default `online` |
| approval_prompt | **`auto`** / `force`                | Optional with default `auto`   |

* Response in Step (6)

| Parameter | Value / Example                                           | Note                   |
|-----------|-----------------------------------------------------------|------------------------|
| code      | `AC-1-mFs7MrWvaQy1fiidWGwXTw4dbAH30wk39cAELJnxizjGCUXYJl` | If `ALLOW` in step (5) |

| Parameter | Value / Example | Note                  |
|-----------|-----------------|-----------------------|
| error     | `access_denied` | If `DENY` in step (5) |

<br><hr>

### `POST /oauth2/token`

#### Exchange Code for Token

Exchanges the authorization code for an access token and potentially a refresh token if `offline` mode was specified **when requesting for the authorization code**.

* Request

```
https://accounts.osf.io/oauth2/token
```

* `POST` Body Parameters

| Parameter     | Value / Example                                           | Notes |
|---------------|-----------------------------------------------------------|-------|
| code          | `AC-1-mFs7MrWvaQy1fiidWGwXTw4dbAH30wk39cAELJnxizjGCUXYJl` |       |
| client_id     | `ffe5247b810045a8a9277d3b3b4edc7a`                        |       |
| client_secret | `5PgE96R3Z53dBuwBDkJfbK6ItDXvGhaxYpQ6r4cU`                |       |
| redirect_uri  | `https://my.app.io/oauth2/callback`                       |       |
| grant_type    | `authorization_code`                                      |       |

* Response Status

```
HTTP 200 OK
```
* Response Body if `online` Mode

```json
{
    "access_token": "AT-2-p5jtVLATgft5EHqqbCTagg5i3q9e1htdcGEBvcpq0l1b2RyQav4bItEKPcDh94c5z7d7EK",
    "token_type": "Bearer",
    "expires_in": 3600
}
```

* Response Body if `offline` Mode

```json
{
    "access_token": "AT-1-IBGuzWBdencAMz74LQkIuNcbLuu9WM3TYyacadkecrHUlcivs1GnWHjFmlkZPYg4TTAUM4",
    "refresh_token": "RT-1-xfQXZaqXSQIJykCg2vnfdQjc5efVKdtteXaPo0OwCxWzIAacfC",
    "token_type": "Bearer",
    "expires_in": 3600
}
```

#### Refresh Access Token

In *`offline`* mode, the client application may request for a new access token by presenting the previously granted refresh token.

* Request

```
https://accounts.osf.io/oauth2/token
```

* `POST` Body Parameters

| Parameter     | Value / Example                                           | Note |
|---------------|-----------------------------------------------------------|------|
| refresh_token | `RT-1-xfQXZaqXSQIJykCg2vnfdQjc5efVKdtteXaPo0OwCxWzIAacfC` |      |
| client_id     | `ffe5247b810045a8a9277d3b3b4edc7a`                        |      |
| client_secret | `5PgE96R3Z53dBuwBDkJfbK6ItDXvGhaxYpQ6r4cU`                |      |
| grant_type    | `refresh_token`                                           |      |

* Response Status

```
HTTP 200 OK
```

* Response Body

```json
{
    "access_token": "AT-3-WbBmXVTsPlhUatrs5sQmilVLnA30wVv3holmfFCbIfePRjzQ6UXCb7LwJHGbFqmad3wNXu",
    "token_type": "Bearer",
    "expires_in": 3600
}
```

<br><hr>

### `GET /oauth2/profile`

#### Profile

Provides the user's principal ID, any released attributes and a list of granted scopes.

* Request

```
https://accounts.osf.io/oauth2/profile
```

* Authorization Header

| Name          | Value / Example                                                                      | Note |
|---------------|--------------------------------------------------------------------------------------|------|
| Authorization | `Bearer AT-4-IdanI4hWiybRzARBiLrlMdeMTlDJIqo1UgVLb4MHzbF13pNIT5POrfQTMW5yEyVD1oXXcz` |      |

* Response Status

```
HTTP 200 OK
```

* Response Body

```json
{
    "scope": [
        "osf.full_read"
    ],
    "id": "f2t7d"
}
```

<br><hr>

### `POST /oauth2/revoke`

#### Revoke One Token

Handles revocation of refresh and access tokens.

* Request: Revoke one access token

```
https://accounts.osf.io/oauth2/revoke
```

* `POST` Body Parameters for revoke one access token

| Name  | Value / Example                                                               | Note |
|-------|-------------------------------------------------------------------------------|------|
| token | `AT-6-0ckMxjkBHgs5PMqbCtg9BgFo49Y60A1bC5QxFnQeWdiWe9ZfvKwWS52jyIwLrrwVMGFxfa` |      |

* `POST` Body Parameters for revoke one refresh token and all access token granted by this refresh token.

| Name  | Value / Example                                           | Note |
|-------|-----------------------------------------------------------|------|
| token | `RT-1-xfQXZaqXSQIJykCg2vnfdQjc5efVKdtteXaPo0OwCxWzIAacfC` |      |

* Response Status

```
HTTP 204 NO CONTENT
```

#### Revoke Tokens for a Client Application

Revokes all tokens associated with a client application specified by the given client ID.

* Request

```
https://accounts.osf.io/oauth2/revoke
```

* `POST` Body Parameters

| Parameter     | Value / Example                            | Note |
|---------------|--------------------------------------------|------|
| client_id     | `ffe5247b810045a8a9277d3b3b4edc7a`         |      |
| client_secret | `5PgE96R3Z53dBuwBDkJfbK6ItDXvGhaxYpQ6r4cU` |      |

* Response Status

```
HTTP 204 NO CONTENT
```

#### Revoke All Tokens for a Resource User

Revokes all tokens of a client application that have been issued to a resource user. The application is specified by the client ID and the user is specified by the principal ID associated with the access token. The token used for authorization must have been generated by the application *unless it is of token type `CAS`*.

* Request

```
https://accounts.osf.io/oauth2/revoke
```

* Authorization Header

| Name          | Value / Example                                                                      | Note |
|---------------|--------------------------------------------------------------------------------------|------|
| Authorization | `Bearer AT-7-PvVw9wIcTOZYXFCVWbFhwsf9Q3idASiJeBdiWmLExcXSG54lCycokgCefWsy2Nzds4LoAW` |      |

* `POST` Body Parameters

| Parameter | Value / Example                    | Note |
|-----------|------------------------------------|------|
| client_id | `ffe5247b810045a8a9277d3b3b4edc7a` |      |

* Response Status

```
HTTP 204 NO CONTENT
```
