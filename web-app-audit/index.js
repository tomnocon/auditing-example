const Hapi = require('@hapi/hapi');
const Inert = require('@hapi/inert');
const Path = require('path');
const yamlFormat = require('nconf-yaml');
const nconf = require('nconf/lib/nconf');
const fluentd = require('fluent-logger');
const promster = require('@promster/hapi');

const getIpAddress = (request) => {
    const forwardedHeader = request.headers['x-forwarded-for'];
    return forwardedHeader ? forwardedHeader.split(',')[0] : request.info.remoteAddress;
};

async function run() {

    nconf.env('_');

    const config = nconf.file({
        file: Path.join(__dirname, 'config/config.yaml'),
        format: yamlFormat
    }).get();

    console.log(JSON.stringify(config, null, 2));

    const serverOptions = Object.assign({
        routes: {
            files: {
                relativeTo: Path.join(__dirname, 'public')
            }
        }
    }, config.server);

    const logger = fluentd.createFluentSender('audit', config.fluentd);

    const server = Hapi.server(serverOptions);

    await server.register(Inert);

    await server.register(promster.createPlugin());

    await server.register({
        plugin: require('@hapi/yar'),
        options: config.session
    });

    await server.register({
        plugin: require('keycloak-hapi'),
        options: config.keycloak
    });

    server.route({
        method: 'GET',
        path: '/{param*}',
        handler: {
            directory: {
                path: '.',
                redirectToSlash: true,
                index: true,
            }
        }
    });

    server.route({
        method: 'GET',
        path: '/sso/account',
        handler: (request, h) => {
            const accountUrl = `${config.keycloak.serverUrl}/realms/${config.keycloak.realm}/account/`;
            return h.redirect(accountUrl);
        }
    });

    server.route({
        method: 'GET',
        path: '/metrics',
        handler: (request, h) => {
            return h.response(promster.getSummary())
                .type(promster.getContentType());
        },
        config: {
            auth: false
        }
    });

    server.route({
        method: 'POST',
        path: '/api/audit-event',
        handler: (request) => {
            const {key, data} = request.payload;
            const accessToken = request.auth.credentials.accessToken.content;
            const tag = config.keycloak.clientId + '.' + key;
            const message = {
                timestamp: new Date().toISOString(),
                auth: {
                    realmId: config.keycloak.realm,
                    clientId: accessToken.azp,
                    userId: accessToken.sub,
                    username: accessToken.preferred_username,
                    sessionId: accessToken.session_state,
                    ipAddress: getIpAddress(request)
                },
                details: data,
                tag
            };
            logger.emit(`audit.${tag}`, message);
            return null;
        }
    });

    server.auth.strategy('keycloak', 'keycloak');
    server.auth.default('keycloak');
    await server.start();
    console.log(`Server running at: ${server.info.uri}`);
}

run().catch(error => {
    console.error(error);
    process.exit(1);
});
