# Simple search engine

Overview
--------
This repo contains simple implementation of search engine and includes
* Indexer
* Searcher

Usage
--------
To run indexer you just have to execute one-line command

```
./gradlew runIndexer -PappArgs="['/Users/gtkachenko/Programming/Common/SearchEngineCourseGradle/data']"
```

The same thing for searcher

```
./gradlew runSearcher -PappArgs="['NOT relevant AND Caesar OR immediately']"
```
