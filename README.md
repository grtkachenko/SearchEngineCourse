# Simple search engine

Overview
--------
This repo contains simple implementation of search engine and includes
* Indexer
* Searcher

Package dependencies
--------------------
You only need to have [gradle](https://www.gradle.org/) locally. 

OS X:
```
brew install gradle
```


Ubuntu:
```
sudo add-apt-repository ppa:cwchien/gradle
sudo apt-get update
sudo apt-get install gradle
```

Usage
--------
Once you fetched the repo you should execute the command below. It should be performed only at the first build. 

```
gradle wrapper
```


To run indexer you just have to execute one-line command

```
./gradlew runIndexer -PappArgs="['/path/to/data']"
```

The same thing for searcher. You can use standart boolean operators (not = '~', and = '&&', or = '||') and parenthesis 

```
./gradlew runSearcher -PappArgs="['(~~true) && false && ~ (value || geographical)']"
```

Another option is to build an executable and run it afterwards (the same for Searcher and Indexer) 
```
./gradlew buildSearcher 
./build/install/SearchEngineCourse/bin/SearchEngineCourse '(~~true) && false && ~ (value || geographical)'
```
