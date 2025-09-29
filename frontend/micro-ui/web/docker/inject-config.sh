#!/bin/sh

echo "Injecting runtime configuration..."

# Set default GA_MEASUREMENT_ID if not provided
GA_ID=${GA_MEASUREMENT_ID:-"G-SSSSSSSSSS"}

echo "Using GA Measurement ID: $GA_ID"

# Replace placeholder in all JavaScript files
find /var/web/digit-ui -name "*.js" -type f -exec sed -i "s/GA_MEASUREMENT_ID_PLACEHOLDER/$GA_ID/g" {} \;

# Also replace in any HTML files that might contain the placeholder
find /var/web/digit-ui -name "*.html" -type f -exec sed -i "s/GA_MEASUREMENT_ID_PLACEHOLDER/$GA_ID/g" {} \;

echo "Runtime configuration injection completed"