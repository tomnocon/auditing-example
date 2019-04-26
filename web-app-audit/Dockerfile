FROM node:12.0-alpine

WORKDIR /opt/web-app

COPY . .

RUN npm install

ENTRYPOINT ["npm", "run", "start"]