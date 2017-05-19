#!/usr/bin/env bash
username=$1
password=$2
if [ -z $password ]
then
    echo Password for user $username :
    #password=$2
    read -s password
fi
echo $username:vertx:$(echo -n "$username:vertx:$password" | md5sum | cut -d ' ' -f 1) >> .htdigest
