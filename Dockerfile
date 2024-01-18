# Use an official timescaledb image as a base image
FROM timescale/timescaledb-ha:pg14-latest

# Set environment variables
ENV POSTGRES_PASSWORD=abc123
ENV POSTGRES_DB=anomalydb

VOLUME /home/postgres/pgdata

# Expose the PostgreSQL port
EXPOSE 5432
