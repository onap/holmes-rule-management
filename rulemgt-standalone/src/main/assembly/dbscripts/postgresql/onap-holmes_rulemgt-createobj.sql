--
-- Copyright 2017 ZTE Corporation.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
\c postgres

/******************CREATE NEW DATABASE AND USER***************************/
CREATE DATABASE DBNAME;

CREATE ROLE DBUSER with PASSWORD 'DBPWD' LOGIN;

\encoding UTF8;

/******************DELETE OLD TABLE AND CREATE NEW***************************/
\c DBNAME;

CREATE TABLE IF NOT EXISTS APLUS_RULE (
  RID VARCHAR(30) NOT NULL,
  NAME VARCHAR(150) NOT NULL,
  CTRLLOOP VARCHAR(150) NOT NULL,
  DESCRIPTION VARCHAR(4000) NULL,
  ENABLE SMALLINT NOT NULL,
  TEMPLATEID BIGINT NOT NULL,
  ENGINEID VARCHAR(20)  NOT NULL,
  ENGINETYPE VARCHAR(20)  NOT NULL,
  CREATOR VARCHAR(20)  NOT NULL,
  CREATETIME TIMESTAMP NOT NULL,
  UPDATOR VARCHAR(20)  NULL,
  UPDATETIME TIMESTAMP NULL,
  PARAMS VARCHAR(4000) NULL,
  CONTENT VARCHAR(20000) NOT NULL,
  VENDOR VARCHAR(100)  NOT NULL,
  ENGINEINSTANCE VARCHAR(100) NOT NULL,
  PACKAGE VARCHAR(255) NULL,
  PRIMARY KEY (RID),
  UNIQUE (NAME)
);

CREATE INDEX IDX_APLUS_RULE_NAME ON APLUS_RULE (NAME);
CREATE INDEX IDX_APLUS_RULE_CTRLLOOP ON APLUS_RULE (CTRLLOOP);
CREATE INDEX IDX_APLUS_RULE_ENABLE ON APLUS_RULE (ENABLE);
CREATE INDEX IDX_APLUS_RULE_TEMPLATEID ON APLUS_RULE (TEMPLATEID);
CREATE INDEX IDX_APLUS_RULE_ENGINEID ON APLUS_RULE (ENGINEID);
CREATE INDEX IDX_APLUS_RULE_ENGINETYPE ON APLUS_RULE (ENGINETYPE);

GRANT ALL PRIVILEGES ON APLUS_RULE TO DBUSER;
