#!/usr/bin/env bash
# Checks that the Modrinth token used by mc-publish is valid before anything is published, so a
# bad token fails the run up front instead of after some platforms went out. GitHub uploads use
# the built-in GITHUB_TOKEN, which is minted per run and can't expire, so it isn't checked here.
#
# Expects: MODRINTH_TOKEN, MODRINTH_PROJECT_ID.
set -uo pipefail

body=$(mktemp)
trap 'rm -f "$body"' EXIT
ua="${GITHUB_REPOSITORY:-orbis} (GitHub Actions credential check)"

if [ -z "${MODRINTH_TOKEN:-}" ]; then
    echo "::error::MODRINTH_TOKEN secret is empty or not available to this workflow."
    exit 1
fi

code=$(curl -sS -o "$body" -w '%{http_code}' \
    -H "Authorization: $MODRINTH_TOKEN" -H "User-Agent: $ua" \
    "https://api.modrinth.com/v2/user")
if [ "$code" != "200" ]; then
    echo "::error::MODRINTH_TOKEN was rejected by Modrinth (HTTP $code: $(jq -r '.description // empty' "$body" 2>/dev/null)). The token needs the 'Read user data' scope as well as 'Create versions'."
    exit 1
fi

user_id=$(jq -r '.id' "$body")
username=$(jq -r '.username' "$body")

code=$(curl -sS -o "$body" -w '%{http_code}' -H "User-Agent: $ua" \
    "https://api.modrinth.com/v2/project/$MODRINTH_PROJECT_ID/members")
if [ "$code" = "200" ] && ! jq -e --arg id "$user_id" 'any(.[]; .user.id == $id)' "$body" >/dev/null; then
    echo "::error::MODRINTH_TOKEN belongs to '$username', who is not a member of Modrinth project $MODRINTH_PROJECT_ID."
    exit 1
fi

echo "MODRINTH_TOKEN OK ($username)"
