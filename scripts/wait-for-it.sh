#!/usr/bin/env bash
# Use this script to wait for another service to become available

TIMEOUT=15
QUIET=0
HOST=""
PORT=""

print_usage() {
  echo "Usage: $0 host:port [-t timeout] [-- command args]"
  echo "  -h HOST       Host or IP under test"
  echo "  -p PORT       TCP port under test"
  echo "  -t TIMEOUT    Timeout in seconds, zero for no timeout"
  echo "  -q            Do not output any status messages"
  echo "  -- COMMAND ARGS  Execute command with args after the test finishes"
  exit 1
}

wait_for() {
  if [[ "$TIMEOUT" -gt 0 ]]; then
    echo "Waiting for $HOST:$PORT for $TIMEOUT seconds..."
  else
    echo "Waiting for $HOST:$PORT without a timeout..."
  fi

  while ! nc -z "$HOST" "$PORT"; do
    TIMEOUT=$((TIMEOUT - 1))
    if [[ "$TIMEOUT" -eq 0 ]]; then
      echo "Timeout occurred while waiting for $HOST:$PORT"
      exit 1
    fi
    sleep 1
  done

  echo "$HOST:$PORT is available!"
}

# Process command-line arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    *:* )
    HOST=$(echo $1 | cut -d : -f 1)
    PORT=$(echo $1 | cut -d : -f 2)
    shift 1
    ;;
    -q)
    QUIET=1
    shift 1
    ;;
    -t)
    TIMEOUT="$2"
    shift 2
    ;;
    --)
    shift
    COMMAND="$@"
    break
    ;;
    *)
    print_usage
    ;;
  esac
done

if [[ "$HOST" == "" || "$PORT" == "" ]]; then
  echo "Error: You need to provide a host and port to wait for."
  print_usage
fi

wait_for

# Execute the provided command, if any
if [[ "$COMMAND" != "" ]]; then
  echo $COMMAND
  exec $COMMAND
else
  exit 0
fi
