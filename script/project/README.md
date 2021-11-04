# Project

[project.zip](https://github.com/ngageoint/projections-java/releases/latest/download/project.zip)

Performs coordinate transformations from a source projection to a target projection.  Projection coordinate reference systems may be defined by:
 * EPSG Code
 * Authority and Code
 * PROJ / PROJ4 Parameters
 * OGC Coordinate Reference System Well-Known Text (1|2)

## Run

### Script

    ./project.sh [from_projection to_projection [coordinates]]

### Jar

    java -jar project.jar [from_projection to_projection [coordinates]]

### Alias

Add an alias to your shell to run from any location

    alias proj="~/project/project.sh"

And run

    proj [from_projection to_projection [coordinates]]

## Examples

Run using the script, Jar, or alias.

    proj 4326 3857 [-110.0, 75.0], [95.0, -20.0, 0.0]

    proj

    proj 4326 3857

## Help

```
USAGE

	[from_projection to_projection [coordinates]]

DESCRIPTION

	Transform coordinates between projections

	from_projection
		Projection to transform from (EPSG_CODE, AUTHORITY:CODE, PROJ_PARAMS, or OGC_WKT)
			Examples: 4326, EPSG:4326, "+proj=longlat ...", "GEOGCRS[..."

	to_projection
		Projection to transform to (EPSG_CODE, AUTHORITY:CODE, PROJ_PARAMS, or OGC_WKT)
			Examples: 3857, EPSG:3857, "+proj=merc ...", "PROJCRS[..."

	coordinates
		Coordinate(s) to transform between projections ([x, y], [x, y, z])
			Examples: [-110.0, 75.0], [95.0, -20.0, 0.0]
```

### Interactive Session

```
From Projection: <from_projection>

To Projection: <to_projection>

<from_projection> -> <to_projection>

Enter 'Inverse' to invert transformation

Coordinate(s): [x, y]

[x, y] -> [x2, y2]

Coordinate(s): Inverse

<to_projection> -> <from_projection>

Coordinate(s): [x2, y2]

[x2, y2] -> [x, y]
```
