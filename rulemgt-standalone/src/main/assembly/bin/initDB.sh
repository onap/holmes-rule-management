#!/bin/bash
#
# Copyright 2017 ZTE Corporation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

DIRNAME=`dirname $0`
HOME=`cd $DIRNAME/; pwd`
user=$1
password=$2
port=$3
host=$4
echo "start init holmes rulemgt db"
main_path=$HOME/..
cat $main_path/dbscripts/postgresql/onap-holmes_rulemgt-createobj.sql
echo "user="$user
echo "password="$password
echo "port="$port
echo "host="$host
export PGPASSWORD=$password
psql -U $user -p $port -h $host -f $main_path/dbscripts/postgresql/onap-holmes_rulemgt-createobj.sql
psql -U $user -p $port -h $host -d holmes --command 'select * from aplus_rule;'
sql_result=$?
unset PGPASSWORD
cat "sql_result="$sql_result
if [ $sql_result != 0 ] ; then
   echo "failed to init rulemgt database!"
   exit 1
fi
echo "init rulemgt success!"
exit 0

