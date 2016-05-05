
# Watson Concept Insights tool

A layer on top of the Java SDK that provides some extra functionality, currently being:

 * Uploading a list of documents to CI, with options of skipping existing documents and deleting other documents
 * Checking status until all is finished

### Prequisites 

 * Java 8
 * Maven 3
 
### Installing locally
 
    $ mvn install
        
### Git hooks

Install the following pre-commit git hook to do a Maven build before committing. It checks that the code builds correctly
and that the style is ok:

```sh
cd .git/hooks
ln -s ../../utils/git-hooks/pre-commit.hook pre-commit
cd -
```

