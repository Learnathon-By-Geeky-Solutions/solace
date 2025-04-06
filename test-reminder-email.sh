#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
EMAIL="ssrabonislam2000@gmail.com"  # Replace with your email for quick testing
PORT=8080                        # Default Spring Boot port
HOST="localhost"                 # Default host

# Parse command line arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
        -e|--email) EMAIL="$2"; shift ;;
        -p|--port) PORT="$2"; shift ;;
        -h|--host) HOST="$2"; shift ;;
        *) echo "Unknown parameter: $1"; exit 1 ;;
    esac
    shift
done

# Check if curl is installed
if ! command -v curl &> /dev/null; then
    echo -e "${RED}Error: curl is not installed. Please install curl to use this script.${NC}"
    exit 1
fi

echo -e "${BLUE}Testing Reminder Email Service...${NC}"

# 1. Test the GET endpoint for quick testing (simple version)
echo -e "\n${BLUE}1. Testing simple GET endpoint for quick email test...${NC}"
echo -e "   Sending test email to: ${GREEN}$EMAIL${NC}"

curl -s -X GET "http://$HOST:$PORT/api/reminders/test/$EMAIL" | jq .

# 2. Test the POST endpoint with a complete request
echo -e "\n${BLUE}2. Testing POST endpoint with a complete request...${NC}"

curl -s -X POST "http://$HOST:$PORT/api/reminders/send" \
  -H "Content-Type: application/json" \
  -d '{
    "plantName": "Script Test Plant",
    "reminderType": "Water",
    "reminderDate": "'$(date -I)'",
    "reminderTime": "10:00 AM",
    "notes": "This is a test from the bash script",
    "userEmail": "'$EMAIL'",
    "imageUrl": "https://images.unsplash.com/photo-1463936575829-25148e1db1b8?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2090&q=80",
    "gardenSpaceName": "Script Test Garden",
    "gardenSpaceId": "script-test-id"
  }' | jq .

echo -e "\n${GREEN}Testing complete!${NC}"
echo -e "${BLUE}Check your email inbox: ${GREEN}$EMAIL${NC}\n"

# Make the script executable with:
# chmod +x test-reminder-email.sh
#
# Run with:
# ./test-reminder-email.sh --email your@email.com 