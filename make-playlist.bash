#!/usr/bin/env bash

while getopts "ou:d:s:f:" opt; do
  case $opt in
    s)
      server="$OPTARG"
      ;;
    f)
      file="$OPTARG"
      ;;
    o)
      output=true
      ;;
    u)
      action="update"
      id=$OPTARG
      ;;
    d)
      action="delete"
      id=$OPTARG
      ;;
    \?)
      echo "Invalid option: -$OPTARG"
      exit 1
      ;;
  esac
done

server="${server:-localhost:12345}"
action="${action:-create}"

case $action in
  "update"|"create")
    if [ -z "${file}" ]; then
        echo "must specify playlist file"
        exit 1
    fi
    ;;
esac

case $action in
  "update")
    curl -f -s -X PUT "http://$server/playlist/$id" --data-binary @"$file"
    ;;
  "delete")
    curl -f -s -X DELETE "http://$server/playlist/$id"
    ;;
  *)
    id=$(curl -f -s -X POST "http://$server/playlist" --data-binary @"$file")
esac

if [ $? -eq 0 ]; then
  if [ "true" == "$output" ]; then
    curl -s "http://$server/playlist/$id"
  else
    echo "playlist ${action}d http://$server/playlist/$id"
  fi
else
  echo "failed to ${action}"
fi
