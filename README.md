The Vilya library
=================

The Vilya library provides various facilities for making networked multiplayer
games. Its various packages include:

* [whirled] - builds on the crowd framework and defines a scene graph with
  portals to move between scenes and provides hooks for distributing and
  updating scene data (for example isometric rendering information) over the
  network
* [parlor] - builds upon the crowd framework to create the notion of a game with
  players and provides tools for making turn based games
* [puzzle] - builds on the parlor and media frameworks to provide tools for
  implementing puzzle games in a networked environment
* [micasa] - builds on the parlor framework to provide lobbies and matchmaking
  for multiplayer games

[Javadoc documentation](http://threerings.github.com/vilya/apidocs/) is provided.

Tutorial-style documentation is somewhat sparse at the moment, but inspection
of the code in the `src/test/java/` directory shows examples of use of many
features of the library.

Building
--------

The library is built using [Ant](http://ant.apache.org/).

Invoke ant with any of the following targets:

    all: builds the distribution files and javadoc documentation
    compile: builds only the class files (dist/classes)
    javadoc: builds only the javadoc documentation (dist/docs)
    dist: builds the distribution jar files (dist/*.jar)

Artifacts
---------

A Maven repository containing released versions of the Vilya Java and
ActionScript artifacts are maintained here. To add a Vilya dependency to a
Maven project, add the following to your `pom.xml`:

    <repositories>
      <repository>
        <id>vilya-repo</id>
        <url>http://threerings.github.com/vilya/maven</url>
      </repository>
    </repositories>
    <dependencies>
      <dependency>
        <groupId>com.threerings</groupId>
        <artifactId>vilya</artifactId>
        <version>1.3</version>
      </dependency>
    </dependencies>

To add it to an Ivy, SBT, or other Maven repository using project, simply
remove the vast majority of the boilerplate above.

If you prefer to download pre-built binaries, those can be had here:

* [vilya-1.3.jar](http://threerings.github.com/vilya/maven/com/threerings/vilya/1.3/vilya-1.3.jar)
* [vilyalib-1.3.swc](http://threerings.github.com/vilya/maven/com/threerings/vilyalib/1.3/vilyalib-1.3.swc)

Distribution
------------

The Vilya library is released under the LGPL. The most recent version of the
library is available at http://github.com/threerings/vilya.

Contact
-------

Questions, comments, and other worldly endeavors can be handled via the [Three
Rings Libraries](http://groups.google.com/group/ooo-libs) Google Group.

Vilya is actively developed by the scurvy dogs at
[Three Rings](http://www.threerings.net) Contributions are welcome.

[whirled]: http://threerings.github.com/vilya/apidocs/com/threerings/whirled/package-summary.html
[parlor]: http://threerings.github.com/vilya/apidocs/com/threerings/parlor/package-summary.html
[puzzle]: http://threerings.github.com/vilya/apidocs/com/threerings/puzzle/package-summary.html
[micasa]: http://threerings.github.com/vilya/apidocs/com/threerings/micasa/package-summary.html
