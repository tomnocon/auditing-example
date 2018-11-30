const Hapi = require('hapi');
const Inert = require('inert');
const Path = require('path');
const yamlFormat = require('nconf-yaml');
const nconf = require('nconf/lib/nconf');
const fluentd = require('fluent-logger');

async function run() {

    const config = nconf.file({
        file: Path.join(__dirname, 'config/config.yaml'),
        format: yamlFormat
    }).get();

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

    await server.register({
        plugin: require('yar'),
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
        method: 'POST',
        path: '/api/audit-event',
        handler: (request) => {
            const { key, data } = request.payload;
            const accessToken = request.auth.credentials.accessToken.content;
            const message = {
                time: Math.floor(new Date().getTime() / 1000),
                type: "UI_EVENT",
                realmId: config.keycloak.realm,
                clientId: accessToken.azp,
                userId: accessToken.sub,
                sessionId: accessToken.session_state,
                ipAddress: request.info.remoteAddress,
                details: data
            };
            logger.emit(key, message);
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
