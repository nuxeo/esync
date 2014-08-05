# esync Nuxeo Elasticsearch VCS repository checker

This tool enables to detect difference between the Nuxeo database
repository and the indexed content in Elasticsearch.

# Install

## Building from sources

Build:

        mvn package
		
Create a all in one jar

        ./target/nuxeo-esync-1.0-SNAPSHOT-capsule-full.jar
		
# Usage

## Configuration

Create a /etc/esync.conf or ~/.esync.conf file

      nuxeo.db.user=nuxeo
      nuxeo.db.password=nuxeo
      nuxeo.db.driver=org.postgresql.Driver
      nuxeo.db.jdbc.url=jdbc:postgresql://localhost:5432/nuxeoes

      elasticsearch.addressList=localhost:9300
      elasticsearch.indexName=nuxeo
      elasticsearch.clusterName=elasticsearch

      checker.pool.size=10


## Invocation

       java -jar /path/to/nuxeo-esync-1.0-SNAPSHOT-capsule-full.jar
	   # or
       java -jar /path/to/nuxeo-esync-1.0-SNAPSHOT-capsule-full.jar /path/to/config-file.conf

# Checkers

Checkers compare the reference (database) with the Elasticsearch.
They reports differents things:
- Errors like a different number of documents
- Missing documents in Elasticsearch
- Trailing documents in Elasticsearch
- Difference in document properties like ACL, path...

## Cardinality Checker

Checks the total number of documents

False positive cases: 
- none

False negative cases:
- some system documents are not indexed (like Comments)

## Type Cardinality Checker

Checks the number of each type of documents.

False positive cases:
- none

False negative cases:
- some system documents are not indexed (like Comments)

## ACL Checker

Checks ACL in ES for all documents that holds an ACL.
Checks that all children of these documents holds the same ACLs.

False positive cases:
- some ACL can be more permissive on ES we check only that the read acls
  is right

False negative cases:
- none
- 

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
