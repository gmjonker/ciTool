
# Watson Concept Insights tool

[![Release](https://jitpack.io/v/gmjonker/WatsonCiTool.svg)](https://jitpack.io/#gmjonker/WatsonCiTool)


A layer on top of the Java SDK that provides some extra functionality, currently being:

 * Uploading a list of documents to CI, with options of skipping existing documents and deleting other documents
 * Checking status until all is finished

## Usage

To use this tool in your project, add this to your `pom.xml`:

    <repositories>
        <!-- Jitpack allows using Github/BitBucket/GitLab releases as dependencies -->
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    
    <dependency>
        <groupId>com.github.gmjonker</groupId>
        <artifactId>WatsonCiTool</artifactId>
        <version>0.1</version>
    </dependency>

See also https://jitpack.io/#gmjonker/WatsonCiTool/0.1
            
## Development
            
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

