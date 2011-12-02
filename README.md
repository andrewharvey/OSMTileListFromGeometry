Ensure your system has the dependencies listed in `debian/control`, then compile
by running `ant`.

Then run using,
    java -jar ./dist/osmTileListFromGeometry.jar -o 01-tileList.txt
    java -jar ./dist/metaTile.jar -i 01-tileList.txt -o 02-metaTileList.txt

You need an `osm2pgsql` database running for the first command. You can specify
the connection details as program arguments. Run with `--help` for details.

This tile list can then be parsed to https://gist.github.com/1397377 to render the tiles to disk.
