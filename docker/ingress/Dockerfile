FROM nginx:alpine

# Copy the template and entrypoint script
COPY nginx-template.conf /etc/nginx/nginx-template.conf
COPY entrypoint.sh /entrypoint.sh

# Make entrypoint script executable
RUN chmod +x /entrypoint.sh

# Set entrypoint
ENTRYPOINT ["./entrypoint.sh"]
