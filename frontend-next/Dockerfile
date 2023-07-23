# Dockerfile
# Step 1: Build the app in a node.js environment
FROM node:14-alpine as builder

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Step 2: Serve the app from Nginx
FROM nginx:alpine

# Copy built app from the 'builder' stage into the default nginx public folder
COPY --from=builder /app/.next /usr/share/nginx/html

# Expose port 80 for the app
EXPOSE 80

# Start Nginx server
CMD ["nginx", "-g", "daemon off;"]
