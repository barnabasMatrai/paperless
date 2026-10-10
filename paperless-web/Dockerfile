# --- Build stage ---
FROM node:22 AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npx ng build --configuration production

# --- Runtime stage ---
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/paperless-web/browser /usr/share/nginx/html

EXPOSE 80