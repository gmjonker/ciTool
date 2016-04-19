# Watson Concept Insights tool

A layer on top of the Java SDK that provides some extra functionality.




### Git

Install the following pre-commit git hook to do a Maven build before committing. It checks that the code builds alright
and that the style is ok:

```sh
cd .git/hooks
ln -s ../../utils/git-hooks/pre-commit.hook pre-commit
cd -
```

