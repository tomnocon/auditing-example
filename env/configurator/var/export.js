const http = require('http');
const fs = require('fs');

http.get({
    hostname: 'localhost',
    port: 5601,
    path: '/api/saved_objects/_find?type=index-pattern&type=visualization&type=search&type=dashboard&per_page=100',
    agent: false
}, (res) => {

    let body = '';
    res.on('data', function(chunk) {
        body = body.concat(chunk);
    });
    res.on('end', function() {
        const response = JSON.parse(body);
        const savedObjects = response.saved_objects;
        for(const savedObject of savedObjects){
            delete savedObject.updated_at
        }
        fs.writeFileSync('saved-objects.json', JSON.stringify(savedObjects, null, 2))
    });

}).on('error', function(e) {
    console.error("Http error: " + e.message);
    exit(1)
});