# Authentication Options

By customizing the flag `APP_AUTHENTICATION` you can pick your authentication strategy that suits you best.

## Single Tenant
### Root Login
`APP_AUTHENTICATION=root` will enable the single tenant authentication. You can login in the UI using `$APP_ROOT_EMAIL` and `$APP_ROOT_SECRET_KEY`.

## Multi Tenant
### Magic Link via email (Multi Tenant)
Magic Auth Links is the most decoupled authentication strategy. 

`APP_AUTHENTICATION=mail` will activate a two-phased login process using magic links via email.
In order to send emails you need to configure an SMTP server by filling out all `APP_MAIL_*` fields.

This feature is not yet fully implemented. It would need some kind of registration for new users (invitation links or plain signup). Create a ticket if
you think this is useful.

### Single Sign On (Multi Tenant)
SSO is the strategy used in the `feedless` prod deployment, cause it is the most seamless. 
By setting `APP_AUTHENTICATION=sso` authentication is run through the defined SSO providers. If the oauth reply is successful, `feedless` will 
create its own token for internal authentication.
