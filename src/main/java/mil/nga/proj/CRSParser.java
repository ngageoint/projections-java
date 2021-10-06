package mil.nga.proj;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.Registry;
import org.locationtech.proj4j.datum.Datum;
import org.locationtech.proj4j.datum.Ellipsoid;
import org.locationtech.proj4j.parser.DatumParameters;
import org.locationtech.proj4j.proj.Projection;
import org.locationtech.proj4j.units.DegreeUnit;

import mil.nga.crs.CRS;
import mil.nga.crs.CRSException;
import mil.nga.crs.CompoundCoordinateReferenceSystem;
import mil.nga.crs.SimpleCoordinateReferenceSystem;
import mil.nga.crs.common.Axis;
import mil.nga.crs.common.CoordinateSystem;
import mil.nga.crs.common.Unit;
import mil.nga.crs.common.UnitType;
import mil.nga.crs.common.Units;
import mil.nga.crs.geo.GeoCoordinateReferenceSystem;
import mil.nga.crs.geo.GeoDatum;
import mil.nga.crs.geo.PrimeMeridian;
import mil.nga.crs.geo.TriaxialEllipsoid;
import mil.nga.crs.operation.OperationMethod;
import mil.nga.crs.operation.OperationParameter;
import mil.nga.crs.projected.MapProjection;
import mil.nga.crs.projected.ProjectedCoordinateReferenceSystem;
import mil.nga.crs.util.proj.ProjConstants;
import mil.nga.crs.util.proj.ProjParser;
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
	 * Name to known datum mapping
	 */
	private static final Map<String, Datum> datums = new HashMap<>();

	/**
	 * Name to known ellipsoid mapping
	 */
	private static final Map<String, Ellipsoid> ellipsoids = new HashMap<>();

	static {
		for (Datum datum : Registry.datums) {
			datums.put(datum.getCode().toLowerCase(), datum);
			datums.put(datum.getName().toLowerCase(), datum);
		}
		for (Ellipsoid ellipsoid : Ellipsoid.ellipsoids) {
			ellipsoids.put(ellipsoid.getShortName().toLowerCase(), ellipsoid);
			String name = ellipsoid.getName().toLowerCase();
			ellipsoids.put(name, ellipsoid);
			int index = name.indexOf("(");
			if (index > -1) {
				String namePrefix = name.substring(0, index).trim();
				if (!ellipsoids.containsKey(namePrefix)) {
					ellipsoids.put(namePrefix, ellipsoid);
				}
			}
		}
	}

	/**
	 * Get the CRS Factory
	 * 
	 * @return crs factory
	 */
	public static CRSFactory getCRSFactory() {
		return crsFactory;
	}

	/**
	 * Get a predefined proj4j datum by name or short name
	 * 
	 * @param name
	 *            name or short name
	 * @return datum or null
	 * @since 1.1.0
	 */
	public static Datum getDatum(String name) {
		return datums.get(name.toLowerCase());
	}

	/**
	 * Get a predefined proj4j ellipsoid by name or short name
	 * 
	 * @param name
	 *            name or short name
	 * @return ellipsoid or null
	 */
	public static Ellipsoid getEllipsoid(String name) {
		return ellipsoids.get(name.toLowerCase());
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
	 * Parse crs well-known text into proj4 params, then to a proj4 coordinate
	 * reference system
	 * 
	 * @param wkt
	 *            crs well-known text
	 * @return coordinate reference system
	 * @since 1.1.0
	 */
	public static CoordinateReferenceSystem parseAsParams(String wkt) {

		CRS crsObject = null;
		try {
			crsObject = CRSReader.read(wkt);
		} catch (IOException e) {
			throw new ProjectionException("Failed to parse WKT: " + wkt, e);
		}

		CoordinateReferenceSystem crs = null;
		if (crsObject != null) {
			crs = convertAsParams(crsObject);
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

		case COMPOUND:
			crs = convert((CompoundCoordinateReferenceSystem) crsObject);
			break;

		default:

		}

		return crs;
	}

	/**
	 * Convert a CRS object into proj4 params, then to a proj4 coordinate
	 * reference system
	 * 
	 * @param crsObject
	 *            CRS object
	 * @return coordinate reference system
	 * @since 1.1.0
	 */
	public static CoordinateReferenceSystem convertAsParams(CRS crsObject) {

		CoordinateReferenceSystem crs = null;

		String params = ProjParser.paramsText(crsObject);
		if (params != null) {
			crs = CRSParser.getCRSFactory()
					.createFromParameters(crsObject.getName(), params);
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

		Projection projection = createProjection(geo.getCoordinateSystem());
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

		CoordinateSystem coordinateSystem = projected.getCoordinateSystem();
		MapProjection mapProjection = projected.getMapProjection();
		OperationMethod method = mapProjection.getMethod();
		GeoDatum geoDatum = projected.getGeoDatum();

		Datum datum = getDatum(geoDatum.getName());

		if (datum == null) {

			Ellipsoid ellipsoid = convert(geoDatum.getEllipsoid());
			DatumParameters datumParameters = new DatumParameters();

			if (projected.hasIdentifiers()
					&& projected.getIdentifier(0).getNameAndUniqueIdentifier()
							.equalsIgnoreCase(ProjectionConstants.AUTHORITY_EPSG
									+ ":"
									+ ProjectionConstants.EPSG_WEB_MERCATOR)) {
				datumParameters.setA(ellipsoid.getA());
				datumParameters.setES(0);
			} else {
				datumParameters.setEllipsoid(ellipsoid);
			}

			datumParameters.setDatumTransform(convertDatumTransform(method));

			datum = datumParameters.getDatum();

		}

		Projection projection = createProjection(coordinateSystem,
				mapProjection);
		updateProjection(projection, datum.getEllipsoid(), geoDatum);
		updateProjection(projection, method, coordinateSystem.getUnit());
		projection.initialize();

		return new CoordinateReferenceSystem(projected.getName(), null, datum,
				projection);
	}

	/**
	 * Convert a compound crs into a proj4 coordinate reference system
	 * 
	 * @param compound
	 *            compound crs
	 * @return coordinate reference system
	 */
	public static CoordinateReferenceSystem convert(
			CompoundCoordinateReferenceSystem compound) {

		CoordinateReferenceSystem crs = null;

		for (SimpleCoordinateReferenceSystem simpleCrs : compound
				.getCoordinateReferenceSystems()) {

			crs = convert(simpleCrs);

			if (crs != null) {
				break;
			}

		}

		return crs;
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
		Ellipsoid ellipsoid = convert(geoDatum.getEllipsoid());

		String code = name;
		if (geoDatum.hasIdentifiers()) {
			code = geoDatum.getIdentifier(0).getNameAndUniqueIdentifier();
		}

		return new Datum(code, null, null, ellipsoid, name);
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

		Ellipsoid converted = getEllipsoid(name);

		if (converted == null) {

			String shortName = name;
			if (ellipsoid.hasIdentifiers()) {
				shortName = ellipsoid.getIdentifier(0)
						.getNameAndUniqueIdentifier();
			}

			double equatorRadius = convertValue(ellipsoid.getSemiMajorAxis(),
					ellipsoid.getUnit(), Units.METRE);

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
				poleRadius = convertValue(triaxial.getSemiMinorAxis(),
						ellipsoid.getUnit(), Units.METRE);
				break;
			default:
				throw new CRSException(
						"Unsupported Ellipsoid Type: " + ellipsoid.getType());
			}

			converted = new Ellipsoid(shortName, equatorRadius, poleRadius,
					reciprocalFlattening, name);
		}

		return converted;
	}

	/**
	 * Convert the operation method into datum transform
	 * 
	 * @param method
	 *            operation method
	 * @return transform
	 */
	public static double[] convertDatumTransform(OperationMethod method) {

		double[] transform3 = new double[3];
		double[] transform7 = new double[7];
		boolean param3 = false;
		boolean param7 = false;

		for (OperationParameter parameter : method.getParameters()) {

			if (parameter.hasParameter()) {

				switch (parameter.getParameter()) {

				case X_AXIS_TRANSLATION:
					transform3[0] = getValue(parameter, Units.METRE);
					param3 = true;
					break;

				case Y_AXIS_TRANSLATION:
					transform3[1] = getValue(parameter, Units.METRE);
					param3 = true;
					break;

				case Z_AXIS_TRANSLATION:
					transform3[2] = getValue(parameter, Units.METRE);
					param3 = true;
					break;

				case X_AXIS_ROTATION:
					transform7[3] = getValue(parameter, Units.ARC_SECOND);
					param7 = true;
					break;

				case Y_AXIS_ROTATION:
					transform7[4] = getValue(parameter, Units.ARC_SECOND);
					param7 = true;
					break;

				case Z_AXIS_ROTATION:
					transform7[5] = getValue(parameter, Units.ARC_SECOND);
					param7 = true;
					break;

				case SCALE_DIFFERENCE:
					transform7[6] = getValue(parameter,
							Units.PARTS_PER_MILLION);
					param7 = true;
					break;

				default:
					break;

				}
			}
		}

		double[] transform = null;
		if (param7) {
			transform7[0] = transform3[0];
			transform7[1] = transform3[1];
			transform7[2] = transform3[2];
			transform = transform7;
		} else if (param3) {
			transform = transform3;
		}

		return transform;
	}

	/**
	 * Update the projection ellipsoid and prime meridian
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
			String name = primeMeridian.getName();
			if (name != null) {
				name = name.toLowerCase();
			}
			projection.setPrimeMeridian(name);
			if (!projection.getPrimeMeridian().getName().equals(name)) {
				double primeMeridianLongitude = convertValue(
						primeMeridian.getLongitude(),
						primeMeridian.getLongitudeUnit(), Units.DEGREE);
				projection.setPrimeMeridian(
						Double.toString(primeMeridianLongitude));
			}
		}

	}

	/**
	 * Create a proj4j projection for the coordinate system
	 * 
	 * @param coordinateSystem
	 *            coordinate system
	 * @return projection
	 */
	public static Projection createProjection(
			CoordinateSystem coordinateSystem) {

		Unit unit = coordinateSystem.getAxisUnit();

		String projectionName = null;
		if (unit != null && (unit.getType() == UnitType.ANGLEUNIT
				|| (unit.getType() == UnitType.UNIT
						&& unit.getName().toLowerCase()
								.startsWith(ProjConstants.UNITS_DEGREE)))) {
			projectionName = ProjConstants.NAME_LONGLAT;
		} else {
			projectionName = ProjConstants.NAME_MERC;
		}

		return createProjection(projectionName, coordinateSystem);
	}

	/**
	 * Create a proj4j projection for the coordinate system and map projection
	 * 
	 * @param coordinateSystem
	 *            coordinate system
	 * @param mapProjection
	 *            map projection
	 * @return projection
	 */
	public static Projection createProjection(CoordinateSystem coordinateSystem,
			MapProjection mapProjection) {

		Projection projection = null;

		OperationMethod method = mapProjection.getMethod();

		if (method.hasMethod()) {

			String projectionName = null;

			switch (method.getMethod()) {

			case ALBERS_EQUAL_AREA:
				projectionName = ProjConstants.NAME_AEA;
				break;

			case AMERICAN_POLYCONIC:
				projectionName = ProjConstants.NAME_POLY;
				break;

			case CASSINI_SOLDNER:
				projectionName = ProjConstants.NAME_CASS;
				break;

			case EQUIDISTANT_CYLINDRICAL:
				projectionName = ProjConstants.NAME_EQC;
				break;

			case HOTINE_OBLIQUE_MERCATOR_A:
				projectionName = ProjConstants.NAME_OMERC;
				break;

			case HOTINE_OBLIQUE_MERCATOR_B:
				if (mapProjection.getName().toLowerCase()
						.contains(ProjConstants.SWISS_OBLIQUE_MERCATOR)
						|| method.getName().toLowerCase().contains(
								ProjConstants.SWISS_OBLIQUE_MERCATOR_COMPAT)) {
					projectionName = ProjConstants.NAME_SOMERC;
				} else {
					projectionName = ProjConstants.NAME_OMERC;
				}
				break;

			case KROVAK:
				projectionName = ProjConstants.NAME_KROVAK;
				break;

			case LAMBERT_AZIMUTHAL_EQUAL_AREA:
				projectionName = ProjConstants.NAME_LAEA;
				break;

			case LAMBERT_CONIC_CONFORMAL_1SP:
			case LAMBERT_CONIC_CONFORMAL_2SP:
				projectionName = ProjConstants.NAME_LCC;
				break;

			case LAMBERT_CYLINDRICAL_EQUAL_AREA:
				projectionName = ProjConstants.NAME_CEA;
				break;

			case MERCATOR_A:
			case MERCATOR_B:
				projectionName = ProjConstants.NAME_MERC;
				break;

			case NEW_ZEALAND_MAP_GRID:
				projectionName = ProjConstants.NAME_NZMG;
				break;

			case OBLIQUE_STEREOGRAPHIC:
				projectionName = ProjConstants.NAME_STEREA;
				break;

			case POLAR_STEREOGRAPHIC_A:
			case POLAR_STEREOGRAPHIC_B:
			case POLAR_STEREOGRAPHIC_C:
				projectionName = ProjConstants.NAME_STERE;
				break;

			case POPULAR_VISUALISATION_PSEUDO_MERCATOR:
				projectionName = ProjConstants.NAME_MERC;
				break;

			case TRANSVERSE_MERCATOR:
			case TRANSVERSE_MERCATOR_SOUTH_ORIENTATED:
				if (mapProjection.getName().toLowerCase()
						.contains(ProjConstants.UTM_ZONE)) {
					projectionName = ProjConstants.NAME_UTM;
				} else {
					projectionName = ProjConstants.NAME_TMERC;
				}
				break;

			default:

			}

			if (projectionName != null) {
				projection = createProjection(projectionName, coordinateSystem);

				switch (method.getMethod()) {

				case HOTINE_OBLIQUE_MERCATOR_A:
					projection.setNoUoff(true);
					break;

				default:

				}
			}
		}

		if (projection == null) {
			projection = createProjection(coordinateSystem);
		}

		return projection;
	}

	/**
	 * Create a proj4j projection for the projection name and coordinate system
	 * 
	 * @param projectionName
	 *            projection name
	 * @param coordinateSystem
	 *            coordinate system
	 * @return projection
	 */
	public static Projection createProjection(String projectionName,
			CoordinateSystem coordinateSystem) {

		Projection projection = getCRSFactory().getRegistry()
				.getProjection(projectionName);

		String axisOrder = convert(coordinateSystem.getAxes());
		// Only known proj4 axis specification is wsu
		if (axisOrder.equals(ProjConstants.AXIS_WEST_SOUTH_UP)) {
			projection.setAxisOrder(axisOrder);
		}

		if (coordinateSystem.hasUnit()) {

			Unit unit = coordinateSystem.getUnit();

			if (unit.hasConversionFactor()
					&& unit.getConversionFactor() != 1.0) {

				boolean fromMetres = false;

				switch (unit.getType()) {
				case LENGTHUNIT:
					fromMetres = true;
					break;
				case UNIT:
					UnitType type = Units.getUnitType(unit.getName());
					fromMetres = type == null || type == UnitType.LENGTHUNIT;
					break;
				default:
				}

				if (fromMetres) {
					projection.setFromMetres(1.0 / unit.getConversionFactor());
				}

			}
		}

		return projection;
	}

	/**
	 * Update the method parameters in the projection
	 * 
	 * @param projection
	 *            proj4j projection
	 * @param method
	 *            operation method
	 * @param unit
	 *            unit
	 * @since 1.1.0
	 */
	public static void updateProjection(Projection projection,
			OperationMethod method, Unit unit) {
		if (method.hasParameters()) {
			for (OperationParameter parameter : method.getParameters()) {
				updateProjection(projection, method, unit, parameter);
			}
		}
	}

	/**
	 * Update the method parameter in the projection
	 * 
	 * @param projection
	 *            proj4j projection
	 * @param method
	 *            operation method
	 * @param unit
	 *            unit
	 * @param parameter
	 *            operation parameter
	 * @since 1.1.0
	 */
	public static void updateProjection(Projection projection,
			OperationMethod method, Unit unit, OperationParameter parameter) {

		if (parameter.hasParameter()) {

			switch (parameter.getParameter()) {

			case FALSE_EASTING:
			case EASTING_AT_PROJECTION_CENTRE:
			case EASTING_AT_FALSE_ORIGIN:
				projection.setFalseEasting(
						getValue(parameter, unit, projection.getUnits()));
				break;

			case FALSE_NORTHING:
			case NORTHING_AT_PROJECTION_CENTRE:
			case NORTHING_AT_FALSE_ORIGIN:
				projection.setFalseNorthing(
						getValue(parameter, unit, projection.getUnits()));
				break;

			case SCALE_FACTOR_AT_NATURAL_ORIGIN:
			case SCALE_FACTOR_ON_INITIAL_LINE:
			case SCALE_FACTOR_ON_PSEUDO_STANDARD_PARALLEL:
				projection.setScaleFactor(getValue(parameter, Units.UNITY));
				break;

			case LATITUDE_OF_1ST_STANDARD_PARALLEL:
				if (method.hasMethod()) {
					switch (method.getMethod()) {
					case LAMBERT_CYLINDRICAL_EQUAL_AREA:
						projection.setTrueScaleLatitude(
								getValue(parameter, Units.RADIAN));
						break;
					default:
						projection.setProjectionLatitude1(
								getValue(parameter, Units.RADIAN));
					}
				} else {
					projection.setProjectionLatitude1(
							getValue(parameter, Units.RADIAN));
				}
				break;

			case LATITUDE_OF_2ND_STANDARD_PARALLEL:
				projection.setProjectionLatitude2(
						getValue(parameter, Units.RADIAN));
				break;

			case LATITUDE_OF_PROJECTION_CENTRE:
			case LATITUDE_OF_NATURAL_ORIGIN:
			case LATITUDE_OF_FALSE_ORIGIN:
				if (method.hasMethod()) {
					switch (method.getMethod()) {
					case POLAR_STEREOGRAPHIC_A:
					case POLAR_STEREOGRAPHIC_B:
					case POLAR_STEREOGRAPHIC_C:
						projection.setTrueScaleLatitude(
								getValue(parameter, Units.RADIAN));
						if (projection.getTrueScaleLatitude() >= 0) {
							projection.setProjectionLatitudeDegrees(90);
						} else {
							projection.setProjectionLatitudeDegrees(-90);
						}
						break;
					case EQUIDISTANT_CYLINDRICAL:
						projection.setTrueScaleLatitude(
								getValue(parameter, Units.RADIAN));
						projection.setProjectionLatitude(0);
						break;
					case LAMBERT_CYLINDRICAL_EQUAL_AREA:
					case MERCATOR_A:
					case MERCATOR_B:
					case POPULAR_VISUALISATION_PSEUDO_MERCATOR:
						projection.setTrueScaleLatitude(
								getValue(parameter, Units.RADIAN));
						break;
					case LAMBERT_CONIC_CONFORMAL_1SP:
					case LAMBERT_CONIC_CONFORMAL_2SP:
						projection.setProjectionLatitude(
								getValue(parameter, Units.RADIAN));
						if (projection.getProjectionLatitude1() == 0.0) {
							projection.setProjectionLatitude1(
									projection.getProjectionLatitude());
						}
						break;
					default:
						projection.setProjectionLatitude(
								getValue(parameter, Units.RADIAN));
					}
				} else {
					projection.setProjectionLatitude(
							getValue(parameter, Units.RADIAN));
				}
				break;

			case LONGITUDE_OF_PROJECTION_CENTRE:
			case LONGITUDE_OF_NATURAL_ORIGIN:
			case LONGITUDE_OF_FALSE_ORIGIN:
			case LONGITUDE_OF_ORIGIN:
				if (method.hasMethod()) {
					switch (method.getMethod()) {
					case HOTINE_OBLIQUE_MERCATOR_A:
						projection.setLonC(getValue(parameter, Units.RADIAN));
						break;
					case HOTINE_OBLIQUE_MERCATOR_B:
						if (projection.getName() != null && projection.getName()
								.equals(ProjConstants.NAME_SOMERC)) {
							projection.setProjectionLongitude(
									getValue(parameter, Units.RADIAN));
						} else {
							projection
									.setLonC(getValue(parameter, Units.RADIAN));
						}
						break;
					default:
						projection.setProjectionLongitude(
								getValue(parameter, Units.RADIAN));
					}
				} else {
					projection.setProjectionLongitude(
							getValue(parameter, Units.RADIAN));
				}
				break;

			case AZIMUTH_OF_INITIAL_LINE:
			case CO_LATITUDE_OF_CONE_AXIS:
				if (method.hasMethod()) {
					switch (method.getMethod()) {
					case HOTINE_OBLIQUE_MERCATOR_B:
						if (projection.getName() == null || !projection
								.getName().equals(ProjConstants.NAME_SOMERC)) {
							projection.setAlpha(
									getValue(parameter, Units.RADIAN));
						}
						break;
					default:
						projection.setAlpha(getValue(parameter, Units.RADIAN));
						break;
					}
				} else {
					projection.setAlpha(getValue(parameter, Units.RADIAN));
				}
				break;

			case ANGLE_FROM_RECTIFIED_TO_SKEW_GRID:
				if (method.hasMethod()) {
					switch (method.getMethod()) {
					case HOTINE_OBLIQUE_MERCATOR_B:
						if (projection.getName() == null || !projection
								.getName().equals(ProjConstants.NAME_SOMERC)) {
							projection.setGamma(
									getValue(parameter, Units.RADIAN));
						}
						break;
					default:
						projection.setGamma(getValue(parameter, Units.RADIAN));
						break;
					}
				} else {
					projection.setGamma(getValue(parameter, Units.RADIAN));
				}
				break;

			default:

			}

		}

	}

	/**
	 * Convert the list of axes to a proj4j axis order
	 * 
	 * @param axes
	 *            list of axes
	 * @return axis order
	 */
	public static String convert(List<Axis> axes) {

		String axisValue = null;

		int axesCount = axes.size();
		if (axesCount == 2 || axesCount == 3) {

			StringBuilder axisString = new StringBuilder();

			for (Axis axis : axes) {

				switch (axis.getDirection()) {

				case EAST:
					axisString.append(ProjConstants.AXIS_EAST);
					break;

				case WEST:
					axisString.append(ProjConstants.AXIS_WEST);
					break;

				case NORTH:
					axisString.append(ProjConstants.AXIS_NORTH);
					break;

				case SOUTH:
					axisString.append(ProjConstants.AXIS_SOUTH);
					break;

				case UP:
					axisString.append(ProjConstants.AXIS_UP);
					break;

				case DOWN:
					axisString.append(ProjConstants.AXIS_DOWN);
					break;

				default:
					axisString = null;

				}

				if (axisString == null) {
					break;
				}
			}

			if (axisString != null) {

				if (axesCount == 2) {
					axisString.append(ProjConstants.AXIS_UP);
				}

				axisValue = axisString.toString();
			}

		}

		return axisValue;
	}

	/**
	 * Get the operation parameter value in the specified unit
	 * 
	 * @param parameter
	 *            operation parameter
	 * @param unit
	 *            desired unit
	 * @return value
	 */
	public static double getValue(OperationParameter parameter,
			org.locationtech.proj4j.units.Unit unit) {
		return getValue(parameter, null, unit);
	}

	/**
	 * Get the operation parameter value in the specified unit
	 * 
	 * @param parameter
	 *            operation parameter
	 * @param unit
	 *            unit
	 * @param inUnit
	 *            in unit
	 * @return value
	 * @since 1.1.0
	 */
	public static double getValue(OperationParameter parameter, Unit unit,
			org.locationtech.proj4j.units.Unit inUnit) {

		Units desiredUnit = null;

		if (inUnit instanceof DegreeUnit) {
			desiredUnit = Units.DEGREE;
		} else {
			desiredUnit = Units.METRE;
		}

		return getValue(parameter, unit, desiredUnit);
	}

	/**
	 * Get the operation parameter value in the specified unit
	 * 
	 * @param parameter
	 *            operation parameter
	 * @param unit
	 *            desired unit
	 * @return value
	 * @since 1.1.0
	 */
	public static double getValue(OperationParameter parameter, Units unit) {
		return getValue(parameter, null, unit);
	}

	/**
	 * Get the operation parameter value in the specified unit
	 * 
	 * @param parameter
	 *            operation parameter
	 * @param unit
	 *            unit
	 * @param inUnit
	 *            in unit
	 * @return value
	 * @since 1.1.0
	 */
	public static double getValue(OperationParameter parameter, Unit unit,
			Units inUnit) {
		return getValue(parameter, unit, inUnit.createUnit());
	}

	/**
	 * Get the operation parameter value in the specified unit
	 * 
	 * @param parameter
	 *            operation parameter
	 * @param unit
	 *            desired unit
	 * @return value
	 */
	public static double getValue(OperationParameter parameter, Unit unit) {
		return getValue(parameter, null, unit);
	}

	/**
	 * Get the operation parameter value in the specified unit
	 * 
	 * @param parameter
	 *            operation parameter
	 * @param unit
	 *            unit
	 * @param inUnit
	 *            in unit
	 * @return value
	 * @since 1.1.0
	 */
	public static double getValue(OperationParameter parameter, Unit unit,
			Unit inUnit) {
		Unit parameterUnit = parameter.getUnit();
		if (parameterUnit == null) {
			parameterUnit = unit;
		}
		return convertValue(parameter.getValue(), parameterUnit, inUnit);
	}

	/**
	 * Convert the value from a unit to another
	 * 
	 * @param value
	 *            value to convert
	 * @param fromUnit
	 *            from unit
	 * @param toUnit
	 *            to unit
	 * @return value
	 * @since 1.1.0
	 */
	public static double convertValue(double value, Unit fromUnit,
			Units toUnit) {
		return convertValue(value, fromUnit, toUnit.createUnit());
	}

	/**
	 * Convert the value from a unit to another
	 * 
	 * @param value
	 *            value to convert
	 * @param fromUnit
	 *            from unit
	 * @param toUnit
	 *            to unit
	 * @return value
	 */
	public static double convertValue(double value, Unit fromUnit,
			Unit toUnit) {

		if (fromUnit == null) {
			fromUnit = Units.createDefaultUnit(toUnit.getType());
		}

		if (Units.canConvert(fromUnit, toUnit)) {
			value = Units.convert(value, fromUnit, toUnit);
		}

		return value;
	}

}
