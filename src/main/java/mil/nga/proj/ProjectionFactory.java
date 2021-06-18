package mil.nga.proj;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.locationtech.proj4j.CoordinateReferenceSystem;

import mil.nga.crs.CRS;
import mil.nga.crs.common.Identifier;
import mil.nga.crs.wkt.CRSReader;

/**
 * Projection factory for coordinate projections and transformations
 * 
 * @author osbornb
 */
public class ProjectionFactory {

	/**
	 * Logger
	 */
	private static final Logger logger = Logger
			.getLogger(ProjectionFactory.class.getName());

	/**
	 * Default projection factory retrieval order
	 */
	private static final ProjectionFactoryType[] DEFAULT_ORDER = new ProjectionFactoryType[] {
			ProjectionFactoryType.CACHE, ProjectionFactoryType.DEFINITION,
			ProjectionFactoryType.PARAMETERS, ProjectionFactoryType.PROPERTIES,
			ProjectionFactoryType.NAME };

	/**
	 * Projections
	 */
	private static final Projections projections = new Projections();

	/**
	 * Projection factory retrieval order
	 */
	private static final Set<ProjectionFactoryType> order = new LinkedHashSet<>();
	static {
		resetOrder();
	}

	/**
	 * Reset the projection factory retrieval order to the default
	 */
	public static void resetOrder() {
		setOrder(DEFAULT_ORDER);
	}

	/**
	 * Get a copy of the projection factory retrieval order
	 * 
	 * @return order set copy
	 */
	public static Set<ProjectionFactoryType> getOrder() {
		return new LinkedHashSet<>(order);
	}

	/**
	 * Get a copy of the projection factory retrieval order without caching
	 * 
	 * @return order set copy without cache
	 */
	public static Set<ProjectionFactoryType> getCachelessOrder() {
		Set<ProjectionFactoryType> orderCopy = getOrder();
		orderCopy.remove(ProjectionFactoryType.CACHE);
		return orderCopy;
	}

	/**
	 * Remove the projection factory retrieval type from the retrieval ordering
	 * 
	 * @param type
	 *            retrieval type
	 * @return true if removed
	 */
	public static boolean removeOrderType(ProjectionFactoryType type) {
		boolean removed = order.remove(type);
		if (order.isEmpty()) {
			resetOrder();
		}
		return removed;
	}

	/**
	 * Set the projection factory retrieval order
	 * 
	 * @param types
	 *            factory retrieval types
	 */
	public static void setOrder(ProjectionFactoryType... types) {
		order.clear();
		if (types == null || types.length == 0) {
			resetOrder();
		} else {
			for (ProjectionFactoryType type : types) {
				order.add(type);
			}
		}
	}

	/**
	 * Build a default order set for specified ordered projection retrievals
	 * without changing the global ordering
	 * 
	 * @return projection factory retrieval order
	 */
	public static Set<ProjectionFactoryType> buildDefaultOrder() {
		return buildOrder(DEFAULT_ORDER);
	}

	/**
	 * Build an order set for specified ordered projection retrievals without
	 * changing the global ordering
	 * 
	 * @param types
	 *            factory retrieval types
	 * @return projection factory retrieval order
	 */
	public static Set<ProjectionFactoryType> buildOrder(
			ProjectionFactoryType... types) {
		Set<ProjectionFactoryType> tempOrder = new LinkedHashSet<>();
		for (ProjectionFactoryType type : types) {
			tempOrder.add(type);
		}
		return tempOrder;
	}

	/**
	 * Get the projection for the EPSG code
	 * 
	 * @param epsg
	 *            EPSG coordinate code
	 * @return projection
	 */
	public static Projection getProjection(long epsg) {
		return getProjection(ProjectionConstants.AUTHORITY_EPSG,
				String.valueOf(epsg));
	}

	/**
	 * Get the cacheless projection for the EPSG code
	 * 
	 * @param epsg
	 *            EPSG coordinate code
	 * @return projection
	 */
	public static Projection getCachelessProjection(long epsg) {
		return getCachelessProjection(ProjectionConstants.AUTHORITY_EPSG,
				String.valueOf(epsg));
	}

	/**
	 * Get the projection for the projection name, expected as 'authority:code'
	 * or 'epsg_code'
	 * 
	 * @param name
	 *            projection name
	 * @return projection
	 */
	public static Projection getProjection(String name) {
		String authorityAndCode[] = parseAuthorityAndCode(name);
		return getProjection(authorityAndCode[0], authorityAndCode[1]);
	}

	/**
	 * Get the cacheless projection for the projection name, expected as
	 * 'authority:code' or 'epsg_code'
	 * 
	 * @param name
	 *            projection name
	 * @return projection
	 */
	public static Projection getCachelessProjection(String name) {
		String authorityAndCode[] = parseAuthorityAndCode(name);
		return getCachelessProjection(authorityAndCode[0], authorityAndCode[1]);
	}

	/**
	 * Get the projection for authority and code
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @return projection
	 */
	public static Projection getProjection(String authority, long code) {
		return getProjection(authority, String.valueOf(code));
	}

	/**
	 * Get the cacheless projection for authority and code
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @return projection
	 */
	public static Projection getCachelessProjection(String authority,
			long code) {
		return getCachelessProjection(authority, String.valueOf(code));
	}

	/**
	 * Get the projection for authority and code
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @return projection
	 */
	public static Projection getProjection(String authority, String code) {
		return getProjection(authority, code, null, null);
	}

	/**
	 * Get the cacheless projection for authority and code
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @return projection
	 */
	public static Projection getCachelessProjection(String authority,
			String code) {
		return getCachelessProjection(authority, code, null, null);
	}

	/**
	 * Get the projection for authority, code, and parameter string
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param paramStr
	 *            proj4 string
	 * @return projection
	 */
	public static Projection getProjection(String authority, long code,
			String paramStr) {
		return getProjection(authority, String.valueOf(code), paramStr);
	}

	/**
	 * Get the cacheless projection for authority, code, and parameter string
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param paramStr
	 *            proj4 string
	 * @return projection
	 */
	public static Projection getCachelessProjection(String authority, long code,
			String paramStr) {
		return getCachelessProjection(authority, String.valueOf(code),
				paramStr);
	}

	/**
	 * Get the projection for authority, code, and parameter string
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param paramStr
	 *            proj4 string
	 * @return projection
	 */
	public static Projection getProjection(String authority, String code,
			String paramStr) {
		return getProjection(authority, code, buildParameters(paramStr));
	}

	/**
	 * Get the cacheless projection for authority, code, and parameter string
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param paramStr
	 *            proj4 string
	 * @return projection
	 */
	public static Projection getCachelessProjection(String authority,
			String code, String paramStr) {
		return getCachelessProjection(authority, code,
				buildParameters(paramStr));
	}

	/**
	 * Get the projection for authority, code, and parameters
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param params
	 *            proj4 params array
	 * @return projection
	 */
	public static Projection getProjection(String authority, long code,
			String[] params) {
		return getProjection(authority, String.valueOf(code), params);
	}

	/**
	 * Get the cacheless projection for authority, code, and parameters
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param params
	 *            proj4 params array
	 * @return projection
	 */
	public static Projection getCachelessProjection(String authority, long code,
			String[] params) {
		return getCachelessProjection(authority, String.valueOf(code), params);
	}

	/**
	 * Get the projection for authority, code, and parameters
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param params
	 *            proj4 params array
	 * @return projection
	 */
	public static Projection getProjection(String authority, String code,
			String[] params) {
		return getProjection(authority, code, params, null);
	}

	/**
	 * Get the cacheless projection for authority, code, and parameters
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param params
	 *            proj4 params array
	 * @return projection
	 */
	public static Projection getCachelessProjection(String authority,
			String code, String[] params) {
		return getCachelessProjection(authority, code, params, null);
	}

	/**
	 * Get the projection for the authority, code, and definition
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param definition
	 *            definition
	 * @return projection
	 */
	public static Projection getProjectionByDefinition(String authority,
			long code, String definition) {
		return getProjectionByDefinition(authority, String.valueOf(code),
				definition);
	}

	/**
	 * Get the cacheless projection for the authority, code, and definition
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param definition
	 *            definition
	 * @return projection
	 */
	public static Projection getCachelessProjectionByDefinition(
			String authority, long code, String definition) {
		return getCachelessProjectionByDefinition(authority,
				String.valueOf(code), definition);
	}

	/**
	 * Get the projection for the authority, code, and definition
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param definition
	 *            definition
	 * @return projection
	 */
	public static Projection getProjectionByDefinition(String authority,
			String code, String definition) {
		return getProjection(authority, code, null, definition);
	}

	/**
	 * Get the cacheless projection for the authority, code, and definition
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param definition
	 *            definition
	 * @return projection
	 */
	public static Projection getCachelessProjectionByDefinition(
			String authority, String code, String definition) {
		return getCachelessProjection(authority, code, null, definition);
	}

	/**
	 * Get the projection for the authority, code, definition, and custom
	 * parameter array
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param params
	 *            proj4 params array
	 * @param definition
	 *            definition
	 * @return projection
	 */
	public static Projection getProjection(String authority, long code,
			String[] params, String definition) {
		return getProjection(authority, String.valueOf(code), params,
				definition);
	}

	/**
	 * Get the cacheless projection for the authority, code, definition, and
	 * custom parameter array
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param params
	 *            proj4 params array
	 * @param definition
	 *            definition
	 * @return projection
	 */
	public static Projection getCachelessProjection(String authority, long code,
			String[] params, String definition) {
		return getCachelessProjection(authority, String.valueOf(code), params,
				definition);
	}

	/**
	 * Get the projection for the authority, code, definition, and custom
	 * parameter array
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param params
	 *            proj4 params array
	 * @param definition
	 *            definition
	 * @return projection
	 */
	public static Projection getProjection(String authority, String code,
			String[] params, String definition) {
		return getProjection(order, authority, code, params, definition);
	}

	/**
	 * Get the cacheless projection for the authority, code, definition, and
	 * custom parameter array
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param params
	 *            proj4 params array
	 * @param definition
	 *            definition
	 * @return projection
	 */
	public static Projection getCachelessProjection(String authority,
			String code, String[] params, String definition) {
		return getProjection(getCachelessOrder(), authority, code, params,
				definition);
	}

	/**
	 * Get the projection for the authority, code, definition, and custom
	 * parameter array
	 * 
	 * @param types
	 *            projection factory retrieval types
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param params
	 *            proj4 params array
	 * @param definition
	 *            definition
	 * @return projection
	 */
	public static Projection getProjection(Set<ProjectionFactoryType> types,
			String authority, String code, String[] params, String definition) {

		Projection projection = null;

		for (ProjectionFactoryType type : types) {

			projection = getProjection(type, authority, code, params,
					definition);

			if (projection != null) {

				switch (type) {

				case CACHE:
					// Check if the definition does not match the cached
					// projection
					if (definition != null && !definition.isEmpty()
							&& !definition.equals(projection.getDefinition())) {
						projection = null;
					}
					break;

				default:

				}

			}

			if (projection != null) {
				break;
			}

		}

		if (projection == null) {
			throw new ProjectionException(
					"Failed to create projection for authority: " + authority
							+ ", code: " + code + ", definition: " + definition
							+ ", params: " + Arrays.toString(params));
		}

		return projection;
	}

	/**
	 * Get the projection for the authority, code, definition, and custom
	 * parameter array
	 * 
	 * @param type
	 *            projection factory retrieval type
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            authority coordinate code
	 * @param params
	 *            proj4 params array
	 * @param definition
	 *            definition
	 * @return projection
	 */
	public static Projection getProjection(ProjectionFactoryType type,
			String authority, String code, String[] params, String definition) {

		Projection projection = null;

		authority = authority.toUpperCase();

		switch (type) {

		case CACHE:
			projection = fromCache(authority, code);
			break;

		case DEFINITION:
			projection = fromDefinition(authority, code, definition);
			break;

		case NAME:
			projection = fromName(authority, code, definition);
			break;

		case PARAMETERS:
			projection = fromParams(authority, code, params, definition);
			break;

		case PROPERTIES:
			projection = fromProperties(authority, code, definition);
			break;

		default:
			throw new ProjectionException(
					"Unsupported projection factory type: " + type);

		}

		return projection;
	}

	/**
	 * Get the projection for the definition
	 * 
	 * @param definition
	 *            definition
	 * @return projection
	 */
	public static Projection getProjectionByDefinition(String definition) {
		return getProjectionByDefinition(false, definition);
	}

	/**
	 * Get the cacheless projection for the definition
	 * 
	 * @param definition
	 *            definition
	 * @return projection
	 */
	public static Projection getCachelessProjectionByDefinition(
			String definition) {
		return getProjectionByDefinition(true, definition);
	}

	/**
	 * Get the projection for the definition
	 * 
	 * @param cacheless
	 *            cacheless retrieval
	 * @param definition
	 *            definition
	 * @return projection
	 */
	private static Projection getProjectionByDefinition(boolean cacheless,
			String definition) {

		Projection projection = null;

		if (definition != null && !definition.isEmpty()) {

			CRS definitionCRS = null;
			try {
				definitionCRS = CRSReader.read(definition);
			} catch (IOException e) {
				throw new ProjectionException(
						"Failed to parse definition: " + definition, e);
			}

			if (definitionCRS != null) {

				String authority = null;
				String code = null;

				if (definitionCRS.hasIdentifiers()) {
					Identifier identifier = definitionCRS.getIdentifier(0);
					authority = identifier.getName();
					code = identifier.getUniqueIdentifier();
				}

				boolean cacheProjection = true;

				if (authority != null && code != null) {

					if (!cacheless) {

						// Check if the projection already exists
						projection = fromCache(authority, code);

						// Check if the definition does not match the cached
						// projection
						if (projection != null && !definition
								.equals(projection.getDefinition())) {
							projection = null;
						}

					}

				} else {

					cacheProjection = false;

					if (authority == null) {
						authority = "";
					}
					if (code == null) {
						code = "";
					}

				}

				if (projection == null) {

					CoordinateReferenceSystem crs = CRSParser
							.convert(definitionCRS);
					if (crs != null) {
						projection = new Projection(authority, code, crs,
								definition, definitionCRS);
						if (cacheProjection) {
							projections.addProjection(projection);
						}
					}

				}

			}

		}

		if (projection == null) {
			throw new ProjectionException(
					"Failed to create projection for definition: "
							+ definition);
		}

		return projection;
	}

	/**
	 * Get the projections
	 * 
	 * @return projections
	 */
	public static Projections getProjections() {
		return projections;
	}

	/**
	 * Get the projections for the authority
	 * 
	 * @param authority
	 *            coordinate authority
	 * @return authority projections
	 */
	public static AuthorityProjections getProjections(String authority) {
		return projections.getProjections(authority);
	}

	/**
	 * Parse a projection name, expected as 'authority:code' or 'epsg_code',
	 * into an authority and code
	 * 
	 * @param name
	 *            projection name
	 * @return [authority, code]
	 */
	public static String[] parseAuthorityAndCode(String name) {

		String authority = null;
		String code = null;

		String[] projectionParts = name.split(":");

		switch (projectionParts.length) {
		case 1:
			authority = ProjectionConstants.AUTHORITY_EPSG;
			code = projectionParts[0];
			break;
		case 2:
			authority = projectionParts[0];
			code = projectionParts[1];
			break;
		default:
			throw new ProjectionException("Invalid projection name '" + name
					+ "', expected 'authority:code' or 'epsg_code'");
		}

		return new String[] { authority, code };
	}

	/**
	 * Build a proj4 parameters array from a proj4 string
	 * 
	 * @param paramStr
	 *            proj4 string
	 * @return proj4 params array
	 */
	public static String[] buildParameters(String paramStr) {
		String[] params = null;
		if (paramStr != null && !paramStr.isEmpty()) {
			params = paramStr.split("\\s+");
		}
		return params;
	}

	/**
	 * Clear all authority projections
	 */
	public static void clear() {
		projections.clear();
	}

	/**
	 * Clear the authority projections
	 * 
	 * @param authority
	 *            coordinate authority
	 */
	public static void clear(String authority) {
		projections.clear(authority);
	}

	/**
	 * Clear the authority projection code
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            coordinate code
	 */
	public static void clear(String authority, long code) {
		projections.remove(authority, code);
	}

	/**
	 * Clear the authority projection code
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            coordinate code
	 */
	public static void clear(String authority, String code) {
		projections.remove(authority, code);
	}

	/**
	 * Retrieve a projection from the cache
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            coordinate code
	 * @return projection
	 */
	private static Projection fromCache(String authority, String code) {
		return projections.getProjection(authority, code);
	}

	/**
	 * Create a projection from the WKT definition
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            coordinate code
	 * @param definition
	 *            WKT coordinate definition
	 * @return projection
	 */
	private static Projection fromDefinition(String authority, String code,
			String definition) {

		Projection projection = null;

		if (definition != null && !definition.isEmpty()) {

			try {
				CoordinateReferenceSystem crs = null;
				CRS definitionCRS = CRSReader.read(definition);
				if (definitionCRS != null) {
					crs = CRSParser.convert(definitionCRS);
				}
				if (crs != null) {
					projection = new Projection(authority, code, crs,
							definition, definitionCRS);
					projections.addProjection(projection);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING,
						"Failed to create projection for authority: "
								+ authority + ", code: " + code
								+ ", definition: " + definition,
						e);
			}

		}

		return projection;
	}

	/**
	 * Create a projection from the proj4 parameters
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            coordinate code
	 * @param params
	 *            proj4 parameters
	 * @param definition
	 *            WKT coordinate definition
	 * @return projection
	 */
	private static Projection fromParams(String authority, String code,
			String[] params, String definition) {

		Projection projection = null;

		if (params != null && params.length > 0) {
			try {
				CoordinateReferenceSystem crs = CRSParser.getCRSFactory()
						.createFromParameters(coordinateName(authority, code),
								params);
				projection = new Projection(authority, code, crs, definition);
				projections.addProjection(projection);
			} catch (Exception e) {
				logger.log(Level.WARNING,
						"Failed to create projection for authority: "
								+ authority + ", code: " + code
								+ ", parameters: " + Arrays.toString(params),
						e);
			}
		}

		return projection;
	}

	/**
	 * Create a projection from configured coordinate properties
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            coordinate code
	 * @param definition
	 *            WKT coordinate definition
	 * @return projection
	 */
	private static Projection fromProperties(String authority, String code,
			String definition) {

		Projection projection = null;

		String parameters = ProjectionRetriever.getProjection(authority, code);

		if (parameters != null && !parameters.isEmpty()) {
			try {
				CoordinateReferenceSystem crs = CRSParser.getCRSFactory()
						.createFromParameters(coordinateName(authority, code),
								parameters);
				projection = new Projection(authority, code, crs, definition);
				projections.addProjection(projection);
			} catch (Exception e) {
				logger.log(Level.WARNING,
						"Failed to create projection for authority: "
								+ authority + ", code: " + code
								+ ", parameters: " + parameters,
						e);
			}
		}

		return projection;
	}

	/**
	 * Create a projection from the coordinate authority and code name
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            coordinate code
	 * @param definition
	 *            WKT coordinate definition
	 * @return projection
	 */
	private static Projection fromName(String authority, String code,
			String definition) {

		Projection projection = null;

		String name = coordinateName(authority, code);
		try {
			CoordinateReferenceSystem crs = CRSParser.getCRSFactory()
					.createFromName(name);
			projection = new Projection(authority, code, crs, definition);
			projections.addProjection(projection);
		} catch (Exception e) {
			logger.log(Level.WARNING,
					"Failed to create projection from name: " + name, e);
		}

		return projection;
	}

	/**
	 * Build a coordinate name from the authority and code
	 * 
	 * @param authority
	 *            coordinate authority
	 * @param code
	 *            coordinate code
	 * @return name
	 */
	private static String coordinateName(String authority, String code) {
		return authority.toUpperCase() + ":" + code;
	}

}
