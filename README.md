# Projections Java

#### Projections Lib ####

The Projections Library was developed at the [National Geospatial-Intelligence Agency (NGA)](http://www.nga.mil/) in collaboration with [BIT Systems](https://www.caci.com/bit-systems/). The government has "unlimited rights" and is releasing this software to increase the impact of government investments by providing developers with the opportunity to take things in new directions. The software use, modification, and distribution rights are stipulated within the [MIT license](http://choosealicense.com/licenses/mit/).

### Pull Requests ###
If you'd like to contribute to this project, please make a pull request. We'll review the pull request and discuss the changes. All pull request contributions to this project will be released under the MIT license.

Software source code previously released under an open source license and then modified by NGA staff is considered a "joint work" (see 17 USC § 101); it is partially copyrighted, partially public domain, and as a whole is protected by the copyrights of the non-government authors and must be released according to the terms of the original open source license.

### About ###

[Projections](http://ngageoint.github.io/projections-java/) is a Java library for performing projection conversions between coordinates.

For conversions between geometries, see [Simple Features Projections](https://ngageoint.github.io/simple-features-proj-java/).

### Usage ###

View the latest [Javadoc](http://ngageoint.github.io/projections-java/docs/api/)

```java

// ProjCoordinate coordinate = ...

Projection projection1 = ProjectionFactory.getProjection(
    ProjectionConstants.AUTHORITY_EPSG,
    ProjectionConstants.EPSG_WEB_MERCATOR);
Projection projection2 = ProjectionFactory.getProjection("EPSG:4326");
Projection projection3 = ProjectionFactory.getProjection(
    ProjectionConstants.AUTHORITY_EPSG, 3123,
    "+proj=tmerc +lat_0=0 +lon_0=121 +k=0.99995 +x_0=500000 +y_0=0 +ellps=clrk66 "
        + "+towgs84=-127.62,-67.24,-47.04,-3.068,4.903,1.578,-1.06 +units=m +no_defs");
Projection projection4 = ProjectionFactory.getProjectionByDefinition(
    "PROJCS[\"Lambert_Conformal_Conic (1SP)\","
        + "GEODCRS[\"GCS_North_American_1983\","
        + "DATUM[\"North_American_Datum_1983\","
        + "SPHEROID[\"GRS_1980\",6371000,0]],"
        + "PRIMEM[\"Greenwich\",0],"
        + "UNIT[\"Degree\",0.017453292519943295]],"
        + "PROJECTION[\"Lambert_Conformal_Conic_1SP\"],"
        + "PARAMETER[\"latitude_of_origin\",25],"
        + "PARAMETER[\"central_meridian\",-95],"
        + "PARAMETER[\"scale_factor\",1],"
        + "PARAMETER[\"false_easting\",0],"
        + "PARAMETER[\"false_northing\",0],"
        + "PARAMETER[\"standard_parallel_1\",25],"
        + "UNIT[\"Meter\",1],AUTHORITY[\"EPSG\",\"9801\"]]");

ProjectionTransform transform = projection1
    .getTransformation(projection2);
ProjectionTransform inverseTransform = transform
    .getInverseTransformation();

ProjCoordinate transformed = transform.transform(coordinate);
ProjCoordinate inverseTransformed = inverseTransform
    .transform(transformed);

```

### Installation ###

Pull from the [Maven Central Repository](http://search.maven.org/#artifactdetails|mil.nga|proj|1.1.2|jar) (JAR, POM, Source, Javadoc)

```xml

<dependency>
    <groupId>mil.nga</groupId>
    <artifactId>proj</artifactId>
    <version>1.1.2</version>
</dependency>

```

### Build ###

[![Build & Test](https://github.com/ngageoint/projections-java/workflows/Build%20&%20Test/badge.svg)](https://github.com/ngageoint/projections-java/actions/workflows/build-test.yml)

Build this repository using Eclipse and/or Maven:

    mvn clean install

### Standalone Projections ###

The jar can be built as standalone (or combined with required dependency jars) to run utilities from the command line.

To build the jar into a standalone jar that includes all dependencies:

    mvn clean install -Pstandalone

Performs coordinate transformations from a source projection to a target projection. Download [project.zip](https://github.com/ngageoint/projections-java/releases/latest/download/project.zip) and follow the [instructions](script/project/).

Or run against the jar:

    java -jar proj-*standalone.jar [from_projection to_projection [coordinates]]

### Remote Dependencies ###

* [Proj4J](https://github.com/locationtech/proj4j) (Apache License, Version 2.0) - Projection Library
* [Coordinate Reference Systems](https://github.com/ngageoint/coordinate-reference-systems-java) (The MIT License (MIT)) - Coordinate Reference Systems Library
