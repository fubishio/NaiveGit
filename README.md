# NaiveGit

This is a basic version control system that is implemented in Java which specifically stores changes as copies of files instead of the changes as hashes. I made this because the first time I was learning git, I was not quick on understanding how git version control. The terms HEAD and using SHA-1 to track changes were confusing and it cleared up when a friend worked out the process with files and a directory on their PC. I made this project because it would be nice to have a program where you can watch "git" save changes and modify files with a debugger without having to read through the hashes themselves. This is NOT meant to be a fully functional nor efficient version control system. This is a learning tool for people who would not be able to comprehend the source code at first and would like a more hands-on approach. It supports most file tracking features but not remote control and repositories.

## Getting Started

Requires Java and if JUnit is how you roll (which you should), that too. Copy and paste into directory then plug and play!     
Compile with `javac *.java` and run with `java NaiveGit command [input]`

### Features
These are the commands that are in NaiveGit:  
* init - initializes in directory  
* add [file] - add file to be tracked  
* commit [commit message] - pretty much same as git commit -m  
* rm [file] - removes file to be tracked  
* log - displays commit from pointer to head  
* global-log - displays all commits  
* find [commit message] - finds a specific commit (does not exist in actual git) 
* status - shows which files are tracked  
* checkout [file] or [commit id] [file] or [branch name] - checks out a commit like it does in git  
* branch [branch name] - makes a new branch or pointer at a specific place in the commit tree  
* rm-branch [branch name] - deletes the branch/pointer  
* reset [commit name] - move to the commit and moves pointer to it too  
* *merge [branch name] - merges files from the head of branch into the head of current branch  
* **rebase [branch name] - reattaches head of current branch to head of given branch  
* **i-rebase [branch name] - better description [here](https://git-scm.com/docs/git-rebase)  

*merge conflict handled differently  
**simplified from the actual git but still good for learning the gist of it  

*no command supports flags in the way actual git does*   

## Testing
To test my implementation, I wrote down on paper theoretically where my files should go after each command from my understanding and then doublechecked the behavior with the actual git on sample files. Feel free to add your own tests as the howto on inputting commands in the JUnit is very clear with examples on how to input commands through Java. I have provided a NaiveGit, getText, createFile, writeFile, recursiveDelete, and extractCommitMessage that should make it easy to write tests and try it yourself.


### Running the Tests

To run the tests, you should just run unit test as you would for other projects either ide or in commandline. For commandline, if junit is setup properly use:

`java org.junit.runner.JUnitCore NaiveGit.NaiveGitTest`

## Acknowledgements

Thanks to floormate Robert CommitNode.java and testing, I was a little big hazy on how to store the nodes themselves and how to make the testing helper functions.
