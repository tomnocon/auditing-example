const objects = require('./saved-objects.json')

const objectsIds = [];

for(const obj of objects){
    objectsIds.push({type: obj._type, id: obj._id});
}

console.log(JSON.stringify(objectsIds, null, 2));