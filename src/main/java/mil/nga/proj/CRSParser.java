package mil.nga.proj;

import java.io.IOException;
import java.util.List;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.datum.Datum;
import org.locationtech.proj4j.datum.Ellipsoid;
import org.locationtech.proj4j.datum.Grid;
import org.locationtech.proj4j.parser.DatumParameters;
import org.locationtech.proj4j.proj.Projection;
import org.locationtech.proj4j.units.Units;
import org.locationtech.proj4j.util.ProjectionMath;

import mil.nga.crs.CRS;
import mil.nga.crs.CRSException;
import mil.nga.crs.common.Unit;
import mil.nga.crs.geo.GeoCoordinateReferenceSystem;
import mil.nga.crs.geo.GeoDatum;
import mil.nga.crs.geo.PrimeMeridian;
import mil.nga.crs.geo.TriaxialEllipsoid;
import mil.nga.crs.projected.MapProjection;
import mil.nga.crs.projected.MapProjectionMethod;
import mil.nga.crs.projected.MapProjectionMethods;
import mil.nga.crs.projected.MapProjectionParameter;
import mil.nga.crs.projected.ProjectedCoordinateReferenceSystem;
import mil.nga.crs.wkt.CRSReader;

/**
 * Coordinate Reference System Well-known text parser
 * 
 * @author osbornb
 */
public class CRSParser {

	/**
	 * CRS Factory
	 */
	private static final CRSFactory crsFactory = new CRSFactory();

	/**
	 * Get the CRS Factory
	 * 
	 * @return crs factory
	 */
	public static CRSFactory getCRSFactory() {
		return crsFactory;
	}

	/**
	 * Parse crs well-known text into a proj4 coordinate reference system
	 * 
	 * @param wkt
	 *            crs well-known text
	 * @return coordinate reference system
	 */
	public static CoordinateReferenceSystem parse(String wkt) {

		CRS crsObject = null;
		try {
			crsObject = CRSReader.read(wkt);
		} catch (IOException e) {
			throw new ProjectionException("Failed to parse WKT: " + wkt, e);
		}

		CoordinateReferenceSystem crs = null;
		if (crsObject != null) {
			crs = convert(crsObject);
		}

		return crs;
	}

	/**
	 * Convert a CRS object into a proj4 coordinate reference system
	 * 
	 * @param crsObject
	 *            CRS object
	 * @return coordinate reference system
	 */
	public static CoordinateReferenceSystem convert(CRS crsObject) {

		CoordinateReferenceSystem crs = null;

		switch (crsObject.getType()) {

		case GEODETIC:
		case GEOGRAPHIC:
			crs = convert((GeoCoordinateReferenceSystem) crsObject);
			break;

		case PROJECTED:
			crs = convert((ProjectedCoordinateReferenceSystem) crsObject);
			break;

		default:

		}

		return crs;
	}

	/**
	 * Convert a geodetic or geographic crs into a proj4 coordinate reference
	 * system
	 * 
	 * @param geo
	 *            geodetic or geographic crs
	 * @return coordinate reference system
	 */
	public static CoordinateReferenceSystem convert(
			GeoCoordinateReferenceSystem geo) {

		GeoDatum geoDatum = geo.getGeoDatum();
		Datum datum = convert(geoDatum);

		Projection projection = createProjection(
				geo.getCoordinateSystem().getAxisUnit());
		updateProjection(projection, datum.getEllipsoid(), geoDatum);
		projection.initialize();

		return new CoordinateReferenceSystem(geo.getName(), null, datum,
				projection);
	}

	/**
	 * Convert a projected crs into a proj4 coordinate reference system
	 * 
	 * @param projected
	 *            projected crs
	 * @return coordinate reference system
	 */
	public static CoordinateReferenceSystem convert(
			ProjectedCoordinateReferenceSystem projected) {

		MapProjection mapProjection = projected.getMapProjection();

		GeoDatum geoDatum = projected.getGeoDatum();

		Ellipsoid ellipsoid = convert(geoDatum.getEllipsoid());
		DatumParameters datumParameters = new DatumParameters();

		MapProjectionMethod method = mapProjection.getMethod();
		if (method != null && method
				.getMethod() == MapProjectionMethods.POPULAR_VISUALISATION_PSEUDO_MERCATOR) {
			datumParameters.setA(ellipsoid.getA());
			datumParameters.setES(0);
		} else {
			datumParameters.setEllipsoid(ellipsoid);
		}

		Datum datum = datumParameters.getDatum();

		Projection projection = createProjection(
				projected.getCoordinateSystem().getAxisUnit(), method);
		updateProjection(projection, datum.getEllipsoid(), geoDatum);
		updateProjection(projection, method);
		projection.initialize();

		return new CoordinateReferenceSystem(projected.getName(), null, datum,
				projection);
	}

	/**
	 * Convert a Datum
	 * 
	 * @param geoDatum
	 *            crs wkt geo datum
	 * @return proj4j datum
	 */
	public static Datum convert(GeoDatum geoDatum) {

		String name = geoDatum.getName();
		double[] transform = null; // TODO?
		List<Grid> grids = null; // TODO?
		Ellipsoid ellipsoid = convert(geoDatum.getEllipsoid());

		String code = name;
		if (geoDatum.hasIdentifiers()) {
			code = geoDatum.getIdentifier(0).getNameAndUniqueIdentifier();
		}

		return new Datum(code, transform, grids, ellipsoid, name);
	}

	/**
	 * Convert an Ellipsoid
	 * 
	 * @param ellipsoid
	 *            crs wkt ellipsoid
	 * @return proj4j ellipsoid
	 */
	public static Ellipsoid convert(mil.nga.crs.geo.Ellipsoid ellipsoid) {

		String name = ellipsoid.getName();
		String shortName = name;
		if (ellipsoid.hasIdentifiers()) {
			shortName = ellipsoid.getIdentifier(0).getNameAndUniqueIdentifier();
		}
		double equatorRadius = ellipsoid.getSemiMajorAxis();
		double poleRadius = 0;
		double reciprocalFlattening = 0;

		switch (ellipsoid.getType()) {
		case OBLATE:
			reciprocalFlattening = ellipsoid.getInverseFlattening();
			if (reciprocalFlattening == 0) {
				reciprocalFlattening = Double.POSITIVE_INFINITY;
			}
			break;
		case TRIAXIAL:
			TriaxialEllipsoid triaxial = (TriaxialEllipsoid) ellipsoid;
			poleRadius = triaxial.getSemiMinorAxis();
			break;
		default:
			throw new CRSException(
					"Unsupported Ellipsoid Type: " + ellipsoid.getType());
		}

		return new Ellipsoid(shortName, equatorRadius, poleRadius,
				reciprocalFlattening, name);
	}

	/**
	 * Create a proj4j projection
	 * 
	 * @param projection
	 *            projection
	 * @param ellipsoid
	 *            ellipsoid
	 * @param geoDatum
	 *            geo datum
	 */
	public static void updateProjection(Projection projection,
			Ellipsoid ellipsoid, GeoDatum geoDatum) {

		projection.setEllipsoid(ellipsoid);

		if (geoDatum.hasPrimeMeridian()) {
			PrimeMeridian primeMeridian = geoDatum.getPrimeMeridian();
			double primeMeridianLongitude = primeMeridian.getLongitude();
			if (primeMeridian.hasLongitudeUnit()
					&& Units.findUnits(primeMeridian.getLongitudeUnit()
							.getName()) == Units.RADIANS) {
				primeMeridianLongitude *= ProjectionMath.RTD;
			}
			projection
					.setPrimeMeridian(Double.toString(primeMeridianLongitude));
		}

	}

	/**
	 * Create a proj4j projection for the unit
	 * 
	 * @param unit
	 *            unit
	 * @return projection
	 */
	public static Projection createProjection(Unit unit) {

		org.locationtech.proj4j.units.Unit projUnit = Units.METRES;
		if (unit != null) {
			projUnit = Units.findUnits(unit.getName());
		}

		String projectionName = null;
		if (projUnit == Units.DEGREES) {
			projectionName = "longlat";
		} else {
			projectionName = "merc";
		}

		return createProjection(projectionName, unit);
	}

	/**
	 * Create a proj4j projection for the method and unit
	 * 
	 * @param unit
	 *            unit
	 * @param method
	 *            map projection method
	 * @return projection
	 */
	public static Projection createProjection(Unit unit,
			MapProjectionMethod method) {

		Projection projection = null;

		if (method.hasMethod()) {

			String projectionName = null;

			switch (method.getMethod()) {

			case LAMBERT_AZIMUTHAL_EQUAL_AREA:
				projectionName = "laea";
				break;

			case POPULAR_VISUALISATION_PSEUDO_MERCATOR:
				projectionName = "merc";
				break;

			// TODO

			default:

			}

			if (projectionName != null) {
				projection = createProjection(projectionName, unit);
			}
		}

		if (projection == null) {
			projection = createProjection(null);
		}

		return projection;
	}

	/**
	 * Create a proj4j projection for the projection name and unit
	 * 
	 * @param projectionName
	 *            projection name
	 * @param unit
	 *            unit
	 * @return projection
	 */
	public static Projection createProjection(String projectionName,
			Unit unit) {

		Projection projection = getCRSFactory().getRegistry()
				.getProjection(projectionName);

		org.locationtech.proj4j.units.Unit projUnit = Units.METRES;
		if (unit != null) {
			projUnit = Units.findUnits(unit.getName());
		}
		projection.setUnits(projUnit);

		if (unit != null) {
			// TODO
			// proj.setFromMetres(unit.getConversionFactor());
		}

		return projection;
	}

	/**
	 * Update the method parameters in the projection
	 * 
	 * @param projection
	 *            proj4j projection
	 * @param method
	 *            map projection method
	 */
	public static void updateProjection(Projection projection,
			MapProjectionMethod method) {
		if (method.hasParameters()) {
			for (MapProjectionParameter parameter : method
					.getMapProjectionParameters()) {
				updateProjection(projection, parameter);
			}
		}
	}

	/**
	 * Update the method parameter in the projection
	 * 
	 * @param projection
	 *            proj4j projection
	 * @param parameter
	 *            map projection parameter
	 */
	public static void updateProjection(Projection projection,
			MapProjectionParameter parameter) {

		if (parameter.hasParameter()) {

			double value = parameter.getValue();

			switch (parameter.getParameter()) {

			case FALSE_EASTING:
			case EASTING_AT_PROJECTION_CENTRE:
			case EASTING_AT_FALSE_ORIGIN:
				projection.setFalseEasting(value);
				break;

			case FALSE_NORTHING:
			case NORTHING_AT_PROJECTION_CENTRE:
			case NORTHING_AT_FALSE_ORIGIN:
				projection.setFalseNorthing(value);
				break;

			case SCALE_FACTOR_AT_NATURAL_ORIGIN:
			case SCALE_FACTOR_ON_INITIAL_LINE:
				projection.setScaleFactor(value);
				break;

			case LATITUDE_OF_1ST_STANDARD_PARALLEL:
				projection.setProjectionLatitude1(value);
				break;

			case LATITUDE_OF_2ND_STANDARD_PARALLEL:
				projection.setProjectionLatitude2(value);
				break;

			case LATITUDE_OF_PROJECTION_CENTRE:
			case LATITUDE_OF_NATURAL_ORIGIN:
			case LATITUDE_OF_FALSE_ORIGIN:
				projection.setProjectionLatitudeDegrees(value);
				break;

			case LONGITUDE_OF_PROJECTION_CENTRE:
			case LONGITUDE_OF_NATURAL_ORIGIN:
			case LONGITUDE_OF_FALSE_ORIGIN:
				projection.setProjectionLongitudeDegrees(value);
				break;

			default:

			}

		}

	}

}