function request(method, url, data){
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        const processRequest = () => {
            if (xhr.readyState === 4) {
                if (xhr.status >= 200 && xhr.status < 300) {
                    try {
                        resolve(xhr.responseText);
                    } catch (err) {
                        reject(new Error(`Unable to parse response from the endpoint: ${url}. ${err}`));
                    }
                } else if (xhr.status === 401) {
                    window.location.reload(true);
                    reject(new Error('A user is not authenticated.'));
                } else {
                    reject(new Error(`A ${method} Request to ${url} failed with status code ${xhr.status}`));
                }
            }
        };
        xhr.open(method, url, true);
        xhr.addEventListener('readystatechange', processRequest, false);
        if (data) {
            xhr.setRequestHeader("Content-Type", "application/json");
            xhr.send(JSON.stringify(data));
        } else {
            xhr.send();
        }
    });
}

function reportAuditEvent(key, data) {
    request('POST', '/api/audit-event', {key: key, data: data}).catch(ex => {
        console.error(ex);
    });
}

function registerErrorHandlers() {
    window.addEventListener('error', function (event) {
        //event.preventDefault();
        // ignore generic script errors, which come from non-origin sites
        if (event.message.toLowerCase().indexOf('script error') !== -1) {
            return false;
        }
        reportAuditEvent('ui.error', {message: event.message, stack: event.error.stack});
    }, true);
    window.addEventListener('unhandledrejection', function (event) {
        //event.preventDefault();
        reportAuditEvent('ui.error.unhandledrejection', {message: event.reason});
    }, true);
}

registerErrorHandlers();

