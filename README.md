The Vilya library
=================

The Vilya library provides various facilities for making networked multiplayer
games. Its various packages include:

* whirled - builds on the crowd framework and defines a scene graph with
  portals to move between scenes and provides hooks for distributing and
  updating scene data (for example isometric rendering information) over the
  network
* parlor - builds upon the crowd framework to create the notion of a game with
  players and provides tools for making turn based games
* puzzle - builds on the parlor and media frameworks to provide tools for
  implementing puzzle games in a networked environment
* micasa - builds on the parlor framework to provide lobbies and matchmaking
  for multiplayer games

Documentation is somewhat sparse at the moment, but inspection of the code in
the `src/main/tests/` directory shows examples of use of many features of the
library.

Building
--------

The library is built using [Ant](http://ant.apache.org/).

Invoke ant with any of the following targets:

    all: builds the distribution files and javadoc documentation
    compile: builds only the class files (dist/classes)
    javadoc: builds only the javadoc documentation (dist/docs)
    dist: builds the distribution jar files (dist/*.jar)

Distribution
------------

The Vilya library is released under the LGPL. The most recent version of the
library is available at http://github.com/threerings/vilya.

Contact
-------

Questions, comments, contributions, and other worldly endeavors can be handled
in the [Google Group for Three Rings
libraries](http://groups.google.com/group/ooo-libs).

Vilya is actively developed by the scurvy dogs at [Three Rings Design,
Inc.](http://www.threerings.net) Contributions are welcome.
