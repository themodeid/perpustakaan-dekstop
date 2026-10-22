FROM mysql:8.0

# Copy init script into MySQL initialization directory
COPY database/init.sql /docker-entrypoint-initdb.d/init.sql
