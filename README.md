Installation
============
It's quite easy: go to JetBrains' Plugin repository and look for jQAssistant Plugin.
Install it like any other plugin. You will find a new toolwindow called "Find using jQA DB".

Usage
=====
This plugin doesn't create the jQAssistant database. So you should get a copy
from [jqassistant.org](http://jqassistant.org), configure and run it following the jQAssistant documentation.

When you have created your database, you can open the jQA toolwindow. The database
location has the default value where most probably the database files should reside.
Take the example query or enter your own and hit the 'Find' button gently. The result
is shown in the standard Find toolwindow and you can use it to navigate as you know it.

What can you find?
==================
You can find classes, methods and files where

* nodes have a 'fqn' property that contains the fully qualified name of a class in project scope,
 e.g. nodes with Class, CheckstyleFile, BugInstanceClass (of FindBugs plugin), JacocoClass lables

* nodes have a 'relativePath' property that contains the path of a file relative to the project root,
 e.g. with GitFile, GitCommitFile labels
