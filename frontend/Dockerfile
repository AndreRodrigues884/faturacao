# =========================================================================
# Imagem do FRONTEND (Angular servido pelo nginx)
# Etapa 1 compila com o Node; etapa 2 serve os ficheiros estáticos com o nginx.
# =========================================================================

# ---------- Etapa 1: compilar ----------
FROM node:24-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---------- Etapa 2: servir ----------
FROM nginx:alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80