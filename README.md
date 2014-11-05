# esync a tool to compare Nuxeo repository and Elasticsearch content

When using [nuxeo-elasticsearch](http://doc.nuxeo.com/x/iYElAQ) we
want to be sure that the repository content is in sync with the content
indexed in Elasticsearch.

This tool enables to detect difference between the Nuxeo database
repository and the indexed content in Elasticsearch.

# Install

## Building from sources

Create the all in one jar:

        mvn package

The jar is located here:

        ./target/nuxeo-esync-VERSION-capsule-full.jar

# Usage

## Configuration

Create a `/etc/esync.conf` or `~/.esync.conf` using one of the samples provided :
- `esync-postgresql.conf.example`
- `esync-mssql.conf.example`

You will need to configure the database and Elasticsearch access.

Refer to the [source for the full list of options](https://github.com/bdelbosc/esync/blob/master/src/main/java/config/ESyncConfig.java)
available.

## Invocation

     # using a default conf located in /etc/esync.conf or ~/.esync.conf
     java -jar /path/to/nuxeo-esync-$VERSION-capsule-full.jar

	   # using an another config file
     java -jar /path/to/nuxeo-esync-$VERSION-capsule-full.jar /path/to/config-file.conf

     # customizing the log
     java -Dlog4j.configuration=file:mylog4j.xml -jar nuxeo-esync-$VERSION-capsule-full.jar


You can find the default [log4.xml here](https://github.com/bdelbosc/esync/blob/master/src/main/resources/log4j.xml)
default log file is in `/tmp/trace.log`.

# Checkers

The tool runs concurrently different checkers.

Checkers compare the reference database (expected) with the Elasticsearch content (actual).

They report different things:

- Errors like a different number of documents
- Missing documents in Elasticsearch
- Trailing documents in Elasticsearch
- Difference in document properties like ACL, path...


Here is a list of available checkers.

## Cardinality Checker

This is a quick check to count the total number of documents in the db
and Elasticsearch.

False positive cases:
- this does not garantee that we have the same documents, just the same number.

False negative cases:
- some system documents are not indexed (like Comments)

## Type Cardinality Checker

Checks the number of each document types.

False positive cases:
- this does not garantee that we have the same documents, just the same number.

False negative cases:
- some system documents are not indexed (like Comments), in
  this case the total number is 0 on the Elasticsearch part

## Type Document Lister

When there is a difference raise by the Type Cardinality checker the list of ids for this type
is compared, to gives the missing and spurious document ids.

False positive cases: None
False negative cases: None

It can takes time and memory to list all doc ids from the database.

## ACL Checker

It performs 2 checks:
- Checks that all documents that hold an ACL are well indexed in ES
- Checks that all documents in ES have a correct ACL

False positive cases:
- some ACL can be more permissive on ES

False negative cases:
- none

## Orphan Checker

Cardinality checkers don't take in account document with no parentid.

TODO: impl and document


# About Nuxeo

Nuxeo provides a modular, extensible Java-based
[open source software platform for enterprise content management](http://www.nuxeo.com/en/products/ep)
and packaged applications for
[document management](http://www.nuxeo.com/en/products/document-management),
[digital asset management](http://www.nuxeo.com/en/products/dam) and
[case management](http://www.nuxeo.com/en/products/case-management). Designed
by developers for developers, the Nuxeo platform offers a modern
architecture, a powerful plug-in model and extensive packaging
capabilities for building content applications.

More information on: <http://www.nuxeo.com/>
