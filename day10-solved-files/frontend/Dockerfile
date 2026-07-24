# ============================================================
# TICKET-F109 (Day 10, Sprint 9) — Frontend Dockerfile (Multi-Stage Build)
# ============================================================
#
# WHAT: Builds the React app and serves it with nginx.
#       Same multi-stage concept as the backend Dockerfile:
#         Stage 1: Uses Node.js to COMPILE React (JSX → plain JavaScript)
#         Stage 2: Uses nginx to SERVE the compiled static files
#       Node.js is NOT needed at runtime — only nginx.
#
# WHY:  React code (JSX, modern JavaScript, imports) can't run directly in browsers.
#       "npm run build" transforms it into plain HTML/CSS/JS that any browser understands.
#       In production, you need a web server (nginx) to deliver those files — not Node.js.
#       nginx is faster, uses less memory, and handles thousands of concurrent connections.
#
# ANALOGY:
#       Stage 1 is the bakery kitchen (oven, mixer, ingredients) → produces bread
#       Stage 2 is the shop counter (just a shelf) → serves the bread to customers
#       The kitchen equipment doesn't go to the customer.
#
# HOW TO BUILD & RUN:
#   cd frontend
#   docker build -t smartbudget-frontend .
#   docker run -p 80:80 smartbudget-frontend
#   # Visit http://localhost — UI loads (but /api fails without the backend)
# ============================================================

# syntax=docker/dockerfile:1.6

# -------------------------------------------------------
# Stage 1 — Build stage (Node.js compiles React → static files)
# -------------------------------------------------------
FROM node:22-alpine AS builder

WORKDIR /app

# Copy package files first so `npm ci` is cached when only source code changes.
COPY package*.json ./

# `npm ci` = deterministic install from package-lock.json — the correct choice
# for Docker/CI (faster and reproducible; fails if lock file is out of sync).
RUN npm ci

# Now bring in the rest of the source and produce the optimized Vite bundle in /app/dist
COPY . .
RUN npm run build


# -------------------------------------------------------
# Stage 2 — Serve stage (nginx serves the built static files)
# -------------------------------------------------------
FROM nginx:alpine

# Copy the built React bundle into nginx's default web-root.
COPY --from=builder /app/dist /usr/share/nginx/html

# Replace nginx's default site config with ours (SPA fallback + /api proxy).
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# Simple container-level healthcheck: is nginx responding on / ?
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -qO- http://localhost/ || exit 1
