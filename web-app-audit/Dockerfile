FROM node:12.2

WORKDIR /opt/web-app

COPY . .

RUN npm install

ENTRYPOINT ["npm", "run", "start"]